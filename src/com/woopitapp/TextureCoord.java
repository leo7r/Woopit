package com.woopitapp;

public  class TextureCoord{
	
	private float u;
	private float v;
	
	public TextureCoord(float u, float v){
		this.u = u;
		this.v = v;			
	}
	public void setU(float u){
		this.u = u;
	}
	public void setV(float v){
		this.v = v;
	}
	public float getU(){
		return u;
	}
	public float getV(){
		return v;
	}
}