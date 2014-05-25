package com.woopitapp;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Vector;

import javax.microedition.khronos.opengles.GL10;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;



public class GroupMesh{
	
	private Material m;
    private ShortBuffer  mIndexBuffer;
    private FloatBuffer  mTextureBuffer;
    private FloatBuffer  mVertexBuffer;
    private FloatBuffer  mNormalBuffer;
    private Vector<Face> caras;
    private Bitmap bitmap;
    private int primitive = GL10.GL_TRIANGLES;
    
    public GroupMesh(Context context){
    	caras = new Vector<Face>();
    	
    }
    public Vector<Face> getCaras(){
    	return this.caras;
    }
    public GroupMesh(Material m, Vector<Face> subFaces, FloatBuffer mVertexBuffer,FloatBuffer mColorBuffer,ShortBuffer mIndexBuffer,FloatBuffer mNormalBuffer){
    	this.m = m;
    	this.mIndexBuffer = mIndexBuffer;
    	this.mNormalBuffer = mNormalBuffer;
    }
    public void setMaterial(Material m){
    	this.m = m;
    }
    public void setIndexBuffer(ShortBuffer mIndexBuffer){
    	this.mIndexBuffer = mIndexBuffer;
    }
    public void putFace(Face cara){
    	caras.add(cara);
    }
    public ShortBuffer getIndexBuffer(){
    	return mIndexBuffer;
    }
    public FloatBuffer getTextureBuffer(){
    	return mTextureBuffer;
    }
    public FloatBuffer getVertexBuffer(){
    	return mVertexBuffer;
    }
    public FloatBuffer getNormalBuffer(){
    	return mNormalBuffer;
    }
    public Material getMaterial(){
    	return m;
    }
    public void setPrimitive(int primitive){
    	this.primitive = primitive;
    }
    public int getPrimitive(){
    	return this.primitive;
    }
    public void crearBuffers(Vector<TextureCoord> textCoord,Vector<Float> vertices, Vector<Normal> normals){
    	
    	ByteBuffer byteBuf = ByteBuffer.allocateDirect(vertices.size()*3*4*3);
		byteBuf.order(ByteOrder.nativeOrder());
		mVertexBuffer = byteBuf.asFloatBuffer();
		
    	byteBuf = ByteBuffer.allocateDirect(vertices.size()*3*4*3);
		byteBuf.order(ByteOrder.nativeOrder());
		mNormalBuffer = byteBuf.asFloatBuffer();
		
		byteBuf = ByteBuffer.allocateDirect(textCoord.size()*2*40);
		byteBuf.order(ByteOrder.nativeOrder());
		mTextureBuffer = byteBuf.asFloatBuffer();    
    	
		mIndexBuffer = ShortBuffer.allocate(caras.size()*3*40); 
    	
    	for(Face cara : this.caras){
    		
    		for(int i : cara.getIndex()){
    			
    			mVertexBuffer.put(vertices.get(i*3));
    			mVertexBuffer.put(vertices.get(i*3+1));
    			mVertexBuffer.put(vertices.get(i*3+2));
    			mIndexBuffer.put((short)(i));

    		}	
    		for(int j :cara.getTextCoordIndex()){
    			mTextureBuffer.put(textCoord.get(j).getU());
    			mTextureBuffer.put(textCoord.get(j).getV());
    		}
    		for(int k :cara.getNormal()){
    			
    			
    			mNormalBuffer.put(normals.get(k).getX());
    			mNormalBuffer.put(normals.get(k).getY());
    			mNormalBuffer.put(normals.get(k).getZ());
    		}
    	}

    	mNormalBuffer.position(0);
    	mVertexBuffer.position(0);
    	mTextureBuffer.position(0);
    	mIndexBuffer.position(0);
    	
		
    }
}
