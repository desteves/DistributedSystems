package edu.cmu.ds.rmi.registry;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import edu.cmu.ds.rmi.comm.RegistryMessage;
import edu.cmu.ds.rmi.comm.RegistryMessage.Type;
import edu.cmu.ds.rmi.server.test.RemoteObjectA_1Interface;

public class LocateRemoteRegistry {

	String registryIP ;
	int registryPortNumber ;

	public LocateRemoteRegistry(String registryIP, int registryPortNumber) {
		//super();
		this.registryIP = registryIP;
		this.registryPortNumber = registryPortNumber;
	}

	//Method will locate the remote registry in the given port and get the remote object reference for the service name
	public Object Locate(String serviceName)  {
		
		RemoteObjectReference ror=null;

		try {

			RegistryMessage message = new RegistryMessage();
			message.type = Type.lookup;
			message.ServiceName = serviceName;
			
			Socket regSocket;
			regSocket = new Socket(registryIP, registryPortNumber);
			ObjectOutputStream oos = new ObjectOutputStream(
					regSocket.getOutputStream());
			oos.writeObject(message);

			ObjectInputStream ois = new ObjectInputStream(
					regSocket.getInputStream());
			
			//reference to the remote object
			ror = (RemoteObjectReference) ois.readObject();

		} catch (Exception e) {
			
			System.out.println("Error locating registry.");
		}
		
		if(ror!=null)
		{
			// localize returns stub 
			Object o = ror.localise();
			return o;	
		}
		else
		{
			System.out.println("Service not found");
			return null;
		}
		
		
	}
	
	public ArrayList<String> List()
	{
		ArrayList<String> list= new ArrayList<String>();
		RegistryMessage message = new RegistryMessage();
		message.type = Type.list;
		try {
			
		Socket regSocket;
		regSocket = new Socket(registryIP, registryPortNumber);
		ObjectOutputStream oos = new ObjectOutputStream(
				regSocket.getOutputStream());
		oos.writeObject(message);

		ObjectInputStream ois = new ObjectInputStream(
				regSocket.getInputStream());
		
		//reference to the remote object
		
		HashMap<String,RemoteObjectReference> rorList = (HashMap<String,RemoteObjectReference>) ois.readObject();
		
		
		
		for(String s : rorList.keySet())
		{
			list.add(s+"   -   "+rorList.get(s).Class_Name);
		}
		
		
		
		
		} catch (ClassNotFoundException | IOException e) {
			System.err.println("Error with connection. ");
		}
		
		return list;
		
	}
}
