package edu.cmu.ds.rmi.comm;

import java.io.Serializable;

public class StubMessage implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6148430923174027921L;

	public enum MessageType {stubRequest,stubResponse};
	
	private Class stubClass;

	private String stubClassName;
	
	public String getStubClassName() {
		return stubClassName;
	}

	public void setStubClassName(String stubClassName) {
		this.stubClassName = stubClassName;
	}

	private MessageType messageType;

	public Class getStubClass() {
		return stubClass;
	}

	public void setStubClass(Class stubClass) {
		this.stubClass = stubClass;
	}

	public MessageType getMessageType() {
		return messageType;
	}

	public void setMessageType(MessageType messageType) {
		this.messageType = messageType;
	}

}
