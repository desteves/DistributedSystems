package edu.cmu.ds.rmi.registry;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import edu.cmu.ds.rmi.comm.RegistryMessage;
import edu.cmu.ds.rmi.comm.RegistryMessage.Type;

public class RegistryServer {
	
static Registry registry = new Registry();
	
	static Integer REGISTRY_PORT;


    public static void main(String args[]) 
    {
    	
    	
    	
    	while(true)
    	{
    	try
    	{
    		Scanner scan = new Scanner(System.in);
        	
        	System.out.println("Enter the port number for registry server");
    		
        	REGISTRY_PORT = scan.nextInt();
        	break;
    	}
    	catch(Exception e)
    	{
    		System.out.println("Invalid port Number");
    		//continue;
    	}
    	}
    	
    	
    	// Open server socket where registry server listens to . 
    	
    	Socket socket;
    	InputStreamReader ir;
    	BufferedReader br;

    	PrintStream ps;
    	int Port;
    	
    
    	ServerSocket servsocket = null;
		try {
			
			servsocket = new ServerSocket(REGISTRY_PORT);
			System.out.println("Registry Server listening.......");
			
		
		} catch (Exception e) {
			
			System.out.println("Port not available. Restart and try some other port");
			return;
		}
		
    	
    	
    	
    	while(true)
    	{
    		try {

				
				

				socket = servsocket.accept();

				InputStream is = socket.getInputStream();
				

				ObjectInputStream ois = new ObjectInputStream(is);
				RegistryMessage message =(RegistryMessage) ois.readObject();

				 
				if(message.type==Type.bind)
				{
					//bind message from a remote object
					
					RemoteObjectReference ror = new RemoteObjectReference(message.ip, message.port,message.ClassName, message.InterfaceName);
					
					System.out.println("Bind - "+ message.ClassName+" "+message.ServiceName);
					
					if(registry.bind(message.ServiceName, ror))
					{
						//Bound
					}
					else
					{
						//Already bound message;
					}
					
				
				}
				else if(message.type==Type.rebind)
				{
					//rebind message from a remote object
					
					RemoteObjectReference ror = new RemoteObjectReference(message.ip, message.port,message.ClassName, message.InterfaceName);
					
					System.out.println("Bind - "+ message.ClassName+" "+message.ServiceName);
					
					registry.rebind(message.ServiceName, ror);
					
				}
				else if(message.type==Type.lookup)
				{
					//lookup request from client
					
					System.out.println("lookup - "+ message.ServiceName);
					
					ObjectOutputStream oos = new  ObjectOutputStream(socket.getOutputStream());
					
					RemoteObjectReference ror = registry.lookup(message.ServiceName);
					
					oos.writeObject(ror);

					
				}
				else if(message.type==Type.list)
				{
					System.out.println("List request ");
					
					ObjectOutputStream oos = new  ObjectOutputStream(socket.getOutputStream());
					
					HashMap<String,RemoteObjectReference> rorList = registry.list();
					
					oos.writeObject(rorList);
				}
					
    		}
    		catch(Exception e)
    		{
    			
    		}
    	
    	}
    

    }
}
