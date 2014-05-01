package com.example.prueba;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
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
import android.os.Bundle;
import android.view.Menu;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

public class MainActivity extends Activity {
	public final static String EXTRA_MESSAGE = "com.example.prueba.MESSAGE";
	private int cache = 0;
	private int operacion = 0;
	public static SensorManager sensorMan; 
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
	static final float ALPHA = 0.15f;
	GLClearRenderer render = new GLClearRenderer();
	  AlertDialog a;
	   SensorEventListener listener = new SensorEventListener(){
		 
    	   public void onAccuracyChanged(Sensor arg0, int a){}

    	   public void onSensorChanged(SensorEvent evt)
    	   {
    	      float vals[] = evt.values;   
    	      float direction = vals[0];
    	   
    	   }
    	};
    	LocationListener gpsListener = new LocationListener(){
    	      Location curLocation;
    	      boolean locationChanged = false; 
    	      public void onLocationChanged(Location location)
    	      {
    	         if(curLocation == null)
    	         {
    	            curLocation = location;
    	            locationChanged = true;
    	         }
    	         
    	         if(curLocation.getLatitude() == location.getLatitude() &&
    	               curLocation.getLongitude() == location.getLongitude())
    	            locationChanged = false;
    	         else
    	            locationChanged = true;
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

    	}; 
    	private double convertir(double i){
    		
    		return i*(-10.0)/0.02;
    	}
    	private double getLast(double i){
    		
    		String num = String.valueOf(i);
    		return Double.parseDouble(num.substring(num.length()-5,num.length()-1)+"");
    	}
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

  
        setContentView(R.layout.activity_main);

        Button bSuma = (Button) findViewById(R.id.botonSuma);
        bSuma.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				crearCamara();
			}});
        
     
    }

    public class CustomCameraView extends SurfaceView
    {
        Camera camera;
        SurfaceHolder previewHolder;
    	 SurfaceHolder.Callback surfaceHolderListener = new SurfaceHolder.Callback() {
   	      public void surfaceCreated(SurfaceHolder holder) {
   	            camera = Camera.open();
   	     
   	            try {
   	                    camera.setPreviewDisplay(previewHolder);
   	                
   	            }
   	            catch (Throwable e ){ }
   	           }
   	   public void surfaceChanged(SurfaceHolder holder, int format, int width,
   	         int height)
   	   {
   		   
   	      Parameters params = camera.getParameters();
   	     /*   params.setPreviewSize(width, height);
   	        params.setPictureFormat(PixelFormat.JPEG);
   	       -*/   
	      params.setRotation(90);

   	      
   	        camera.setParameters(params);
   	        camera.setDisplayOrientation(90);
   	      //  direccion.setText(camera.getParameters().get)
   	        camera.startPreview();
   	   }

   	   public void surfaceDestroyed(SurfaceHolder arg0)
   	   {
   	      camera.stopPreview();
   	      camera.release();   
   	   }
   	 };

      
       public CustomCameraView(Context ctx)
       {
    	   
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
          	   locMan.requestLocationUpdates(LocationManager.GPS_PROVIDER, 
          	                           100, 1, gpsListener);
          previewHolder = this.getHolder();
                previewHolder.setType 
                  (SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
                previewHolder.addCallback(surfaceHolderListener);
       }
      
    }
    private double getDistance(double lat1,double lon1, double lat2,double lon2){
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
    protected float[] lowPass(float[] input, float[] output)
    {
        if (output == null)
            return input;

        for (int i = 0; i < input.length; i++)
        {
            output[i] = output[i] + ALPHA * (input[i] - output[i]);
        }
        return output;
    }
    private SensorEventListener listener2 = new SensorEventListener(){
    	   float[] mGravity;
    	   float[] mGeomagnetic;
    	   public  volatile float kFilteringFactor = (float)0.05;
    	   public  float aboveOrBelow = (float)0;

    	   public void onAccuracyChanged(Sensor arg0, int arg1){     	 }

    	   public void onSensorChanged(SensorEvent evt)
    	   {
    			
    		   
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
    		        boolean success = SensorManager.getRotationMatrix(R, I, mGravity,
    		                mGeomagnetic);
    		       /* if(acelerometro.equalsIgnoreCase("Y")){
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
//    	    	  	 direccion.setText(" azimut: "+(azimut) + " angulo: " + angulo );
    	    	     render.rotarMundo(offset);
    	      
    	      
    	      // Set the Icon for the Dialog
   	    

    
    	   }
    	};
    	class Cube {
            
    	    private FloatBuffer mVertexBuffer;
    	    private FloatBuffer mColorBuffer;
    	    private ByteBuffer  mIndexBuffer;
    	        
    		private float vertices[] = {
    			      -1.0f,  1.0f, 0.0f,  // 0, Top Left
    			      -1.0f, -1.0f, 0.0f,  // 1, Bottom Left
    			       1.0f, -1.0f, 0.0f,  // 2, Bottom Right
    			       1.0f,  1.0f, 0.0f,  // 3, Top Right
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
    	                              0, 1, 2, 0, 2, 3,
   
    	                              };
    	                
    	    public Cube() {
    	            ByteBuffer byteBuf = ByteBuffer.allocateDirect(vertices.length * 4);
    	            byteBuf.order(ByteOrder.nativeOrder());
    	            mVertexBuffer = byteBuf.asFloatBuffer();
    	            mVertexBuffer.put(vertices);
    	            mVertexBuffer.position(0);
    	                
    	            byteBuf = ByteBuffer.allocateDirect(colors.length * 4);
    	            byteBuf.order(ByteOrder.nativeOrder());
    	            mColorBuffer = byteBuf.asFloatBuffer();
    	            mColorBuffer.put(colors);
    	            mColorBuffer.position(0);
    	                
    	            mIndexBuffer = ByteBuffer.allocateDirect(indices.length);
    	            mIndexBuffer.put(indices);
    	            mIndexBuffer.position(0);
    	    }

    	    public void draw(GL10 gl) {             
    	            gl.glFrontFace(GL10.GL_CW);
    	            
    	            gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mVertexBuffer);
    	            gl.glColorPointer(4, GL10.GL_FLOAT, 0, mColorBuffer);
    	            
    	            gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
    	            gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
    	             
    	            gl.glDrawElements(GL10.GL_TRIANGLES, indices.length, GL10.GL_UNSIGNED_BYTE, 
    	                            mIndexBuffer);
    	                
    	            gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
    	            gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
    	    }
    	}
    	public static boolean isNumeric(String str)  
    	{  
    	  try  
    	  {  
    	    double d = Double.parseDouble(str);  
    	  }  
    	  catch(NumberFormatException nfe)  
    	  {  
    	    return false;  
    	  }  
    	  return true;  
    	}
    	public class GLClearRenderer implements Renderer {
    	    private Cube mCube = new Cube();
    	    private float rotation = 0.0f;
    	    private float desplazamientoZ = -10.0f;
    	    public void onDrawFrame( GL10 gl ) {
      	        c -= 1.0f;
    	        gl.glClearColor( 0.1f,0.1f,0.1f, 0.1f );
    	        
    	        gl.glClear( GL10.GL_COLOR_BUFFER_BIT );
  
    	        gl.glPushMatrix();
    	        
    	        gl.glRotatef(rotation, 0,1, 0);
    	        gl.glTranslatef(0.0f, 0.0f, desplazamientoZ);
                mCube.draw(gl);
                gl.glPopMatrix();
          

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
    	        
    	    	gl.glMatrixMode(GL10.GL_PROJECTION);
    			// Reset the projection matrix
    			gl.glLoadIdentity();
    			// Calculate the aspect ratio of the window
    			GLU.gluPerspective(gl, 45.0f, (float) width / (float) height, 0.1f,
    					100.0f);
    			// Select the modelview matrix
    			gl.glMatrixMode(GL10.GL_MODELVIEW);
    			// Reset the modelview matrix
    			gl.glLoadIdentity();
    	    }
    	    
    	    public void onSurfaceCreated( GL10 gl, EGLConfig config ) {
    	        // No need to do anything here.
    	    }
    	}
    public void crearCamara(){
        RadioGroup rdgGrupo = (RadioGroup)findViewById(R.id.rdgGrupo);   
        
        if(rdgGrupo.getCheckedRadioButtonId() == R.id.rdbOne){
        	acelerometro = "Z";
        }else{
        	acelerometro = "Y";
        }
    	getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    	latitud = ((EditText)findViewById(R.id.latitud)).getText() +"";
    	longitud = ((EditText)findViewById(R.id.longitud)).getText()+"";
    	if(isNumeric(latitud) && isNumeric(longitud)){
      try{
    	 //setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE );
    	  //requestWindowFeature( Window.FEATURE_NO_TITLE );
    	 // getWindow().setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN,
           //       WindowManager.LayoutParams.FLAG_FULLSCREEN );
   
    	  glView = new GLSurfaceView( this );
    	  glView.setEGLConfigChooser( 8, 8, 8, 8, 16, 0 );
    	  glView.getHolder().setFormat( PixelFormat.TRANSLUCENT );
    	  glView.setRenderer(render);
    	  direccion = new TextView(glView.getContext());
   	      direccion.setTextColor(Color.RED);
    	  setContentView( glView );
    	  CustomCameraView cv = new CustomCameraView(
      	        this);
      	   //   setContentView(rl);
      	    //  rl.addView(cv);
    	 // cameraView = new CameraView( this );
    	  addContentView( cv, new LayoutParams( LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT ) );

    	  addContentView(direccion,new LayoutParams( LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT ));
      }catch(Exception e){
    	  
      }
    	}else{
    		
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
            // Once the surface is created, simply open a handle to the camera hardware.
            camera = Camera.open();
            Camera.Parameters p = camera.getParameters();

    	     /*   params.setPreviewSize(width, height);
    	        params.setPictureFormat(PixelFormat.JPEG);
    	       -*/   
    	      //p.set("rotation", "90");

    	        
            camera.setParameters( p );
     
        }
     
        public void surfaceChanged( SurfaceHolder holder, int format, int width, int height ) {
    
            Camera.Parameters p = camera.getParameters();
            p.setPreviewSize( width, height );
    	     /*   params.setPreviewSize(width, height);
    	        params.setPictureFormat(PixelFormat.JPEG);
    	       -*/   
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
    public void recopilar(int num) {
    	TextView editText = (TextView) findViewById(R.id.texto);
    	editText.setText(editText.getText() + "" + num);
    }
    /** Called when the user clicks the Send button */
    public void sendMessage(View view) {

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
}
