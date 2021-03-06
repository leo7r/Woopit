package com.woopitapp.graphics;


import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.Timer;
import java.util.TimerTask;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLUtils;
import android.os.Environment;
import android.util.Log;

public class MultiTextObject {

	private Vector<Float> vertices = new Vector<Float>();
	private Vector<Normal> normals = new Vector<Normal>();
	private Vector<TextureCoord> textureCoor = new Vector<TextureCoord>();
	private Vector<Material> materiales = new Vector<Material>();
	private Vector<GroupMesh> groups;
	private int renderCount = 0;
	private int primitive = GL10.GL_TRIANGLES;
	private int[] textureIds;
	private Bitmap bmSelfie = null;
	private Context context;
	private ArrayList<Bitmap> images;
	private int iText = 0;
	
	public MultiTextObject(String nombre,Context context,ArrayList<Bitmap> images){
		  Timer timer = new Timer("Printer");
		try{
			
			this.images = images;
			this.context = context;
			groups = new Vector<GroupMesh>();
			this.crearBuffers(context,nombre);
		}catch(Exception e){
			System.gc();
			this.bmSelfie = bmSelfie;
			this.context = context;
			groups = new Vector<GroupMesh>();
			this.crearBuffers(context,nombre);
		}
	}

	public MultiTextObject(String nombre,Context context){
		try{
			this.context = context;

			groups = new Vector<GroupMesh>();
			this.crearBuffers(context,nombre);
		}catch(Exception e){
			System.gc();
			groups = new Vector<GroupMesh>();
			this.crearBuffers(context,nombre);
		}
	}
	public void draw(GL10 gl){
         



		if(renderCount == 0 || renderCount%10 == 0){

			textureIds = new int[groups.size()];
			gl.glGenTextures(groups.size(), textureIds, 0 );

			for(int i = 0 ; i < groups.size();i++){
				
				GroupMesh g  = groups.get(i);

			    if( g.getMaterial().getTexture() != null || images.size() > 0){
			       
			       
				    	gl.glBindTexture(GL10.GL_TEXTURE_2D, textureIds[i]);
					    gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
					    gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
					    gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
					    gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
					    if(renderCount%20 == 0 && renderCount != 0){
					    	if(iText < (images.size()-1)){
					    		iText++;
					    	}else{
					    		iText = 0;
					    	}
					    	if(bmSelfie != null){
					    		//bmSelfie.recycle();
					    	}
					    }
				//	    int resID = context.getResources().getIdentifier(nombres[iText], "drawable", context.getPackageName());
						bmSelfie = images.get(iText);
						GLUtils.texImage2D( GL10.GL_TEXTURE_2D, 0, bmSelfie, 0);
					  
			       	
			    }else{
				

			    }
			}
			renderCount++;
		} 	
		for(int i = 0 ; i < groups.size();i++){
			
		
			GroupMesh g  = groups.get(i);
			if((g.getMaterial() != null && g.getMaterial().getTexture() != null) || images.size()>0){
				gl.glEnable(GL10.GL_TEXTURE_2D);
				gl.glBindTexture(GL10.GL_TEXTURE_2D, textureIds[i]);
			}
		
			gl.glEnable(GL10.GL_DEPTH_TEST);
			gl.glEnable(GL10.GL_BLEND);
	        gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
	        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
	        gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
	        gl.glShadeModel(GL10.GL_SMOOTH);
	        gl.glEnableClientState(GL10.GL_NORMAL_ARRAY);
	        gl.glEnableClientState(GL10.GL_COLOR_MATERIAL);
		    gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_AMBIENT,g.getMaterial().getAmbient(), 0);			   			        
		    gl.glMaterialfv(GL10.GL_FRONT_AND_BACK ,GL10.GL_DIFFUSE, g.getMaterial().getDiffusse(), 0);
		    gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_SPECULAR, g.getMaterial().getSpecular(), 0);
		    gl.glMaterialf(GL10.GL_FRONT_AND_BACK, GL10.GL_SHININESS,g.getMaterial().getSIndex());
	        
