package edu.cmu.ds.monkeymr.comm;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

import edu.cmu.ds.monkeymr.master.MRTask;

public class MRHeartBeat implements Serializable{

	private String taskNodeName ;
	private HashMap<MRTask,MRTask.TaskStatus> tasks ;
	public String getTaskNodeName() {
		return taskNodeName;
	}
	public void setTaskNodeName(String taskNodeName) {
		this.taskNodeName = taskNodeName;
	}
	public HashMap<MRTask,MRTask.TaskStatus> getTasks() {
		return tasks;
	}
	public void setTasks(HashMap<MRTask,MRTask.TaskStatus> tasks) {
		this.tasks = tasks;
	}

}
