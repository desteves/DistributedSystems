package edu.cmu.ds.monkeymr.interfaces;

/**
 * Constants
 * 
 * @author Linne
 * 
 */
public interface Constants {

	//inner classes for map & reduce 
	public static final String JOB_REDUCE = "$Reduce";
	public static final String JOB_MAPPER = "$Map";
	
	public static final String METHOD_REDUCE = "reduce";
	public static final String METHOD_MAPPER = "map";
	
	//local jar file path
	public static final String JAR_PATH = "";	
	
	// property file names
	public static final String MR_CONFIG = "config.properties";
	

	// add other property file names necessary here

	// property names
	public static final String MASTER_IP = "masterip";
	public static final String MASTER_PORT = "masterport";
	public static final String PARTICIPANT_IP = "partip";
	public static final String PARTICIPANT_PORT = "partport";
	public static final String DFS_IP = "dsfip";
	public static final String DFS_PORT = "dfsport";
	
	public static final String BLOCK = "blockSize";
	public static final String ISDFS = "isInDFS";
	public static final String INPUT = "inputFile";
	public static final String OUTPUT = "outputDir";

	public static final String NODE_PORT_START ="nodeportstart";
	public static final String LOCAL_JARS="LOCAL_JARS";
	
	public static final Integer NUMBEROFMAPSPERNODE=3;
	
	public static final String DFS_PATH ="DFS_PATH";
	public static final String DFS_PATH_JARS = "DFS_PATH_JARS";	
	
	public static final String LOCAL_INTERMEDIATE_REDUCE = "LOCAL_INTERMEDIATE_REDUCE";
	
	//  add other properties necessary here

}
