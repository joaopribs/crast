package br.edu.ufmg.joaopaulo.crast.rast;

import java.util.ArrayList;
import java.util.List;

public abstract class ParentNode {

	private List<Node> nodes = new ArrayList<Node>();

	public List<Node> getNodes() {
		return nodes;
	}

	public void setNodes(List<Node> nodes) {
		this.nodes = nodes;
	}
	
}
