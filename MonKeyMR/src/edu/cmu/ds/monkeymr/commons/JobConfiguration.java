package edu.cmu.ds.monkeymr.commons;

import java.io.Serializable;

public class JobConfiguration implements Serializable {

	private static final long serialVersionUID = 5123626131766014607L;

	private String inputfile;
	private String outputfile;
	private int numReds;

	private Class mapper;
	private Class reducer;
	private Class mapperOpKeyFormat;
	private Class mapperOpValueFormat;
	private Class reducerOpKeyFormat;
	private Class reducerOpValueFormat;

	public String getInputfile() {
		return inputfile;
	}

	public void setInputfile(String inputfile) {
		this.inputfile = inputfile;
	}

	public String getOutputfile() {
		return outputfile;
	}

	public void setOutputfile(String outputpath) {
		this.outputfile = outputpath;
	}

	public Class getMapper() {
		return mapper;
	}

	public void setMapper(Class mapper) {
		this.mapper = mapper;
	}

	public Class getReducer() {
		return reducer;
	}

	public void setReducer(Class reducer) {
		this.reducer = reducer;
	}

	public Class getMapperOpKeyFormat() {
		return mapperOpKeyFormat;
	}

	public void setMapperOpKeyFormat(Class mapperOpKeyFormat) {
		this.mapperOpKeyFormat = mapperOpKeyFormat;
	}

	public Class getMapperOpValueFormat() {
		return mapperOpValueFormat;
	}

	public void setMapperOpValueFormat(Class mapperOpValueFormat) {
		this.mapperOpValueFormat = mapperOpValueFormat;
	}

	public Class getReducerOpKeyFormat() {
		return reducerOpKeyFormat;
	}

	public void setReducerOpKeyFormat(Class reducerOpKeyFormat) {
		this.reducerOpKeyFormat = reducerOpKeyFormat;
	}

	public Class getReducerOpValueFormat() {
		return reducerOpValueFormat;
	}

	public void setReducerOpValueFormat(Class reducerOpValueFormat) {
		this.reducerOpValueFormat = reducerOpValueFormat;
	}

	private boolean isInputInDFS;

	public boolean getIsInputInDFS() {
		return isInputInDFS;
	}

	public void setIsInputInDFS(boolean isInputInDFS) {
		this.isInputInDFS = isInputInDFS;
	}

	public int getNumReds() {
		return numReds;
	}

	public void setNumReds(int numReds) {
		this.numReds = numReds;
	}

}
