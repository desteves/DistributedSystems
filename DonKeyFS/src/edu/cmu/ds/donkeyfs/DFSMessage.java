package edu.cmu.ds.donkeyfs;

import java.io.Serializable;
import java.util.List;

public class DFSMessage implements Serializable {

	private static final long serialVersionUID = 6746738146565120577L;

	public static enum MessageType {
		ADDFILE, LIST, GETBLOCK, GETFILE, ADDNODE, REMOVENODE, HEARTBEAT, REMOVEFILE, REMOVEBLOCK, REPLYMSG, PUTBLOCK, SAVEJAR, PUTFILEBLOCKS, GETFILEBLOCKS,ADDENTRY
	}

	private MessageType Type;
	private String Content;
	private DFSBlock block;
	private DFSFile file;
	private String filename;
	private int blockId;
	private int blockSize;
	private DFSFile DFSfile;
	private List<DFSBlock> DFSBlocks;
	private byte[] jarBytes;
	private String jarPath;
	private String jarName;

	public byte[] getJarBytes() {
		return jarBytes;
	}

	public void setJarBytes(byte[] jarBytes) {
		this.jarBytes = jarBytes;
	}

	public MessageType getType() {
		return Type;
	}

	public void setType(MessageType type) {
		Type = type;
	}

	public String getContent() {
		return Content;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public int getBlockId() {
		return blockId;
	}

	public void setBlockId(int blockId) {
		this.blockId = blockId;
	}

	public void setContent(String content) {
		Content = content;
	}

	public List<DFSBlock> getDFSBlocks() {
		return DFSBlocks;
	}

	public void setDFSBlocks(List<DFSBlock> blocks) {
		DFSBlocks = blocks;
	}

	public DFSFile getDFSfile() {
		return DFSfile;
	}

	public void setDFSfile(DFSFile dFSfile) {
		DFSfile = dFSfile;
	}

	public int getBlockSize() {
		return blockSize;
	}

	public void setBlockSize(int blockSize) {
		this.blockSize = blockSize;
	}

	public DFSBlock getBlock() {
		return block;
	}

	public void setBlock(DFSBlock block) {
		this.block = block;
	}

	public DFSFile getFile() {
		return file;
	}

	public void setFile(DFSFile file) {
		this.file = file;
	}

	public String getJarPath() {
		return jarPath;
	}

	public void setJarPath(String jarPath) {
		this.jarPath = jarPath;
	}

	public String getJarName() {
		return jarName;
	}

	public void setJarName(String jarName) {
		this.jarName = jarName;
	}

}
