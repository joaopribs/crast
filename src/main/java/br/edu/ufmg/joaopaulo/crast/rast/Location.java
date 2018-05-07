package br.edu.ufmg.joaopaulo.crast.rast;

public class Location {

	private String file;
	private Integer begin;
	private Integer end;
	private Integer bodyBegin;
	private Integer bodyEnd;
	
	public String getFile() {
		return file;
	}
	public void setFile(String file) {
		this.file = file;
	}
	public Integer getBegin() {
		return begin;
	}
	public void setBegin(Integer begin) {
		this.begin = begin;
	}
	public Integer getEnd() {
		return end;
	}
	public void setEnd(Integer end) {
		this.end = end;
	}
	public Integer getBodyBegin() {
		return bodyBegin;
	}
	public void setBodyBegin(Integer bodyBegin) {
		this.bodyBegin = bodyBegin;
	}
	public Integer getBodyEnd() {
		return bodyEnd;
	}
	public void setBodyEnd(Integer bodyEnd) {
		this.bodyEnd = bodyEnd;
	}
	
}