		    if( bmSelfie != null ){
		        gl.glFrontFace(GL10.GL_CW);
		    }
	
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
	        renderCount++;
	        		
	        	
		}
	}
	private void cargarMateriales(String lineaMaterial,Context context){
		StringTokenizer tok = new StringTokenizer(lineaMaterial);
		tok.nextToken();
		while(tok.hasMoreElements()){
			this.materiales.addAll(Material.parseMaterials(tok.nextToken(),context));
		}
	}
	
	private void crearMateriales(FileChannel in){
		try{
			while(true){
				
				Material m = new Material();
				
				//setear nombrea material
				ByteBuffer byteBuf = ByteBuffer.allocateDirect(4);
				byteBuf.order(ByteOrder.nativeOrder());
				ByteBuffer matBuff = byteBuf;
				in.read(matBuff);	
				matBuff.position(0);
				int tamMat = matBuff.getInt();
				if(tamMat == -1){
					break;
				}
		
				
				byteBuf = ByteBuffer.allocateDirect(tamMat*2);
				byteBuf.order(ByteOrder.nativeOrder());
				matBuff = byteBuf;
				in.read(matBuff);	
				matBuff.position(0);
				String lineaNombre = "";
				for(int i = 0; i < tamMat;i++){
					lineaNombre += matBuff.getChar(i*2);

				}
				m.setName(lineaNombre);
	
				
				//setear Ambient
				byteBuf = ByteBuffer.allocateDirect(4*4);
				byteBuf.order(ByteOrder.nativeOrder());
				ByteBuffer extraBuff = byteBuf;
				in.read(extraBuff);	
				extraBuff.position(0);
				
				float aR = extraBuff.getFloat();
				float aG = extraBuff.getFloat();
				float aB = extraBuff.getFloat();
				float aA = extraBuff.getFloat();
				float [] ambient = {aR,aG,aB,aA};
				m.setAmbient(ambient);
				
				//setear Diffuse
				byteBuf = ByteBuffer.allocateDirect(4*4);
				byteBuf.order(ByteOrder.nativeOrder());
				extraBuff = byteBuf;
				in.read(extraBuff);	
				extraBuff.position(0);
				
				float dR = extraBuff.getFloat();
				float dG = extraBuff.getFloat();
				float dB = extraBuff.getFloat();
				float dA = extraBuff.getFloat();
				float [] diffuse = {dR,dG,dB,dA};
				m.setDiffusse(diffuse);
		
				
				//setear Specular
				byteBuf = ByteBuffer.allocateDirect(4*4);
				byteBuf.order(ByteOrder.nativeOrder());
				extraBuff = byteBuf;
				in.read(extraBuff);	
				extraBuff.position(0);
				
				float sR = extraBuff.getFloat();
				float sG = extraBuff.getFloat();
				float sB = extraBuff.getFloat();
				float sA = extraBuff.getFloat();
				float [] specular = {sR,sG,sB,sA};
				m.setSpecular(specular);
			
				
				//setear Ni, Si
				byteBuf = ByteBuffer.allocateDirect(4*2);
				byteBuf.order(ByteOrder.nativeOrder());
				extraBuff = byteBuf;
				in.read(extraBuff);	
				extraBuff.position(0);
				
				float Ni = extraBuff.getFloat();
				float Si = extraBuff.getFloat();
				
				m.setNi(Ni);
				m.setSIndex(Si);
	
				
				byteBuf = ByteBuffer.allocateDirect(4);
				byteBuf.order(ByteOrder.nativeOrder());
				extraBuff = byteBuf;
				in.read(extraBuff);	
				extraBuff.position(0);
				
				int tamTex = extraBuff.getInt();
				if(tamTex > 0){
					byteBuf = ByteBuffer.allocateDirect(tamTex);
					byteBuf.order(ByteOrder.nativeOrder());
					extraBuff = byteBuf;
					in.read(extraBuff);	
					extraBuff.position(0);
					m.setTexture(BitmapFactory.decodeByteArray(extraBuff.array(), 0, tamTex));

				}
		
				this.materiales.add(m);
			}
		}catch(Exception e){
			
		}
	}
	
	private  void crearBuffers(Context context,String nombre){
			try{
					
				File dir = Environment.getExternalStorageDirectory();
				File woopitDir;
				FileInputStream inStream = null;
				FileChannel in ;
				if(images.size() > 0){
					AssetFileDescriptor afd = context.getAssets().openFd(nombre);  
				 	FileInputStream fis = afd.createInputStream();
				 	in = fis.getChannel();
				}else{
					 woopitDir = new File(dir,"/Woopit/models/"+nombre);
					 inStream = new FileInputStream(woopitDir);
					 in = inStream.getChannel();

				}
				
				crearMateriales(in);
				ByteBuffer byteBuf = ByteBuffer.allocateDirect(4);
				
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
				if(inStream != null){
					inStream.close();
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


	public void liberarMemoria() {
		for(Material m : materiales){
			if(m.getTexture()!= null){
				m.setTexture(null);
			}
		}
		for(GroupMesh i : groups){
			i.liberarMemoria();
		}
	}
}
