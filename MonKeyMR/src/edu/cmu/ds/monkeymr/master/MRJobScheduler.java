package edu.cmu.ds.monkeymr.master;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

import edu.cmu.ds.donkeyfs.DFSBlock;
import edu.cmu.ds.monkeymr.master.MRJob.JobStatus;
import edu.cmu.ds.monkeymr.master.MRTask.TaskType;

public class MRJobScheduler implements Runnable {

	private String DfsJarPath;

	public MRJobScheduler(String dFS_PATH_JARS) {
		DfsJarPath = dFS_PATH_JARS;

	}

	@Override
	public void run() {

		while (true) {
			try {

				Thread.sleep(3000);

				if (!MRMasterCLI.jobQueue.isEmpty()) {
					MRJob nextJob = MRMasterCLI.jobQueue.element();
					if (!nextJob.isInitialized)
						InitializeJob(nextJob);
					boolean running = false;
					for (MRJob job : MRMasterCLI.jobRegistry.keySet()) {

						MRJob.JobStatus Status = MRMasterCLI.jobRegistry
								.get(job);
						if (!(Status == MRJob.JobStatus.COMPLETE))
							running = true;

					}

					if (!running) {
						MRMasterCLI.jobRegistry.put(nextJob, JobStatus.WAITING);
						MRMasterCLI.jobQueue.remove(nextJob);
					}
				}

			} catch (Exception e) {
				System.err
						.println("[MRJobScheduler] ! Err: Socket connection ");
			}

		}

	}

	private void InitializeJob(MRJob nextJob) {
		ConcurrentHashMap<String, Queue<MRTask>> tasksPerNode = new ConcurrentHashMap<String, Queue<MRTask>>();

		Queue<MRTask> tasks;

		List<DFSBlock> blocks = nextJob.getInputFileBlocks();

		
		// foreach block assign mappers to the node containing the block. 
		
		for (DFSBlock block : blocks) {

			String nodeIp = block.getIp();
			String node = "";
			
			// Node containing the same IP. Not good . Can change to fixed NODE names for both DFS and MR
			for (String n : MRMasterCLI.nodeRegistry.keySet()) {
				if (n.contains(nodeIp))
					node = n;
			}

			Integer nodePort = Integer.parseInt(node.split("-")[1]);
			MRTask task = new MRTask();
			task.setJarName(nextJob.getJarName());
			task.setTaskType(TaskType.MAP);
			task.setNodeIp(nodeIp);
			task.setNodePort(nodePort);
			task.setInputBlock(block);
			task.setOutputFile("INTERMEDIATE" + nextJob.getJobId());
			task.setJarFilePath(DfsJarPath);
			task.setId(nextJob.getJobId() + TaskType.MAP.name() + "-"
					+ blocks.indexOf(block));
			task.setTaskClassName(nextJob.getJobClassName());
			task.setJobId(nextJob.getJobId());

			task.setMapperOpKeyFormat(nextJob.getJobConf()
					.getMapperOpKeyFormat());
			task.setMapperOpValueFormat(nextJob.getJobConf()
					.getMapperOpValueFormat());
			task.setReducerOpKeyFormat(nextJob.getJobConf()
					.getReducerOpKeyFormat());
			task.setReducerOpValueFormat(nextJob.getJobConf()
					.getReducerOpValueFormat());
			if (!tasksPerNode.keySet().contains(nodeIp)) {
				tasksPerNode.put(nodeIp, new LinkedList<MRTask>());
			}

			tasks = tasksPerNode.get(nodeIp);
			tasks.add(task);
		}
		tasks = new LinkedList<MRTask>();		
		nextJob.setTasksPerNode(tasksPerNode);
		nextJob.isInitialized = true;
	}

}
