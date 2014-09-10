package edu.cmu.ds.monkeymr.worker.reducer;

import edu.cmu.ds.monkeymr.commons.Context;

/**
 * 
 * @author Linne
 *
 * @param <T1>
 * @param <T2>
 * @param <T3>
 * @param <T4>
 * 
 * 
 * implements common red concepts
 */
public abstract class MRReduce<T1,T2,T3,T4> {

	public abstract void reduce(T1 t1,Iterable<T2> t2,Context c);
}
