package edu.cmu.ds.rmi.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;

import edu.cmu.ds.rmi.comm.RMIMessage;
import edu.cmu.ds.rmi.comm.StubMessage;
import edu.cmu.ds.rmi.exception.RemoteException;

/**
 * 
 * @author Linne
 * 
 *         Every RemoveObject Class has a Dispatcher which listens for msgs from
 *         the client to execute on this
 * 
 */

public class ServerProxyDispatcher implements Runnable {

	private ServerSocket ss;
	private ObjectOutputStream oos;
	private ObjectInputStream ois;
	private InputStream is;
	private OutputStream os;
	private Socket socket;
	private Server server;
	private String className; // used to create the stub
	private String stringClass;

	/**
	 * 
	 * @param server
	 * @param className
	 *            -- IS-A Server
	 */
	public ServerProxyDispatcher(Server server, String className) {

		this.server = server;
		this.className = className;

		try {
			this.ss = new ServerSocket(Integer.valueOf(server.getPort()));
		} catch (NumberFormatException | IOException e) {

			System.err.println("! Err ServerSocket not created. ");
			e.printStackTrace();
		}

		GenerateStub();
	}

	/**
	 * Generates a stub based on an interface. The class must adhere to the
	 * protocol: Stub name will be className_stub Interface will be
	 * classNameInterface All extend the Stub super class
	 */
	private void GenerateStub() {
		String stubName = className + "_stub";
		String stubNameShort = stubName
				.substring(stubName.lastIndexOf('.') + 1);

		stringClass = "package edu.cmu.ds.rmi.server.test;"
				+ "import java.util.ArrayList; " + "import java.util.List; "
				+ "import edu.cmu.ds.rmi.client.Stub; "
				+ "import edu.cmu.ds.rmi.comm.MethodDescriptor; "
				+ "import edu.cmu.ds.rmi.comm.Param; "
				+ "import edu.cmu.ds.rmi.comm.RMIMessage;  " + "public class "
				+ stubNameShort + " extends Stub implements " + className
				+ "Interface { ";

		for (Method method : server.getClass().getDeclaredMethods()) {

			if (method.getName().equals("main")) // ignored
				continue;
			String head = method.toGenericString();
			head = head.replaceAll("\\s[^\\s]+" + method.getName(), " "
					+ method.getName()); // only get the raw method
			String body = "	edu.cmu.ds.rmi.comm.Param returnResult = null; java.util.List<edu.cmu.ds.rmi.comm.Param> params = null; ";

			if (!head.contains("()")) {// we have params, add them

				body += " params = new java.util.ArrayList<edu.cmu.ds.rmi.comm.Param>(); ";

				int i = 0; // incremental arguments
				final String arg = "arg";
				String temp = head.substring(head.indexOf('(') + 1,
						head.indexOf(')')); // get the original argument
											// section
				String args = "";
				for (String s : temp.split(",")) { // for every argument
					String name = arg + i++;
					args += s + " " + name + ", ";
					body += "params.add(new edu.cmu.ds.rmi.comm.Param(" + s
							+ ".class, " + name + ")); ";
				}
				args = "(" + args.substring(0, args.length() - 2) + ")"; // new
																			// naming
																			// convention
				head = head.substring(0, head.indexOf('(')) + args;
			}
			String legs = "edu.cmu.ds.rmi.comm.MethodDescriptor methodDescriptor = new edu.cmu.ds.rmi.comm.MethodDescriptor(returnResult, params); "
					+ "edu.cmu.ds.rmi.comm.RMIMessage rmiMessage = new edu.cmu.ds.rmi.comm.RMIMessage(\""
					+ className
					+ "\", \""
					+ method.getName()
					+ "\", methodDescriptor); "
					+ "rmiMessage = execute(rmiMessage); ";

			// if its not a void method then need to handle the return type
			if (!method.getGenericReturnType().toString().equals("void")) {

				String returnType = method.getGenericReturnType().toString();
				if (returnType.indexOf(' ') != -1) // has class
				{
					returnType = returnType.substring(returnType.indexOf(' '));
				}
				body += "returnResult = new edu.cmu.ds.rmi.comm.Param("
						+ returnType + ".class , 0); ";

				legs += "return ("
						+ returnType
						+ ") rmiMessage.getMethodDescriptor().getReturnResult().getValue();";
			}

			String sMethod = head + " { " + body + " " + legs + "}";

			stringClass += sMethod; // build the class one method at a time.

		} // end for each

		stringClass += "}";

	}

	@Override
	public void run() {
		// main thread, wait for messages from the client
		while (true) {

			try {
				try {
					socket = ss.accept();
					is = socket.getInputStream();
					ois = new ObjectInputStream(is);
					os = socket.getOutputStream();
					oos = new ObjectOutputStream(os);

					Object obj = ois.readObject();

					if (obj instanceof RMIMessage) {
						// using reflection, call the method in the RMI Message
						RMIMessage rmiMessage = (RMIMessage) obj;
						Class<?> c = Class.forName(rmiMessage.getClassName());
						Method method = c.getMethod(rmiMessage.getMethodName(),
								rmiMessage.getMethodDescriptor()
										.getParamsClasses());

						Object result = method.invoke(server, rmiMessage
								.getMethodDescriptor().getParamsObjects());

						// if not void, save the result
						if (!rmiMessage.getMethodDescriptor().isVoid()) {
							rmiMessage.getMethodDescriptor().getReturnResult()
									.setValue(result);
						}
						String clientMessage = "Invoked: "
								+ method.toGenericString() + " Successfully!";
						System.out.println(clientMessage); // for
															// testing/logging
						rmiMessage.setClientMessage(clientMessage);

						// send back the rmi message to the caller with the
						// results
						oos.writeObject(rmiMessage);

					} else if (obj instanceof StubMessage) {

						// the client has request a stub because its not
						// available locally
						oos.writeObject(stringClass);
					}
				} catch (ClassNotFoundException | NoSuchMethodException
						| SecurityException | IllegalAccessException
						| IllegalArgumentException | InvocationTargetException e) {

					// send a remote exception object to client
					RemoteException rex = new RemoteException(e);

					oos.writeObject(rex);
					System.err.println("! Err send to caller. ");
					e.printStackTrace();
				}

			} catch (IOException io) {

				System.err.println("! Err IOException ");
				io.printStackTrace();
			}

		}
	}
}
