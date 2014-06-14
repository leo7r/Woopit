package com.woopitapp.graphics;


import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.microedition.khronos.opengles.GL10;


import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.opengl.GLUtils;
import android.util.Log;

 

public class Objeto {

	private Vector<Float> vertices = new Vector<Float>();
	private Vector<Normal> normals = new Vector<Normal>();
	private Vector<TextureCoord> textureCoor = new Vector<TextureCoord>();
	private Vector<Material> materiales = new Vector<Material>();
	private Vector<GroupMesh> groups;
	private int renderCount = 0;
	private int primitive = GL10.GL_TRIANGLES;


	public Objeto(String nombre,Context context){
		groups = new Vector<GroupMesh>();
	 	this.crearBuffers(context,nombre);
	}


	public void draw(GL10 gl){
         



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
			renderCount++;
		} 	
		for(int i = 0 ; i < groups.size();i++){
			
		
			GroupMesh g  = groups.get(i);
			if(g.getMaterial() != null && g.getMaterial().getTexture() != null){
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
	        
	        gl.glFrontFace(GL10.GL_CCW);
	
	        gl.glEnable(GL10.GL_CULL_FACE);
	        gl.glNormalPointer(GL10.GL_FLOAT, 0, g.getNormalBuffer());
	        if( g.getTextureBuffer() != null){
	        	gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, g.getTextureBuffer());
	        }
		    gl.glVertexPointer(3,GL10.GL_FLOAT, 0, g.getVertexBuffer());
		    gl.glDrawArrays(primitive, 0, g.getCarasCount()*3);

		    gl.glDisableClientState(GL10.GL_NORMAL_ARRAY);
	        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
	        gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
	        gl.glDisable(GL10.GL_TEXTURE_2D);
	        	
	        		
	        	
		}
	}
	private void cargarMateriales(String lineaMaterial,Context context){
		StringTokenizer tok = new StringTokenizer(lineaMaterial);
		tok.nextToken();
		while(tok.hasMoreElements()){
			this.materiales.addAll(Material.parseMaterials(tok.nextToken(),context));
		}
	}
	private  void crearBuffers(Context context,String nombre){
			try{
				AssetFileDescriptor afd = context.getAssets().openFd(nombre);  
				FileInputStream fis = afd.createInputStream();
				FileChannel in =  fis.getChannel();
				
				ByteBuffer byteBuf = ByteBuffer.allocateDirect(4);
				byteBuf.order(ByteOrder.nativeOrder());
				ByteBuffer matBuff = byteBuf;
				in.read(matBuff);	
				matBuff.position(0);
				int tamMat = matBuff.getInt();
				matBuff.clear();
				
				byteBuf = ByteBuffer.allocateDirect(tamMat*2);
				byteBuf.order(ByteOrder.nativeOrder());
				matBuff = byteBuf;
				in.read(matBuff);
				matBuff.position(0);
				String lineaMaterial = "";
				
				for(int i = 0; i < tamMat;i++){
					lineaMaterial += matBuff.getChar(i*2);

				}
				cargarMateriales(lineaMaterial,context);
				
				while(true){
					
					GroupMesh group = new GroupMesh();
			    	byteBuf = ByteBuffer.allocateDirect(4*2);
					byteBuf.order(ByteOrder.nativeOrder());
					ByteBuffer extraBuff = byteBuf;
					in.read(extraBuff);	
					extraBuff.position(0);
					
					int material = extraBuff.getInt();
					int cantCaras = extraBuff.getInt();
					
					if(material == -1){				
						break;
					}
					group.crearBuffers(in,cantCaras);
					group.setMaterial(materiales.get(material));
					groups.add(group);
					
				}
			}catch(Exception e){
				Log.e("PASO","Error " + e);
			}
	
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
	
	public Vector<Material>  getMateriales(){
		return materiales;
		
	}
}
