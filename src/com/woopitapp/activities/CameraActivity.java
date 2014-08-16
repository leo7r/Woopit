package com.woopitapp.activities;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PictureCallback;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Toast;

import com.woopitapp.R;
import com.woopitapp.WoopitActivity;
import com.woopitapp.entities.User;
import com.woopitapp.services.ServerConnection;
import com.woopitapp.services.Utils;

public class CameraActivity extends WoopitActivity {

	private Camera camera;
	private int cameraId = 0;
	private CameraPreview mPreview;
	List<Camera.Size> mSupportedPreviewSizes;
	
	int border_height;
	boolean back_camera = false;
	int IMAGE_REQUEST = 1;
	int userId;
	String userName;

	String encoded_image;
	final int SELECT_IMAGE = 100;
	private static final String TEMP_PHOTO_FILE = "woopit_user_image.jpg";
	private final int image_size = 400;
	
	/* Multiple shots */
	ArrayList<Bitmap> shots;
	
	@SuppressLint("NewApi")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		/*
		if (Build.VERSION.SDK_INT < 16) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
		else{
			View decorView = getWindow().getDecorView();
			// Hide the status bar.
			int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
			decorView.setSystemUiVisibility(uiOptions);
			// Remember that you should never show the action bar if the
			// status bar is hidden, so hide that too if necessary.
			ActionBar actionBar = getActionBar();
			actionBar.hide();
		}*/
				
		setContentView(R.layout.activity_camera);
		setBorders();
		shots = new ArrayList<Bitmap>();
		
