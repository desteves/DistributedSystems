/**
 * 
 */
package edu.cmu.ds.donkeyfs;

import java.io.Serializable;

/**
 * @author Linne
 * 
 */
public class DFSFile implements Serializable {

	private static final long serialVersionUID = 6456821248377525505L;
	private String filename;
	private String sourcepath;
	private int blocksize;

	public DFSFile(String filename, String sourcepath, int blocksize) {
		this.filename = filename;
		this.sourcepath = sourcepath;
		this.blocksize = blocksize;
	}

	public DFSFile(String filename) {
		this.filename = filename;
	}

	
	
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((filename == null) ? 0 : filename.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DFSFile other = (DFSFile) obj;
		if (filename == null) {
			if (other.filename != null)
				return false;
		} else if (!filename.equals(other.filename))
			return false;
		return true;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getSourcepath() {
		return sourcepath;
	}

	public void setSourcepath(String sourcepath) {
		this.sourcepath = sourcepath;
	}

	public int getBlocksize() {
		return blocksize;
	}

	public void setBlocksize(int blocksize) {
		this.blocksize = blocksize;
	}

	@Override
	public String toString() {
		return "File [fileName=" + filename + ", sourcePpath=" + sourcepath
				+ ", blockSize=" + blocksize + "]";
	}

}
