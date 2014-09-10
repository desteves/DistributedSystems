package edu.cmu.ds.monkeymr.user;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Properties;

import org.apache.commons.io.FilenameUtils;

import edu.cmu.ds.monkeymr.comm.MRJobRequestMessage;
import edu.cmu.ds.monkeymr.interfaces.Constants;

/**
 * 
 * Initiate the execution of the program from any participating node
 * 
 * Execute portions of the program other than maps and reduces locally on the
 * initiating node, or on other nodes, as your design should dictate
 * 
 * processes should be able to use the map-reduce facility from any participant
 * 
 * Reads from config file followed example at
 * http://www.mkyong.com/java/java-properties-file-examples/
 * 
 * @author Linne
 * To submit jobs -- Future implementation - After submitting should poll for status
 */
public class MRJobUserCLI {

	private static Socket s;

	public static void main(String[] args) {
		int masterPort;
		String masterIp;
		Properties prop = new Properties();
		InputStream input = null;
		try {
			input = new FileInputStream(Constants.MR_CONFIG);
			prop.load(input);
			MRJobRequestMessage newJob = new MRJobRequestMessage();

			// load properties
			masterIp = prop.getProperty(Constants.MASTER_IP);
			masterPort = Integer.valueOf(prop
					.getProperty(Constants.MASTER_PORT));
			//newJob.setInDFS(Integer.valueOf(prop.getProperty(Constants.ISDFS)) == 1);

			Path path = Paths.get(args[0]); // path to jar file
			byte[] data = Files.readAllBytes(path);
			newJob.setJarFile(data);
			
			File file = new File(path.toString());

			String filename = FilenameUtils.removeExtension(file.getName());

			String fileExtension = "." + FilenameUtils.getExtension(file.getName());
			
			
			newJob.setJarName(filename+ fileExtension);

			// send rest of the args as parameter to the User's map reduce Jar
			newJob.setJarArgs(new ArrayList<String>(Arrays.asList(Arrays.copyOfRange(args, 1,
					args.length))));

			// send out the message to the master node
			String reply = SendMessage(newJob, masterIp, masterPort);

		} catch (IOException ex) {
			System.err.println("[MRJobUserCLI] ! Err - Properties file not found.");
			ex.printStackTrace();
		} 
		catch (ArrayIndexOutOfBoundsException ex)
		{
			System.err.println("[MRJobUserCLI] ! Wrong usage \n Sample Usage \n\n  java edu/cmu/ds/monkeymr/user/MRJobUserCLI [MapReduce Jar File] [Map Reduce Class name] [Input file] [Output file name]");
		}
		
		finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					System.err.println("! Err - Properties file cannot close.");
				}
			}
		}

	}

	/**
	 * Sends the argument to the master participant
	 * 
	 * @param msg
	 * @param ip
	 * @param port
	 */
	private static String SendMessage(MRJobRequestMessage msg, String ip,
			int port) {
		String reply = "";

		try {
			s = new Socket(ip, port);
			ObjectOutputStream oos;
			oos = new ObjectOutputStream(s.getOutputStream());
			oos.writeObject(msg);
			ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
			reply = (String) ois.readObject();

			
			
		} catch (IOException | ClassNotFoundException e) {
			System.err.println("! Err - Failed to send job to master.");
			//e.printStackTrace();
		}
		return reply;
	}
}
