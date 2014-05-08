package com.woopitapp;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.StringTokenizer;
import java.util.Vector;

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
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLU;
import android.opengl.GLUtils;
import android.os.Bundle;
import android.os.Handler;
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
	static final float ALPHA = 0.1f;
	private int mTextureId = -1;
	Bitmap bitmap;
	int indice = 0;
	GLClearRenderer render;
	
	boolean notif = true;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_main);
        bitmap = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.texturacorazon);
        Button bSuma = (Button) findViewById(R.id.botonSuma);
        
        bSuma.setOnClickListener(new OnClickListener(){
        	@Override
			public void onClick(View arg0) {
				
				crearCamara();
			}
        });
        
        if ( notif ){
        	new Handler().postDelayed(new Runnable(){

				@Override
				public void run() {
		        	setNotification();
				}
			}, 10000);
        	
        }
        
    }
	
	/* Imagen en 3D */
	
	class Cube {
        
	    private FloatBuffer mVertexBuffer;
	    private FloatBuffer mColorBuffer;
	    private ShortBuffer  mIndexBuffer;
	    	
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
	    float textureCoordinates[] = {  
	    		0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f,

	            0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f,

	            0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f,

	            0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f,

	            0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f,

	            0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f,};
	    private Vector<Float>  vertices = new Vector();
	    
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
	    
	    private Vector<Integer> indices = new Vector();
	    private Vector normals = new Vector();
	    private Vector<Float> textureCoor = new Vector();
	    private Vector faceNormals = new Vector();
	    private Vector textureIndex = new Vector();

	    protected int parseInt(String val) {
			if (val.length() == 0) {
				return -1;
			}
			return Integer.parseInt(val);
		}

		protected int[] parseIntTriple(String face) {
			int ix = face.indexOf("/");
			if (ix == -1)
				return new int[] {Integer.parseInt(face)-1};
			else {
				int ix2 = face.indexOf("/", ix+1);
				if (ix2 == -1) {
					return new int[] 
					               {Integer.parseInt(face.substring(0,ix))-1,
							Integer.parseInt(face.substring(ix+1))-1};
				}
				else {
					return new int[] 
					               {parseInt(face.substring(0,ix))-1,
							parseInt(face.substring(ix+1,ix2))-1,
							parseInt(face.substring(ix2+1))-1
					               };
				}
			}
		}
		public void leerArchivo(InputStream in){
			boolean file_normal = false;
			int nCount = 0;
			float[] coord = new float[2];
		
			LineNumberReader input = new LineNumberReader(new InputStreamReader(in));	    
			String line = null;
			try {
				for (line = input.readLine(); 
				line != null; 
				line = input.readLine())
				{
					if (line.length() > 0) {
						if (line.startsWith("v ")) {
							float[] vertex = new float[3];
							StringTokenizer tok = new StringTokenizer(line);
							tok.nextToken();
							vertex[0] = Float.parseFloat(tok.nextToken());
							vertex[1] = Float.parseFloat(tok.nextToken());
							vertex[2] = Float.parseFloat(tok.nextToken());
							vertices.addElement(vertex[0]);
							vertices.addElement(vertex[1]);
							vertices.addElement(vertex[2]);
						}
						else if (line.startsWith("vt ")) {
							StringTokenizer tok = new StringTokenizer(line);
							tok.nextToken();
							coord[0] = Float.parseFloat(tok.nextToken());
							coord[1] = Float.parseFloat(tok.nextToken());
							textureCoor.addElement(coord[0]);
							textureCoor.addElement(coord[1]);
						}
						else if (line.startsWith("f ")) {
							int[] face = new int[3];
							int[] face_n_ix = new int[3];
							int[] face_tx_ix = new int[3];
							int[] val;
		
							StringTokenizer tok = new StringTokenizer(line);
							tok.nextToken();
							val = parseIntTriple(tok.nextToken());
							face[0] = val[0];
							if (val.length > 1 && val[1] > -1)
								face_tx_ix[0] = val[1];
							if (val.length > 2 && val[2] > -1)
								face_n_ix[0] = val[2];
		
							val = parseIntTriple(tok.nextToken());
							face[1] = val[0];
							if (val.length > 1 && val[1] > -1)
								face_tx_ix[1] = val[1];
							if (val.length > 2 && val[2] > -1)
								face_n_ix[1] = val[2];
		
							val = parseIntTriple(tok.nextToken());
							face[2] = val[0];
							if (val.length > 1 && val[1] > -1) {
								face_tx_ix[2] = val[1];
								textureIndex.addElement(face_tx_ix);
							}
							if (val.length > 2 && val[2] > -1) {
								face_n_ix[2] = val[2];
								faceNormals.addElement(face_n_ix);
							}
							indices.addElement(face[0]);
							indices.addElement(face[1]);
							indices.addElement(face[2]);
							if (tok.hasMoreTokens()) {
								val = parseIntTriple(tok.nextToken());
								face[1] = face[2];
								face[2] = val[0];
								if (val.length > 1 && val[1] > -1) {
									face_tx_ix[1] = face_tx_ix[2];
									face_tx_ix[2] = val[1];
									textureIndex.addElement(face_tx_ix);
								}
								if (val.length > 2 && val[2] > -1) {
									face_n_ix[1] = face_n_ix[2];
									face_n_ix[2] = val[2];
									faceNormals.addElement(face_n_ix);
								}
								//indices.addElement(face);
							}
		
						}
						else if (line.startsWith("vn ")) {
							nCount++;
							float[] norm = new float[3];
							StringTokenizer tok = new StringTokenizer(line);
							tok.nextToken();
							norm[0] = Float.parseFloat(tok.nextToken());
							norm[1] = Float.parseFloat(tok.nextToken());
							norm[2] = Float.parseFloat(tok.nextToken());
							normals.addElement(norm);
							file_normal = true;
						}
					}
				}
			}
			catch (Exception ex) {
				System.err.println("Error parsing file:");
				System.err.println(input.getLineNumber()+" : "+line);
			}
		
			//for (int i=0;i<vertex_normals.size();i++) {
			//m.setVertexNormal(i, vertex_normals.get(i));
			//}
			
		}
		public  float[] convertFloats(Float[] floats)
		{
			float[] r = new float[floats.length];
		    for (int i=0; i < floats.length; i++)
		    {
		        r[i] = floats[i];
		    }
		    return r;
		}
		public  short[] convertInts(Integer[] ints)
		{
			short[] r = new short[ints.length];
		    for (int i=0; i < ints.length; i++)
		    {
		        r[i] = Short.parseShort(ints[i]+"");
		    }
		    return r;
		}
	    public Cube() {
	    	try{
	    		
	    		InputStream in =   getAssets().open("corazon.obj");
	    		leerArchivo(in);
	    		
	    		ByteBuffer byteBuf = ByteBuffer.allocateDirect(vertices.size() * 4);
				byteBuf.order(ByteOrder.nativeOrder());
				mVertexBuffer = byteBuf.asFloatBuffer();
				
				mVertexBuffer.put(convertFloats(vertices.toArray(new Float[vertices.size()])));
				mVertexBuffer.position(0);
				    
				/*
				byteBuf = ByteBuffer.allocateDirect(colors.length * 4);
				byteBuf.order(ByteOrder.nativeOrder());
				mColorBuffer = byteBuf.asFloatBuffer();
				mColorBuffer.put(colors);
				mColorBuffer.position(0);*/
				
				
				//GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
				byteBuf = ByteBuffer.allocateDirect(textureCoor.size() * 4);
				byteBuf.order(ByteOrder.nativeOrder());
				mTextureBuffer = byteBuf.asFloatBuffer();
				mTextureBuffer.put(convertFloats(textureCoor.toArray(new Float[textureCoor.size()])));
				mTextureBuffer.position(0);
				
				mIndexBuffer = ShortBuffer.allocate(indices.size());
				mIndexBuffer.put(convertInts(indices.toArray(new Integer[indices.size()])));
				mIndexBuffer.position(0);
	    	}catch(Exception e){
	    		direccion.setText("err: " + e);

	    	}
	    	
		
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

	        gl.glDrawElements(GL10.GL_TRIANGLES, indices.size(), GL10.GL_UNSIGNED_SHORT, mIndexBuffer);
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
		private float rotationX = 0.0f;
		private float rotationY = 0.0f;
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
	        gl.glRotatef(rotationX, 0,1, 0);
	        gl.glRotatef(rotationY,1 ,0, 0);
	        gl.glTranslatef(0.0f, -3.0f, desplazamientoZ);
	        gl.glRotatef(mCubeRotation, 0, 1, 0);
	        gl.glScalef(0.7f, 0.7f, 0.7f);
            mCube.draw(gl);
            
            gl.glPopMatrix();
            mCubeRotation -= 0.70f;
            
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
    			direccion = new TextView(this);
    			direccion.setTextColor(Color.RED);
    			//direccion.setText("AQUI ");
    			glView = new GLSurfaceView( this );
    			glView.setEGLConfigChooser( 8, 8, 8, 8, 16, 0 );
    			glView.getHolder().setFormat( PixelFormat.TRANSLUCENT );
    			render = new GLClearRenderer();
    			glView.setRenderer(render);

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
