package com.woopitapp.activities;

import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLU;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.woopitapp.R;
import com.woopitapp.WoopitActivity;
import com.woopitapp.graphics.Objeto;
import com.woopitapp.server_connections.ModelDownloader;

public class MessageActivity extends WoopitActivity {
	
	public final static String EXTRA_MESSAGE = "com.woopitapp.MESSAGE";
	
	public SensorManager sensorMan;
	GLSurfaceView glView;
	CameraView cameraView;
	float x = 0.0f;
	TextView messageText;
	float c = 0.1f;
	double angulo = 0;
	float distanciaZ = -10.0f;
	String latitud = "";
	String longitud = "";
	String text;
	String acelerometro = "X";
	private FloatBuffer mTextureBuffer;
	static final float ALPHA = 0.1f;
	private int mTextureId = -1;
	Bitmap bitmap;
	int indice = 0;
	GLClearRenderer render;
	Objeto corazon;
	int modelo;
	boolean sensorOk = false;
	boolean sensorDistancia = false;
	boolean notif = false;
	LocationChangeListener location_listener;
	
	String vertexShaderSource = "attribute vec4 vPosition; \n"
			+	"void main () \n"
			+	"{ \n"
			+ 	" gl_Position = Matrix*vPosition; \n"
			+	"} \n";
	String nombre = "";
	//int shader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
	CustomCameraView cv;
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        Bundle extras = getIntent().getExtras();
                
        latitud = extras.getString("latitud");
        longitud = extras.getString("longitud");
        modelo = extras.getInt("modelo");
        text = extras.getString("text");
        nombre = extras.getString("nombre");
        
