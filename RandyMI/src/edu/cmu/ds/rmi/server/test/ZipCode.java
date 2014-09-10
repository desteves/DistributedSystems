package edu.cmu.ds.rmi.server.test;

import edu.cmu.ds.rmi.server.Server;
import edu.cmu.ds.rmi.server.ServerProxyDispatcher;

public class ZipCode extends Server implements ZipCodeInterface {
	ZipCodeList l;

	/**
	 * Constructor. Always create a new server thread
	 */
	public ZipCode() {
		super();
		l = null;
		Thread t = new Thread(new ServerProxyDispatcher(this, this.getClass()
				.getCanonicalName()));
		t.start();
	}
	
	public static void main(String args[]) {
		new ZipCode();
	}

	// when this is called, marshalled data
	// should be sent to this remote object,
	// and reconstructed.
	public void initialise(ZipCodeList newlist) {
		l = newlist;
	}

	// basic function: gets a city name, returns the zip code.
	public String find(String request) {
		// search the list.
		ZipCodeList temp = l;
		while (temp != null && !temp.city.equals(request))
			temp = temp.next;

		// the result is either null or we found the match.
		if (temp == null)
			return null;
		else
			return temp.ZipCode;
	}

	// this very short method should send the marshalled
	// whole list to the local site.
	public ZipCodeList findAll() {
		return l;
	}

	// this method does printing in the remote site, not locally.
	public void printAll() {
		ZipCodeList temp = l;
		while (temp != null) {
			System.out.println("city: " + temp.city + ", " + "code: "
					+ temp.ZipCode + "\n");
			temp = temp.next;
		}
	}
}
