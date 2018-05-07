package br.edu.ufmg.joaopaulo.crast.rast;

import java.util.ArrayList;
import java.util.List;

public class Node extends ParentNode {

	private Long id;
	private String type;
	private Location location;
	private String simpleName;
	private String localName;
	private String namespace;
	private List<String> stereotypes;
	private List<Parameter> parameters = new ArrayList<Parameter>();

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public Location getLocation() {
		return location;
	}
	public void setLocation(Location location) {
		this.location = location;
	}
	public String getSimpleName() {
		return simpleName;
	}
	public void setSimpleName(String simpleName) {
		this.simpleName = simpleName;
	}
	public String getLocalName() {
		return localName;
	}
	public void setLocalName(String localName) {
		this.localName = localName;
	}
	public String getNamespace() {
		return namespace;
	}
	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}
	public List<String> getStereotypes() {
		return stereotypes;
	}
	public void setStereotypes(List<String> stereotypes) {
		this.stereotypes = stereotypes;
	}
	public List<Parameter> getParameters() {
		return parameters;
	}
	public void setParameters(List<Parameter> parameters) {
		this.parameters = parameters;
	}

	public void print(int level) {
		StringBuilder line = new StringBuilder();

		int childrenLevel = level + 1;

		while (level > 0) {
			line.append("|--- ");
			level--;
		}

		line.append(this.getId());
		line.append(" ");
		line.append(this.getType());
		line.append(" ");

		if (this.getSimpleName() != null) {
			line.append(this.getSimpleName() + " ");
		}
		
		if (this.getParameters().size() > 0) {
			line.append("(");
			
			String separator = "";
			for (Parameter parameter : this.getParameters()) {
				line.append(separator);
				line.append(parameter.getName());
				separator = ", ";
			}
			
			line.append(") ");
		}

		line.append("(begin: ");
		line.append(this.getLocation().getBegin());
		line.append(", end: ");
		line.append(this.getLocation().getEnd());
		
		if (this.getLocation().getBodyBegin() != null) {
			line.append(", bodyBegin: ");
			line.append(this.getLocation().getBodyBegin());
		}
		
		if (this.getLocation().getBodyEnd() != null) {
			line.append(", bodyEnd: ");
			line.append(this.getLocation().getBodyEnd());
		}
		
		line.append(", file: ");
		line.append(this.getLocation().getFile());
		line.append(")");

		System.out.println(line.toString());

		for (Node child : super.getNodes()) {
			child.print(childrenLevel);
		}
	}

	public Node searchBySimpleName(String simpleName) {
		if (this.getSimpleName() != null && this.getSimpleName().equals(simpleName)) {
			return this;
		}

		for (Node child : super.getNodes()) {
			Node found = child.searchBySimpleName(simpleName);
			if (found != null) {
				return found;
			}
		}

		return null;
	}
}
