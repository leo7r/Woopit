package com.woopitapp.graphics;


import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.nio.channels.FileChannel;
import java.util.Vector;

import javax.microedition.khronos.opengles.GL10;

import android.util.Log;

public class GroupMesh{
	
	private Material m;
	private int carasCount = 0;
    private ShortBuffer  mIndexBuffer;
    private ByteBuffer  mTextureBuffer;
    private ByteBuffer  mVertexBuffer;
    private ByteBuffer  mNormalBuffer;
    private Vector<Face> caras;
    public int bTextcoord = 0;
    public int bNormal = 0;
    private int primitive = GL10.GL_TRIANGLES;
    
    public GroupMesh(){
    	caras = new Vector<Face>();
    	
    }
    public Vector<Face> getCaras(){
    	return this.caras;
    }
    public GroupMesh(Material m, Vector<Face> subFaces, ByteBuffer mVertexBuffer,FloatBuffer mColorBuffer,ShortBuffer mIndexBuffer,ByteBuffer mNormalBuffer){
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
    public ByteBuffer getTextureBuffer(){
    	return mTextureBuffer;
    }
    public ByteBuffer getVertexBuffer(){
    	return mVertexBuffer;
    }
    public ByteBuffer getNormalBuffer(){
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
    public void setCarasCount(int carasCount){
    	this.carasCount = carasCount;
    }
    public int getCarasCount(){
    	return this.carasCount;
    }
    public void crearBuffers(FileChannel in,int cantCaras){
    	try{
    		carasCount = cantCaras;
					
	    	ByteBuffer byteBuf = ByteBuffer.allocateDirect(cantCaras*3*3*4);
			byteBuf.order(ByteOrder.nativeOrder());
			mVertexBuffer = byteBuf;
			in.read(mVertexBuffer);	
			
			byteBuf = ByteBuffer.allocateDirect(4);
			byteBuf.order(ByteOrder.nativeOrder());
			in.read(byteBuf);
			byteBuf.position(0);
			
			if(byteBuf.getInt(0)>0){
				byteBuf = ByteBuffer.allocateDirect(cantCaras*3*2*4);
				byteBuf.order(ByteOrder.nativeOrder());
				mTextureBuffer = byteBuf;    
				in.read(mTextureBuffer);	
			}
			
			byteBuf = ByteBuffer.allocateDirect(4);
			byteBuf.order(ByteOrder.nativeOrder());
		
			in.read(byteBuf);
			byteBuf.position(0);
			if(byteBuf.getInt(0)>0){
				byteBuf.position(0);
				byteBuf = ByteBuffer.allocateDirect(cantCaras*3*3*4);
				byteBuf.order(ByteOrder.nativeOrder());
				mNormalBuffer = byteBuf;
				in.read(mNormalBuffer);
			}

	    	mNormalBuffer.position(0);
	    	mVertexBuffer.position(0);
	    	mTextureBuffer.position(0);

    	}catch(Exception e){
    		Log.e("PASOO---","error: " +  e);
    	}
		
    }
 
    public void liberarMemoria(){
        mNormalBuffer = null;
        mVertexBuffer = null;
        mTextureBuffer = null;
    }
}
