/**
 * 
 */
package edu.cmu.ds.donkeyfs;

import java.io.Serializable;
import java.util.List;

/**
 * @author Linne
 * 
 */
public class DFSBlock implements Serializable {

	private static final long serialVersionUID = 128243091065497101L;
	private String ip; // where the block is located
	private int port; // where the block is located
	private int id; // identifier block within a file
	private String blockname; // dfs name
	private String blockpath; // dfs path

	private List<String> records;
	private String filename;
	
private String fileExtension;
	
	
	public String getfileExtension() {
		return fileExtension;
	}
	public void setfileExtension(String fileExtension) {
		this.fileExtension = fileExtension;
	}
	
	public String getFilename() {
		return filename;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}
	
	public List<String> getRecords() {
		return records;
	}

	public void setRecords(List<String> records) {
		this.records = records;
	}

	public DFSBlock() {

	}

	public DFSBlock(int id) {
		this.id = id;
	}

	public String getBlockname() {
		return blockname;
	}

	public void setBlockname(String blockname) {
		this.blockname = blockname;
	}

	public String getBlockpath() {
		return blockpath;
	}

	public void setBlockpath(String blockpath) {
		this.blockpath = blockpath;
	}

	@Override
	public String toString() {
		return "FileBlock [ip=" + ip + ", port=" + port + ", id=" + id + "]";
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DFSBlock other = (DFSBlock) obj;
		if (id != other.id)
			return false;
		return true;
	}

}
