package edu.cmu.ds.monkeymr.master;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Queue;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

import edu.cmu.ds.monkeymr.interfaces.Constants;

/**
 * Provide management tools enabling the start-up and shut-down of the facility,
 * as well as the management of jobs, e.g. start, monitor, and stop.
 * 
 * Has console which listens to the incoming Job requests and maintains a list
 * of all jobs.
 * 
 * @author Linne
 * 
 */
public class MRMasterCLI {

	
	public static ConcurrentHashMap<String, HashMap<MRTask, MRTask.TaskStatus>> nodeRegistry = new ConcurrentHashMap<String, HashMap<MRTask, MRTask.TaskStatus>>();
	public static ConcurrentHashMap<MRJob, MRJob.JobStatus> jobRegistry = new ConcurrentHashMap<MRJob, MRJob.JobStatus>();
	public static Queue<MRJob> jobQueue = new LinkedList<MRJob>();
	public static String DFS_IP = "";
	public static int DFS_PORT = 0;
	private static int masterPort = -1;
	private static Scanner scan;
	public static String DFS_PATH_JARS = "DFS_PATH_JARS";

	
	/**
	 * Process the properties file
	 */
	public MRMasterCLI() {
		Properties prop = new Properties();
		InputStream input = null;
		try {
			input = new FileInputStream(Constants.MR_CONFIG);
			prop.load(input);
			// load properties
			masterPort = Integer.valueOf(prop
					.getProperty(Constants.MASTER_PORT));
			DFS_PORT = Integer.valueOf(prop.getProperty(Constants.DFS_PORT));
			DFS_IP = prop.getProperty(Constants.DFS_IP);
			DFS_PATH_JARS = prop.getProperty(Constants.DFS_PATH_JARS);
			System.out
					.println("[MonkeyMR - Master Node] Properties successfully loaded. ");
		} catch (IOException ex) {
			System.err
					.println("[MRMasterCLI] ! Err - Properties file not found.");
			// ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					System.err
							.println("[MRMasterCLI] ! Err - Properties file cannot close.");
					// e.printStackTrace();
				}
			}
		}
	}

	/**
	 * main
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		new MRMasterCLI();
		// listens to the incoming Job requests
		MRJobRequestListener msglistener = new MRJobRequestListener(masterPort);
		Thread t = new Thread(msglistener);
		t.start();
		scan = new Scanner(System.in);
		int i = -1;

		Thread jobSchedulerThread = new Thread(
				new MRJobScheduler(DFS_PATH_JARS));
		jobSchedulerThread.start();

		Thread jobTrackThread = new Thread(new MRJobTracker());
		jobTrackThread.start();

		System.out.println("MonKeyMR Master Node Launched");
		
		// management -- Future implementation
		while (true) {
			i = -1;
			// System.out
			// .println("\n 1.List Jobs \n 2. Start Job \n 3.View Job \n 4. Stop Job\n 5. Shut Down \n Enter an option:  ");

			try {
				i = scan.nextInt();
			} catch (Exception e) {
				System.err.println(" Bad Input. Try again.");
				continue;
			} finally {
				if (i < 1 || i > 5) {
					System.err.println(" Bad Input. Try again.");
					continue;
				}
			}
			if (i == 1) { // list all the jobs
				System.out.println(jobRegistry.toString());
				// TODO
			} else if (i == 2) {
				// TODO
			} else if (i == 3) {
				// TODO
			} else if (i == 4) {
				// TODO
			} else if (i == 5) {
				// TODO
			}
		}

	}
}
