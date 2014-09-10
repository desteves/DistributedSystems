package edu.cmu.ds.monkeymr.master;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import edu.cmu.ds.monkeymr.comm.MRHeartBeat;
import edu.cmu.ds.monkeymr.master.MRTask.TaskStatus;
import edu.cmu.ds.monkeymr.master.MRTask.TaskType;

// each of this thread will recieve heartbeats from nodes and update the node reg in Console 

public class MRNodeTracker implements Runnable {

	protected Socket clientSocket = null;
	protected String name = null;
	private String NodeIP;
	private Integer NodePort;

	public MRNodeTracker(Socket socket, String name) {

		this.clientSocket = socket;
		this.name = name;
		this.NodeIP = name.split("-")[1];

		NodePort = Integer.parseInt(name.split("-")[2]);

	}

	@Override
	public void run() {
		try {
			InputStream input = clientSocket.getInputStream();
			OutputStream output = clientSocket.getOutputStream();
			PrintStream ps = new PrintStream(output);

			InputStreamReader ir = new InputStreamReader(input);
			BufferedReader br = new BufferedReader(ir);
			while (true) {

				// recieve heartbeat 
				try {

					ObjectInputStream ois = new ObjectInputStream(
							clientSocket.getInputStream());
					MRHeartBeat heartbeat = (MRHeartBeat) ois.readObject();

					HashMap<MRTask, TaskStatus> tasks = heartbeat.getTasks();

					HashMap<MRTask, TaskStatus> newTasks = new HashMap<MRTask, MRTask.TaskStatus>();

					for (MRTask task : tasks.keySet()) {
						if (task.getTaskType() == TaskType.MAP) {

							
							//get keys list for completed mappers. Not a good design - Can change sending keys through heartbeat to a different message.
							if (!(tasks.get(task) == MRTask.TaskStatus.COMPLETE))
								newTasks.put(task, tasks.get(task));
							else {
								for (MRJob job : MRMasterCLI.jobRegistry
										.keySet()) {
									if (job.getJobId().equals(task.getJobId())) {

										if (!job.completedTasks.contains(task)) {
											List<String> keys = job
													.getKeysList();
											keys.addAll(task.getPartitionKeys());

											Set<String> depdupe = new LinkedHashSet<>(
													keys);
											keys.clear();
											keys.addAll(depdupe);

										}

										// add to completed
										job.completedTasks.add(task);

										MRTask runningTask = null;
										for (MRTask t : job.runningTasks) {
											if (t.getId().equals(task.getId()))
												runningTask = t;
										}
										// remove from running tasks list
										job.runningTasks.remove(runningTask);
									}
								}

								
							}
							MRMasterCLI.nodeRegistry.put(NodeIP + "-"
									+ NodePort, newTasks);
						} else {
							// if red is complete:
							if (!(tasks.get(task) == MRTask.TaskStatus.COMPLETE))
								newTasks.put(task, tasks.get(task));
							else {
								// remove from job.running and add to complete

								for (MRJob job : MRMasterCLI.jobRegistry
										.keySet()) {
									if (job.getJobId().equals(task.getJobId())) {

										//add to completed
										job.completedTasks.add(task);

										MRTask runningTask = null;
										for (MRTask t : job.runningTasks) {
											if (t.getId().equals(task.getId()))
												runningTask = t;
										}
										// remove from running tasks list
										job.runningTasks.remove(runningTask);
									}
								}
							}
							MRMasterCLI.nodeRegistry.put(NodeIP + "-"
									+ NodePort, newTasks);

						}
					}
				} catch (SocketException | ClassNotFoundException se) {
					break;
				} catch (java.io.EOFException ex) {
					System.out.println(name + " Stopped");
					MRMasterCLI.nodeRegistry.remove(name);
					break;
				}

			}

			System.out.println("Connection lost to " + name);

			MRMasterCLI.nodeRegistry.remove(name);

			input.close();
			output.close();
			clientSocket.close();

		} catch (IOException e) {
			// report exception somewhere.
			System.err.println("[MRNodeTracker] ! Err -IOException .");
		}
	}

}
