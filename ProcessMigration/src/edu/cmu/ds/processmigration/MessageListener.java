package edu.cmu.ds.processmigration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class MessageListener implements Runnable {
	ServerSocket servsocket;
	Socket socket;
	InputStreamReader ir;
	BufferedReader br;

	PrintStream ps;

	public void run() {

		try {
			servsocket = new ServerSocket(9999);
		} catch (IOException e) {
			System.out.println(e);
		}

		while (true) {
			try {

				socket = servsocket.accept();
				ir = new InputStreamReader(socket.getInputStream());
				br = new BufferedReader(ir);
				ps = new PrintStream(socket.getOutputStream());

				String message;
				message = br.readLine();
				if (message.isEmpty())
					continue;
				// message from client - format
				// NEWCLIENT-<IPADDRESS>-<PORTNUMBER>
				if (message.startsWith("NEWCLIENT")) {
					RegisterClient(socket, message.substring(10));
					ps.println("PROCESSES-REQ");
				}

				if (message.startsWith("CLIENTSTOPPED")) {
					// message from client with name - format
					// CLIENTSTOPPED-<IDENTIFIER>
					DeRegisterClient(message.substring(14));
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public String RegisterClient(Socket clientSocket, String str) {

		int count = ProcessManager.registry.size();

		String name = "CLIENT-" + str;

		String[] arr = str.split("-");

		ProcessManager.registry.put(name, new HashMap<String,String>());

		new Thread(new WorkerRunnable(clientSocket, name)).start();

		return name;

	}

	public void DeRegisterClient(String name) {

		HashMap<String, String> removed = ProcessManager.registry.remove(name);
		System.out.println("DeRegisterClient " + name);
	}

}
