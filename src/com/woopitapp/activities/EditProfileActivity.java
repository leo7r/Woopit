package com.woopitapp.activities;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.woopitapp.R;
import com.woopitapp.WoopitActivity;
import com.woopitapp.entities.User;
import com.woopitapp.services.Data;
import com.woopitapp.services.ServerConnection;
import com.woopitapp.services.Utils;

public class EditProfileActivity extends WoopitActivity {

	User user;
	TextView edit_name;
	ImageView image;
	
	final int SELECT_IMAGE = 100;
	private static final String TEMP_PHOTO_FILE = "woopit_user_image.jpg";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit_profile);
		
		user = User.get(getApplicationContext());
		
		image = (ImageView) findViewById(R.id.image);
		edit_name = (TextView) findViewById(R.id.name);
		
        Utils.setUserImage(getApplicationContext(), image, user.id);
		edit_name.setText(user.name);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) { 
	    super.onActivityResult(requestCode, resultCode, imageReturnedIntent); 

	    switch(requestCode) {
	    
	    case SELECT_IMAGE:
	        if(resultCode == RESULT_OK){
	        	
	        	if ( imageReturnedIntent != null ) {

                    String filePath= Environment.getExternalStorageDirectory()+"/"+TEMP_PHOTO_FILE;
                    Bitmap selectedImage =  BitmapFactory.decodeFile(filePath);
                    
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    selectedImage.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    byte[] imageBytes = baos.toByteArray();
                    String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
                    
                    new SaveImage( getApplicationContext(), encodedImage ).execute();
                }
	        }
	    }
	}
	
	public void saveChanges( View v ){
		
		user.name = edit_name.getText().toString();
		
		new UpdateUser( this ).execute();
	}
	
	public void selectImage( View v ){
		
		Intent photoPickerIntent = new Intent(Intent.ACTION_PICK,
		        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		photoPickerIntent.setType("image/*");
		photoPickerIntent.putExtra("crop", "true");   
		photoPickerIntent.putExtra("outputX", 150);
		photoPickerIntent.putExtra("outputY", 150); 
		photoPickerIntent.putExtra("aspectX", 1);  
		photoPickerIntent.putExtra("aspectY", 1);
		photoPickerIntent.putExtra(MediaStore.EXTRA_OUTPUT, getTempUri());
		photoPickerIntent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
		startActivityForResult(photoPickerIntent, SELECT_IMAGE);
		
		/*Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
		photoPickerIntent.setType("image/*");
		
		startActivityForResult(photoPickerIntent, SELECT_IMAGE);*/
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
	
    class UpdateUser extends ServerConnection{
    	
    	Context con;
		ProgressDialog dialog;
    	
    	public UpdateUser( Activity act ){
    		super();
    		
    		this.con = act;
			dialog = ProgressDialog.show(act, "",act.getResources().getString(R.string.guardando_informacion), true);
    		
			init(con,"update_user",new Object[]{ ""+user.id , user.name });
    	}

		@Override
		public void onComplete(String result) {

			dialog.dismiss();
			
			if ( result != null && result.length() > 0 ){
				
				if ( result.toLowerCase().equals("ok") ){
					Toast.makeText(getApplicationContext(), R.string.informacion_actualizada, Toast.LENGTH_SHORT).show();
					
					Data data = new Data(con);
					data.open();
					data.updateUser(null, user.name, null, -1, -1);
					data.close();
					
					Utils.sendBroadcast(con, R.string.broadcast_profile_update);
					finish();
				}
				else{
					Toast.makeText(getApplicationContext(), R.string.error_desconocido, Toast.LENGTH_SHORT).show();
				}				
			}
			else{
				Toast.makeText(getApplicationContext(), R.string.error_de_conexion, Toast.LENGTH_SHORT).show();
			}
			
		}
    	
    }

    class SaveImage extends ServerConnection{
    	
    	Context con;
		ProgressDialog dialog;
    	
    	public SaveImage( Context c , String image ){
    		super();
    		
    		this.con = c;
			init(con,"update_user_image",new Object[]{ ""+user.id , image  });
    	}

		@Override
		public void onComplete(String result) {
			
			if ( result != null && result.length() > 0 ){
				
				ImageLoader imageLoader = Utils.getImageLoader(con);
				
				File imageFile = imageLoader.getDiscCache().get(Utils.getUserImageURI(user.id));
				
				if (imageFile.exists()) {
				    imageFile.delete();
				}
				
		        Utils.setUserImage(getApplicationContext(), image, user.id);
				Utils.sendBroadcast(con, R.string.broadcast_profile_update);
								
				Log.i("Image", result);
			}
			else{
				Toast.makeText(getApplicationContext(), R.string.error_de_conexion, Toast.LENGTH_SHORT).show();
			}
			
		}
    	
    }
        
}
