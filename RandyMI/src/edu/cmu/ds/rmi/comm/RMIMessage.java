package edu.cmu.ds.rmi.comm;

import java.io.Serializable;

/**
 * 
 * @author Linne
 * 
 *         capable of encapsulating a method invocation
 * 
 */
public class RMIMessage implements Serializable {

	private static final long serialVersionUID = 1L;
	private String className; // must be known to unmarshall
	private String methodName; // method to be invoked remotely
	private MethodDescriptor methodDescriptor; 

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public MethodDescriptor getMethodDescriptor() {
		return methodDescriptor;
	}

	public void setMethodDescriptor(MethodDescriptor methodDescriptor) {
		this.methodDescriptor = methodDescriptor;
	}

	@Override
	public String toString() {
		return "RMIMessage [className=" + className + ", methodName="
				+ methodName + ", methodDescriptor=" + methodDescriptor + "]";
	}

	public RMIMessage(String className, String methodName,
			MethodDescriptor methodDescriptor) {
		super();
		this.className = className;
		this.methodName = methodName;
		this.methodDescriptor = methodDescriptor;
	}

	public RMIMessage() {
		super();
	}

	private String clientMessage; // message to be displayed in the Clients
	// machine regarding the execution of a method.

	public String getClientMessage() {
		return clientMessage;
	}

	public void setClientMessage(String clientMessage) {
		this.clientMessage = clientMessage;
	}

}
