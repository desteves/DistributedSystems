package edu.cmu.ds.donkeyfs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import edu.cmu.ds.donkeyfs.DFSMessage.MessageType;

public class DFSNameNode {

	public static Map<DFSFile, List<DFSBlock>> fileRegistry = new HashMap<DFSFile, List<DFSBlock>>();
	public static HashMap<String, HashMap<String, String>> nodeRegistry = new HashMap<String, HashMap<String, String>>();

	/**
	 * main
	 * @param args
	 */
	public static void main(String[] args) {
		DFSRequestListener msglistener = new DFSRequestListener();
		Thread t = new Thread(msglistener);
		t.start();
		
		System.out.println("DonKeyFS Name Node Launched");
	}
}
