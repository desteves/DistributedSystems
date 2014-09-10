package edu.cmu.ds.donkeyfs;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Properties;

/**
 * A node in the DonKeyFS
 * 
 */
public class DFSNode {
	public static void main(String[] args) throws UnknownHostException,
			IOException, InterruptedException {
		Properties prop = new Properties();
		InputStream input = null;
		input = new FileInputStream(DFSConstants.DFS_CONFIG);
		prop.load(input);
		String masterIp = prop.getProperty(DFSConstants.MASTER_IP);
		Integer masterPort = Integer.valueOf(prop
				.getProperty(DFSConstants.MASTER_PORT));
		System.out
		.println("[DonKeyFS - Node] Properties successfully loaded. ");
		
		//launch server in random port number
		ServerSocket listeningServer = new ServerSocket(0);

		int listeningNumber = listeningServer.getLocalPort();

		Socket s = new Socket(masterIp, masterPort);

		String sendMessage = s.getLocalAddress().toString().split("/")[1] + "-"
				+ listeningNumber;

		DFSMessage msg = new DFSMessage();
		msg.setType(DFSMessage.MessageType.ADDNODE);
		msg.setContent(sendMessage);

		ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
		oos.writeObject(msg);

		//open a listening thread
		Thread t = new Thread(new DFSNodeListener(listeningServer, sendMessage));
		t.start();

		while (true) {

			//Listen for heartbeat
			
			Thread.sleep(3000);
			msg = new DFSMessage();
			msg.setType(DFSMessage.MessageType.HEARTBEAT);
			msg.setContent("LUB-DUB");

			oos = new ObjectOutputStream(s.getOutputStream());
			oos.writeObject(msg);
		}
	}
}
