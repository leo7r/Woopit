package com.woopitapp.activities;

	import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
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
import android.text.format.DateFormat;
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
		Long fecha;
		String modelName;
		String nombre;
		String texto;
		@Override
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.message_sent_info);
			
			Bundle extras = getIntent().getExtras();
			status = extras.getInt("leido");
			fecha = extras.getLong("fecha");
			modelName = extras.getString("modelName");
			nombre = extras.getString("nombre");
			texto = extras.getString("texto");
			Date date = new Date(fecha);
	
			SimpleDateFormat sdfDate = new SimpleDateFormat("HH:mm:ss yyyy-MM-dd",Locale.ENGLISH);//dd/MM/yyyy
		    String strDate = sdfDate.format(date);
			

			((TextView)findViewById(R.id.woop_para)).setText(Html.fromHtml(getResources().getString(R.string.woop_enviado_para) + " " + nombre));
			
			if(status == 0){
				((TextView)findViewById(R.id.woop_status)).setText(Html.fromHtml(getResources().getString(R.string.woop_enviado_status) + " " + getResources().getString(R.string.woop_enviado_noLeido)));
			}else{
				((TextView)findViewById(R.id.woop_status)).setText(Html.fromHtml(getResources().getString(R.string.woop_enviado_status) + " " + getResources().getString(R.string.woop_enviado_leido)));
			}
			
			((TextView)findViewById(R.id.woop_enviado)).setText(Html.fromHtml(getResources().getString(R.string.woop_enviado) + " " + modelName) );
			((TextView)findViewById(R.id.woop_texto)).setText(Html.fromHtml(getResources().getString(R.string.woop_enviado_texto) + " " + texto) );
			((TextView)findViewById(R.id.woop_enviado_fecha)).setText(Html.fromHtml(getResources().getString(R.string.woop_enviado_fecha) + " " +strDate));
			
		}
		
		protected void onStart(){
			
			super.onStart();
			
		}
		
		@Override
	    protected void onStop() {
			
	        super.onStop();
	    }
		
		

	
	

}
