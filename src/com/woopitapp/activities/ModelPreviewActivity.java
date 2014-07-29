package com.woopitapp.activities;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLU;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.woopitapp.R;
import com.woopitapp.WoopitActivity;
import com.woopitapp.dialogs.BuyModelDialog;
import com.woopitapp.entities.Message;
import com.woopitapp.entities.User;
import com.woopitapp.graphics.Objeto;
import com.woopitapp.server_connections.InsertCoins;
import com.woopitapp.server_connections.ModelDownloader;
import com.woopitapp.services.ServerConnection;
import com.woopitapp.services.Utils;

public class ModelPreviewActivity extends WoopitActivity {

	private final int REQUEST_SEND_MESSAGE = 0;
	private final int REQUEST_BUY_MODEL = 1;
	GLSurfaceView glView;
	GLClearRenderer render;
	User user;
	int userId;
	int modelId;
	String userName;
	Objeto o;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.model_preview);
		Bundle extras = getIntent().getExtras();
		user = User.get(getApplicationContext());
		modelId = extras.getInt("modelId");
		
		new MDownloader( this , modelId ).execute();
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		if ( requestCode == REQUEST_SEND_MESSAGE ) {
			if (resultCode == RESULT_OK) {
				setResult(RESULT_OK);
				finish();
			}
		}
		else{
			
			if ( requestCode == REQUEST_BUY_MODEL && resultCode == RESULT_OK ){
				Intent i = getIntent();
				i.putExtra("enable", true);
				
				finish();
				startActivity(i);
			}
			
		}
	}
	
	public void init( ){

		Bundle extras = getIntent().getExtras();
		
		iniciarPreview();
		RelativeLayout previewCanvas =  (RelativeLayout) findViewById(R.id.previewCanvas);
		LinearLayout sendButtons =  (LinearLayout) findViewById(R.id.send_buttons);
		LinearLayout buyOrSendModel =  (LinearLayout) findViewById(R.id.buy_or_send_model);
		TextView title = (TextView) findViewById(R.id.title);
		final EditText text = (EditText) findViewById(R.id.message_text);
		
		previewCanvas.addView(glView);
		iniciarModelo( modelId+".jet");

		if ( extras.containsKey("userId") && extras.containsKey("userName") && extras.containsKey("enable") && extras.getBoolean("enable") ){
			
			userId = extras.getInt("userId");
			userName = extras.getString("userName");
			title.setText(getResources().getString(R.string.enviar_a,userName));

			Button bMapa = (Button)findViewById(R.id.enviarMapa);
			
			bMapa.setOnClickListener(new  View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Intent lookOnMapi =  new  Intent(getApplicationContext(),MapActivity.class);
					lookOnMapi.putExtra("userId", userId);
					lookOnMapi.putExtra("modelId", modelId);
					lookOnMapi.putExtra("userName", userName);
					lookOnMapi.putExtra("modelId", modelId);
					lookOnMapi.putExtra("message", text.getText().toString());
					
					startActivityForResult(lookOnMapi,REQUEST_SEND_MESSAGE);

					Utils.onMessageNew(getApplicationContext(), "ModelPreviewActivity", userId);
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
			text.setVisibility(View.GONE);
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
						Utils.onMessageNew(getApplicationContext(), "ModelPreviewActivity");
					}
				});
			}
			else{
				button.setText(R.string.comprar_modelo);
				button.setOnClickListener(new OnClickListener(){
					
					@Override
					public void onClick(View arg0) {
						Intent i = new Intent(getApplicationContext(),BuyModelDialog.class);
						i.putExtra("modelId", modelId);
						startActivityForResult( i , REQUEST_BUY_MODEL );
						Utils.onModelBuy(getApplicationContext(), "ModelPreviewActivity", "Entrar", modelId);
					}
				});
			}
			
		}
	}
	
	public void enviarActual(){
		
		String message = ((EditText)findViewById(R.id.message_text)).getText().toString();
		
		Send_Message sm = new Send_Message(this, "",message,500,500);
		sm.execute();

		setResult(RESULT_OK);
	    finish();
	}
	
	public class GLClearRenderer implements Renderer {
	    	
	    	private float mCubeRotation;
		
			int[] textures = new int[1];
			private float desplazamientoZ = -13.0f;
			
			public void onDrawFrame( GL10 gl ) {
				
	            gl.glLoadIdentity();	                  
				gl.glClearColor(0.039f,0.0f,0.16f, 0.0f );
		        gl.glClearDepthf(1.0f);
		        gl.glDepthMask(true);
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
	    		    float[] lightPos = {0.1f, 5.1f, 0.1f, 1.0f};
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
		//glView.setEGLConfigChooser( 8, 8, 8, 8, 16, 0 );
		glView.getHolder().setFormat( PixelFormat.OPAQUE );
		render = new GLClearRenderer();
		glView.setRenderer(render);
	}
	
	class Send_Message extends ServerConnection{
    	
		Activity act;
		int cantCoins = 1;
		String text;
		double latitude, longitude;
		
		public Send_Message(Activity act,String title,String text,double latitud, double longitud){
			this.act = act;
			this.text = text;
			this.latitude = latitud;
			this.longitude = longitud;
			
			init(act,"send_message",new Object[]{user.id,userId,modelId,title,text,latitud,longitud});
		}

		@Override
		public void onComplete(String result) {
			
			if( result != null && result.equals("OK") ){

				new InsertCoins(act , cantCoins , R.string.por_enviar_mensaje ).execute();
				Utils.onMessageSent(getApplicationContext(), "ModelPreviewActivity", modelId, text, latitude, longitude);
				Utils.sendBroadcast(getApplicationContext(), R.string.broadcast_messages);
				
				Toast.makeText(getApplicationContext(), getResources().getString(R.string.mensaje_enviado , userName ) , Toast.LENGTH_LONG).show();				
			}
			else{
				
				if ( result == null ){
					Toast.makeText(getApplicationContext(), R.string.error_de_conexion, Toast.LENGTH_SHORT).show();
				}
				else{
					Toast.makeText(getApplicationContext(), R.string.error_desconocido, Toast.LENGTH_SHORT).show();
					Log.e("Error sending message","result: "+result);
				}
				
			}
		}
	}

    /* Descarga el modelo si no esta ya descargado */
    
    class MDownloader extends ModelDownloader{
    	RelativeLayout loader;
		public MDownloader(Activity act, int modelId) {
			super(act, modelId);
			loader = (RelativeLayout)findViewById(R.id.loaderModel);
			loader.setVisibility(View.VISIBLE);
		}

		@Override
		protected void onPostExecute(Boolean success) {
			loader.setVisibility(View.GONE);
			if ( success ){
				
				init();
			}
			else{
				Toast.makeText(getApplicationContext(), "ERROR EN MODEL DOWNLOADER", Toast.LENGTH_SHORT).show();
				finish();
			}
			
		}
    	
    }
    
	
}
