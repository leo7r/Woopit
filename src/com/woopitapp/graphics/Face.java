package com.woopitapp.graphics;


public class Face{
	

	private int [] faceArray;
	private Material material;

	
	public Face(int[] faceArray, Material material){
		this.faceArray = faceArray;
		this.material = material;

	}
	/*public void setIndex(int[] index){
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
	}*/
	public int[] getFace(){
		return faceArray;
	}
	public void setFace(int[] faceArray){
		this.faceArray = faceArray;
	}
	/*public int[] getTextCoordIndex(){
		return textCoordIndex;
	}*/
	public Material getMaterial(){
		return material;
	}
	
}
