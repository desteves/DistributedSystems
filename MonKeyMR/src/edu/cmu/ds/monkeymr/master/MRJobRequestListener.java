package edu.cmu.ds.monkeymr.master;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import edu.cmu.ds.donkeyfs.DFSFile;
import edu.cmu.ds.donkeyfs.DFSMessage;
import edu.cmu.ds.donkeyfs.DFSMessage.MessageType;
import edu.cmu.ds.monkeymr.comm.MRHeartBeat;
import edu.cmu.ds.monkeymr.comm.MRJobRequestMessage;
import edu.cmu.ds.monkeymr.commons.JobConfiguration;
import edu.cmu.ds.monkeymr.commons.MonkeyMR;
import edu.cmu.ds.monkeymr.interfaces.Constants;

/**
 * Receives new job requests from participants Processes the new job request and
 * registers it.
 * 
 * It also receives heart beats to register nodes
 * 
 * @author Linne
 * 
 */
public class MRJobRequestListener implements Runnable {

	private ServerSocket servsocket;
	private Socket socket;
	private InputStream is;
	private ObjectInputStream ois;
	private int port;
	private static Socket s;
	private MonkeyMR monkeyMR;
	private String localJars;
	private String MRProgramClassName;

	public MRJobRequestListener(int port) {
		this.port = port;
		Properties prop = new Properties();
		InputStream input = null;
		try {
			input = new FileInputStream(Constants.MR_CONFIG);
			prop.load(input);
			// load properties
			localJars = prop.getProperty(Constants.LOCAL_JARS);
		} catch (IOException ex) {
			System.err.println("! Err - Properties file not found.");
			// ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					System.err.println("! Err - Properties file cannot close.");
					// e.printStackTrace();
				}
			}
		}

	}

	@Override
	public void run() {
		try {
			servsocket = new ServerSocket(port);
		} catch (IOException e) {
			System.err
					.println("[MRJobRequestListener] ! Err: Server Socket Connection");
			// e.printStackTrace();
		}
		while (true) {
			try {
				socket = servsocket.accept();
				is = socket.getInputStream();
				ois = new ObjectInputStream(is);
				Object obj = ois.readObject();

				DFSMessage reply = null;

				// if a new job request, handle it here
				if (obj != null && obj instanceof MRJobRequestMessage) {
					MRJobRequestMessage message = (MRJobRequestMessage) obj;

					// register the job and set the status as
					MRJob job = new MRJob();

					// save JarFile in the DFS get path
					SaveJarFileDFS(message);
					String jarpath = SaveJarFileLocally(message);
					JobConfiguration jobConf = GetJobConf(message, jarpath);

					if (jobConf != null) {
						job.setJobConf(jobConf);
						job.setJarName(message.getJarName());
						// if the file is not loaded onto our DFS, then do so
						// by sending message to the DFS with info
						if (!jobConf.getIsInputInDFS()) {
							DFSMessage dfsMessage = new DFSMessage();
							dfsMessage.setType(MessageType.ADDFILE);
							dfsMessage.setFilename(jobConf.getInputfile());
							dfsMessage.setContent(jobConf.getInputfile());
							reply = SendDFSMessage(dfsMessage);
							job.setInputFile(reply.getDFSfile());
							job.setInputFileBlocks(reply.getDFSBlocks());
						} else {
							job.setInputFile(new DFSFile(jobConf.getInputfile()));

						}
						// unique job id = file name + current timestamp
						job.setJobId(FilenameUtils
								.removeExtension(FilenameUtils.getName(jobConf
										.getInputfile()))
								+ String.valueOf(System.currentTimeMillis()));

						job.setOutputDir(jobConf.getOutputfile());
						System.out
								.println("[MRJobRequestListener] Extracted Job Conf");
					} else {
						System.err
								.println("[MRJobRequestListener] ! Err: Job Conf not retrieved.");
					}

					job.setJarPath(jarpath);
					job.setJarArgs(message.getJarArgs());

					job.setJobClassName(MRProgramClassName);

					if (Enqueue(job))// add job to queue
						System.out
								.println("[MRJobRequestListener] Queued Job as "
										+ job.getJobId());

					// write the reply
					ObjectOutputStream oos;
					oos = new ObjectOutputStream(socket.getOutputStream());
					oos.writeObject("Job Added");

				} else if (obj != null && obj instanceof MRHeartBeat) {
					MRHeartBeat hb = (MRHeartBeat) obj;
					String name = hb.getTaskNodeName();

					String ip = name.split("-")[1];
					Integer port = Integer.parseInt(name.split("-")[2]);

					MRMasterCLI.nodeRegistry.put(ip + "-" + port, new HashMap<MRTask, MRTask.TaskStatus>());
					System.out.println("[MRJobRequestListener] RegisterMRNode "
							+ name);
					new Thread(new MRNodeTracker(socket, name)).start();
				}

			} catch (IOException | ClassNotFoundException e) {
				System.err
						.println("[MRJobRequestListener] ! Err: Socket Connection");
				// e.printStackTrace();
			}
		}
	}

	private String SaveJarFileLocally(MRJobRequestMessage message) {
		File parent = new File(localJars);
		parent.mkdirs();
		File file = new File(parent, message.getJarName());
		FileOutputStream output = null;
		try {
			output = new FileOutputStream(file);

			IOUtils.write(message.getJarFile(), output);
			output.close();
		} catch (IOException e) {
			System.err.println("[saveJar] ! Err: Failed to save Jar file. ");
			// e.printStackTrace();
		} finally {
			if (output != null) {
				try {
					output.close();
					// System.out.println("[saveJar] Jar file saved.");
				} catch (IOException e) {
					System.err
							.println("[saveJar] ! Err: Failed to close Jar file. ");
				}
			}
		}
		return file.getPath();
	}

	/**
	 * Adds job to the jobQueue
	 * 
	 * @param job
	 * @return
	 */
	private boolean Enqueue(MRJob job) {
		return MRMasterCLI.jobQueue.add(job);
	}

	/**
	 * Opens jar file by saving it locally and populate the DFSMessage
	 * 
	 * @param message
	 * @return
	 */
	private String SaveJarFileDFS(MRJobRequestMessage message) {
		// put jar file in the DFS and return jar file path
		DFSMessage reply = null;

		DFSMessage dfsMessage = new DFSMessage();
		dfsMessage.setType(MessageType.SAVEJAR);
		dfsMessage.setJarBytes(message.getJarFile());
		dfsMessage.setJarName(message.getJarName());
		reply = SendDFSMessage(dfsMessage);
		if (reply != null)
			return reply.getJarPath();
		else
			return "";
	}

	/**
	 * 
	 * @param msg
	 * @param pathToJar
	 * @return
	 * 
	 *         https://stackoverflow.com/questions/11016092/how-to-load-classes-
	 *         at-runtime-from-a-folder-or-jar
	 */
	private JobConfiguration GetJobConf(MRJobRequestMessage msg,
			String pathToJar) {

		String monkeymr = msg.getJarArgs().get(0); // MonkeyMR Class
		MRProgramClassName = monkeymr;
		String monkeymrmap = monkeymr + "$Map";
		String monkeymrred = monkeymr + "$Reduce";
		JobConfiguration jobConf = null;
		URLClassLoader cl = null;
		try {
			URL[] urls = { new URL("jar:file:" + pathToJar + "!/") };
			cl = URLClassLoader.newInstance(urls);
			Class<?> c0 = cl.loadClass(monkeymrmap);
			Class<?> c1 = cl.loadClass(monkeymrred);
			Class<?> c2 = cl.loadClass(monkeymr);
		} catch (IOException | ClassNotFoundException e1) {
			System.err
					.println("[GetJobConf] ! Err: Failed to load jars' classes. ");
			// e1.printStackTrace();
		}

		// create jobConf object with monkeymr
		try {
			List<String> args = (ArrayList<String>) msg.getJarArgs();
			args.remove(0); // remove the class to instantiate

			Class<?> reflectClass = Class.forName(monkeymr, true, cl);
			Class[] reflectArgType = new Class[1];
			reflectArgType[0] = String[].class;
			Constructor<?> reflectConstructor = reflectClass
					.getConstructor(reflectArgType);
			String[] argsArr = args.toArray(new String[args.size()]);
			Object arg = argsArr;
			Object reflectObj = reflectConstructor.newInstance(arg);
			if (reflectObj instanceof MonkeyMR) {
				monkeyMR = (MonkeyMR) reflectObj;
				jobConf = monkeyMR.conf;
				System.out
						.println("[MRJobRequestListener] Created monkeyMR through reflections");
			}

		} catch (ClassNotFoundException | NoSuchMethodException
				| SecurityException | InstantiationException
				| IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			System.err.println("[GetJobConf] ! Err: Failed to get job conf. ");
			// e.printStackTrace();
		}

		return jobConf;
	}

	/**
	 * Adds AFS file to DonKeyFS
	 * 
	 * @param msg
	 * @param ip
	 * @param port
	 */
	private static DFSMessage SendDFSMessage(DFSMessage msg) {
		DFSMessage reply = null;

		try {
			s = new Socket(MRMasterCLI.DFS_IP, MRMasterCLI.DFS_PORT);
			ObjectOutputStream oos;
			oos = new ObjectOutputStream(s.getOutputStream());
			oos.writeObject(msg);
			ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
			reply = (DFSMessage) ois.readObject();

		} catch (IOException | ClassNotFoundException e) {
			System.err.println("! Err - Failed to send job to master.");
			// e.printStackTrace();
		}
		return reply;
	}
}
