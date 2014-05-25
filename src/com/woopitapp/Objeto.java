package com.woopitapp;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.microedition.khronos.opengles.GL10;

import org.lwjgl.opengl.GL11;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;
import android.widget.TextView;
 

public class Objeto {

	private Vector<Float> vertices = new Vector<Float>();
	private Vector<Normal> normals = new Vector<Normal>();
	private Vector<TextureCoord> textureCoor = new Vector<TextureCoord>();
	private Vector<Face> faces = new Vector<Face>();
	private Vector<Material> materiales = new Vector<Material>();
	private Vector<GroupMesh> groups;
	private int renderCount = 0;
	
	public Objeto(InputStream in,Context context){
		
		boolean file_normal = false;
		float[] coord = new float[2];
		groups = new Vector<GroupMesh>();
		GroupMesh group;
		Material materialActual = null;
		LineNumberReader input = new LineNumberReader(new InputStreamReader(in));	    
		String line = null;
		try {
			for (line = input.readLine(); 
					line != null; 
					line = input.readLine())
			{
				if (line.length() > 0) {
					if (line.startsWith("v ")) {
						
						float[] vertex = new float[3];
						StringTokenizer tok = new StringTokenizer(line);
						tok.nextToken();
						vertex[0] = Float.parseFloat(tok.nextToken());
						vertex[1] = Float.parseFloat(tok.nextToken());
						vertex[2] = Float.parseFloat(tok.nextToken());
						vertices.addElement(vertex[0]);
						vertices.addElement(vertex[1]);
						vertices.addElement(vertex[2]);
						
	
					}
					else if (line.startsWith("vt ")) {
						StringTokenizer tok = new StringTokenizer(line);
						tok.nextToken();
						coord[0] = Float.parseFloat(tok.nextToken());
						coord[1] = Float.parseFloat(tok.nextToken());
						textureCoor.addElement(new TextureCoord(coord[0],coord[1]));
						
					}
					
					else if (line.startsWith("f ")) {
						int[] face = new int[3];
						int[] face_n_ix = new int[3];
						int[] face_tx_ix = new int[3];
						int[] val;
						
						StringTokenizer tok = new StringTokenizer(line);
						tok.nextToken();
						val = Utils.parseIntTriple(tok.nextToken());
						face[0] = val[0];
						if (val.length > 1 && val[1] > -1)
							face_tx_ix[0] = val[1];
						if (val.length > 2 && val[2] > -1)
							face_n_ix[0] = val[2];
	
						val = Utils.parseIntTriple(tok.nextToken());
						face[1] = val[0];
						if (val.length > 1 && val[1] > -1)
							face_tx_ix[1] = val[1];
						if (val.length > 2 && val[2] > -1)
							face_n_ix[1] = val[2];
	
						val = Utils.parseIntTriple(tok.nextToken());
						face[2] = val[0];
						if (val.length > 1 && val[1] > -1) {
							face_tx_ix[2] = val[1];
						}
						if (val.length > 2 && val[2] > -1) {
							face_n_ix[2] = val[2];
						}
						if (tok.hasMoreTokens()) {
							int primitive = GL10.GL_TRIANGLE_FAN;
							groups.get(groups.size()-1).setPrimitive(primitive);
						}

						Face cara = new Face(face,face_tx_ix,face_n_ix,materialActual);
						faces.addElement(cara);
						groups.get(groups.size()-1).putFace(cara);

					}
					else if (line.startsWith("vn ")) {
						
						float[] norm = new float[3];
						StringTokenizer tok = new StringTokenizer(line);
						tok.nextToken();
						norm[0] = Float.parseFloat(tok.nextToken());
						norm[1] = Float.parseFloat(tok.nextToken());
						norm[2] = Float.parseFloat(tok.nextToken());
						
						normals.addElement(new Normal(norm[0],norm[1],norm[2]));
						file_normal = true;
					}
					else if(line.startsWith("mtllib ")){
						StringTokenizer tok = new StringTokenizer(line);
						tok.nextToken();
						this.materiales = Material.parseMaterials(tok.nextToken(),context);
					}
					else if(line.startsWith("usemtl ")){

						StringTokenizer tok = new StringTokenizer(line);
						tok.nextToken();
						String nombre = tok.nextToken();
						group = new GroupMesh(context);
						for(int i = 0; i < materiales.size();i++){
							materialActual = materiales.get(i);
							if(materialActual.getName().equals(nombre)){
								break;
							}
						}
						group.setMaterial(materialActual);
						groups.add(group);
					}
				}
			}
		 	
			this.crearBuffers(groups,this.textureCoor,this.vertices,this.normals);
			
		}catch (Exception ex) {
			System.err.println("ERROR: " + ex);
		}
		
	}

	
	
