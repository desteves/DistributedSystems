package edu.cmu.ds.rmi.client.test;

import java.io.*;
import java.util.Scanner;

import edu.cmu.ds.rmi.registry.LocateRemoteRegistry;
import edu.cmu.ds.rmi.server.test.ZipCodeInterface;
import edu.cmu.ds.rmi.server.test.ZipCodeList;

public class ZipCodeClient {

	// the main takes 1 argument:
	// (0) a file name as above.
	public static void main(String[] args) {

		Scanner scan = new Scanner(System.in);
		String ip;
		int portNumber;
		System.out.println("Enter IP: ");
		ip = scan.nextLine();
		System.out.println("Enter Port: ");
		while (true) {
			try {
				scan = new Scanner(System.in);
				portNumber = scan.nextInt();
				break;
			} catch (Exception ex) {
				System.out.println("Invalid port Number. Retry ");
				continue;
			}
		}
		LocateRemoteRegistry lrr = new LocateRemoteRegistry(ip, portNumber);
		System.out.println(lrr.List().toString());
		System.out.println("Enter the service name to look for");

		scan.nextLine();
		
		scan = new Scanner(System.in);
		
		String ServiceName = scan.nextLine();
		BufferedReader in;

		try {
			in = new BufferedReader(new FileReader(args[0]));
			ZipCodeInterface zcs = (ZipCodeInterface) lrr.Locate(ServiceName);

			// reads the data and make a "local" zip code list.
			// later this is sent to the server.
			// again no error check!
			ZipCodeList l = null;
			boolean flag = true;
			while (flag) {
				String city = in.readLine();
				String code = in.readLine();
				if (city == null)
					flag = false;
				else
					l = new ZipCodeList(city.trim(), code.trim(), l);
			}
			// the final value of l should be the initial head of
			// the list.

			// we print out the local zipcodelist.
			System.out.println("This is the original list.");
			ZipCodeList temp = l;
			while (temp != null) {
				System.out.println("city: " + temp.city + ", " + "code: "
						+ temp.ZipCode);
				temp = temp.next;
			}

			// test the initialise.
			zcs.initialise(l);
			System.out.println("\n Server initalised.");

			// test the find.
			System.out.println("\n This is the remote list given by find.");
			temp = l;
			while (temp != null) {
				// here is a test.
				String res = zcs.find(temp.city);
				System.out
						.println("city: " + temp.city + ", " + "code: " + res);
				temp = temp.next;
			}

			// test the findall.
			System.out.println("\n This is the remote list given by findall.");
			// here is a test.
			temp = zcs.findAll();
			while (temp != null) {
				System.out.println("city: " + temp.city + ", " + "code: "
						+ temp.ZipCode);
				temp = temp.next;
			}

			// test the printall.
			System.out.println("\n We test the remote site printing.");
			// here is a test.
			zcs.printAll();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
