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

public class MapSentDirectMessageActivity extends FragmentActivity {
	

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
		int status;
		String fecha;
		String modelName;
		String nombre;
		
		@Override
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.message_sent_info);
			
			Bundle extras = getIntent().getExtras();
			
			status = extras.getInt("leido");
			fecha = extras.getString("fecha");
			modelName = extras.getString("modelName");
			nombre = extras.getString("nombre");
			
			((TextView)findViewById(R.id.woop_para)).setText(getResources().getString(R.string.woop_enviado_para) + " " + nombre);
			
			if(status == 0){
				((TextView)findViewById(R.id.woop_status)).setText(getResources().getString(R.string.woop_enviado_status) + " " + getResources().getString(R.string.woop_enviado_noLeido));
			}else{
				((TextView)findViewById(R.id.woop_status)).setText(getResources().getString(R.string.woop_enviado_status) + " " + getResources().getString(R.string.woop_enviado_leido));
			}
			
			((TextView)findViewById(R.id.woop_enviado)).setText(getResources().getString(R.string.woop_enviado) + " " + modelName );
			((TextView)findViewById(R.id.woop_enviado_fecha)).setText(getResources().getString(R.string.woop_enviado_fecha) + " " + fecha );
			
		}
		
		protected void onStart(){
			
			super.onStart();
			
		}
		
		@Override
	    protected void onStop() {
			
	        super.onStop();
	    }
		
		

	
	

}
