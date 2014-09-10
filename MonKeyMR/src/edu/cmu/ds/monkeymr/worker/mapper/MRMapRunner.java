package edu.cmu.ds.monkeymr.worker.mapper;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import edu.cmu.ds.donkeyfs.DFSBlock;
import edu.cmu.ds.monkeymr.commons.Context;
import edu.cmu.ds.monkeymr.interfaces.Constants;
import edu.cmu.ds.monkeymr.master.MRTask;
import edu.cmu.ds.monkeymr.worker.MRTaskNode;

public class MRMapRunner implements Runnable {

	private MRMap map;
	private MRTask task;
	private Method method;

	public MRMapRunner(MRTask task, Object newInstance, Method method) {
		this.map = (MRMap) newInstance;
		this.task = task;
		this.method = method;
	}

	@Override
	public void run() {
		Properties prop = new Properties();
		InputStream input = null;

		try {
			input = new FileInputStream(Constants.MR_CONFIG);
			prop.load(input);
		} catch (IOException e) {
			System.err.println("[MRMapRunner] ! Err: Failed to load propss. ");
		}

		FileInputStream fstream;
		try {
			String DFSpath = prop.getProperty(Constants.DFS_PATH);
			String blockpath = task.getInputBlock().getBlockpath();
			fstream = new FileInputStream(DFSpath
					+ blockpath.substring(blockpath.indexOf("-") + 1)
					+ task.getInputBlock().getBlockname()
					+ task.getInputBlock().getfileExtension());
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine = null;
			Context context = new Context(DFSpath, task);
			long lineNumber = 0;
			Object[] args = new Object[3];
			args[2] = context;
			while ((strLine = br.readLine()) != null) {
				args[0] = lineNumber;
				args[1] = strLine;
				try {
					
					
					// foreach line invoke map
					method.invoke(map, args);
					
					lineNumber++;
				} catch (IllegalAccessException | IllegalArgumentException
						| InvocationTargetException e) {
					System.err
							.println("[MRMapRunner] ! Err: Failed to invoke method. ");
				}
				lineNumber++;
			}
			task.setPartitionKeys(context.keys);
			MRTaskNode.taskNodeRegistry.put(task, MRTask.TaskStatus.COMPLETE);
		} catch (IOException e) {
			System.err
					.println("[MRMapRunner] ! Err: Failed to load properties. ");
			// e.printStackTrace();
		}

	}

}
