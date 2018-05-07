package br.edu.ufmg.joaopaulo.crast.rast;

import java.util.ArrayList;
import java.util.List;

public class RAST extends ParentNode {

	private List<Relationship> relationships = new ArrayList<Relationship>();
	
	public List<Relationship> getRelationships() {
		return relationships;
	}
	public void setRelationships(List<Relationship> relationships) {
		this.relationships = relationships;
	}
	
	public boolean hasRelationship(Long n1, Long n2, String type) {
		for (Relationship relationship : this.getRelationships()) {
			if (relationship.getType().equals(type)
					&& ((relationship.getN1().equals(n1) && relationship.getN2().equals(n2)) 
							|| (relationship.getN2().equals(n1) && relationship.getN1().equals(n2)))) {
				return true;
			}
		}
		
		return false;
	}
	
	public void print() {
		System.out.println("Nodes");
		
		for (Node node : super.getNodes()) {
			node.print(1);
		}
		
		System.out.println("Relationships");
		
		for (Relationship relationship : this.getRelationships()) {
			System.out.println(
					relationship.getType() + " N1: " + relationship.getN1() + " N2: " + relationship.getN2());
		}
	}
}
