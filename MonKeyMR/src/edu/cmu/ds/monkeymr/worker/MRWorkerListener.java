package edu.cmu.ds.monkeymr.worker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.IOUtils;

import edu.cmu.ds.donkeyfs.DFSBlock;
import edu.cmu.ds.donkeyfs.DFSMessage;
import edu.cmu.ds.donkeyfs.DFSMessage.MessageType;
import edu.cmu.ds.monkeymr.comm.MRTaskMessage;
import edu.cmu.ds.monkeymr.interfaces.Constants;
import edu.cmu.ds.monkeymr.master.MRMasterCLI;
import edu.cmu.ds.monkeymr.master.MRTask;
import edu.cmu.ds.monkeymr.master.MRTask.TaskType;
import edu.cmu.ds.monkeymr.worker.mapper.MRMap;
import edu.cmu.ds.monkeymr.worker.mapper.MRMapRunner;
import edu.cmu.ds.monkeymr.worker.reducer.MRRedRunner;
import edu.cmu.ds.monkeymr.worker.reducer.MRReduce;

//send heart beats to node tracker. (Can update job status and other details )

public class MRWorkerListener implements Runnable {

	private Socket socket;
	private InputStream is;
	private ObjectInputStream ois;
	private ServerSocket listeningServer;
	private Integer DFS_PORT;
	private String DFS_IP;
	public static String REDUCERS_PATH = "";

	public MRWorkerListener(String nodeName, ServerSocket listeningServer) {
		this.listeningServer = listeningServer;
	}

	@Override
	public void run() {

		LoadProperties();
		// Listen to incoming tasks and launch them
		while (true) {
			try {
				socket = listeningServer.accept();
				is = socket.getInputStream();
				ois = new ObjectInputStream(is);
				Object obj = ois.readObject();
				// if a new job request, handle it here
				if (obj != null && obj instanceof MRTaskMessage) {
					MRTaskMessage msg = (MRTaskMessage) obj;
					MRTask task = msg.getTask();
					if (task.getTaskType() == TaskType.MAP) {
						MRTaskNode.taskNodeRegistry.put(task,
								MRTask.TaskStatus.RUNNING);
						System.out.println(task.getId() + "-Started");
						
						LaunchMapper(task);
						
					} else if (task.getTaskType() == TaskType.REDUCE) {
						MRTaskNode.taskNodeRegistry.put(task,
								MRTask.TaskStatus.RUNNING);
						System.out.println(task.getId() + "-Started");
						
						LaunchReducer(task);
					}
				}
			} catch (IOException | ClassNotFoundException e) {
				System.err.println("[MRWorkerListener] ! Err: Bad message.");
			//	e.printStackTrace();
			}
		}
	}

	/**
	 * Starts a reducer job
	 * 
	 * @param task
	 */
	private void LaunchReducer(MRTask task) {
		// this node will get a list of keys that it needs to reduce
		// the list are filenames, the node needs to request the blocks
		// from the name node
		// open prop send msg to name node
		String filename = "INTERMEDIATE" + task.getJobId() + "-";
		// //////////////////////////////////////
		// create all intermediate files for this reducer (key files)
		for (String partFile : task.getPartitionKeys()) {

			createIntermediateFile(filename + partFile, task.getId());
		}

		// //////////////////////////////////////
		// Use reflection to start reducer
		String monkeymrred = task.getTaskClassName() + Constants.JOB_REDUCE;
		URLClassLoader cl = null;
		try {
			URL[] urls = { new URL("jar:file:" + task.getJarFilePath() + "/"
					+ task.getJarName() + "!/") };
			cl = URLClassLoader.newInstance(urls);
			Class<?> c0 = cl.loadClass(monkeymrred);
			Class<?> reflectClass = Class.forName(monkeymrred, true, cl);
			MRReduce redObj = (MRReduce) reflectClass.newInstance();
			Method[] methods = reflectClass.getDeclaredMethods();
			Method method = null;
			for (Method m : methods) {
				if (m.getName() == Constants.METHOD_REDUCE) {
					method = m;
					break;
				}
			}
			Thread mapperThread = new Thread(new MRRedRunner(task, redObj,
					method));
			mapperThread.start();
		} catch (ClassNotFoundException | MalformedURLException
				| InstantiationException | IllegalAccessException ex) {
			System.err.println("[LaunchReducer] ! Err: Failed to load reducer");
		}
	}

