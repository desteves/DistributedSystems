package edu.cmu.ds.rmi.server.test;

import edu.cmu.ds.rmi.server.ServerProxyDispatcher;
import edu.cmu.ds.rmi.server.Server;

/**
 * 
 * @author Linne Sample test class
 * 
 */
public class RemoteObjectA_1 extends Server implements RemoteObjectA_1Interface {
	private int two;

	/**
	 * Constructor. Always create a new server thread
	 */
	public RemoteObjectA_1() {
		super();
		this.two = 2;
		Thread t = new Thread(new ServerProxyDispatcher(this, this.getClass()
				.getCanonicalName()));
		t.start();
	}

	/**
	 * Method that can be called remotely
	 */
	public void methodOne() {
		System.out.println("hello from methodOne");
	}

	/**
	 * Method that can be called remotely
	 */
	public int methodTwo(int number) {
		System.out.println("hello from methodTwo");
		return two * number;
	}

	public static void main(String args[]) {
		new RemoteObjectA_1();
	}

}
