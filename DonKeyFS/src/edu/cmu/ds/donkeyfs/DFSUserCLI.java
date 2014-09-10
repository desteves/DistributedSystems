package edu.cmu.ds.donkeyfs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.IOUtils;

import edu.cmu.ds.donkeyfs.DFSMessage.MessageType;

public class DFSUserCLI {

	
	// program is for downloading a file - all the blocks in it from DFS. 
	// First argument DFS file name , Second argument Local Folder.
	
	public static void main(String[] args) {
		Socket s;
		try {
			String dfsFile = args[0];
			String localFilePath = args[1];

			DFSMessage message = new DFSMessage();
			message.setFilename(dfsFile);
			message.setType(DFSMessage.MessageType.GETFILEBLOCKS);

			try {

				Properties prop = new Properties();
				InputStream input = null;
				input = new FileInputStream(DFSConstants.DFS_CONFIG);
				prop.load(input);
				String masterIp = prop.getProperty(DFSConstants.MASTER_IP);
				Integer masterPort = Integer.valueOf(prop
						.getProperty(DFSConstants.MASTER_PORT));

				s = new Socket(masterIp, masterPort);
				ObjectOutputStream oos;
				oos = new ObjectOutputStream(s.getOutputStream());
				oos.writeObject(message);
				ObjectInputStream ois = new ObjectInputStream(
						s.getInputStream());
				DFSMessage reply = (DFSMessage) ois.readObject();

				List<DFSBlock> blocks = reply.getDFSBlocks();

				for (DFSBlock block : blocks) {
					List<String> records = new ArrayList<String>();
					DFSMessage msg = new DFSMessage();
					msg.setBlock(block);
					msg.setType(MessageType.GETBLOCK);
					try {
						// send reduce task message 2 node
						Socket socket = new Socket(block.getIp(),
								block.getPort());
						OutputStream os = socket.getOutputStream();
						ObjectOutputStream ooos = new ObjectOutputStream(os);
						ooos.writeObject(msg);
						ois = new ObjectInputStream(socket.getInputStream());
						msg = (DFSMessage) ois.readObject(); // replymsg
						records = msg.getBlock().getRecords(); // only thing I'm
																// interested
																// in
					} catch (IOException | ClassNotFoundException e) {
						System.err
								.println("[DFSUserCLI - getBlockRecords] ! Err: Failed to send message to node.");
					}

					File parent = new File(localFilePath);
					parent.mkdirs();

					File file = new File(parent, block.getBlockname());

					FileOutputStream output = null;
					output = new FileOutputStream(file);
					IOUtils.writeLines(records, "\n", output);

				}

			} catch (IOException | ClassNotFoundException e) {
				System.err.println("! Err - Failed to send request.");
				// e.printStackTrace();
			}

		} catch (ArrayIndexOutOfBoundsException a) {
			System.out
					.println("Usage:  java  edu.cmu.ds.donkeyfs.DFSUserCLI [DFS-FILE-NAME] [LOCAL-FOLDER]");
		}

	}

}
