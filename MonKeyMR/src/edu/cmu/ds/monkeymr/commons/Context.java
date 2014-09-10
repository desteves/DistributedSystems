package edu.cmu.ds.monkeymr.commons;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import edu.cmu.ds.donkeyfs.DFSBlock;
import edu.cmu.ds.donkeyfs.DFSFile;
import edu.cmu.ds.donkeyfs.DFSMessage;
import edu.cmu.ds.monkeymr.interfaces.Constants;
import edu.cmu.ds.monkeymr.master.MRTask;

public class Context {

	private String dfsPath;
	public FileWriter writer;
	public List<String> keys;
	private String fileName;
	private MRTask task;
	private String DfsIp = "";
	private Integer DfsPort = 0;

	public Context(String dfsPath, MRTask task) {
		this.dfsPath = dfsPath;
		keys = new ArrayList<String>();
		fileName = task.getOutputFile();
		this.task = task;
	}

	
	// write method writes mapper or reducers output to intermediate or final output files
	
	public void write(Object key, Object value) {
		Socket socket;
		LoadProperties();
		
		
		//If task is map. 
		
		if (task.getTaskType() == MRTask.TaskType.MAP) {
			File file = new File(dfsPath + fileName + "-" + key.toString()
					+ "/");
			String BlockName = fileName + "-" + key.toString();

			try {
				
				
				// add the new file entry to DFS Name node
				
				socket = new Socket(DfsIp, DfsPort);
				DFSMessage mss = new DFSMessage();
				mss.setType(DFSMessage.MessageType.ADDENTRY);
				DFSBlock block = new DFSBlock();
				block.setBlockname(BlockName);
				block.setIp(socket.getLocalAddress().toString().split("/")[1]);
				block.setfileExtension("");
				block.setBlockpath(fileName + "-" + key.toString() + "/");
				DFSFile dfsfile = new DFSFile(fileName + "-" + key.toString());
				dfsfile.setFilename(fileName + "-" + key.toString());
				mss.setBlock(block);
				mss.setDFSfile(dfsfile);
				OutputStream o = socket.getOutputStream();
				ObjectOutputStream oos = new ObjectOutputStream(o);
				oos.writeObject(mss);
			} catch (IOException e) {
				System.err.println("[Context] ! Err - Bad socket connection.");
			}

			file.mkdirs();
			try {
				writer = new FileWriter(dfsPath + fileName + "-"
						+ key.toString() + "/" + fileName + "-"
						+ key.toString(), true);

				if (!keys.contains(key.toString()))
					keys.add(key.toString());
				writer.write(key.toString() + "\t" + value.toString() + "\n");
				writer.close();
			} catch (IOException e) {
				System.err.println("[Context] ! Err - Bad file write.");
			}
		} else {
			
			
			// If task is reduce. Lots of duplicate code. Need to refractor. 
			
			String outfile = task.getOutputFile();
			File file = new File(dfsPath
					+ fileName.substring(0, fileName.lastIndexOf("-")) + "/");
			String BlockName = fileName;

			try {
				
				// add the new file entry to DFS Name node
				
				socket = new Socket(DfsIp, DfsPort);
				DFSMessage mss = new DFSMessage();
				mss.setType(DFSMessage.MessageType.ADDENTRY);
				DFSBlock block = new DFSBlock();
				block.setBlockname(BlockName);
				block.setIp(socket.getLocalAddress().toString().split("/")[1]);
				block.setfileExtension("");
				block.setBlockpath(fileName.substring(0,
						fileName.lastIndexOf("-"))
						+ "/");
				DFSFile dfsfile = new DFSFile(fileName.substring(0,
						fileName.lastIndexOf("-")));
				dfsfile.setFilename(fileName.substring(0,
						fileName.lastIndexOf("-")));
				mss.setBlock(block);
				mss.setDFSfile(dfsfile);
				OutputStream o = socket.getOutputStream();
				ObjectOutputStream oos = new ObjectOutputStream(o);
				oos.writeObject(mss);
			} catch (IOException e) {
				System.err.println("[Context] ! Err - Bad socket connection.");
			}

			file.mkdirs();
			try {
				writer = new FileWriter(dfsPath
						+ fileName.substring(0, fileName.lastIndexOf("-"))
						+ "/" + fileName, true);
				if (!keys.contains(key.toString()))
					keys.add(key.toString());
				writer.write(key.toString() + "\t" + value.toString() + "\n");
				writer.close();
			} catch (Exception e) {
				System.err.println(" Context writer error");
			}
		}

	}

	private void LoadProperties() {
		Properties prop = new Properties();
		InputStream input = null;
		try {
			input = new FileInputStream(Constants.MR_CONFIG);
			prop.load(input);
			// load properties
			DfsIp = prop.getProperty(Constants.DFS_IP);
			DfsPort = Integer.parseInt(prop.getProperty(Constants.DFS_PORT));
		} catch (IOException ex) {
			System.err.println("! Err - Properties file not found.");
		}

	}

}