		if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
		      Toast.makeText(this, "No camera on this device", Toast.LENGTH_LONG).show();
		}
		else {
			cameraId = findFrontFacingCamera();
			if (cameraId < 0) {
				Toast.makeText(this, "No front facing camera found.",Toast.LENGTH_LONG).show();
			}
			else {
				camera = Camera.open(cameraId);
				mSupportedPreviewSizes = camera.getParameters().getSupportedPreviewSizes();
			}
		}
		
		mPreview = new CameraPreview(this, camera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);
        
        Bundle extras = getIntent().getExtras();
        
        if ( extras != null && extras.containsKey("userId") && extras.containsKey("userName") ){
        	userId = extras.getInt("userId"); 
        	userName = extras.getString("userName");
        }
        
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		if ( requestCode == IMAGE_REQUEST && resultCode == RESULT_OK ){
			finish();
		}
		if ( requestCode == SELECT_IMAGE && resultCode == RESULT_OK ){
        	
        	if ( data != null ) {

                String filePath= Environment.getExternalStorageDirectory()+"/"+TEMP_PHOTO_FILE;
                Bitmap selectedImage =  BitmapFactory.decodeFile(filePath);
                
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                selectedImage.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] imageBytes = baos.toByteArray();
                String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
                
                Intent i = new Intent(getApplicationContext(),ImagePreviewActivity.class);
                
                if ( userId != -1 && userName != null ){
                	i.putExtra("userId", userId);
                	i.putExtra("userName", userName);
                }
                
                i.putExtra("image", encodedImage);
                startActivityForResult(i,IMAGE_REQUEST);
            }
        }
		
	}
	
	public void onStop(){
		super.onStop();
		
	}
	
	public void onDestroy(){
		super.onDestroy();
		
		if (camera != null) {
			camera.release();
			camera = null;
		}
		
		for ( Bitmap b : shots ){
			b.recycle();
		}
		
	}
	
	class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
	    private SurfaceHolder mHolder;
	    private Camera mCamera;
	    private Camera.Size mPreviewSize;

	    public CameraPreview(Context context, Camera camera) {
	        super(context);
	        mCamera = camera;
	        
	        // Install a SurfaceHolder.Callback so we get notified when the
	        // underlying surface is created and destroyed.
	        mHolder = getHolder();
	        mHolder.addCallback(this);
	        // deprecated setting, but required on Android versions prior to 3.0
	        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	    }
	    
	    @Override
	    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
	        final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
	        final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);
	        setMeasuredDimension(width, height);

	        if (mSupportedPreviewSizes != null) {
	           mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, width, height);
	        }
	    }

	    public void surfaceCreated(SurfaceHolder holder) {
	        // The Surface has been created, now tell the camera where to draw the preview.
	        try {
	            mCamera.setPreviewDisplay(holder);
	            mCamera.startPreview();
	        } catch (IOException e) {
	            Log.d("Camera", "Error setting camera preview: " + e.getMessage());
	        }
	    }

	    public void surfaceDestroyed(SurfaceHolder holder) {
	        // empty. Take care of releasing the Camera preview in your activity.
	    }

	    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {

	        if (mHolder.getSurface() == null){
	          return;
	        }

	        try {
	            mCamera.stopPreview();
	        } catch (Exception e){
	        }

	        try {
	        	
	        	Camera.Parameters parameters = mCamera.getParameters();
	        	
	        	Display display = getWindowManager().getDefaultDisplay();
	        	
	        	switch (display.getRotation()) {
	                case Surface.ROTATION_0: // This is display orientation
	                    if (mPreviewSize.height > mPreviewSize.width) parameters.setPreviewSize(mPreviewSize.height, mPreviewSize.width);
	                    else parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
	                    camera.setDisplayOrientation(90);
	                    break;
	                case Surface.ROTATION_90:
	                    if (mPreviewSize.height > mPreviewSize.width) parameters.setPreviewSize(mPreviewSize.height, mPreviewSize.width);
	                    else parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
	                    camera.setDisplayOrientation(0);
	                    break;
	                case Surface.ROTATION_180:
	                    if (mPreviewSize.height > mPreviewSize.width) parameters.setPreviewSize(mPreviewSize.height, mPreviewSize.width);
	                    else parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
	                    camera.setDisplayOrientation(270);
	                    break;
	                case Surface.ROTATION_270:
	                    if (mPreviewSize.height > mPreviewSize.width) parameters.setPreviewSize(mPreviewSize.height, mPreviewSize.width);
	                    else parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
	                    camera.setDisplayOrientation(180);
	                    break;
	            }
	        	
	            //parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
	            mCamera.setParameters(parameters);
	            //mCamera.setDisplayOrientation(90);
	            mCamera.setPreviewDisplay(mHolder);
	            mCamera.startPreview();
	        	
	        	/*Camera.Parameters mParameters = mCamera.getParameters();
	        	Camera.Size bestSize = null;
	        	
	        	List<Camera.Size> sizeList = mCamera.getParameters().getSupportedPreviewSizes();
	        	bestSize = sizeList.get(0);
	        	
	        	for(int i = 1; i < sizeList.size(); i++){
	        		if((sizeList.get(i).width * sizeList.get(i).height) > (bestSize.width * bestSize.height)){
	        			bestSize = sizeList.get(i);
	        		}
	        	}
	        	
	        	mParameters.setPreviewSize(bestSize.height, bestSize.width);
	        	mCamera.setParameters(mParameters);
	            mCamera.setPreviewDisplay(mHolder);
	            mCamera.startPreview();*/

	        } catch (Exception e){
	            Log.d("Camera", "Error starting camera preview: " + e.getMessage());
	        }
	    }
	}
	
	private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio=(double)h / w;

        if (sizes == null) return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        
        return optimalSize;
    }
	
	public void takeShot( View v ){
		
		camera.takePicture(null, null, new PhotoHandler());
	}
	
	private int findFrontFacingCamera() {

		int cameraId = -1;
		
	    int numberOfCameras = Camera.getNumberOfCameras();
	    
	    for (int i = 0; i < numberOfCameras; i++) {
	    	CameraInfo info = new CameraInfo();
	    	Camera.getCameraInfo(i, info);
	    	
	    	if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
	    		Log.d("Camera", "Camera found");
	    		cameraId = i;
	    		break;
	    	}
	    }
	    
	    if ( cameraId == -1 ){
	    	cameraId = 0;
	    	back_camera = true;
	    }
	    
	    return cameraId;
	  }
	
	@SuppressLint("NewApi")
	private void setBorders(){
		Display display = getWindowManager().getDefaultDisplay();
		int width,height;
		
		if ( Build.VERSION.SDK_INT > 13 ){
			Point size = new Point();
			display.getSize(size);
			width = size.x;
			height = size.y;
		}
		else{
			width = display.getWidth();  // deprecated
			height = display.getHeight();  // deprecated
		}
		
		if ( height > width ){
			border_height = (height-width);
		}
		else{
			border_height = (width-height);
		}
		
		border_height-=Utils.dpToPx(50, getApplicationContext());
		
		RelativeLayout border1 = (RelativeLayout) findViewById(R.id.border);
		
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, border_height);
		params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

		border1.setLayoutParams(params);
	}
	
	public String saveImageToSd( int num , byte[] bytes ){
		
		try{

			Random r = new Random();
			int random = r.nextInt(100000);
			
			String uri = Environment.getExternalStorageDirectory()+ File.separator + "Woopit" + File.separator + "wpt_"+ num + "_"+ random +".jpg";
			
			File f = new File(uri);
			f.createNewFile();
			//write the bytes in file
			FileOutputStream fo = new FileOutputStream(f);
			fo.write(bytes);
			
			//remember close de FileOutput
			fo.close();
			return uri;
		}
		catch( Exception e ){
			e.printStackTrace();
		}
		
		return null;
	}
	
	public void goPreview(){

        Intent i = new Intent(getApplicationContext(),ImagePreviewActivity.class);
        if ( userId != -1 && userName != null ){
        	i.putExtra("userId", userId);
        	i.putExtra("userName", userName);
        }
        
		for ( int c = 0 ; c < shots.size() ; c++ ){
			
			Bitmap bm = shots.get(c);
			
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
		    bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);
	        byte[] imageBytes = baos.toByteArray();
	        //String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
	        //i.putExtra("image"+c, encodedImage);
	        
	        String uri = saveImageToSd(c,imageBytes);
	        i.putExtra("image"+c,uri );
		}
		
        startActivityForResult(i,IMAGE_REQUEST);
		Utils.onMessageImageNew(getApplicationContext());
	}
	
	class PhotoHandler implements PictureCallback {
		
		@SuppressLint("NewApi")
		@Override
		public void onPictureTaken(byte[] bitmapdata, Camera arg1) {
			
			Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapdata , 0, bitmapdata .length);
			/*Matrix mat = new Matrix();
		    mat.postRotate(-90);
		    bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), mat, true);
			*/
			
			if (bitmap.getWidth() >= bitmap.getHeight()){
				
				bitmap = Bitmap.createBitmap(
					bitmap, 
					bitmap.getWidth()-bitmap.getHeight(),//border*(w/height),//bitmap.getWidth()/2 - bitmap.getHeight()/2 + Utils.dpToPx(50, getApplicationContext()),
					0,
					bitmap.getHeight(), 
					bitmap.getHeight()
				);

			}
			else{

				bitmap = Bitmap.createBitmap(
					bitmap,
					0, 
					bitmap.getHeight()-bitmap.getWidth(),//bitmap.getHeight()/2 - bitmap.getWidth()/2 + Utils.dpToPx(50, getApplicationContext()),
					bitmap.getWidth(),
					bitmap.getWidth() 
				);
			}
			
			Display display = getWindowManager().getDefaultDisplay();
			int rotation = 0;
			switch (display.getRotation()) {
			    case Surface.ROTATION_0: // This is display orientation
			    rotation = 90;
			    break;
			case Surface.ROTATION_90:
			    rotation = 0;
			    break;
			case Surface.ROTATION_180:
			    rotation = 270;
			    break;
			case Surface.ROTATION_270:
			    rotation = 180;
			        break;
			 }
			
			Matrix mat = new Matrix();
		    mat.postRotate(back_camera ? rotation : -rotation );
		    Bitmap bitmap2 = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), mat, true);
		    bitmap.recycle();
			
		    Bitmap bitmap3 = Bitmap.createScaledBitmap(bitmap2, image_size, image_size, false);
		    bitmap2.recycle();
		    
		    shots.add(bitmap3);
		    
		    if ( shots.size() >= 4 ){
		    	goPreview();
		    }
		    else{
		    	ImageView last_image = (ImageView) findViewById(R.id.last_image);
		    	ImageView done = (ImageView) findViewById(R.id.from_gallery);
		    	
		    	last_image.setImageBitmap(bitmap3);
		    	last_image.setVisibility(View.VISIBLE);
		    	done.setImageResource(R.drawable.send_message);
		    	done.setOnClickListener(new OnClickListener(){

					@Override
					public void onClick(View arg0) {
						goPreview();
					}
				});
		    	
		    	camera.startPreview();
		    }
		                
            //new sendImageMessage( 26 , "leo" , "" , "fru" , 500 , 500 , encodedImage ).execute();
            //bitmap2.recycle();
		}
		
	} 
	
	class getImageMessage extends ServerConnection{

		public getImageMessage( String image_name ){
			super();
			
			init(getApplicationContext(),"get_image_message", new Object[]{User.get(getApplicationContext()).id,image_name});
		}
		
		@Override
		public void onComplete(String result) {
			
			if ( result != null && !result.equals("error") ){
				
				byte[] decodedString = Base64.decode(result, Base64.DEFAULT);
				Bitmap bm = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
				ImageView iv = new ImageView(getApplicationContext());
				iv.setImageBitmap(bm);
				setContentView(iv);
			}
			else{
				Toast.makeText(getApplicationContext(), R.string.error_de_conexion, Toast.LENGTH_SHORT).show();
				Log.e("image", ""+result);
			}
			
		}
		
	}
	
	private Uri getTempUri() {
	    return Uri.fromFile(getTempFile());
	}
	
	private File getTempFile() {

	    if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {

	        File file = new File(Environment.getExternalStorageDirectory(),TEMP_PHOTO_FILE);
	        try {
	            file.createNewFile();
	        } catch (IOException e) {}

	        return file;
	    } else {

	        return null;
	    }
	}

	public void selectImage( View v ){
		
		Intent photoPickerIntent = new Intent(Intent.ACTION_PICK,
		        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		photoPickerIntent.setType("image/*");
		photoPickerIntent.putExtra("crop", "true");   
		photoPickerIntent.putExtra("outputX", image_size);
		photoPickerIntent.putExtra("outputY", image_size); 
		photoPickerIntent.putExtra("aspectX", 1);
		photoPickerIntent.putExtra("aspectY", 1);
		photoPickerIntent.putExtra(MediaStore.EXTRA_OUTPUT, getTempUri());
		photoPickerIntent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
		startActivityForResult(photoPickerIntent, SELECT_IMAGE);
		
		/*Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
		photoPickerIntent.setType("image/*");
		
		startActivityForResult(photoPickerIntent, SELECT_IMAGE);*/
	}	
	
}
