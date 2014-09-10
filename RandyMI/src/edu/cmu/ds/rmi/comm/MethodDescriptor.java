package edu.cmu.ds.rmi.comm;

import java.io.Serializable;
import java.util.List;

/**
 * 
 * @author Linne
 * 
 * 
 *         http://www.artima.com/underthehood/invocationP.html method's return
 *         type and the number and types of its arguments
 */
public class MethodDescriptor implements Serializable {


	private static final long serialVersionUID = 1L;
	private Param returnResult; // the return type
	private List<Param> params; // list of method's parameters, if any

	public MethodDescriptor(Param returnResult, List<Param> params) {
		super();
		this.returnResult = returnResult;
		this.params = params;
	}

	public Param getReturnResult() {
		return returnResult;
	}

	public boolean isVoid() {
		if (returnResult == null || returnResult.getType() == null)
			return true;
		return false;

	}

	public void setReturnResult(Param returnResult) {
		this.returnResult = returnResult;
	}

	public List<Param> getParams() {
		return params;
	}

	public boolean hasParams() {
		if (params == null || params.isEmpty())
			return false;
		return true;
	}

	/**
	 *  Iterates through the parameters and return the Class[]
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public Class[] getParamsClasses() {
		if (!hasParams())
			return null;
		int size = params.size();
		Class[] args = new Class[size];
		int i = 0;
		for (Param param : params) {
			args[i++] = param.getType();
		}
		return args;

	}

	/**
	 * Iterates through the parameters and return the Object[]
	 * @return
	 */
	public Object[] getParamsObjects() {
		if (!hasParams())
			return null;
		int size = params.size();
		Object[] args = new Object[size];
		int i = 0;
		for (Param param : params) {
			args[i++] = param.getValue();
		}
		return args;
	}

	public void setParams(List<Param> params) {
		this.params = params;
	}

	public MethodDescriptor() {
		super();
	}

}
