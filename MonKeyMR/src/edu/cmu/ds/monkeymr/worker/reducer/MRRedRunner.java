package edu.cmu.ds.monkeymr.worker.reducer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;

import edu.cmu.ds.monkeymr.commons.Context;
import edu.cmu.ds.monkeymr.interfaces.Constants;
import edu.cmu.ds.monkeymr.master.MRTask;
import edu.cmu.ds.monkeymr.worker.MRTaskNode;
import edu.cmu.ds.monkeymr.worker.MRWorkerListener;

/**
 * Runs a reducer task.
 * 
 * @author Linne
 * 
 */
public class MRRedRunner implements Runnable {
	private MRReduce red;
	private MRTask task;
	private Method method;

	public MRRedRunner(MRTask task, Object newInstance, Method method) {
		this.red = (MRReduce) newInstance;
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
			System.err.println("[MRRedRunner] ! Err: Failed to load propss. ");
		}
		String DFSpath = prop.getProperty(Constants.DFS_PATH);
		Context context = new Context(DFSpath, task);
		// ///////////////
		// /iterate through intermediate files and pass each one to the reduce
		// ///////////////
		System.out.println(MRWorkerListener.REDUCERS_PATH + task.getId());
		Iterator it = FileUtils.iterateFiles(new File(
				MRWorkerListener.REDUCERS_PATH + task.getId()), null, false);

		
		
		Object[] args = new Object[3];
		args[2] = context;

		String keyFileName;
		Path keyFilePath;
		String key;
		while (it.hasNext()) {// for each file
			File f = ((File) it.next());
			keyFileName = f.getName();
			keyFilePath = f.toPath();
			key = keyFileName.substring(keyFileName.lastIndexOf('-') + 1);
			System.out.println("[MRRedRunner] keyFilePath: "
					+ keyFilePath.toString());

			try {
				args[0] = key;
				Class iterType = task.getMapperOpValueFormat();
				List<Object> list = new ArrayList<Object>();

				// read each line and add 2nd column to list of objects of tipe:
				// task.getMapperOpValueFormat()
				for (String line : Files.readAllLines(keyFilePath,
						Charset.defaultCharset())) {
					Object newObj = line.split("\t")[1];
					list.add(newObj);
				}
				args[1] = list;

				try {
					
					//forach key invoke reduce
					method.invoke(red, args);
					
				} catch (IllegalAccessException | IllegalArgumentException
						| InvocationTargetException e) {
					//e.printStackTrace();
					System.err
							.println("[MRRedRunner] ! Err: Failed to invoke method. ");
				}
			} catch (IOException e) {
				System.err
						.println("[MRRedRunner] ! Err: Failed to load properties. ");
			}
		}
		 //delete the intermediate files
		try {
			FileUtils.cleanDirectory(new File(MRWorkerListener.REDUCERS_PATH
					+ task.getId()));
			FileUtils.deleteDirectory(new File(MRWorkerListener.REDUCERS_PATH
					+ task.getId()));
		} catch (IOException e) {
			System.err
					.println("[MRRedRunner] ! Err: Failed to delete intermediate files. ");
		}

		MRTaskNode.taskNodeRegistry.put(task, MRTask.TaskStatus.COMPLETE);
		System.out.println("[MRRedRunner] ******** COMPLETED Reducer ID " + task.getId());
	}
}
