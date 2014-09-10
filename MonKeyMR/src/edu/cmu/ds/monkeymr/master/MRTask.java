package edu.cmu.ds.monkeymr.master;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import edu.cmu.ds.donkeyfs.DFSBlock;
import edu.cmu.ds.monkeymr.commons.JobConfiguration;

public class MRTask implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2737178454348193358L;

	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((Id == null) ? 0 : Id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MRTask other = (MRTask) obj;
		if (Id == null) {
			if (other.Id != null)
				return false;
		} else if (!Id.equals(other.Id))
			return false;
		return true;
	}

	public enum TaskType {
		MAP, REDUCE
	};

	public enum TaskStatus {
		WAITING, RUNNING, COMPLETE
	};

	private String jarName;
	private TaskType taskType;
	private Class taskClass;
	private String nodeIp;
	private Integer nodePort;
	private DFSBlock inputBlock;
	//private Map<String, List<DFSBlock>> inputKeys; //partition key and blocks
	private String outputFile;
	private String jarFilePath;
	private String Id;
	private String taskClassName;

	private String jobId;

	private List<String> partitionKeys;
	private Class mapperOpKeyFormat;
	private Class mapperOpValueFormat;
	private Class reducerOpKeyFormat;
	private Class reducerOpValueFormat;



	public Class getMapperOpKeyFormat() {
		return mapperOpKeyFormat;
	}

	public void setMapperOpKeyFormat(Class mapperOpKeyFormat) {
		this.mapperOpKeyFormat = mapperOpKeyFormat;
	}

	public Class getMapperOpValueFormat() {
		return mapperOpValueFormat;
	}

	public void setMapperOpValueFormat(Class mapperOpValueFormat) {
		this.mapperOpValueFormat = mapperOpValueFormat;
	}

	public Class getReducerOpKeyFormat() {
		return reducerOpKeyFormat;
	}

	public void setReducerOpKeyFormat(Class reducerOpKeyFormat) {
		this.reducerOpKeyFormat = reducerOpKeyFormat;
	}

	public Class getReducerOpValueFormat() {
		return reducerOpValueFormat;
	}

	public void setReducerOpValueFormat(Class reducerOpValueFormat) {
		this.reducerOpValueFormat = reducerOpValueFormat;
	}

	public String getJobId() {
		return jobId;
	}

	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

	public TaskType getTaskType() {
		return taskType;
	}

	public void setTaskType(TaskType taskType) {
		this.taskType = taskType;
	}

	public Class getTaskClass() {
		return taskClass;
	}

	public void setTaskClass(Class taskClass) {
		this.taskClass = taskClass;
	}

	public String getTaskClassName() {
		return taskClassName;
	}

	public void setTaskClassName(String taskClassName) {
		this.taskClassName = taskClassName;
	}

	public String getNodeIp() {
		return nodeIp;
	}

	public void setNodeIp(String nodeIp) {
		this.nodeIp = nodeIp;
	}

	public Integer getNodePort() {
		return nodePort;
	}

	public void setNodePort(Integer nodePort) {
		this.nodePort = nodePort;
	}

	public DFSBlock getInputBlock() {
		return inputBlock;
	}

	public void setInputBlock(DFSBlock inputBlock) {
		this.inputBlock = inputBlock;
	}

	public String getOutputFile() {
		return outputFile;
	}

	public void setOutputFile(String outputFile) {
		this.outputFile = outputFile;
	}

	public String getJarFilePath() {
		return jarFilePath;
	}

	public void setJarFilePath(String jarFilePath) {
		this.jarFilePath = jarFilePath;
	}

	public String getId() {
		return Id;
	}

	public void setId(String id) {
		Id = id;
	}

//	public Map<String, List<DFSBlock>> getInputKeys() {
//		return inputKeys;
//	}
//
//	public void setInputKeys(Map<String, List<DFSBlock>> inputKeys) {
//		this.inputKeys = inputKeys;
//	}

	public List<String> getPartitionKeys() {
		return partitionKeys;
	}

	public void setPartitionKeys(List<String> partitionKeys) {
		this.partitionKeys = partitionKeys;
	}

	public String getJarName() {
		return jarName;
	}

	public void setJarName(String jarName) {
		this.jarName = jarName;
	}

}
