package edu.cmu.ds.rmi.comm;

import java.io.Serializable;

import edu.cmu.ds.rmi.registry.RemoteObjectReference;

public class RegistryMessage implements Serializable {

public enum Type {bind,rebind,unbind,lookup,list};
	
	public Type type;
	
	public RemoteObjectReference ror;
	
	public String ServiceName;
	
	public String port;
	
	public String ip;
	
	public String ClassName;
	
	public String InterfaceName;
}
