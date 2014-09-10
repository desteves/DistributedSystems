package edu.cmu.ds.processmigration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.io.ObjectInputStream;
import java.io.InputStream;

import edu.cmu.ds.framework.classes.MigratableProcess;

public class SlaveListener implements Runnable {

	private int portNumber;
	ServerSocket slavesocket;
	Socket socket;
	InputStreamReader ir;
	BufferedReader br;
	PrintStream ps;
	static Integer processid = 0;
	InputStream is;
	ObjectInputStream ois;

	HashMap<String, Thread> MapOfThreads = new HashMap<String, Thread>();
	HashMap<Thread, MigratableProcess> MapOfThreadstoProcess = new HashMap<Thread, MigratableProcess>();

	public SlaveListener(int portNumber) {
		this.portNumber = portNumber;
	}

	@SuppressWarnings("deprecation")
	public void run() {

		try {

			Thread tracker = new Thread(new TaskTracker(MapOfThreads));
			tracker.start();

			slavesocket = new ServerSocket(portNumber);

			while (true) {
				socket = slavesocket.accept();

				is = socket.getInputStream();
				// ir = new InputStreamReader(is);
				socket.setSoTimeout(100);

				ois = new ObjectInputStream(is);
				Object obj = ois.readObject();

				
				
				
				
				
//				if (obj instanceof String) {
//					String s = (String) obj;
//					if (s.startsWith("TERMINATE")) // else
//													// if(message.startsWith("TERMINATE"))
//					{
//						String name = s.substring(10);
//
//						Thread thread = MapOfThreads.get(name);
//
//						if (thread != null) {
//							MapOfThreads.remove(name);
//							Slave.processes.remove(name);
//							MapOfThreadstoProcess.remove(thread);
//							thread.stop();
//							// figure out how to suspend
//						}
//					} else if (s.startsWith("SEND")) {
//						String name = s.substring(5);
//
//						Thread thread = MapOfThreads.get(name);
//
//						if (thread != null) {
//							Runnable process = MapOfThreadstoProcess
//									.get(thread);
//
//							((MigratableProcess) process).suspend(); // suspend();
//							ObjectOutputStream oos = new ObjectOutputStream(
//									socket.getOutputStream());
//
//							oos.writeObject(process);
//							oos.close();
//						}
//					}
//				} else if (obj instanceof MigratableProcess) { // if
//					// (message.startsWith("LAUNCH"))
//					// {
//					processid++;
//					MigratableProcess process = (MigratableProcess) obj;
//
//					// Launch
//					Thread p = new Thread(process);
//					p.start();
//
//					String processName = socket.getInetAddress() + "/"
//							+ portNumber + "-" + processid;
//					String processClass = process.getClass().getCanonicalName();
//					Slave.processes.put(processName, processClass);
//					MapOfThreads.put(processName, p);
//					MapOfThreadstoProcess.put(p, process);
//				}
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