	public void draw(GL10 gl){
         


		int renderCount = 0;
		if(renderCount == 0){
			for(int i = 0 ; i < groups.size();i++){
				
				GroupMesh g  = groups.get(i);
				gl.glBindTexture(GL10.GL_TEXTURE_2D, i);
			    gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
			    gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
			    gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
			    gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
			    if( g.getMaterial().getTexture() != null){
			    	GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, g.getMaterial().getTexture(), 0);
			    }
			}
		} 	
		for(int i = 0 ; i < groups.size();i++){

			GroupMesh g  = groups.get(i);
			if( g.getMaterial().getTexture() != null){
				gl.glEnable(GL10.GL_TEXTURE_2D);
			}
			gl.glEnable(GL10.GL_DEPTH_TEST);
			gl.glEnable(GL10.GL_BLEND);
	        gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
	        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
	        gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
	        gl.glBindTexture(GL10.GL_TEXTURE_2D, i);
	        gl.glShadeModel(GL10.GL_SMOOTH);
	        gl.glEnableClientState(GL10.GL_NORMAL_ARRAY);
	        
	        gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_AMBIENT,g.getMaterial().getAmbient(), 0);			   			        
	        gl.glMaterialfv(GL10.GL_FRONT_AND_BACK ,GL10.GL_DIFFUSE, g.getMaterial().getDiffusse(), 0);
	        gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_SPECULAR, g.getMaterial().getSpecular(), 0);
	        gl.glMaterialf(GL10.GL_FRONT_AND_BACK, GL10.GL_SHININESS,g.getMaterial().getSIndex());
	        gl.glFrontFace(GL10.GL_CW);
	
	        gl.glEnable(GL10.GL_CULL_FACE);
	        gl.glColor4f(1.0f, 1.0f,1.0f, 1.0f);
	        gl.glNormalPointer(GL10.GL_FLOAT, 0, g.getNormalBuffer());
	        gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, g.getTextureBuffer());
		    gl.glVertexPointer(3,GL10.GL_FLOAT, 0, g.getVertexBuffer());
		    gl.glDrawArrays(g.getPrimitive(), 0, g.getCaras().size()*3);
		    
		    gl.glDisableClientState(GL10.GL_NORMAL_ARRAY);
	        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
	        gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
	        gl.glDisable(GL10.GL_COLOR_MATERIAL);
	        gl.glDisable(GL10.GL_TEXTURE_2D);
		}
	}
	private  void crearBuffers(Vector<GroupMesh> groups, Vector<TextureCoord> textureCoords,Vector<Float> vertices,Vector<Normal> normals){
		
			for(int i = 0; i < groups.size(); i++){
				groups.get(i).crearBuffers(textureCoords,vertices,normals);
			}
	
	}
	public short[] getIndexArray(){
		short[] r = new short[this.faces.size()*3];
		int c = 0;
		for(Face f: this.faces){
			for(int i : f.getIndex()){
				r[c]  = (short)i;
				c++;
			}
		}
		return r;
	}
	
	public void setVertices(Vector<Float> vertices){
		this.vertices = vertices;
		
	}
	public void setNormals(Vector<Normal> normals){
		this.normals = normals;
	}
	public void setTextureCoord(Vector<TextureCoord> textureCoor){
		this.textureCoor = textureCoor;
	}
	public void setFaces(Vector<Face> faces){
		this.faces = faces;
	}
	public void setMateriales(Vector<Material> materiales){
		this.materiales = materiales;
		
	}
	public Vector<Float> getVertices(){
		return vertices;
		
	}
	public Vector<Normal> getNormals(){
		return normals;
	}
	public Vector<TextureCoord> getTextureCoord(){
		return textureCoor;
	}
	public Vector<Face> getFaces(){
		return faces;
	}
	public Vector<Material>  getMateriales(){
		return materiales;
		
	}
}
