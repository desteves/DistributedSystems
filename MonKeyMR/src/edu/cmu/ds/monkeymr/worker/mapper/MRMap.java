package edu.cmu.ds.monkeymr.worker.mapper;

import edu.cmu.ds.monkeymr.commons.Context;


//implements common map concepts like context etc. 

public abstract class MRMap<T1,T2,T3,T4> {

	public abstract void map(T1 t1,T2 t2,Context c);
	
}
