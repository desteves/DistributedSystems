package edu.cmu.ds.monkeymr.worker;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import edu.cmu.ds.donkeyfs.DFSConstants;
import edu.cmu.ds.monkeymr.comm.MRHeartBeat;
import edu.cmu.ds.monkeymr.interfaces.Constants;
import edu.cmu.ds.monkeymr.master.MRTask;

// main thread of a worker node

public class MRTaskNode {

	public static HashMap<MRTask, MRTask.TaskStatus> taskNodeRegistry = new HashMap<MRTask, MRTask.TaskStatus>();

	public static void main(String[] args) {

		Properties prop = new Properties();
		InputStream input = null;

		try {
			ServerSocket listeningServer = new ServerSocket(0);
			input = new FileInputStream(Constants.MR_CONFIG);
			prop.load(input);
			String masterIp = prop.getProperty(Constants.MASTER_IP);
			Integer masterPort = Integer.valueOf(prop
					.getProperty(Constants.MASTER_PORT));
			System.out
					.println("[MonkeyMR - Task Node] Properties successfully loaded. ");

			int listeningPort = listeningServer.getLocalPort();

			Socket socket = new Socket(masterIp, masterPort);
			OutputStream os = socket.getOutputStream();

			ObjectOutputStream oos = new ObjectOutputStream(os);

			String nodeName = "MRNODE-"
					+ socket.getLocalAddress().toString().split("/")[1] + "-"
					+ listeningPort;

			MRHeartBeat hb = new MRHeartBeat();
			hb.setTaskNodeName(nodeName);
			hb.setTasks(taskNodeRegistry);
			oos.writeObject(hb);

			Thread t = new Thread(new MRWorkerListener(nodeName,
					listeningServer));
			t.start();


			while (true) {

				//heartbeats 
				
				Thread.sleep(3000);
				hb = new MRHeartBeat();
				hb.setTaskNodeName(nodeName);
				hb.setTasks(taskNodeRegistry);
				// System.out.println(MRTaskNode.taskNodeRegistry.toString());
				oos = new ObjectOutputStream(os);
				oos.writeObject(hb);

			}

		} catch (IOException | InterruptedException e) {
			System.err.println("[TaskNode] ! Err - Connection/Timeout .");
		}

	}

}