	/**
	 * Load needed properties
	 */
	private void LoadProperties() {
		Properties prop = new Properties();
		InputStream input = null;
		try {
			input = new FileInputStream(Constants.MR_CONFIG);
			prop.load(input);
			// load properties
			DFS_PORT = Integer.valueOf(prop.getProperty(Constants.DFS_PORT));
			DFS_IP = prop.getProperty(Constants.DFS_IP);
			REDUCERS_PATH = prop
					.getProperty(Constants.LOCAL_INTERMEDIATE_REDUCE);
		} catch (IOException ex) {
			System.err
					.println("[LaunchReducer] ! Err - Properties file not found.");
			//ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					System.err
							.println("[LaunchReducer] ! Err - Properties file cannot close.");
					//e.printStackTrace();
				}
			}
		}

	}

	/**
	 * Obtains all the dfs blocks that makeup the partFile along with records
	 * 
	 * @param partFile
	 * @return
	 */
	private void createIntermediateFile(String partFile, String taskId) {
		DFSMessage msg = new DFSMessage();
		msg.setFilename(partFile);
		msg.setType(DFSMessage.MessageType.GETFILEBLOCKS);
		// System.out.println("[MRWorkerListener] partFile " + partFile);
		try {
			// //////////////////////////////////////
			// get blocks for partFile key
			socket = new Socket(DFS_IP, DFS_PORT);
			OutputStream os = socket.getOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(os);
			oos.writeObject(msg);
			ois = new ObjectInputStream(socket.getInputStream());
			msg = (DFSMessage) ois.readObject(); // replymsg
			// //////////////////////////////////////
			// create intermediate file
			File parent = new File(REDUCERS_PATH + taskId);
			parent.mkdirs();
			File file = new File(parent, partFile);
			// System.err.println("[MRWorkerListener] file" + file.toString());
			FileOutputStream output = null;
			// //////////////////////////////////////
			// add records to intermediate file
			try {
				output = new FileOutputStream(file);
				if (msg.getDFSBlocks() == null) {

					System.err
							.println("[MRWorkerListener] DFSBlocks is null for: "
									+ file.getPath());
					output.close();

				} else {
					for (DFSBlock b : msg.getDFSBlocks()) {
						List<String> records = getBlockRecords(b);
						IOUtils.writeLines(records, "\n", output); // test TODO
					}
				}
				output.close();
			} catch (IOException e) {
				System.err
						.println("[MRWorkerListener] ! Err: Failed to save LOCAL_INTERMEDIATE_REDUCE file. ");
				//e.printStackTrace();
			} finally {
				if (output != null) {
					try {
						output.close();
					} catch (IOException e) {
						System.err
								.println("[MRWorkerListener] ! Err: Failed to close LOCAL_INTERMEDIATE_REDUCE file. ");
					}
				}
			}
		} catch (IOException | ClassNotFoundException e) {
			System.err
					.println("[MRWorkerListener] ! Err - LOCAL_INTERMEDIATE_REDUCE: Failed to send message to DFS Name Node.");
		} finally {
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException e) {
					System.err
							.println("[MRWorkerListener] ! Err - LOCAL_INTERMEDIATE_REDUCE: Failed to close socket.");
				}
			}
		}
		socket = null;
	}

	private List<String> getBlockRecords(DFSBlock b) {
		List<String> records = new ArrayList<String>();
		DFSMessage msg = new DFSMessage();
		msg.setBlock(b);
		msg.setType(MessageType.GETBLOCK);
		try {
			// send reduce task message 2 node
			socket = new Socket(b.getIp(), b.getPort());
			OutputStream os = socket.getOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(os);
			oos.writeObject(msg);
			ois = new ObjectInputStream(socket.getInputStream());
			msg = (DFSMessage) ois.readObject(); // replymsg
			records = msg.getBlock().getRecords(); // only thing I'm interested
													// in
		} catch (IOException | ClassNotFoundException e) {
			System.err
					.println("[MRWorkerListener - getBlockRecords] ! Err: Failed to send message to node.");
		} finally {
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException e) {
					System.err
							.println("[MRWorkerListener - getBlockRecords] ! Err: Failed to close socket.");
				}
			}
		}
		return records;
	}

	/**
	 * Start a mapper job
	 * 
	 * @param task
	 */
	private void LaunchMapper(MRTask task) {

		String monkeymrmap = task.getTaskClassName();

		// JobConfiguration jobConf = null;
		URLClassLoader cl = null;
		try {
			URL[] urls = { new URL("jar:file:" + task.getJarFilePath() + "/"
					+ task.getJarName() + "!/") };

			System.out.println(task.getJarFilePath());

			cl = URLClassLoader.newInstance(urls);
			
			// get class from reflections
			Class<?> c0 = cl.loadClass(monkeymrmap + "$Map");

			Class<?> reflectClass = Class.forName(monkeymrmap + "$Map", true,
					cl);

			
			//Get method from reflections
			MRMap Mapobject = (MRMap) reflectClass.newInstance();

			Method[] methods = reflectClass.getDeclaredMethods();

			Method method = null;

			for (Method m : methods) {
				if (m.getName() == "map")
					method = m;
			}

			
			//Launch runner thread
			Thread mapperThread = new Thread(new MRMapRunner(task, Mapobject,
					method));
			mapperThread.start();

		} catch (ClassNotFoundException | MalformedURLException
				| InstantiationException | IllegalAccessException ex) {
			System.err.println("[LaunchMapper] ! Err: Failed to load mapper");
			//ex.printStackTrace();
		}
	}

}
