package edu.cmu.ds.processmigration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import edu.cmu.ds.test.MiniProcess;

public class ProcessManager {

	public ProcessManager() {

	}

	public static HashMap<String, HashMap<String, String>> registry = new HashMap<String, HashMap<String, String>>();
	private static Scanner scan;

	public static void main(String[] args) {

		MessageListener msglistener = new MessageListener();
		Thread t = new Thread(msglistener);
		t.start();

		scan = new Scanner(System.in);

		while (true) {
			System.out
					.println("Enter the option 1.List clients 2.Launch 3.Migrate 4.Terminate");
			int i = scan.nextInt();
			if (i == 1) {
				for (String s : registry.keySet()) {
					System.out.println(s + "\t" + registry.get(s));
				}
			} else if (i == 2) {
				if (registry.isEmpty()) {
					System.out.println("No clients available. Try again later");
					continue;
				}
				System.out
						.println("Select the client that should launch the process");

				int index = 0;
				for (String s : registry.keySet()) {
					System.out.println(index + ".  " + s + "\t"
							+ registry.get(s));
					++index;
				}

				int selectedSlave = scan.nextInt();

				String slave = (String) registry.keySet().toArray()[selectedSlave];

				String ip = slave.split("-")[1];
				String port = slave.split("-")[2];

				// InputStreamReader ir;

				System.out.println("Enter process: "); // edu.cmu.ds.processmigration.MiniProcess
				String processName = scan.next();
				System.out.println("Does the process have arguments? [Y|N]: ");
				String[] processArgs = null;
				String input = scan.next();
				if (input.startsWith("Y") || input.startsWith("y")) {
					BufferedReader br = new BufferedReader(
							new InputStreamReader(System.in));
					System.out.println("Enter args separated by a space: ");
					try {
						input = br.readLine();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					processArgs = input.trim().split(" ");

				}

				try {
					Constructor<?> reflectRunnableConstructor = null;
					Object reflectRunnableObj = null;
					Class<?> reflectRunnable = Class.forName(processName);
					Class[] reflectRunnableArgType = new Class[1];
					reflectRunnableArgType[0] = String[].class;
					if (processArgs == null)
						reflectRunnableConstructor = reflectRunnable
								.getConstructor();
					else
						reflectRunnableConstructor = reflectRunnable
								.getConstructor(reflectRunnableArgType);
					String n = reflectRunnableConstructor.getName();
					Object arg = processArgs;

					if (processArgs == null)
						reflectRunnableObj = reflectRunnableConstructor
								.newInstance();
					else
						reflectRunnableObj = reflectRunnableConstructor
								.newInstance(arg);
					System.out.println(ip + " " + port);

					Socket s = new Socket(ip, Integer.parseInt(port));
					// String sendMessage = "LAUNCH-" + processName;
					// OutputStream os = s.getOutputStream();
					ObjectOutputStream oos = new ObjectOutputStream(
							s.getOutputStream());
					// oos.writeObject("LAUNCH-" + processName);
					// Thread.sleep(100);
					oos.writeObject(reflectRunnableObj);
					oos.close();

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NoSuchMethodException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SecurityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InstantiationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} else if (i == 3) {
				if (registry.isEmpty()) {
					System.out.println("No clients available. Try again later");
					continue;
				}

				System.out.println("Select source client: ");
				int index = 0;
				for (String s : registry.keySet()) {
					System.out.println(index + ".  " + s + "\t"
							+ registry.get(s));
					++index;
				}
				int slaveClientSource = scan.nextInt();

				if (slaveClientSource < 0
						|| slaveClientSource >= registry.size()) {
					System.out.println("Out of bounds client. Try again.");
					continue;
				}

				// source is valid
				String slaveSource = (String) registry.keySet().toArray()[slaveClientSource];
				String ipSource = slaveSource.split("-")[1];
				String portSource = slaveSource.split("-")[2];

				HashMap<String, String> procsSource = registry.get(slaveSource);

				int procIndex = 0;
				System.out.println("Select process to migrate: ");
				for (String s : procsSource.keySet()) {
					System.out.println(procIndex + ". " + s + " "
							+ procsSource.get(s));
					++procIndex;
				}
				int clientProc = scan.nextInt();
				System.out.println("Select target client: ");
				index = 0;
				for (String s : registry.keySet()) {
					System.out.println(index + ".  " + s + "\t"
							+ registry.get(s));
					++index;
				}

				int slaveClientTarget = scan.nextInt();
				if (slaveClientTarget == slaveClientSource) {
					System.out.println("Target == Source. Nothing to do");
					continue;
				}
				if (slaveClientTarget < 0
						|| slaveClientTarget >= registry.size()) {
					System.out.println("Out of bounds client. Try again.");
					continue;
				}

				// target is valid
				String slaveTarget = (String) registry.keySet().toArray()[slaveClientTarget];
				String ipTarget = slaveTarget.split("-")[1];
				String portTarget = slaveTarget.split("-")[2];

				Socket s;
				Socket sTarget;
				try {
					String termMsg = "SEND-"
							+ procsSource.keySet().toArray()[clientProc];
					s = new Socket(ipSource, Integer.parseInt(portSource));
					ObjectOutputStream oos = new ObjectOutputStream(
							s.getOutputStream());
					oos.writeObject(termMsg);
//					oos.close();
					// get the obj
					ObjectInputStream ois = new ObjectInputStream(
							s.getInputStream());
					Object obj = ois.readObject();
//					s.close();

					if (obj instanceof Runnable) {
						Runnable r = (Runnable) obj;
						// send to target
						sTarget = new Socket(ipTarget, Integer.parseInt(portTarget));
						oos = new ObjectOutputStream(sTarget.getOutputStream());
						oos.writeObject(r);
						oos.close();
						sTarget.close();
					} else {
						// got something else?
						continue;
					}

				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else if (i == 4) {

				if (registry.isEmpty()) {
					System.out.println("No clients available. Try again later");
					continue;
				}
				System.out.println("Select the client that runs the process");

				int index = 0;
				for (String s : registry.keySet()) {
					System.out.println(index + ".  " + s + "\t"
							+ registry.get(s));
					++index;
				}

				int selectedSlave = scan.nextInt();

				String slave = (String) registry.keySet().toArray()[selectedSlave];

				String ip = slave.split("-")[1];
				String port = slave.split("-")[2];

				HashMap<String, String> procs = registry.get(slave);

				int procIndex = 0;
				System.out.println("Select the process to kill: ");
				for (String s : procs.keySet()) {

					System.out.println(procIndex + ". " + s + " "
							+ procs.get(s));
					procIndex++;
				}

				int selectedProc = scan.nextInt();

				try {

					String termMsg = "TERMINATE-"
							+ procs.keySet().toArray()[selectedProc];
					Socket s = new Socket(ip, Integer.parseInt(port));
					ObjectOutputStream oos = new ObjectOutputStream(
							s.getOutputStream());
					oos.writeObject(termMsg);
					oos.close();

				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} else {
				System.out.println("Invalid option. Try again.");
			}

		}

	}

}
