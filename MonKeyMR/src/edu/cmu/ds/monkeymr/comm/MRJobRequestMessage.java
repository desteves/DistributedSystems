package edu.cmu.ds.monkeymr.comm;

import java.io.Serializable;
import java.util.List;


/**
 * Any participating node can send the master node this message It will start
 * the job contained within.
 * 
 * @author Linne
 * 
 */
public class MRJobRequestMessage implements Serializable {

	private static final long serialVersionUID = 7114265932064527763L;

	private byte[] jarFile;
	private String jarName;
	private List<String> jarArgs;
	

	public byte[] getJarFile() {
		return jarFile;
	}

	public void setJarFile(byte[] jarFile) {
		this.jarFile = jarFile;
	}

	public String getJarName() {
		return jarName;
	}

	public void setJarName(String jarName) {
		this.jarName = jarName;
	}

	public List<String> getJarArgs() {
		return jarArgs;
	}

	public void setJarArgs(List<String> jarArgs) {
		this.jarArgs = jarArgs;
	}


}
