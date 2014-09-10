package edu.cmu.ds.processmigration;

import java.util.HashMap;

public class SlaveProcessTracker implements Runnable {

	HashMap<String, Thread> mapOfThreads;
	
	public SlaveProcessTracker(HashMap<String, Thread> MapOfThreads)
	{
		mapOfThreads = MapOfThreads;
	}
	
	@Override
	public void run() {
		
		while(true)
		{
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			for(String s : mapOfThreads.keySet() )
			{
				Thread proc = mapOfThreads.get(s);
				 if(!proc.isAlive())
				 { 	
					 	mapOfThreads.remove(s)	;
				 		SuperSlave.processes.remove(s);
				 }
				 
			}
			
			
			
		}
	}

	
	
}
