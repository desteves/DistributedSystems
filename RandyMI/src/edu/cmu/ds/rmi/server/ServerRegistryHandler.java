package edu.cmu.ds.rmi.server;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

import edu.cmu.ds.rmi.comm.RegistryMessage;
import edu.cmu.ds.rmi.comm.RegistryMessage.Type;


public class ServerRegistryHandler {


	String registryIP ;
	int registryPortNumber=0;
	String serviceName;
	String InterFaceName;
	String ip;
	String portNumber;
	String className;
	
	public ServerRegistryHandler(String IP,String Port,String serviceName,String className,String InterfaceName)
	{
		this.ip=IP;
		this.portNumber = Port;
		this.InterFaceName = InterfaceName;
		this.serviceName = serviceName;
		this.className = className;
		
		System.out.println("Enter the registry IP");

		Scanner scan = new Scanner(System.in);
		
		registryIP = scan.nextLine();
		
	
		System.out.println("Enter the registry Port");

		while(true)
		{
		try
		{
			scan = new Scanner(System.in);

			registryPortNumber= scan.nextInt();
			break;
			
		}
		catch(Exception ex)
		{
			System.out.println("Invalid port Number. Retry ");
			continue;
		}
		}
		
	}
	
	public boolean bind()
	{
		RegistryMessage comm = new RegistryMessage();
		comm.type = Type.bind;
		comm.ServiceName = serviceName;
		comm.ip=ip;
		comm.port= portNumber;
		comm.ClassName = className; 
		comm.InterfaceName =InterFaceName;
		
		try {
			Socket regSocket = new Socket(registryIP,registryPortNumber);
			
			ObjectOutputStream oos = new ObjectOutputStream(
					regSocket.getOutputStream());
			
			oos.writeObject(comm);
			oos.close();
				
		} catch (Exception e) {
			
			System.out.println("Wrong Server IP/Port");
		} 
		
		
		
		
		return true;
	}
	
	public boolean rebind()
	{
	RegistryMessage comm = new RegistryMessage();
	comm.type = Type.rebind;
	comm.ServiceName = serviceName;
	comm.ip=ip;
	comm.port= portNumber;
	comm.ClassName = className; 
	
	try {
		Socket regSocket = new Socket(registryIP,registryPortNumber);
		
		ObjectOutputStream oos = new ObjectOutputStream(
				regSocket.getOutputStream());
		
		oos.writeObject(comm);
		oos.close();
			
	} catch (Exception e) {
		
		System.out.println("Wrong Server IP/Port");
	} 
	
	
	
	
	return true;
}
	
	
	public boolean unbind()
	{
	RegistryMessage comm = new RegistryMessage();
	comm.type = Type.unbind;
	comm.ServiceName = serviceName;
	comm.ip=ip;
	comm.port= portNumber;
	comm.ClassName = className; 
	
	try {
		Socket regSocket = new Socket(registryIP,registryPortNumber);
		
		ObjectOutputStream oos = new ObjectOutputStream(
				regSocket.getOutputStream());
		
		oos.writeObject(comm);
		oos.close();
			
	} catch (UnknownHostException e) {
		// TODO Auto-generated catch block
		
	} catch (IOException e) {
		// TODO Auto-generated catch block
		
	}
	
	
	
	
	return true;
}
}
