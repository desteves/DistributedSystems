package edu.cmu.ds.framework.classes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TransactionalFileInputStream extends InputStream implements
		Serializable {

	private File file;
	String filename;
	BufferedReader reader;
	long linesRead;
	public boolean closed;
	public Integer offset =0;

	public TransactionalFileInputStream(String args) {
		filename = args;
		file = new File(filename);
		reader = null;
		linesRead = 0;
	}

	private static final long serialVersionUID = -6443368022778012589L;

	public String readALine() throws IOException {
		String read = "";
		closed = false;
		InputStream ios = null;
		try {
			ios = new FileInputStream(file);
			reader = new BufferedReader(new InputStreamReader(ios));			
			read = reader.readLine();
			++linesRead;
		} catch (FileNotFoundException ex) {
			Logger.getLogger(this.getClass().getCanonicalName()).log(
					Level.SEVERE, null, ex);
		} catch (IOException ex) {
			Logger.getLogger(this.getClass().getCanonicalName()).log(
					Level.SEVERE, null, ex);
		} finally {
			try {
				reader.close();
				ios.close();
			} catch (IOException ex) {
				Logger.getLogger(this.getClass().getCanonicalName()).log(
						Level.SEVERE, null, ex);
			}
			closed = true;
		}

		return read;
	}

	@Override
	public int read()  {
		
		try (FileInputStream fis = new FileInputStream(file)) {
 
			fis.skip(offset);
			offset++;
		
		return fis.read();
	}
		catch(IOException e)
		{
			
			System.out.println("Too many threads accessing the file");
			return -1;
		}

}
}
