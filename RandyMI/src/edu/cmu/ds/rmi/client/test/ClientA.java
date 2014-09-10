package edu.cmu.ds.rmi.client.test;

import java.util.Scanner;

import edu.cmu.ds.rmi.registry.LocateRemoteRegistry;
import edu.cmu.ds.rmi.server.test.RemoteObjectA_1Interface;

public class ClientA {

	
	
	public static void main(String args[])
	{
			
			
			Scanner scan = new Scanner(System.in);

			
			
			System.out.println("Enter the registry IP");

			String regIp = scan.nextLine();
			
			System.out.println("Enter the registry Port");

			int regPort =0;
			try
			{
				regPort= scan.nextInt();
			}
			catch(Exception ex)
			{
				System.out.println("Invalid port Number. Retry ");
				return;
			}
			
			LocateRemoteRegistry lrr = new LocateRemoteRegistry(regIp,regPort);
			
			System.out.println(lrr.List().toString());
			
			System.out.println("Enter the service name to look for");

			scan.nextLine();
			
			scan = new Scanner(System.in);
			
			String ServiceName = scan.nextLine();
			
			
			RemoteObjectA_1Interface a = (RemoteObjectA_1Interface)lrr.Locate(ServiceName);
			
			
			System.out.println(a.methodTwo(2));
			
			a.methodOne();
		
		
		
		
	}
	
}
