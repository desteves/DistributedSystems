package edu.cmu.ds.framework.classes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;

public class TransactionalFileOutputStream extends OutputStream implements
		Serializable {

	private File file;
	String filename;

	private boolean closed;

	private static final long serialVersionUID = -7609952755342897168L;

	public TransactionalFileOutputStream(String filename, boolean b) {
		this.filename = filename;
		file = new File(filename);
		closed = b;
	}

	public void writeALine(String line) throws IOException {
		FileOutputStream fstream = null;
		try {
			fstream = new FileOutputStream(file, true);
			closed = false;
			fstream.write(line.getBytes());
		} catch (IOException e) {
			System.err.println("Error: " + e.getMessage());
		} finally {
			if (fstream != null)
				fstream.close();
			closed = true;
		}
	}

	public boolean isClosed() {
		return closed;
	}

	@Override
	public void write(int b) throws IOException {
		
		if (!file.exists()) {
			file.createNewFile();
		}
		FileOutputStream fstream  = new FileOutputStream(file, true);
		
		fstream.write(b);
		
	}

}
