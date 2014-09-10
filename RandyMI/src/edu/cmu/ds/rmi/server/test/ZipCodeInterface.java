package edu.cmu.ds.rmi.server.test;

public interface ZipCodeInterface // extends YourRemote or whatever
{
	public void initialise(ZipCodeList newlist);

	public String find(String city);

	public ZipCodeList findAll();

	public void printAll();
}
