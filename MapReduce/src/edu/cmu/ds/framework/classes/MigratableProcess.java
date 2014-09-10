package edu.cmu.ds.framework.classes;

import java.util.ArrayList;
import java.util.List;

import edu.cmu.ds.framework.interfaces.MigratableProcessInterface;



public abstract class MigratableProcess implements MigratableProcessInterface {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1024271228488841084L;

	List<String> args;

	public MigratableProcess() {

	}

	public MigratableProcess(String[] args) {
		this.args = new ArrayList<String>();
		if (args != null)
			for (String arg : args)
				this.args.add(arg);
	}

	@Override
	public String toString() {
		return this.getClass().getCanonicalName() + " " + args.toString();
	}

	// public abstract void suspend();

	// public void

}