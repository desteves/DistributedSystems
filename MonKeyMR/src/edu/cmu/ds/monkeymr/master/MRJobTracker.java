package edu.cmu.ds.monkeymr.master;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

import edu.cmu.ds.monkeymr.comm.MRTaskMessage;
import edu.cmu.ds.monkeymr.comm.MRTaskMessage.MessageType;
import edu.cmu.ds.monkeymr.interfaces.Constants;
import edu.cmu.ds.monkeymr.master.MRJob.JobStatus;
import edu.cmu.ds.monkeymr.master.MRTask.TaskType;

//Has registry of tasks running for the current job and all the info about them 

public class MRJobTracker implements Runnable {
	private Socket socket = null;
	private OutputStream os;
	private ObjectOutputStream oos;

	@Override
	public void run() {
		// check every one second for job queue and status and start jobs
		while (true) {
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				System.err.println("[MRJobTracker] ! Err - Can't sleep.");
			}

			// WAITING iterate through job registry and iterate through
			// tasks per node and launch mappers

			List<MRJob> removeList = new ArrayList<MRJob>();

			for (MRJob job : MRMasterCLI.jobRegistry.keySet()) {
				MRJob.JobStatus status = MRMasterCLI.jobRegistry.get(job);

				if (status == MRJob.JobStatus.WAITING
						|| status == MRJob.JobStatus.MAPRUNNING) {

					ConcurrentHashMap<String, Queue<MRTask>> tasksPerNode = job
							.getTasksPerNode();

					
					// launching tasks in each node 
					
					if (tasksPerNode.size() != 0) {
						for (String node : tasksPerNode.keySet()) {

							String nodeName = null;
							for (String n : MRMasterCLI.nodeRegistry.keySet()) {
								if (n.contains(node))
									nodeName = n;
							}

							HashMap<MRTask, MRTask.TaskStatus> tasksRunning = MRMasterCLI.nodeRegistry
									.get(nodeName);

							// if nodes run less than the configured number of SLOTS the node has. 
							if (tasksRunning == null
									|| tasksRunning.size() < Constants.NUMBEROFMAPSPERNODE) {

								if (tasksPerNode.get(node).size() > 0) {
									MRTask nextTask = tasksPerNode.get(node)
											.element();

									// send task to node

									MRTaskMessage message = new MRTaskMessage();
									message.setType(MRTaskMessage.MessageType.LAUNCH);
									message.setTask(nextTask);
									try {
										socket = new Socket(
												nextTask.getNodeIp(),
												nextTask.getNodePort());
										os = socket.getOutputStream();
										oos = new ObjectOutputStream(os);
										oos.writeObject(message);
									} catch (NumberFormatException
											| IOException e) {
										System.err
												.println("[MRJobTracker] ! Err: Processing job. ");
										// e.printStackTrace();
									} finally {
										if (socket != null) {
											try {
												socket.close();
											} catch (IOException e) {
												System.err
														.println("[MRJobTracker] ! Err - Processing: Failed to close socket.");
											}
										}
									}
									tasksPerNode.get(node).remove();
									System.out.println(nextTask.getId()
											+ "- LAUNCHED");
									job.runningTasks.add(nextTask);
								} else {
									tasksPerNode.remove(node);
								}
							}
						}
					} else {
						//no running tasks then map complete
						if (job.runningTasks.size() == 0) {
							MRMasterCLI.jobRegistry.put(job,
									MRJob.JobStatus.MAPCOMPLETE);

							System.out.println(job.getJobId() + "-MAPCOMPLETE");
						}
					}
				} else if (status == MRJob.JobStatus.MAPCOMPLETE) {
					// if map complete, then:
					// we'll partition by round robin assignment of reducers
					// to nodes
					System.out.println("[MRJobTracker] Partitioning job: "
							+ job.getJobId());
					// update status to partitioning
					MRMasterCLI.jobRegistry.put(job, JobStatus.PARTITIONING);
					int reds = job.getJobConf().getNumReds();
					int nodes = MRMasterCLI.nodeRegistry.size();
					int redsAssigned = 0;
					int keys = job.getKeysList().size();
					int keysAssigned = 0;
					// message to be send to the node
					MRTaskMessage taskMsg = new MRTaskMessage();
					taskMsg.setType(MessageType.LAUNCH);
					// task to be send to the node
					MRTask task = new MRTask();
					task.setTaskType(TaskType.REDUCE);
					// distribute the unique keys across reducers in a round
					// robin fashion
					List<List<String>> partitionedKeys = new ArrayList<List<String>>();
					for (int i = 0; i < reds; ++i)
						partitionedKeys.add(new ArrayList<String>());

					for (String key : job.getKeysList()) {
						int index = keysAssigned % reds;
						partitionedKeys.get(index).add(key);
						++keysAssigned;
					}
					if (reds > nodes) // if more reds than available nodes
						System.out
								.println("[MRJobTracker] More reducers than nodes. \n The system will use available nodes ["
										+ nodes + "].");

					task.setJobId(job.getJobId());
					task.setJarFilePath(MRMasterCLI.DFS_PATH_JARS);
					task.setJarName(job.getJarName());
					task.setTaskClassName(job.getJobClassName());

					task.setMapperOpKeyFormat(job.getJobConf()
							.getMapperOpKeyFormat());
					task.setMapperOpValueFormat(job.getJobConf()
							.getMapperOpValueFormat());
					task.setReducerOpKeyFormat(job.getJobConf()
							.getReducerOpKeyFormat());
					task.setReducerOpValueFormat(job.getJobConf()
							.getReducerOpValueFormat());
					Queue<MRTask> tasks = new LinkedList<MRTask>();
					// tell each node what key(s) it'll reduce
					while (redsAssigned < reds)
						// round robin assignment of reducers
						for (String node : MRMasterCLI.nodeRegistry.keySet()) {

							if (redsAssigned == reds)
								break;
							String nodeIp = node.split("-")[0];
							task.setNodeIp(nodeIp);
							Integer nodePort = Integer
									.parseInt(node.split("-")[1]);
							task.setNodePort(nodePort);
							// tell which nodes are going to act as
							// reducers, and what they'll be reducing
							// a.k.a. which records they'll process
							System.out.println(job.getJobId()
									+ "PARTITION - COMPLETE");
							if (!partitionedKeys.isEmpty()) {
								task.setPartitionKeys(partitionedKeys.remove(0));
								task.setId("R" + String.valueOf(redsAssigned));
								task.setOutputFile(job.getJobConf()
										.getOutputfile() + "-" + task.getId());

								taskMsg.setTask(task);
								// add to job's queue
								// job.getReduceTasks().add(task);
								System.out.println("REDUCER - " + task.getId()
										+ "- LAUNCHED");
								SendMessage(taskMsg);
								tasks.add(task);
							}
							++redsAssigned;
							socket = null;
						}

					job.runningTasks.addAll(tasks);
					MRMasterCLI.jobRegistry.put(job,
							JobStatus.PARTITIONCOMPLETE);
				} else if (status == MRJob.JobStatus.PARTITIONCOMPLETE) {
					// if partition complete, REDUCE.
					// update status to reducer running
					job.setKeysList(new ArrayList<String>());
					MRMasterCLI.jobRegistry.put(job, JobStatus.REDUCERUNNING);

					System.out.println(job.getJobId() + "PARTITION - COMPLETE");

				} else if (status == MRJob.JobStatus.REDUCERUNNING) {
					if (job.runningTasks.size() == 0) {
						MRMasterCLI.jobRegistry.put(job,
								MRJob.JobStatus.COMPLETE);
						System.out.println(job.getJobId() + "-REDUCE COMPLETE");

						System.out.println(job.getJobId()
								+ " - OUTPUT FILE IS IN DonKeyFS FILENAME -"
								+ job.getOutputDir());
					}

				}
			}
		}
	}

	/**
	 * Sends task to reducer node
	 * 
	 * @param taskMsg
	 */
	private void SendMessage(MRTaskMessage taskMsg) {
		try {
			// send the reduce task message to node
			socket = new Socket(taskMsg.getTask().getNodeIp(), taskMsg
					.getTask().getNodePort());
			os = socket.getOutputStream();
			oos = new ObjectOutputStream(os);
			oos.writeObject(taskMsg);
		} catch (IOException e) {
			System.err
					.println("[MRJobTracker - SendMessage] ! Err - Partition: Failed to send message to worker node.");
		} finally {
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException e) {
					System.err
							.println("[MRJobTracker - SendMessage] ! Err - Partition: Failed to close socket.");

				}
			}
		}
	}

}
