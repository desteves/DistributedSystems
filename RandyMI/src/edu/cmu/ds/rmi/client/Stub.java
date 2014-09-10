package edu.cmu.ds.rmi.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import edu.cmu.ds.rmi.comm.RMIMessage;
import edu.cmu.ds.rmi.exception.RemoteException;

/**
 * 
 * @author Linne
 * 
 * 
 *         Common functionality across Stubs.
 */
public class Stub implements Serializable{
	private Socket s;
	private ObjectOutputStream oos;
	private ObjectInputStream ois;
	private InputStream is;
	private OutputStream os;

	private String ip;
	private int port;

	public RMIMessage execute(RMIMessage _rmiMessage)  {

		RMIMessage rmiMessage = _rmiMessage;
		try {
			s = new Socket(ip, port);
			os = s.getOutputStream();
			oos = new ObjectOutputStream(os);
			oos.writeObject(rmiMessage);

			//s.setSoTimeout(5000); // 10 second timeout
			is = s.getInputStream();
			ois = new ObjectInputStream(is);
			Object obj = ois.readObject();
			if (obj instanceof RMIMessage) {
				RMIMessage process = (RMIMessage) obj;
				System.out.println(process.getClientMessage());
				return process;
			}
			else if(obj instanceof RemoteException)
			{
				System.out.println("Error occured in the server");
				throw  (RemoteException)obj;
			}

		} catch (ClassNotFoundException| IOException | RemoteException e) {
			System.err.println("Error communicating with server");
			// Right now the Remote exception is caught here. Ideally the stub object should 
			//throw it to the invoking client. 
		}

		return null; // a void method
	}

	public Stub() {

	}

	public void Init(int port, String ip) {
		this.ip = ip;
		this.port = port;
	}

}
