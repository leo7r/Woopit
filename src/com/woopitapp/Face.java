package com.woopitapp;


public class Face{
	
	private int [] index;
	private int [] normal;
	private int [] textCoordIndex;
	private Material material;

	
	public Face(int[] index, int[] textCoordIndex,int[] normal, Material material){
		this.index = index;
		this.normal = normal;
		this.textCoordIndex = textCoordIndex;
		this.material = material;

	}
	public void setIndex(int[] index){
		this.index = index;
	}
	public void setNormal(int[] normal){
		this.normal = normal;
	}
	public void setTextCoordIndex(int[] textCoordIndex){
		this.textCoordIndex = textCoordIndex;
	}
	public void setMaterial(Material material){
		this.material = material;
	}
	public int[] getIndex(){
		return index;
	}
	public int[] getNormal(){
		return normal;
	}
	public int[] getTextCoordIndex(){
		return textCoordIndex;
	}
	public Material getMaterial(){
		return material;
	}
	
}
