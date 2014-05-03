package com.woopitapp;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLUtils;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

public class MainActivity extends Activity {
	
	public final static String EXTRA_MESSAGE = "com.woopitapp.MESSAGE";
	
	public SensorManager sensorMan;
	GLSurfaceView glView;
	CameraView cameraView;
	float x = 0.0f;
	TextView direccion;
	float c = 0.1f;
	double angulo = 0;
	float distanciaZ = -10.0f;
	String latitud = "";
	String longitud = "";
	String acelerometro = "X";
	private FloatBuffer mTextureBuffer;
	static final float ALPHA = 0.15f;
	private int mTextureId = -1;
	Bitmap bitmap;
	int indice = 0;
	GLClearRenderer render;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_main);
        bitmap = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.box_texture2);
        Button bSuma = (Button) findViewById(R.id.botonSuma);
        bSuma.setOnClickListener(new OnClickListener(){
        	@Override
			public void onClick(View arg0) {
				render = new GLClearRenderer();
				crearCamara();
			}
        });
     
    }
	
	/* Imagen en 3D */
	
	class Cube {
        
	    private FloatBuffer mVertexBuffer;
	    private FloatBuffer mColorBuffer;
	    private ByteBuffer  mIndexBuffer;
	    	
	/*	private float vertices[] = {
			-1.0f,  1.0f, 0.0f,		// 0, Top Left
			-1.0f, -1.0f, 0.0f,		// 1, Bottom Left
			1.0f, -1.0f, 0.0f,		// 2, Bottom Right
			1.0f,  1.0f, 0.0f,		// 3, Top Right
		};
			
	    private float colors[] = {
			0.0f,  1.0f,  0.0f,  1.0f,
			0.0f,  1.0f,  0.0f,  1.0f,
			1.0f,  0.5f,  0.0f,  1.0f,
			1.0f,  0.5f,  0.0f,  1.0f,
			1.0f,  0.0f,  0.0f,  1.0f,
			1.0f,  0.0f,  0.0f,  1.0f,
			0.0f,  0.0f,  1.0f,  1.0f,
			1.0f,  0.0f,  1.0f,  1.0f
		};
	   
	    private byte indices[] = {0, 1, 2, 0, 2, 3};
	    */
	    
	    private float vertices[] = {
			-1.0f,
			-1.0f,
			1.0f, // Vertex 0
			1.0f,
			-1.0f,
			1.0f, // v1
			-1.0f,
			1.0f,
			1.0f, // v2
			1.0f,
			1.0f,
			1.0f, // v3
			
			1.0f,
			-1.0f,
			1.0f, // ...
			1.0f, -1.0f, -1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, -1.0f,
			
			1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, 1.0f, -1.0f, -1.0f,
			1.0f, -1.0f,
			
			-1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, 1.0f, -1.0f, -1.0f,
			1.0f, 1.0f,
			
			-1.0f, -1.0f, -1.0f, 1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, 1.0f,
			-1.0f, 1.0f,
			
			-1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, -1.0f, 1.0f, -1.0f, 1.0f,
			1.0f, -1.0f,
	    	};
	    
	    private float colors[] = {
			0.0f,  1.0f,  0.0f,  1.0f,
			0.0f,  1.0f,  0.0f,  1.0f,
			1.0f,  0.5f,  0.0f,  1.0f,
			1.0f,  0.5f,  0.0f,  1.0f,
			1.0f,  0.0f,  0.0f,  1.0f,
			1.0f,  0.0f,  0.0f,  1.0f,
			0.0f,  0.0f,  1.0f,  1.0f,
			1.0f,  0.0f,  1.0f,  1.0f
	    };
	    
	    private byte indices[] = {
			0, 1, 3, 0, 3,
			2, // Face front
			4, 5, 7, 4, 7,
			6, // Face right
			8, 9, 11, 8, 11,
			10, // ...
			12, 13, 15, 12, 15, 14, 16, 17, 19, 16, 19, 18, 20, 21, 23, 20, 23,
			22
	    };
	    
	    float textureCoordinates[] = {  
			0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f,
			
			0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f,
			
			0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f,
			
			0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f,
			
			0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f,
			
			0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f
		};

	    public Cube() {
	    	
			ByteBuffer byteBuf = ByteBuffer.allocateDirect(vertices.length * 4);
			byteBuf.order(ByteOrder.nativeOrder());
			mVertexBuffer = byteBuf.asFloatBuffer();
			mVertexBuffer.put(vertices);
			mVertexBuffer.position(0);
			    
			/*
			byteBuf = ByteBuffer.allocateDirect(colors.length * 4);
			byteBuf.order(ByteOrder.nativeOrder());
			mColorBuffer = byteBuf.asFloatBuffer();
			mColorBuffer.put(colors);
			mColorBuffer.position(0);
			*/
			
			//GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
			byteBuf = ByteBuffer.allocateDirect(textureCoordinates.length * 4);
			byteBuf.order(ByteOrder.nativeOrder());
			mTextureBuffer = byteBuf.asFloatBuffer();
			mTextureBuffer.put(textureCoordinates);
			mTextureBuffer.position(0);
			
			mIndexBuffer = ByteBuffer.allocateDirect(indices.length);
			mIndexBuffer.put(indices);
			mIndexBuffer.position(0);
	    }

	    public void draw(GL10 gl) {        
	    	
            //--------------------------------------------
	    	gl.glEnable(GL10.GL_TEXTURE_2D);
            // Tell OpenGL where our texture is located.
            // Tell OpenGL to enable the use of UV coordinates.
            gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
            // Telling OpenGL where our UV coordinates are.
            ///----------------------------------------------
	    	gl.glBindTexture(GL10.GL_TEXTURE_2D, mTextureId);

	        gl.glFrontFace(GL10.GL_CW);
	            
	        // gl.glColorPointer(4, GL10.GL_FLOAT, 0, mColorBuffer);
	            
	        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
	        // gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
	         
	        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mVertexBuffer);
   	        gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, mTextureBuffer);

	        gl.glDrawElements(GL10.GL_TRIANGLES, indices.length, GL10.GL_UNSIGNED_BYTE, mIndexBuffer);
	        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
	        // gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
	        // Disable the use of UV coordinates.
	        gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
	        // Disable the use of textures.
	        gl.glDisable(GL10.GL_TEXTURE_2D);
	    }
	    
	}
	
	/* Listeners */
	SensorEventListener listener = new SensorEventListener(){
		 
 	   public void onAccuracyChanged(Sensor arg0, int a){
 		   
 	   }

 	   public void onSensorChanged(SensorEvent evt){
 		   
 		   float vals[] = evt.values;
 		   float direction = vals[0];
 	   }
 	};
	
 	private SensorEventListener listener2 = new SensorEventListener(){
  	  
    	float[] mGravity;
    	float[] mGeomagnetic;
    	public  volatile float kFilteringFactor = (float) 0.05;
    	public  float aboveOrBelow = (float )0;
    	
    	public void onAccuracyChanged(Sensor arg0, int arg1){}
    	
    	public void onSensorChanged(SensorEvent evt){
    		
    		float azimut = 0.0f;
    		
    		if (evt.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
    			mGravity =  lowPass(evt.values.clone(), mGravity);
    		}
    		    
    		if (evt.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){
    			mGeomagnetic = lowPass(evt.values, mGeomagnetic);
    		}


    		if (mGravity != null && mGeomagnetic != null) {

		    	float R[] = new float[9];
		        float I[] = new float[9];
		        float R2[] = new float[9];
		        boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
		        
		       /* 
		        if(acelerometro.equalsIgnoreCase("Y")){
		        	SensorManager.remapCoordinateSystem(R, SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_X, R2);
		        }else{
	
		        	SensorManager.remapCoordinateSystem(R, SensorManager.AXIS_Z, SensorManager.AXIS_MINUS_X, R2);
		        }*/
		        
		        SensorManager.remapCoordinateSystem(R, SensorManager.AXIS_Z, SensorManager.AXIS_MINUS_X, R2);
			      
		        if (success) {
		        	float orientation[] = new float[3];
		        	SensorManager.getOrientation(R2, orientation);
		        	azimut =(int) Math.round(Math.toDegrees(orientation[0]));
		        }
    		
    		}
    		
    		int offset =  (int) (azimut + angulo);
    		// direccion.setText(" azimut: "+(azimut) + " angulo: " + angulo );
    		render.rotarMundo(offset);
    		// Set the Icon for the Dialog
    	}

    };

 	LocationListener gpsListener = new LocationListener(){
 		
 		Location curLocation;
 		boolean locationChanged = false; 
 
 		public void onLocationChanged(Location location){
 			
 			if(curLocation == null){
 				curLocation = location;
 				locationChanged = true;
 			}
 			
 			if( curLocation.getLatitude() == location.getLatitude() && curLocation.getLongitude() == location.getLongitude() ){
 				locationChanged = false;
 			}
 			else{
 				locationChanged = true;
 			}
 			
 			angulo = getBearing(Double.parseDouble(latitud),Double.parseDouble(longitud),location.getLatitude(), location.getLongitude());
 			double distancia = getDistance(Double.parseDouble(latitud),Double.parseDouble(longitud),location.getLatitude(), location.getLongitude());
 			distancia = convertir(distancia);
 			//direccion.setText(distancia+"");
 			
 			render.transladarMundo((float) distancia);
 			curLocation = location;
    	         
 		}
 		
 		public void onProviderDisabled(String provider){}
 		
 		public void onProviderEnabled(String provider){}
 		
 		public void onStatusChanged(String provider, int status, Bundle extras){}
 		
 		private double convertir(double i){
    		return i*(-10.0)/0.02;
    	}
    	
 		private double getLast(double i){
    		
    		String num = String.valueOf(i);
    		return Double.parseDouble(num.substring(num.length()-5,num.length()-1)+"");
    	}
 		
    	}; 
    
    /* Camara Views */
    
    public class CustomCameraView extends SurfaceView{
    	
    	Camera camera;
        SurfaceHolder previewHolder;
        SurfaceHolder.Callback surfaceHolderListener = new SurfaceHolder.Callback() {
        	
        	public void surfaceCreated(SurfaceHolder holder) {
        		camera = Camera.open();
        		
        		try {
        			camera.setPreviewDisplay(previewHolder);
        		}
   	            catch (Exception e ){
   	            	e.printStackTrace();
   	            }
        	}
   	   
        	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height){
        		Parameters params = camera.getParameters();
        		//params.setPreviewSize(width, height);
				//params.setPictureFormat(PixelFormat.JPEG);
	      
        		params.setRotation(90);

        		camera.setParameters(params);
        		camera.setDisplayOrientation(90);
        		// direccion.setText(camera.getParameters().get)
        		camera.startPreview();
        	}

        	public void surfaceDestroyed(SurfaceHolder arg0){
        		camera.stopPreview();
        		camera.release();   
        	}
        };

        public CustomCameraView(Context ctx){
    	   
          super(ctx);
          sensorMan = (SensorManager)getSystemService(Context.SENSOR_SERVICE);

          sensorMan.registerListener(
        	       listener2, 
        	       sensorMan.getDefaultSensor(
        	          Sensor.TYPE_ACCELEROMETER), 
        	       SensorManager.SENSOR_DELAY_GAME);
          
          sensorMan.registerListener(
       	       listener2, 
       	       sensorMan.getDefaultSensor(
       	          Sensor.TYPE_MAGNETIC_FIELD), 
       	       SensorManager.SENSOR_DELAY_GAME);
          
          LocationManager locMan;
          locMan = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
          locMan.requestLocationUpdates(LocationManager.GPS_PROVIDER,100, 1, gpsListener);
          previewHolder = this.getHolder();
          previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
          previewHolder.addCallback(surfaceHolderListener);
        }
      
    }
    
    public class CameraView extends SurfaceView implements Callback {
        
    	private Camera camera;
         
        public CameraView( Context context ) {
            super( context );
            
            // We're implementing the Callback interface and want to get notified
            // about certain surface events.
            getHolder().addCallback( (Callback) this );
            
            // We're changing the surface to a PUSH surface, meaning we're receiving
            // all buffer data from another component - the camera, in this case.
            getHolder().setType( SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS );
        }
         
        public void surfaceCreated( SurfaceHolder holder ) {
            
        	camera = Camera.open();
            Camera.Parameters p = camera.getParameters();    	        
            camera.setParameters(p);
        }
     
        public void surfaceChanged( SurfaceHolder holder, int format, int width, int height ) {
    
            Camera.Parameters p = camera.getParameters();
            p.setPreviewSize( width, height );
    	    p.setRotation(90);
            camera.setParameters( p );
            camera.setDisplayOrientation(90);
            camera.startPreview();
        }
     
        public void surfaceDestroyed( SurfaceHolder holder ) {
        	
            // Once the surface gets destroyed, we stop the preview mode and release
            // the whole camera since we no longer need it.
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }
    
    /* Renderer */
    public class GLClearRenderer implements Renderer {
    	
    	private float mCubeRotation;
		private Cube mCube = new Cube();
		private float rotation = 0.0f;
		int[] textures = new int[1];
		private float desplazamientoZ = -10.0f;
		
		public void onDrawFrame( GL10 gl ) {
			
			c -= 1.0f;
			
			gl.glClearColor( 0.1f,0.1f,0.1f, 0.1f );
	        gl.glClearDepthf(1.0f);
	        //gl.glDepthMask(true);
	        gl.glEnable(GL10.GL_DEPTH_TEST);
	        gl.glDepthFunc(GL10.GL_LEQUAL);
            gl.glClear( GL10.GL_COLOR_BUFFER_BIT );
            gl.glClear(GL10.GL_DEPTH_BUFFER_BIT);
	        gl.glEnable(GL10.GL_TEXTURE_2D);
	        gl.glPushMatrix();
	        
	        if(indice == 0){
	        	gl.glGenTextures(1, textures, 0);
    			gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);
    			mTextureId = textures[0];
    	        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
    	        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
    	        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
    	        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
    			GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);

    			indice++;
	        }
	        
	        gl.glRotatef(rotation, 0,1, 0);
	        gl.glTranslatef(0.0f, 0.0f, desplazamientoZ);
	        gl.glRotatef(mCubeRotation, 1.0f, 1.0f, 1.0f);
            mCube.draw(gl);
            
            gl.glPopMatrix();
            mCubeRotation -= 0.70f;
            
		}

		public void transladarMundo(float z){
			desplazamientoZ = z;
		}
		
		public void rotarMundo(float x){
			rotation = (x);
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
    		}

		public void onSurfaceCreated( GL10 gl, EGLConfig config ) {}
    
    }
    
    /* Auxiliares */
    
    private double getDistance( double lat1 , double lon1 , double lat2 , double lon2){
		
    	int R = 6371; // km
		double dLat =  Math.toRadians(lat2-lat1);
		double dLon = Math.toRadians(lon2-lon1);
		lat1 = Math.toRadians(lat1);
		lat2 = Math.toRadians(lat2);
		
		double a =  (Math.sin(dLat/2) * Math.sin(dLat/2) +
		        Math.sin(dLon/2) * Math.sin(dLon/2) * Math.cos(lat1) * Math.cos(lat2)); 
		double c =  (2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a))); 
		double d = R * c;
		
		return d;
    }
    
    private double getBearing(double lat1,double lon1, double lat2,double lon2){
    	int R = 6371; // km
    	double dLat =  Math.toRadians(lat2-lat1);
    	double dLon = Math.toRadians(lon2-lon1);
    	lat1 = Math.toRadians(lat1);
    	lat2 = Math.toRadians(lat2);

    	double a =  (Math.sin(dLat/2) * Math.sin(dLat/2) +
    	        Math.sin(dLon/2) * Math.sin(dLon/2) * Math.cos(lat1) * Math.cos(lat2)); 
    	double c =  (2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a))); 
    	double d = R * c;
        double y = Math.sin(dLon) * Math.cos(lat2);
        double x = Math.cos(lat1)*Math.sin(lat2) -
                Math.sin(lat1)*Math.cos(lat2)*Math.cos(dLon);
        double brng = Math.toDegrees(Math.atan2(y, x));
    	return brng;
    }
    
    protected float[] lowPass(float[] input, float[] output){
        
    	if (output == null)
    		return input;

        for (int i = 0; i < input.length; i++){
            output[i] = output[i] + ALPHA * (input[i] - output[i]);
        }
        
        return output;
    }
      	
    public boolean isNumeric(String str){  
    	  
    	try{
    		  double d = Double.parseDouble(str);  
    	  }  
    	  catch(NumberFormatException nfe){  
    	    return false;  
    	  } 
    	  
    	  return true;  
    }
    
    public void crearCamara(){
        
    	RadioGroup rdgGrupo = (RadioGroup)findViewById(R.id.rdgGrupo);   
        
        if(rdgGrupo.getCheckedRadioButtonId() == R.id.rdbOne){
        	acelerometro = "Z";
        }
        else{
        	acelerometro = "Y";
        }
        
    	getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    	latitud = ((EditText)findViewById(R.id.latitud)).getText() +"";
    	longitud = ((EditText)findViewById(R.id.longitud)).getText()+"";
    	
    	if(isNumeric(latitud) && isNumeric(longitud)){
    		try{
    			//setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE );
    			//requestWindowFeature( Window.FEATURE_NO_TITLE );
    			//getWindow().setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN,
    			//WindowManager.LayoutParams.FLAG_FULLSCREEN );
   
    			glView = new GLSurfaceView( this );
    			glView.setEGLConfigChooser( 8, 8, 8, 8, 16, 0 );
    			glView.getHolder().setFormat( PixelFormat.TRANSLUCENT );
    			glView.setRenderer(render);
    			direccion = new TextView(glView.getContext());
    			direccion.setTextColor(Color.RED);
    			setContentView( glView );
    			
    			CustomCameraView cv = new CustomCameraView(this);
    			//setContentView(rl);
    			//rl.addView(cv);
    			//cameraView = new CameraView( this );
    			
    			addContentView( cv, new LayoutParams( LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT ) );
    			addContentView(direccion,new LayoutParams( LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT ));
      
    		}
    		catch(Exception e){
    			e.printStackTrace();
    		}
    	}
    }
    
    public void recopilar(int num) {
    	TextView editText = (TextView) findViewById(R.id.texto);
    	editText.setText(editText.getText() + "" + num);
    }
    
}
