package com.woopitapp.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.woopitapp.R;
import com.woopitapp.entities.User;
import com.woopitapp.server_connections.InsertCoins;
import com.woopitapp.services.ServerConnection;
import com.woopitapp.services.Utils;

public class ImagePreviewActivity extends Activity {

	int to_user = -1;
	String encoded_image;
	String user_name;
	Activity act;
	
	int TO_MAP_REQUEST = 1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_image_preview);
		act = this;
		
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

				new InsertCoins(act , cantCoins , R.string.por_enviar_mensaje ).execute();
				Utils.onMessageImageSent(getApplicationContext());
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
	
}
