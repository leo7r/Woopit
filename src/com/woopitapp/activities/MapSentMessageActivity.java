package com.woopitapp.activities;

	import java.io.IOException;
import java.util.List;
import java.util.Locale;

	import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

	import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.woopitapp.R;
import com.woopitapp.entities.User;
import com.woopitapp.services.ServerConnection;
import com.woopitapp.services.Utils;

public class MapSentMessageActivity extends FragmentActivity implements 
GooglePlayServicesClient.ConnectionCallbacks,
GooglePlayServicesClient.OnConnectionFailedListener,
GoogleMap.OnMapClickListener {
	

		EditText search_address;
		Button send_woop;
		
		/* Location vars */
		private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
		LocationClient locationClient;
		Location currentLocation;
		
		/* Map vars */
		SupportMapFragment mapFragment;
		GoogleMap mMap;
		GoogleMapOptions options;
		private Marker marker;
		double selectedLatitude,selectedLongitude;
		Circle area;
		float default_zoom = 17.0f;
		double latitud;
		double longitud;
		String modelName;
		String nombre;
		String texto;
		
		@Override
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.map);
			
			Bundle extras = getIntent().getExtras();
			
			latitud = extras.getDouble("latitud");
			longitud = extras.getDouble("longitud");
			modelName = extras.getString("modelName");
			nombre = extras.getString("nombre");
			texto = extras.getString("texto");
			
			options = new GoogleMapOptions();
			options.mapType(GoogleMap.MAP_TYPE_NORMAL)
			.rotateGesturesEnabled(false)
			.tiltGesturesEnabled(false)
			.compassEnabled(false)
			.zoomControlsEnabled(false);
			
			mapFragment = SupportMapFragment.newInstance(options);
			
			FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
			
			transaction.add(R.id.map_container, mapFragment);
			transaction.commit();
			
			locationClient = new LocationClient(this, this, this);
			
			
			
			
		}
		
		protected void onStart(){
			
			super.onStart();
			
			setUpMapIfNeeded();
			locationClient.connect();
		}
		
		@Override
	    protected void onStop() {
			
			locationClient.disconnect();
	        super.onStop();
	    }
		
		@Override
		protected void onActivityResult( int requestCode, int resultCode, Intent data) {
			
			switch (requestCode) {
		    	case CONNECTION_FAILURE_RESOLUTION_REQUEST :
		    
		        switch (resultCode) {
		            case Activity.RESULT_OK :
		            break;
		        }
			}
		}
		
		private void setUpMapIfNeeded() {
			
		    if (mMap == null) {
				mMap = mapFragment.getMap();
				mMap.setMyLocationEnabled(true);
				mMap.setOnMapClickListener(this);
				
		    }
		    LatLng latlng = new LatLng(latitud,longitud);
		    marker = mMap.addMarker(new MarkerOptions()
	        .position(latlng)
	        .title(getResources().getString(R.string.mensaje_enviado_aqui,nombre))
	        .snippet(Html.fromHtml(getResources().getString(R.string.woop_enviado) + " " + modelName + "\n" +getResources().getString(R.string.woop_enviado_texto) + " " + texto).toString()));
	        //.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
			
			marker.showInfoWindow();
		
			
		}
		
		public void sendWoop( View v ){
			
			new Send_Message(getApplicationContext() , selectedLatitude , selectedLongitude ).execute();
		}
		
		class Send_Message extends ServerConnection{
	    	
			Context con;
			
			public Send_Message(Context context,double latitud, double longitud){
				this.con = context;
				
			//	init(con,"send_message",new Object[]{User.get(con).id,userId,modelId,"",message,latitud,longitud});
			}

			@Override
			public void onComplete(String result) {
				
				if( result != null && result.equals("OK") ){

					Toast.makeText(getApplicationContext(), getResources().getString(R.string.mensaje_enviado , nombre ) , Toast.LENGTH_LONG).show();
					Utils.sendBroadcast(getApplicationContext(), R.string.broadcast_messages);
					setResult(RESULT_OK);
				    finish();
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
		
		/* Metodos de Google map */

		@Override
		public void onMapClick(LatLng latlng) {
			
			
		}
		
		public void setMarkerArea( LatLng latlng ){
			
			CircleOptions circleOptions = new CircleOptions()
		    .center(latlng)
		    .fillColor(getResources().getColor(R.color.map_area_fill))
		    .strokeColor(getResources().getColor(R.color.map_area_stroke))
		    .strokeWidth(2)
		    .radius(30);
			
			if ( area != null ){
				area.remove();
			}
			
			area = mMap.addCircle(circleOptions);
		}
		
		public void searchPlace( String place ){
			
			Utils.closeKeyboard(search_address, getApplicationContext());
			Geocoder geocoder = new Geocoder(this, Locale.getDefault());
			
			try {
				
				List<Address> list = geocoder.getFromLocationName(place, 1);
				
				if ( list.size() > 0 ){
					Address searchA = list.get(0);
					LatLng latlng = new LatLng(searchA.getLatitude(),searchA.getLongitude());
					mMap.animateCamera( CameraUpdateFactory.newLatLngZoom(latlng, default_zoom) );
					
					send_woop.setVisibility(View.GONE);
					
					if ( marker != null ){
						marker.remove();
						area.remove();
					}
					
				}
				else{
					Toast.makeText(getApplicationContext(), getResources().getString(R.string.lugar_no_encontrado,place) , Toast.LENGTH_SHORT).show();
				}
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		/* Metodos de localizacion */
		
		@Override
		public void onConnectionFailed(ConnectionResult connectionResult) {
			
			if (connectionResult.hasResolution()) {
	            try {
	                // Start an Activity that tries to resolve the error
	                connectionResult.startResolutionForResult(
	                        this,
	                        CONNECTION_FAILURE_RESOLUTION_REQUEST);
	                /*
	                 * Thrown if Google Play services canceled the original
	                 * PendingIntent
	                 */
	            } catch (IntentSender.SendIntentException e) {
	                // Log the error
	                e.printStackTrace();
	            }
	        } else {
	        	Toast.makeText(this, "ERROR: "+connectionResult.getErrorCode(), Toast.LENGTH_SHORT).show();
	        }
		}
		
		@Override
		public void onConnected(Bundle arg0) {
			
			currentLocation = locationClient.getLastLocation();
			
			if ( currentLocation != null ){
				LatLng location = new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude());
		
			}
			mMap.moveCamera( CameraUpdateFactory.newLatLngZoom( new LatLng(latitud,longitud) , default_zoom) );
		}

		@Override
		public void onDisconnected() {		
		}
		
	

}