        Log.e("latitud","lat " + latitud);
        new MDownloader(this,modelo).execute();
		//crearCamara();
        
    }
	
	public void onDestroy(){
		super.onDestroy();
		
        LocationManager locMan = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        
        if ( location_listener != null ){
        	locMan.removeUpdates(location_listener);
        }
	}
	
	public void onStop () {
	
		super.onStop();
 		sensorMan.unregisterListener(listener);
 		sensorMan.unregisterListener(listener2);
 		render = null;
 		glView.destroyDrawingCache();
 		glView = null;
 		sensorOk = false;
 		sensorDistancia =false;
 		corazon.liberarMemoria();
 		
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
		        	azimut =((int) Math.round(Math.toDegrees(orientation[0])));
		        	vertical =(int) Math.round(Math.toDegrees(orientation[1]));
		        }
    		
    		}
    		if(azimut < 0){
    			azimut = 360+azimut;
    		}
    		
    		int offset =  (int) (azimut - angulo);
    		if(vertical > -35 && vertical < 40 && offset > -30 && offset< 30 && messageText != null){
    			messageText.setVisibility(View.VISIBLE);
    		}else{
    			if(messageText != null){
    				messageText.setVisibility(View.GONE);
    			}
    		}
    		if(render != null){
	    		render.rotarMundoX(offset);
	    		render.rotarMundoY(vertical);
    		}
	  		    sensorOk = true;
    		

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
 			
 			angulo = getBearing(location.getLatitude(), location.getLongitude(),Double.parseDouble(latitud),Double.parseDouble(longitud));
 			double distancia = getDistance(Double.parseDouble(latitud),Double.parseDouble(longitud),location.getLatitude(), location.getLongitude());
 			distancia = convertir(distancia);
 			
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
          location_listener = new LocationChangeListener(this.getContext());
          locMan.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 4000, 0, location_listener);
          locMan.requestLocationUpdates(LocationManager.GPS_PROVIDER, 4000, 0, location_listener);
          previewHolder = this.getHolder();
          previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
          previewHolder.addCallback(surfaceHolderListener);
        }
      
    }
    
    public class CameraView extends SurfaceView implements Callback {
        
    	Camera camera;
         
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
            double latitudVal = Double.parseDouble(latitud);
            
            gl.glRotatef(rotationX, 0,1, 0);
        	gl.glRotatef(rotationY,1 ,0, 0);
            
            
            //gl.glRotatef(rotationX, 0,1, 0);
            //gl.glRotatef(rotationY,1 ,0, 0);
            
	        gl.glTranslatef(0.0f, -1.5f, -15.0f);
	        gl.glRotatef(mCubeRotation, 0, 1, 0);
	        //corazon.draw(gl);
	        
        	//Log.e("PASO", "Modeloooo");
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
    
    public double getBearing(double startLat, double startLng, double endLat, double endLng){
        double longitude1 = startLng;
        double longitude2 = endLng;
        double latitude1 = Math.toRadians(startLat);
        double latitude2 = Math.toRadians(endLat);
        double longDiff= Math.toRadians(longitude2-longitude1);
        double y= Math.sin(longDiff)*Math.cos(latitude2);
        double x=Math.cos(latitude1)*Math.sin(latitude2)-Math.sin(latitude1)*Math.cos(latitude2)*Math.cos(longDiff);

        return (Math.toDegrees(Math.atan2(y, x))+360)%360;


    }
   /* private double getBearing(double lat1,double lon1, double lat2,double lon2){
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
    }*/
 
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

       acelerometro = "Z";
       getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    	
    	if(isNumeric(latitud) && isNumeric(longitud)){
    		try{
    			//setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE );
    			//requestWindowFeature( Window.FEATURE_NO_TITLE );
    			//getWindow().setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN,
    			//WindowManager.LayoutParams.FLAG_FULLSCREEN );
    			    			
    			cv = new CustomCameraView(this);
    			//setContentView(rl);
    			//rl.addView(cv);
    			//cameraView = new CameraView( this );
    			
    			glView = new GLSurfaceView( this );
    			glView.setEGLConfigChooser( 8, 8, 8, 8, 16, 0 );
    			glView.getHolder().setFormat( PixelFormat.TRANSLUCENT );
    			render = new GLClearRenderer();
    			glView.setRenderer(render);
    			glView.setZOrderOnTop(true);
    			corazon =  new Objeto(modelo+".jet",getApplicationContext());
    			
    			if(Double.parseDouble(latitud) == 500.0){
    				LinearLayout camera_layout = (LinearLayout) findViewById(R.id.camera_layout);
    				LinearLayout model_layout = (LinearLayout) findViewById(R.id.model_layout);
    				messageText = (TextView) findViewById(R.id.text);
    				
    				camera_layout.addView(cv,new LayoutParams( LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT ));
    				model_layout.addView(glView,new LayoutParams( LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT ));
    				
    				messageText.setVisibility(View.GONE);
    				if ( text.length() > 0 ){
    					messageText.setText(text);
    					
    				}
    				
    				((RelativeLayout)findViewById(R.id.loading_layout)).setVisibility(View.GONE);
    			}
    			
    		}
    		catch(Exception e){
    			e.printStackTrace();
    		}
    	}
    }
    
    public void recopilar(int num) {
    	//TextView editText = (TextView) findViewById(R.id.texto);
    	//editText.setText(editText.getText() + "" + num);
    }
    
    /* Descarga el modelo si no esta ya descargado */
    
    class MDownloader extends ModelDownloader{

		public MDownloader(Activity act, int modelId) {
			super(act, modelId);
		}

		@Override
		protected void onPostExecute(Boolean success) {
			
			if ( success ){

				crearCamara();
			}
			else{
				Toast.makeText(getApplicationContext(), "ERROR EN MODEL DOWNLOADER", Toast.LENGTH_SHORT).show();
			}
			
		}
    	
    }
    
    public class LocationChangeListener  implements LocationListener{


    	private LocationManager mLocationManager = null;
    	private static final int TWO_MINUTES = 1000 * 60 * 2;
    	private Location currentBestLocation = null;

    	public LocationChangeListener(Context context) {
    	    super();
    	}

    	public void impl(int refreshPeriod, int accuracyInMeters){



    	    // Get the location manager
    	    mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

    	    currentBestLocation = getLastBestLocation();

    	    boolean enabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

    	    // Check if enabled and if not send user to the GSP settings
    	    // Better solution would be to display a dialog and suggesting to 
    	    // go to the settings
    	    if (!enabled) {
    	    
    	    } 


    	    Criteria criteria = new Criteria();
    	    //criteria.setAccuracy(Criteria.ACCURACY_FINE);
    	    //criteria.setCostAllowed(false);

    	    String provider = mLocationManager.getBestProvider(criteria, false);
    	    Location location = mLocationManager.getLastKnownLocation(provider);

    	    LocationListener listener = this;       
    	    mLocationManager.requestLocationUpdates(provider, refreshPeriod, accuracyInMeters, listener);
    	    //mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, listener);
    	    //mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, listener);

    	    this.onLocationChanged(location);

    	    // Initialize the location fields
    	    if (location == null) {
    	        //Location not available

    	        mLocationManager.removeUpdates(this);
    	    }

    	}

    	/**
    	 * This method returns the last know location, between the GPS and the Network one.
    	 * For this method newer is best :)
    	 * 
    	 * @return the last know best location
    	 */
    	private Location getLastBestLocation() {
    	    Location locationGPS = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
    	    Location locationNet = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

    	    long GPSLocationTime = 0;
    	    if (null != locationGPS) { GPSLocationTime = locationGPS.getTime(); }

    	    long NetLocationTime = 0;

    	    if (null != locationNet) {
    	        NetLocationTime = locationNet.getTime();
    	    }

    	    if ( 0 < GPSLocationTime - NetLocationTime ) {
    	        return locationGPS;
    	    }
    	    else{
    	        return locationNet;
    	    }

    	}

    	/**
    	 * This method modify the last know good location according to the arguments.
    	 * 
    	 * @param location      the possible new location
    	 */
    	void makeUseOfNewLocation(Location location) {
    	    if ( isBetterLocation(location, currentBestLocation) ) {
    	        currentBestLocation = location;
    	    }
    	}
    	private double convertir(double i){
    		return i*(-10.0)/0.02;
    	}
    	public void onLocationChanged(Location location) {

			((RelativeLayout)findViewById(R.id.loading_layout)).setVisibility(View.GONE);
    	    makeUseOfNewLocation(location);

    	    if(currentBestLocation == null){
    	        currentBestLocation = location;
    	    }
	
 			
 			angulo = getBearing(location.getLatitude(), location.getLongitude(),Double.parseDouble(latitud),Double.parseDouble(longitud));
 			double distancia = getDistance(Double.parseDouble(latitud),Double.parseDouble(longitud),location.getLatitude(), location.getLongitude());
 			//distancia = convertir(distancia);
    		Log.e("pos","lat lalon1: " +latitud + " " + longitud );

 			//direccion.setText(distancia + "  lalon1: " +latitud + " " + longitud + " lalon2: " +  location.getLatitude() + " " + location.getLongitude());
 			if(render != null && Double.parseDouble(latitud) < 500.0){
 				render.transladarMundo(((float) (distancia+0.190)*-100.0f));
 			}
 			if(!sensorDistancia && render != null &&  Double.parseDouble(latitud) <  500.0){
    			
 				Log.e("paso", "mapactivity");

	 			 if(distancia*1000 <50){
	 				
	    				LinearLayout camera_layout = (LinearLayout) findViewById(R.id.camera_layout);
		 			

	    				LinearLayout model_layout = (LinearLayout) findViewById(R.id.model_layout);
		 			

	    				messageText = (TextView) findViewById(R.id.text);
		 				Log.e("paso", "mapactivity3");

	    				camera_layout.addView(cv,new LayoutParams( LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT ));
	    				model_layout.addView(glView,new LayoutParams( LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT ));

	    				if ( text.length() > 0 ){
	    					messageText.setText(text);
	    				}
	    				else{
	    					messageText.setVisibility(View.GONE);
	    				}
				 }else{
					 
					 if ( cameraView != null && cameraView.camera != null ){
						 cameraView.camera.stopPreview();
						 cameraView.camera.release();
					 }
					 
					 Intent iResult = new Intent();
					 iResult.putExtra("latitud", Double.parseDouble(latitud));
					 iResult.putExtra("longitud", Double.parseDouble(longitud));
					 iResult.putExtra("nombre", nombre);
					 setResult(RESULT_OK,iResult);
					 finish();
				 }
 			}
 			 sensorDistancia = true;
    	}

    	public void onStatusChanged(String provider, int status, Bundle extras) {

    	}

    	public void onProviderEnabled(String provider) {


    	}

    	public void onProviderDisabled(String provider) {


    	}   

    	/** Determines whether one Location reading is better than the current Location fix
    	 * @param location  The new Location that you want to evaluate
    	 * @param currentBestLocation  The current Location fix, to which you want to compare the new one
    	 */
    	protected boolean isBetterLocation(Location location, Location currentBestLocation) {
    	    if (currentBestLocation == null) {
    	        // A new location is always better than no location
    	        return true;
    	    }

    	    // Check whether the new location fix is newer or older
    	    long timeDelta = location.getTime() - currentBestLocation.getTime();
    	    boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
    	    boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
    	    boolean isNewer = timeDelta > 0;

    	    // If it's been more than two minutes since the current location, use the new location
    	    // because the user has likely moved
    	    if (isSignificantlyNewer) {
    	        return true;
    	        // If the new location is more than two minutes older, it must be worse
    	    } else if (isSignificantlyOlder) {
    	        return false;
    	    }

    	    // Check whether the new location fix is more or less accurate
    	    int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
    	    boolean isLessAccurate = accuracyDelta > 0;
    	    boolean isMoreAccurate = accuracyDelta < 0;
    	    boolean isSignificantlyLessAccurate = accuracyDelta > 200;

    	    // Check if the old and new location are from the same provider
    	    boolean isFromSameProvider = isSameProvider(location.getProvider(),
    	            currentBestLocation.getProvider());

    	    // Determine location quality using a combination of timeliness and accuracy
    	    if (isMoreAccurate) {
    	        return true;
    	    } else if (isNewer && !isLessAccurate) {
    	        return true;
    	    } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
    	        return true;
    	    }
    	    return false;
    	}

    	/** Checks whether two providers are the same */
    	private boolean isSameProvider(String provider1, String provider2) {
    	    if (provider1 == null) {
    	        return provider2 == null;
    	    }
    	    return provider1.equals(provider2);
    	}



    	public void destroyManager() {

    	    if(mLocationManager != null){
    	        mLocationManager.removeUpdates(this);

    	        mLocationManager = null;
    	    }       
    	}
    	}
}
