package com.woopitapp.activities;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLU;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.woopitapp.R;
import com.woopitapp.entities.User;
import com.woopitapp.graphics.Objeto;
import com.woopitapp.services.ServerConnection;

public class ModelPreviewActivity extends Activity {
	GLSurfaceView glView;
	GLClearRenderer render;
	User user;
	int userId;
	int modelId;
	String userName;
	Objeto o;
	@Override
	
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.model_preview);
		user = User.get(getApplicationContext());
		Bundle extras = getIntent().getExtras();
		modelId = extras.getInt("modelId");
		
		iniciarPreview();
		LinearLayout previewCanvas =  (LinearLayout) findViewById(R.id.previewCanvas);
		LinearLayout sendButtons =  (LinearLayout) findViewById(R.id.send_buttons);
		LinearLayout buyOrSendModel =  (LinearLayout) findViewById(R.id.buy_or_send_model);
		TextView enviarA = (TextView) findViewById(R.id.enviar_a);
		
		previewCanvas.addView(glView);
		iniciarModelo( modelId+".jet");

		if ( extras.containsKey("userId") && extras.containsKey("userName") && extras.containsKey("enable") && extras.getBoolean("enable") ){

			userId = extras.getInt("userId");
			userName = extras.getString("userName");
			enviarA.setText(getResources().getString(R.string.enviar_a,userName));			

			Button bMapa = (Button)findViewById(R.id.enviarMapa);
			
			bMapa.setOnClickListener(new  View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Intent lookOnMapi =  new  Intent(getApplicationContext(),MapActivity.class);
					lookOnMapi.putExtra("userName", userName);
					startActivity(lookOnMapi);
				}
			});
		 
			Button bUbiacionActual = (Button)findViewById(R.id.enviarActual);
			
			bUbiacionActual.setOnClickListener(new  View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						enviarActual();
					}
			});
			
		}
		else{
			
			TextView previewText = (TextView) findViewById(R.id.previewText);
			Button button = (Button) findViewById(R.id.buy_or_send_button);
			
			sendButtons.setVisibility(View.GONE);
			buyOrSendModel.setVisibility(View.VISIBLE);
			
			if ( extras.containsKey("enable") && extras.getBoolean("enable") ){
				button.setText(R.string.enviar_woop);
				button.setOnClickListener(new OnClickListener(){

					@Override
					public void onClick(View arg0) {
						
						Intent i = new Intent(getApplicationContext(),ChooseFriendActivity.class);
						i.putExtra("modelId", modelId);
						startActivity(i);
					}
				});
			}
			else{
				button.setText(R.string.comprar_modelo);
				button.setOnClickListener(new OnClickListener(){

					@Override
					public void onClick(View arg0) {
						Toast.makeText(getApplicationContext(), "NOT YET :D", Toast.LENGTH_SHORT).show();
					}
				});
			}
			
			enviarA.setVisibility(View.GONE);
			previewText.setVisibility(View.VISIBLE);
		}
		
	}
	
	public void enviarActual(){
		
		Send_Message sm = new Send_Message(getApplicationContext(), "","",500,500);
		sm.execute();
		
		
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
	    		    float[] lightDiffuse = {1.0f, 1.0f, 1.0f, 0.5f};
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
	class Send_Message extends ServerConnection{
    	Context con;
		public Send_Message(Context context,String title,String text,double latitud, double longitud){
			this.con = context;
			
			init(con,"send_message",new Object[]{user.id,userId,modelId,title,text,latitud,longitud});
		}

		@Override
		public void onComplete(String result) {
			if(result.equals("OK")){
				setResult(RESULT_OK, null);
			    finish();
				LayoutInflater inflater = getLayoutInflater();
				 
				View layout = inflater.inflate(R.layout.sent_message_toast,
				  (ViewGroup) findViewById(R.id.custom_toast_layout_id));

				// set a dummy image
				ImageView image = (ImageView) layout.findViewById(R.id.image);
				image.setImageResource(R.drawable.message_sent);

				// set a message
				TextView text = (TextView) layout.findViewById(R.id.text);
				text.setTextColor(getResources().getColor(R.color.woopit_green));
				text.setText(R.string.mensaje_enviado);

				// Toast...
				Toast toast = new Toast(getApplicationContext());
				toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
				toast.setDuration(Toast.LENGTH_SHORT);
				toast.setView(layout);
				toast.show();
			}else{
				Log.e("Error","eeee");
			}
		}
	}
}
