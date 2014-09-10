package edu.cmu.ds.rmi.registry;

import java.util.HashMap;
import java.util.List;

public class Registry {

	private HashMap<String,RemoteObjectReference> map ;
	
	public Registry()
	{
		map = new HashMap<String,RemoteObjectReference>();
	}
	
	public boolean bind(String serviceName,RemoteObjectReference ror)
	{
		
		// check if already bound 
		if(!map.containsKey(serviceName))
		{map.put(serviceName,ror);
			return true;
		}
		else
			return false;
		
	}
		
	
	public void rebind(String serviceName,RemoteObjectReference ror)
	{
		map.put(serviceName,ror);
	}

	public RemoteObjectReference lookup(String serviceName)
	{
		//return ror
		return map.get(serviceName);
	}

	public HashMap<String,RemoteObjectReference> list() {
		// TODO Auto-generated method stub
		return map;
	}
	
	
}
