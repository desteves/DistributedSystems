package edu.cmu.ds.rmi.server;

import java.net.InetAddress;
import java.util.Scanner;

import edu.cmu.ds.rmi.server.test.RemoteObjectA_1Interface;

public class Server {
	private ServerRegistryHandler serverRegistryHandler;
	private String port;
	private String ip;

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public Server() {
		super();
		
		// Any remote object should be of type server
		
		System.out.println("Enter the port number for this Objects listener");
		Scanner scan = new Scanner(System.in);

		port = scan.nextLine();

		System.out
				.println("Enter the service name for this remote object(Key - should be unique)");
		String name = scan.nextLine();
		try {
			ip = InetAddress.getLocalHost().getHostAddress();
			
			// Pass the details to handler for bind and rebind 
			serverRegistryHandler = new ServerRegistryHandler(ip, port, name,
					this.getClass().getCanonicalName(),
					RemoteObjectA_1Interface.class.getCanonicalName());
			
			serverRegistryHandler.rebind();
		
		} catch (Exception e) {
			System.out.println("Problem with binding ");
		}

	}
}
