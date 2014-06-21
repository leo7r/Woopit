package com.woopitapp.activities;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.ShortBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.StringTokenizer;
import java.util.Vector;
import org.lwjgl.opengl.GL11;

import com.woopitapp.R;
import com.woopitapp.R.drawable;
import com.woopitapp.R.id;
import com.woopitapp.R.layout;
import com.woopitapp.graphics.Objeto;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
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
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLU;
import android.opengl.GLUtils;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
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
import android.widget.RemoteViews;
import android.widget.TextView;

public class MessageActivity extends Activity {
	
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
	static final float ALPHA = 0.1f;
	private int mTextureId = -1;
	Bitmap bitmap;
	int indice = 0;
	GLClearRenderer render;
	Objeto corazon;
	boolean notif = false;
	String vertexShaderSource = "attribute vec4 vPosition; \n"
			+	"void main () \n"
			+	"{ \n"
			+ 	" gl_Position = Matrix*vPosition; \n"
			+	"} \n";
	//int shader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
       

		crearCamara();
	
    }
	
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
    		float vertical = 0.0f;
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
		        	vertical =(int) Math.round(Math.toDegrees(orientation[1]));

		        }
    		
    		}
    		
    		int offset =  (int) (azimut + angulo);
    	
    		// direccion.setText(" azimut: "+(azimut) + " angulo: " + angulo );
    		render.rotarMundoX(offset);
    		render.rotarMundoY(vertical);
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
    public int loadShader(int shaderType, String source){
    	int shader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
    	if(shader != 0){
		    GLES20.glShaderSource(shader,source);
		    GLES20.glCompileShader(shader);
		    int[] compiled = new int[1];
		    GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
		    if(compiled[0] == 0){
		    	Log.e("ShaderLoader","Could not compile shader " + shaderType+": ");
		    	Log.e("ShaderLoader", GLES20.glGetShaderInfoLog(shader));
		    	GLES20.glDeleteShader(shader);
		    	shader = 0;
		    }
    	}
    	return shader;
    }
    /* Renderer */
    public class GLClearRenderer implements Renderer {
    	
    	private float mCubeRotation;
		private float rotationX = 0.0f;
		private float rotationY = 0.0f;
		int[] textures = new int[1];
		private float desplazamientoZ = -13.0f;
		
		public void onDrawFrame( GL10 gl ) {
			
            gl.glLoadIdentity();
			c -= 1.0f;
                  
			gl.glClearColor( 0.1f,0.1f,0.1f, 0.1f );
	        gl.glClearDepthf(1.0f);
	        //gl.glDepthMask(true);
	        gl.glEnable(GL10.GL_DEPTH_TEST);
	        gl.glDepthFunc(GL10.GL_LESS);
            gl.glClear( GL10.GL_COLOR_BUFFER_BIT );
            gl.glClear(GL10.GL_DEPTH_BUFFER_BIT);
            

	  
            gl.glPushMatrix();  
            gl.glLoadIdentity();
	   //     gl.glRotatef(rotationX, 0,1, 0);
	    //    gl.glRotatef(rotationY,1 ,0, 0);
            
	        gl.glTranslatef(0.0f, -1.5f, desplazamientoZ);
	        gl.glRotatef(mCubeRotation, 0, 1, 0);
	   //     corazon.draw(gl);
	        corazon.draw(gl);
            mCubeRotation -= 0.70f;
            gl.glPopMatrix();   
           
            
		}

		public void transladarMundo(float z){
			desplazamientoZ = z;
		}
		
		public void rotarMundoX(float x){
			rotationX = (x);
		}
		
		public void rotarMundoY(float y){
			rotationY = (y);
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
    			
    			direccion = new TextView(this);
    			direccion.setTextColor(Color.RED);
    			
    			
    			glView = new GLSurfaceView( this );
    			glView.setEGLConfigChooser( 8, 8, 8, 8, 16, 0 );
    			glView.getHolder().setFormat( PixelFormat.TRANSLUCENT );
    			render = new GLClearRenderer();
    			glView.setRenderer(render);
    			
    			corazon =  new Objeto("objetos/peluche.jet",getApplicationContext());
    			CustomCameraView cv = new CustomCameraView(this);
    			//setContentView(rl);
    			//rl.addView(cv);
    			//cameraView = new CameraView( this );
    			
    			setContentView(  cv);
    			addContentView( glView, new LayoutParams( LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT ) );

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
    
    /* Video notificacion */
    public void setNotification(){
    	
    	Notification notification = new Notification(R.drawable.notif_icon, null, System.currentTimeMillis());
		RemoteViews notificationView = new RemoteViews(getPackageName(), R.layout.notification);
	    
    	
	    Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
	    PendingIntent pendingNotificationIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, PendingIntent.FLAG_ONE_SHOT);

	    notification.contentView = notificationView;
	    notification.contentIntent = pendingNotificationIntent;
	    
	    NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    	mNotificationManager.notify(1, notification);
    }

}
