package edu.cmu.ds.rmi.exception;

public class RemoteException extends Exception{

	/**
	 * Thrown if exception occured in remote server
	 */
	private static final long serialVersionUID = 5714076142365977478L;

		public Exception cause;
	
		public RemoteException(Exception message)
		{
			
			this.cause = message;
			
		}
	
	  public String getMessage() {
	        
	            return super.getMessage() ;
	        
	  }
}
