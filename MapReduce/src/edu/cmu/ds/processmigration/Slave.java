package edu.cmu.ds.processmigration;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Scanner;

import edu.cmu.ds.framework.interfaces.Constants;

public class Slave {

	

	public static HashMap<String,String> processes = new HashMap<String,String>();
	HashMap<String, Thread> MapOfThreads = new HashMap<String, Thread>();
	

	public static void main(String[] args) throws UnknownHostException,
			IOException, InterruptedException {

		
		System.out.println("Enter the port number where this machine listens :");
		Scanner scan = new Scanner(System.in);
		int listeningNumber = scan.nextInt();
		
		Thread t = new Thread(new SlaveListener(listeningNumber));
		t.start();
		
		
		
		
		Socket s = new Socket( Constants.SERVER_IP, Constants.SERVER_PORT);
		InputStreamReader ir = new InputStreamReader(s.getInputStream());
		BufferedReader br = new BufferedReader(ir);
		String sendMessage = "NEWCLIENT-" + s.getLocalAddress().toString().split("/")[1] + "-"
				+ listeningNumber;

		PrintStream ps = new PrintStream(s.getOutputStream());
		
		ps.println(sendMessage);
		
	
		while (true) {
			
			Thread.sleep(3000);
			ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
			oos.writeObject(processes);
		
		}

		

	}
}
