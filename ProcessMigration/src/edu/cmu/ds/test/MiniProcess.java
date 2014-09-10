package edu.cmu.ds.test;

import edu.cmu.ds.framework.classes.MigratableProcess;
import edu.cmu.ds.framework.interfaces.MigratableProcessInterface;

public class MiniProcess extends MigratableProcess {

	long timeDiff; // / variable to keep the state
	String stimeDiff;
	private static final long serialVersionUID = 1L;

	public volatile boolean suspending = false;
	
	public MiniProcess() {
		timeDiff = 0;
	}

	@Override
	public void run() {
		while (!suspending) {
			try {
				Thread.sleep(3000);
				
				//if(!suspending)
				//{
				System.out.println(this.getClass().getCanonicalName() + " "
						+ serialVersionUID + " running. ");
//				long now = System.currentTimeMillis();
				timeDiff++;//= now - timeDiff; // /this value should always be ~3s
				System.out.println(timeDiff);
//				stimeDiff = String.valueOf(timeDiff);
				//}
			} catch (Exception e) {
				// do something
			}
		}
		suspending = false;
		// TODO Auto-generated method stub
		
		
	}

	public void suspend() {
		// TODO Auto-generated method stub
		suspending = true;
		while (suspending)
			;
	}

}
