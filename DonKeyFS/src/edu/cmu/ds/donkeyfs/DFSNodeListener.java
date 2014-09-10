package edu.cmu.ds.donkeyfs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.IOUtils;

public class DFSNodeListener implements Runnable {

	private ServerSocket slavesocket;
	private Socket socket;
	private InputStream is;
	private ObjectInputStream ois;
	private ObjectOutputStream oos;

	HashMap<String, Thread> MapOfThreads = new HashMap<String, Thread>();

	public DFSNodeListener(ServerSocket listeningServer, String nodeName) {
		this.slavesocket = listeningServer;
	}

	public void run() {

		while (true) {
			try {
				socket = slavesocket.accept();
				socket.setSoTimeout(1000);
				is = socket.getInputStream();
				ois = new ObjectInputStream(is);
				DFSMessage message;
				Object obj = ois.readObject();
				DFSMessage reply = null;

				if (obj != null && obj instanceof DFSMessage) {
					message = (DFSMessage) obj;
					if (message.getType() == DFSMessage.MessageType.REMOVENODE) {
						reply = removeNode(message);
					} else if (message.getType() == DFSMessage.MessageType.GETBLOCK) {
						reply = getBlock(message);
					} else if (message.getType() == DFSMessage.MessageType.PUTBLOCK) {
						DFSBlock block = message.getBlock();
						List<String> blockRows = block.getRecords();
						String blockname = block.getBlockname();
						PutFileBlock(blockname, blockRows, block.getFilename(),
								block.getfileExtension());
					} else if (message.getType() == DFSMessage.MessageType.SAVEJAR) {
						reply = saveJar(message);
					}

					if (reply != null) {
						// write the reply
						try {
							oos = new ObjectOutputStream(
									socket.getOutputStream());
							oos.writeObject(reply);
						} catch (IOException e) {
							System.err
									.println("[DFSNodeListener - run] ! Err: Failed to write reply.");
						}
					}
				}
			} catch (IOException | ClassNotFoundException e) {
				System.err
						.println("[DFSNodeListener] ! Err: Socket Connection");
			}
		}

	}

	private DFSMessage saveJar(DFSMessage message) {

		File parent = new File(message.getJarPath());

		parent.mkdirs();
		File file = new File(parent, message.getJarName());
		FileOutputStream output = null;
		try {
			output = new FileOutputStream(file);
			IOUtils.write(message.getJarBytes(), output);
			output.close();
		} catch (IOException e) {
			System.err.println("[saveJar] ! Err: Failed to save Jar file. ");
		} finally {
			if (output != null) {
				try {
					output.close();
					System.out.println("[saveJar] Jar file saved.");
				} catch (IOException e) {
					System.err
							.println("[saveJar] ! Err: Failed to close Jar file. ");
				}
			}
		}
		return null;
	}

	private void PutFileBlock(String blockname, List<String> blockRows,
			String filename, String fileExtension) {

		Properties prop = new Properties();
		InputStream input = null;

		try {
			input = new FileInputStream(DFSConstants.DFS_CONFIG);
			prop.load(input);
		} catch (IOException e) {
			System.err
					.println("[PutFileBlock] ! Err: Failed to load properties. ");
		}
		String path = prop.getProperty(DFSConstants.DFS_PATH);
		File parent = new File(path + filename + "/");
		parent.mkdirs();
		File file = new File(parent, blockname + fileExtension);

		try {
			if (!file.exists()) {
				FileWriter writer = new FileWriter(file);
				for (String str : blockRows) {

					writer.write(str + "\n");

				}
				writer.close();
			}
		} catch (IOException e) {
			System.err
					.println("[PutFileBlock] ! Err: Failed to save Jar file. ");
		}
	}

	/**
	 * Get records in this block -- chunk of file.
	 * 
	 * @param message
	 * @return
	 */
	private DFSMessage getBlock(DFSMessage message) {
		System.out.println("[getBlock] " + message.getBlock().getBlockpath());
		DFSMessage reply = new DFSMessage();
		DFSBlock block = new DFSBlock();
		List<String> records = new ArrayList<String>();
		BufferedReader br = null;
		String line = "";

		Properties prop = new Properties();
		InputStream input = null;

		try {
			input = new FileInputStream(DFSConstants.DFS_CONFIG);
			prop.load(input);
		} catch (IOException e) {
			System.err
					.println("[PutFileBlock] ! Err: Failed to load properties. ");
		}
		String path = prop.getProperty(DFSConstants.DFS_PATH);

		try {
			br = new BufferedReader(new FileReader(new File(path
					+ message.getBlock().getBlockpath() + "/"
					+ message.getBlock().getBlockname())));
			while ((line = br.readLine()) != null) {
				records.add(line);
			}
			block.setRecords(records);
			reply.setBlock(block);

		} catch (IOException e) {
			System.err.println("[getBlock] ! Err: Failed to open file. ");
			// e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					System.err
							.println("[getBlock] ! Err: Failed to close file. ");
					// e.printStackTrace();
				}
			}
			br = null;
			is = null;
		}

		// return the block
		return reply;
	}

	private DFSMessage removeNode(DFSMessage message) {
		return null;
	}
}
