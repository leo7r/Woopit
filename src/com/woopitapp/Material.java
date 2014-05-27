package com.woopitapp;



import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.HashMap;
import java.util.StringTokenizer;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class Material {
	private String name;
	private float diffusse[];
	private float ambient[];
	private float specular[];
	private float nI;
	private float sIndex;
	private int illum;
	private Bitmap texture;
	
	public Material( String name, float diffusse[], float ambient[], float specular[], float nI, float sIndex, int illum, Bitmap texture){
		this.name = name;
		this.diffusse = diffusse;
		this.ambient = ambient;
		this.specular = specular;
		this.nI = nI;
		this.sIndex = sIndex;
		this.illum = illum;
		this.texture = texture;
	}
	
	public static HashMap<String,Material> parseMaterials(String nombreArchivo,Context context){
		HashMap<String,Material> materiales = new HashMap<String,Material>();
		try {
			InputStream in =   context.getAssets().open("materiales/"+nombreArchivo);
			LineNumberReader input = new LineNumberReader(new InputStreamReader(in));	    
			String line = null;
			String name = null;
			float diffusse[] = new float[3];
			float ambient[] = new float[3];
			float specular[] = new float[3];
			float nI = 0.0f;
			float sIndex = 0.0f;
		
			Bitmap texture = null;
			int illum = 0;
			for (line = input.readLine(); 	line != null; 	line = input.readLine()){
				if (line.length() > 0) {
			
					if (line.startsWith("newmtl ")) {
						if(name != null){
							materiales.put(name,new Material(name,diffusse,ambient,specular,nI,sIndex,illum,texture));
						}
						StringTokenizer tok = new StringTokenizer(line);
						tok.nextToken();
						name = tok.nextToken();
						diffusse = new float[4];
						ambient = new float[4];
						specular = new float[4];
						nI = 0.0f;
						sIndex = 0.0f;
						illum = 0;
					}else if(line.startsWith("Kd ")){
						StringTokenizer tok = new StringTokenizer(line);
						tok.nextToken();
						diffusse[0] = Float.parseFloat(tok.nextToken());
						diffusse[1] = Float.parseFloat(tok.nextToken());
						diffusse[2] = Float.parseFloat(tok.nextToken());
			
							
					}else if (line.startsWith("Ka ")){
						StringTokenizer tok = new StringTokenizer(line);
						tok.nextToken();
						ambient[0] = Float.parseFloat(tok.nextToken());
						ambient[1] = Float.parseFloat(tok.nextToken());
						ambient[2] = Float.parseFloat(tok.nextToken());
						
				
					}else if(line.startsWith("Ks ")){
						StringTokenizer tok = new StringTokenizer(line);
						tok.nextToken();
						specular[0] = Float.parseFloat(tok.nextToken());
						specular[1] = Float.parseFloat(tok.nextToken());
						specular[2] = Float.parseFloat(tok.nextToken());
				
		
					}else if(line.startsWith("Ni ")){
						StringTokenizer tok = new StringTokenizer(line);
						tok.nextToken();
						nI = Float.parseFloat(tok.nextToken());
					}else if(line.startsWith("Ns ")){
						StringTokenizer tok = new StringTokenizer(line);
						tok.nextToken();
						sIndex = Float.parseFloat(tok.nextToken());						
					}else if(line.startsWith("illum ")){
						StringTokenizer tok = new StringTokenizer(line);
						tok.nextToken();
						illum = Integer.parseInt(tok.nextToken());
					}else if(line.startsWith("Tf ")){
							StringTokenizer tok = new StringTokenizer(line);
							tok.nextToken();
							diffusse[3] = Float.parseFloat(tok.nextToken()); 
							ambient[3] =  Float.parseFloat(tok.nextToken());
							specular[3] = Float.parseFloat(tok.nextToken());
					}else if(line.startsWith("map_Kd")){
						try{
							StringTokenizer tok = new StringTokenizer(line);
							tok.nextToken();
							StringTokenizer nombre = new StringTokenizer(tok.nextToken(),".");
							int resID = context.getResources().getIdentifier(nombre.nextToken(), "drawable", context.getPackageName());
							texture = BitmapFactory.decodeResource(context.getResources(), resID);
						}catch(Exception e){
							Log.e("Error", "Error abriendo textura " + e);
						}
					}
				}
			}
			if(diffusse[3] == 0.0f & specular[3] == 0.0f && ambient[3] == 0.0f){
				diffusse[3] = 1.0f;
				specular[3] = 1.0f;
				ambient[3] = 1.0f;
			}
			materiales.put(name,new Material(name,diffusse,ambient,specular,nI,sIndex,illum,texture));
			return materiales;
		}catch(Exception ex) {
			System.err.println("Error parsing file: " + ex);
			return null;
		}
	}
	
	public void setName(String name){
		this.name = name;
	}
	public void setDiffusse(float[] diffusse){
		this.diffusse = diffusse;
	}
	public void setAmbient(float[] ambient){
		this.ambient = ambient;
	}
	public void setSpecular(float[] specular){
		this.specular = specular;
	}
	public void setNi(float nI){
		this.nI = nI;
	}
	public void setSIndex(float sIndex){
		this.sIndex = sIndex;
	}
	public void setIllum(int illum){
		this.illum = illum;
	}
	public void setTexture(Bitmap texture){
		this.texture = texture;
	}
	public String getName(){
		return name;
	}
	public float[] getDiffusse(){
		return diffusse;
	}
	public float[] getAmbient(){
		return ambient;
	}
	public float[] getSpecular(){
		return specular;
	}
	public float getNi(){
		return nI;
	}
	public float getSIndex(){
		return sIndex;
	}
	public int getIllum(){
		return illum;
	}
	public Bitmap getTexture(){
		return texture;
	}
}
