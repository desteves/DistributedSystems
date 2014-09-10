package edu.cmu.ds.donkeyfs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.Date;
import java.util.HashMap;
import java.util.Timer;

public class DFSNodeTracker implements Runnable {

	protected Socket clientSocket = null;
	protected String serverText = null;

	public DFSNodeTracker(Socket clientSocket, String name) {
		this.clientSocket = clientSocket;
		this.serverText = name;
	}

	public void run() {
		try {
			InputStream input = clientSocket.getInputStream();
			OutputStream output = clientSocket.getOutputStream();
			PrintStream ps = new PrintStream(output);

			InputStreamReader ir = new InputStreamReader(input);
			BufferedReader br = new BufferedReader(ir);

			clientSocket.setSoTimeout(10000);

			while (true) {

				// recieve heartbeat . Can implement getting list of file blocks from each node and updating name node.
				try {

					ObjectInputStream ois = new ObjectInputStream(
							clientSocket.getInputStream());
					DFSMessage heartbeat = (DFSMessage) ois.readObject();

					// System.out.println(serverText +"-"
					// +heartbeat.getContent());

				} catch (SocketException | ClassNotFoundException se) {
					break;
				} catch (java.io.EOFException ex) {
					System.out.println(serverText + " Stopped");
					DFSNameNode.nodeRegistry.remove(serverText);
					break;
				}

			}

			System.out.println("Connection lost to " + serverText);

			DFSNameNode.nodeRegistry.remove(serverText);

			input.close();
			output.close();
			clientSocket.close();

		} catch (IOException e) {
			// report exception somewhere.
			System.err.println("[DFSNodeTracker] ! Err -IOException .");

			// e.printStackTrace();
		}
	}
}
