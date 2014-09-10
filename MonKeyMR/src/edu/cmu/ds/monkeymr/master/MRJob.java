package edu.cmu.ds.monkeymr.master;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

import edu.cmu.ds.donkeyfs.DFSBlock;
import edu.cmu.ds.donkeyfs.DFSFile;
import edu.cmu.ds.monkeymr.commons.JobConfiguration;

/**
 * Represents a job
 * 
 * @author Linne
 * 
 */
public class MRJob {

	private DFSFile inputFile;
	private String outputDir;
	private String jarPath;
	private String jarName;
	private String jobId;
	private List<DFSBlock> inputFileBlocks;
	private ConcurrentHashMap<String, Queue<MRTask>> tasksPerNode;
	private List<String> jarArgs;
	private JobConfiguration jobConf;
	public boolean isInitialized;
	private int numberOfReducers;
	private Queue<MRTask> reduceTasks;
	private List<String> keysList =new ArrayList<String>();
	private String jobClassName;
	public List<MRTask> runningTasks = new ArrayList<MRTask>();
	public List<MRTask> completedTasks = new ArrayList<MRTask>();
	
	public String getJobClassName() {
		return jobClassName;
	}

	public void setJobClassName(String jobClassName) {
		this.jobClassName = jobClassName;
	}

	public enum JobStatus {WAITING,MAPRUNNING,MAPCOMPLETE,PARTITIONING,PARTITIONCOMPLETE,REDUCERUNNING,COMPLETE}
		
	
	public Queue<MRTask> getReduceTasks() {
		return reduceTasks;
	}

	public void setReduceTasks(Queue<MRTask> reduceTasks) {
		this.reduceTasks = reduceTasks;
	}

	public int getNumberOfReducers() {
		return numberOfReducers;
	}

	public void setNumberOfReducers(int numberOfReducers) {
		this.numberOfReducers = numberOfReducers;
	}

	public JobConfiguration getJobConf() {
		return jobConf;
	}

	public void setJobConf(JobConfiguration jobConf) {
		this.jobConf = jobConf;
	}

	public ConcurrentHashMap<String, Queue<MRTask>> getTasksPerNode() {
		return tasksPerNode;
	}

	public void setTasksPerNode(ConcurrentHashMap<String, Queue<MRTask>> tasksPerNode) {
		this.tasksPerNode = tasksPerNode;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MRJob other = (MRJob) obj;
		if (jobId == null) {
			if (other.jobId != null)
				return false;
		} else if (!jobId.equals(other.jobId))
			return false;
		return true;
	}

	public DFSFile getInputFile() {
		return inputFile;
	}

	public void setInputFile(DFSFile inputFile) {
		this.inputFile = inputFile;
	}

	public String getOutputDir() {
		return outputDir;
	}

	public void setOutputDir(String outputDir) {
		this.outputDir = outputDir;
	}

	public String getJarPath() {
		return jarPath;
	}

	public void setJarPath(String jarPath) {
		this.jarPath = jarPath;
	}

	public List<DFSBlock> getInputFileBlocks() {
		return inputFileBlocks;
	}

	public void setInputFileBlocks(List<DFSBlock> inputFileBlocks) {
		this.inputFileBlocks = inputFileBlocks;
	}

	public String getJobId() {
		return jobId;
	}

	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

	public List<String> getJarArgs() {
		return jarArgs;
	}

	public void setJarArgs(List<String> jarArgs) {
		this.jarArgs = jarArgs;
	}

	public List<String> getKeysList() {
		return keysList;
	}

	public void setKeysList(List<String> keysList) {
		this.keysList = keysList;
	}

	public String getJarName() {
		return jarName;
	}

	public void setJarName(String jarName) {
		this.jarName = jarName;
	}

}
