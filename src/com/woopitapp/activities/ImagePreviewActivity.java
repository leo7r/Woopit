package com.woopitapp.activities;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import android.app.Activity;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.woopitapp.R;
import com.woopitapp.R.id;
import com.woopitapp.R.layout;
import com.woopitapp.R.string;
import com.woopitapp.activities.EditProfileActivity.SaveImage;
import com.woopitapp.entities.User;
import com.woopitapp.services.ServerConnection;

public class ImagePreviewActivity extends Activity {

	int to_user = -1;
	String encoded_image;
	String user_name;
	
	int TO_MAP_REQUEST = 1;
	final int SELECT_IMAGE = 100;
	private static final String TEMP_PHOTO_FILE = "woopit_user_image.jpg";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_image_preview);
		
		ImageView image = (ImageView) findViewById(R.id.image);
		Bundle extras = getIntent().getExtras();
		
		if ( extras.containsKey("image") ){

			encoded_image = extras.getString("image");
			byte[] decodedString = Base64.decode(encoded_image, Base64.DEFAULT);
			
			if ( extras.containsKey("userId") ){
				to_user = extras.getInt("userId");
				user_name = extras.getString("userName");
				
				((LinearLayout)findViewById(R.id.send_buttons)).setVisibility(View.VISIBLE);
				((LinearLayout)findViewById(R.id.send_image)).setVisibility(View.GONE);
				((EditText)findViewById(R.id.message_text)).setVisibility(View.VISIBLE);
			}
			else{
				((LinearLayout)findViewById(R.id.send_image)).setVisibility(View.VISIBLE);
				((LinearLayout)findViewById(R.id.send_buttons)).setVisibility(View.GONE);
				((EditText)findViewById(R.id.message_text)).setVisibility(View.GONE);
			}
			
			Bitmap bm = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
			image.setImageBitmap(bm);
		}
		
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		if ( requestCode == TO_MAP_REQUEST && resultCode == RESULT_OK ){
			Toast.makeText(getApplicationContext(), R.string.enviando_mensaje, Toast.LENGTH_SHORT).show();
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
	
	public void chooseFriend( View v ){
		
		Intent i = new Intent(this, ChooseFriendActivity.class);
		i.putExtra("image", encoded_image);
		startActivity(i);
		finish();
	}
	
	public void sendDirect( View v ){
		
		EditText editT = (EditText) findViewById(R.id.message_text);
		String text = editT.getText().toString();
		
		new sendImageMessage( to_user , user_name , "" , text , 500 , 500 , encoded_image ).execute();

		Toast.makeText(getApplicationContext(), R.string.enviando_mensaje, Toast.LENGTH_SHORT).show();
		setResult(RESULT_OK);
		finish();
	}
	
	public void sendMap( View v ){
		
		EditText editT = (EditText) findViewById(R.id.message_text);
		String text = editT.getText().toString();
		

		Intent i = new Intent(this, MapActivity.class);
		i.putExtra("image", encoded_image);
		i.putExtra("userId", to_user);
		i.putExtra("userName", user_name);
		i.putExtra("message", text);
		i.putExtra("modelId", -1);
		startActivityForResult(i,TO_MAP_REQUEST);
		
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
	
	class sendImageMessage extends ServerConnection{
    	
		int cantCoins = 1;
		int receiver;
		String text, userName;
		double latitude, longitude;
		
		public sendImageMessage(int receiver , String userName , String title,String text,double latitud, double longitud , String encoded_image ){
			this.text = text;
			this.receiver = receiver;
			this.userName = userName;
			this.latitude = latitud;
			this.longitude = longitud;
			
			init(getApplicationContext(),"send_image_message",new Object[]{User.get(getApplicationContext()).id,receiver,title,text,latitud,longitud,encoded_image});
		}

		@Override
		public void onComplete(String result) {
			
			if( result != null && result.equals("ok") ){

				//new InsertCoins(act , cantCoins , R.string.por_enviar_mensaje ).execute();
				//Utils.onMessageSent(getApplicationContext(), "CameraActivity", 0, text, latitude, longitude);
				//Utils.sendBroadcast(getApplicationContext(), R.string.broadcast_messages);
				
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
	
}
