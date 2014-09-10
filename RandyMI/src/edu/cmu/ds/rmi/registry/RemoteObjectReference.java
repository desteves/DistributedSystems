package edu.cmu.ds.rmi.registry;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.Arrays;

import javax.tools.JavaCompiler;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

import edu.cmu.ds.rmi.client.Stub;
import edu.cmu.ds.rmi.comm.StubMessage;
import edu.cmu.ds.rmi.comm.StubMessage.MessageType;

public class RemoteObjectReference implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	// Ip port where that object can be reached
	String IP_adr;
	String Port;
	String Class_Name;
	String Remote_Interface_Name;

	public RemoteObjectReference(String ip, String port, String obj_key,
			String riname) {
		IP_adr = ip;
		Port = port;
		Class_Name = obj_key;
		Remote_Interface_Name = riname;
	}

	// this method is important, since it is a stub creator.
	//
	public Object localise() {

		String stubname = Class_Name + "_stub";

		Stub stubObj = null;
		try {

			Class stubClass;
			try {
				// if stub class exists
				stubClass = Class.forName(stubname);
				
			} catch (ClassNotFoundException ex) {

				// request Download for stub if not found

				Socket sock = new Socket(IP_adr, Integer.parseInt(Port));

				ObjectOutputStream oos = new ObjectOutputStream(
						sock.getOutputStream());

				// build a message for requesting stub
				StubMessage stubMsg = new StubMessage();

				stubMsg.setMessageType(MessageType.stubRequest);
				stubMsg.setStubClassName(stubname);

				oos.writeObject(stubMsg);

				sock.setSoTimeout(10000);

				InputStream is = sock.getInputStream();

				ObjectInputStream ois = new ObjectInputStream(is);

				String stubNameShort = stubname
						.substring(stubname.lastIndexOf('.') + 1);

				// create the source file
				File sourceFile = new File(stubNameShort + ".java");
				FileWriter writer;
				try {
					writer = new FileWriter(sourceFile);

					String stringClass = (String) ois.readObject();
					writer.write(stringClass);
					writer.close();
					// need to use the jdk not the jre for this to work
					JavaCompiler compiler = ToolProvider
							.getSystemJavaCompiler();
					StandardJavaFileManager fileManager = compiler
							.getStandardFileManager(null, null, null);

					fileManager.setLocation(StandardLocation.CLASS_OUTPUT,
							Arrays.asList(new File(".")));
					// Compile the file
					compiler.getTask(
							null,
							fileManager,
							null,
							null,
							null,
							fileManager.getJavaFileObjectsFromFiles(Arrays
									.asList(sourceFile))).call();
					fileManager.close();
				} catch (ClassNotFoundException | IOException e) {
					System.err.println("! Err Stub not generated. ");
					e.printStackTrace();
				}				

				stubClass = null;

				try {

					// recieve stub message

					stubClass = Class.forName(stubname);

				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}

			// create instance for stub
			stubObj = (Stub) stubClass.newInstance();
			stubObj.Init(Integer.parseInt(Port), IP_adr);

		} catch (InstantiationException | IllegalAccessException
				| NumberFormatException | IOException ex) {

			System.out
					.println("Problem downloading stub. Check if remote object server is available");
		}

		return stubObj;
	}
}
