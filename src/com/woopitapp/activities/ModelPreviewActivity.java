package com.woopitapp.activities;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import com.woopitapp.R;
import com.woopitapp.graphics.Objeto;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.opengl.GLSurfaceView.Renderer;
import android.os.Bundle;
import android.widget.LinearLayout;

public class ModelPreviewActivity extends Activity {
	GLSurfaceView glView;
	GLClearRenderer render;
	String userId;
	String modelId;
	Objeto o;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		 setContentView(R.layout.model_preview);
		 Intent intent = getIntent();
		 this.userId = intent.getStringExtra("userId");
		 this.modelId = intent.getStringExtra("modelId");
		 iniciarPreview();
		 LinearLayout previewCanvas =  (LinearLayout) findViewById(R.id.previewCanvas);
		 previewCanvas.addView(glView);
		 iniciarModelo("objetos/" + modelId+".jet");
	}
	public class GLClearRenderer implements Renderer {
	    	
	    	private float mCubeRotation;
		
			int[] textures = new int[1];
			private float desplazamientoZ = -13.0f;
			
			public void onDrawFrame( GL10 gl ) {
				
	            gl.glLoadIdentity();	                  
				gl.glClearColor( 0.1f,0.1f,0.1f, 0.1f );
		        gl.glClearDepthf(1.0f);
		        //gl.glDepthMask(true);
		        gl.glEnable(GL10.GL_DEPTH_TEST);
		        gl.glDepthFunc(GL10.GL_LESS);
	            gl.glClear( GL10.GL_COLOR_BUFFER_BIT );
	            gl.glClear(GL10.GL_DEPTH_BUFFER_BIT);

		  
	            gl.glPushMatrix();  
	            gl.glLoadIdentity();
	            
		        gl.glTranslatef(0.0f, -1.5f, desplazamientoZ);
		        gl.glRotatef(mCubeRotation, 0, 1, 0);
		        o.draw(gl);
	            mCubeRotation -= 0.70f;
	            gl.glPopMatrix();   
	           
	            
			}

			public void transladarMundo(float z){
				desplazamientoZ = z;
			}

			public void onSurfaceChanged( GL10 gl, int width, int height ) {
				
	    	        // This is called whenever the dimensions of the surface have changed.
	    	        // We need to adapt this change for the GL viewport.
	    	        gl.glViewport( 0, 0, width, height );
	    	        gl.glClearDepthf(1.0f);
	                gl.glEnable(GL10.GL_DEPTH_TEST);
	                gl.glDepthFunc(GL10.GL_LEQUAL);
	    	    	gl.glMatrixMode(GL10.GL_PROJECTION);
	    	    	
	    			// Reset the projection matrix
	    			gl.glLoadIdentity();
	    			
	    			// Calculate the aspect ratio of the window
	    			GLU.gluPerspective(gl, 45.0f, (float) width / (float) height, 0.1f,100.0f);
	    			
	    			// Select the modelview matrix
	    			gl.glMatrixMode(GL10.GL_MODELVIEW);
	    			
	    			// Reset the modelview matrix
	    			gl.glLoadIdentity();
	    			
	    		     
	                float[] lightAmbient = {1.0f, 1.0f, 1.0f, 0.5f};
	    		    float[] lightDiffuse = {1.0f, 0.0f, 0.0f, 0.5f};
	    		    float[] lightPos = {0.1f, 0.1f, 0.1f, 1.0f};
	    		    gl.glEnable(GL10.GL_LIGHTING);
	    		    gl.glEnable(GL10.GL_LIGHT0);
	    		    gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_AMBIENT, lightAmbient, 0);
	    		    gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_DIFFUSE, lightDiffuse, 0);
	    		    gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_POSITION, lightPos, 0);
	    		}

			public void onSurfaceCreated( GL10 gl, EGLConfig config ) {
					
						
					 //   loadShader(GLES20.GL_VERTEX_SHADER,vertexShaderSource);
					    

			}
			
	    }
	  public void iniciarModelo(String nombre){
		  o = new Objeto(nombre,getApplicationContext());
	  }
	  public void iniciarPreview(){
		 glView = new GLSurfaceView( this );
		 glView.setEGLConfigChooser( 8, 8, 8, 8, 16, 0 );
		 glView.getHolder().setFormat( PixelFormat.TRANSLUCENT );
		 render = new GLClearRenderer();
		 glView.setRenderer(render);
		
	}
}
