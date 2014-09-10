
package edu.cmu.ds.processmigration;

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

public class SlaveNodeTracker implements Runnable {

	protected Socket clientSocket = null;
	protected String serverText = null;

	public SlaveNodeTracker(Socket clientSocket, String name) {
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

				// recieve heartbeat and do the needful
				try {
					
						ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream());
						HashMap<String,String> processes = (HashMap<String,String>) ois.readObject();
						
						Master.registry.put(serverText,processes);
						
					// System.out.println(beat);
				} catch (SocketException | ClassNotFoundException se) {
					break;
				}
				catch(java.io.EOFException ex)
				{
						System.out.println(serverText+ " Stopped");
						Master.registry.remove(serverText);
						break;
				}

			}

			System.out.println("Connection lost to" + serverText);

			Master.registry.remove(serverText);

			input.close();
			output.close();
			clientSocket.close();

		} catch (IOException e) {
			// report exception somewhere.
			e.printStackTrace();
		}
	}
}
