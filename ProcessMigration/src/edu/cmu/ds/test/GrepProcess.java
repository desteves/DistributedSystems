package edu.cmu.ds.test;

import java.io.PrintStream;
import java.io.EOFException;
import java.io.DataInputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.lang.Thread;
import java.lang.InterruptedException;

import edu.cmu.ds.framework.classes.MigratableProcess;
import edu.cmu.ds.framework.classes.TransactionalFileOutputStream;
import edu.cmu.ds.framework.classes.TransactionalFileInputStream;
import edu.cmu.ds.framework.interfaces.MigratableProcessInterface;

public class GrepProcess extends MigratableProcess
{
	private TransactionalFileInputStream  inFile;
	private TransactionalFileOutputStream outFile;
	private String query;

	private volatile boolean suspending = false;

	public GrepProcess(String args[]) throws Exception
	{
		if (args.length != 3) {
			System.out.println(args.toString());
			System.out.println("usage: GrepProcess <queryString> <inputFile> <outputFile>");
			throw new Exception("Invalid Arguments");
		}
		
		query = args[0];
		inFile = new TransactionalFileInputStream(args[1]);
		outFile = new TransactionalFileOutputStream(args[2], false);
	}

	public void run()
	{
		
		PrintStream out = new PrintStream(outFile);
		DataInputStream in = new DataInputStream(inFile);
		
		try {
			
			while (!suspending) {
				String line = in.readLine();
				
				if (line == null) break;
				
				
				if (line.contains(query)) {
					out.println(line);
					System.out.println(line);
					
				}
				
				
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					
				}
			}
		} catch (EOFException e) {
			
		} catch (IOException e) {
			System.out.println ("GrepProcess: Error: " + e);
		}


		suspending = false;
	}

	public void suspend()
	{
		suspending = true;
		while (suspending);
	}

}