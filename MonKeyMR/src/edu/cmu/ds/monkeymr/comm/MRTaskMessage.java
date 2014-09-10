package edu.cmu.ds.monkeymr.comm;

import java.io.Serializable;

import edu.cmu.ds.monkeymr.master.MRTask;

public class MRTaskMessage implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -6259072853522944519L;
	public enum MessageType {LAUNCH,COMPLETE}
	
	private MRTask task;
	private MessageType type;
	
	public MRTask getTask() {
		return task;
	}
	public void setTask(MRTask task) {
		this.task = task;
	}
	public MessageType getType() {
		return type;
	}
	public void setType(MessageType type) {
		this.type = type;
	}
	
}
