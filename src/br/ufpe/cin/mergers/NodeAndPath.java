package br.ufpe.cin.mergers;

import de.ovgu.cide.fstgen.ast.FSTTerminal;

public class NodeAndPath {
	
	private FSTTerminal node;
	private String filePath;
	
	public NodeAndPath(FSTTerminal node, String path){
		this.node = node;
		this.filePath = path;
	}

	public FSTTerminal getNode() {
		return node;
	}

	public void setNode(FSTTerminal node) {
		this.node = node;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	
	

}
