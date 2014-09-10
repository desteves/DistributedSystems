package edu.cmu.ds.framework.interfaces;

import java.io.Serializable;

public interface MigratableProcessInterface extends Runnable, Serializable {
	public void suspend();
	public String toString();
	public void run();
}
