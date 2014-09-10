package edu.cmu.ds.monkeymr.test;

import java.util.StringTokenizer;

import edu.cmu.ds.monkeymr.commons.Context;
import edu.cmu.ds.monkeymr.commons.MonkeyMR;
import edu.cmu.ds.monkeymr.worker.mapper.MRMap;
import edu.cmu.ds.monkeymr.worker.reducer.MRReduce;

public class FindAverage extends MonkeyMR {

	public FindAverage(String[] args) {
		conf.setInputfile(args[0]);
		conf.setIsInputInDFS(false);
		conf.setMapper(edu.cmu.ds.monkeymr.test.WordCount.Map.class);
		conf.setMapperOpKeyFormat(String.class);
		conf.setMapperOpValueFormat(String.class);
		conf.setOutputfile(args[1]);
		conf.setReducer(edu.cmu.ds.monkeymr.test.WordCount.Reduce.class);
		conf.setReducerOpKeyFormat(String.class);
		conf.setReducerOpValueFormat(Integer.class);
		conf.setNumReds(5);
	}

	// Reduce will always get Long,String as input
	
	public static class Map extends MRMap<Long, String, String, String> {
		private final static Integer one = new Integer(1);
		private String word = new String();

		public void map(Long key, String value, Context context) {
					
			String a[] =value.split("\t");	
			//System.out.println(a[0]+"1"+"~"+a[1]);
			context.write(a[0],"1"+"~"+a[1]);
	
		}

		
	}

	
	// Reduce should always get String, Iterable<String> -----
	
	public static class Reduce extends
			MRReduce<String, String, String, Integer> {

		public void reduce(String key, Iterable<String> values, Context context)
			{
			Integer sum = 0;
			Integer count=0;
			for (String val : values) {
				//System.out.println(val);
				count+=Integer.parseInt(val.split("~")[0]);
				sum+=Integer.parseInt(val.split("~")[1]);
			}
			
			//System.out.println(key +"----------"+ sum);
			
			// if need decimal . dont use integer
			context.write(key, sum/count);
		}
	}
	
}
