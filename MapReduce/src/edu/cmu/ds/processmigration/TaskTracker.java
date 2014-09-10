package edu.cmu.ds.processmigration;

import java.util.HashMap;

public class TaskTracker implements Runnable {

	HashMap<String, Thread> mapOfThreads;
	
	public TaskTracker(HashMap<String, Thread> MapOfThreads)
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
				 		Slave.processes.remove(s);
				 }
				 
			}
			
			
			
		}
	}

	
	
}
