package com.woopitapp.activities;

import java.util.Arrays;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.Request;
import com.facebook.Request.GraphUserCallback;
import com.facebook.Request.GraphUserListCallback;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.plus.PlusClient;
import com.google.android.gms.plus.PlusClient.OnPeopleLoadedListener;
import com.google.android.gms.plus.model.people.Person;
import com.google.android.gms.plus.model.people.PersonBuffer;
import com.woopitapp.R;
import com.woopitapp.WoopitActivity;
import com.woopitapp.entities.User;
import com.woopitapp.services.ServerConnection;
import com.woopitapp.services.Utils;

public class FindFriendsActivity extends WoopitActivity implements ConnectionCallbacks, OnConnectionFailedListener, OnPeopleLoadedListener {
	
	private int SHARE_REQUEST_CODE = 1;
    boolean share_launched = false , share_clicked = false;
    
	/* Facebook */
	private static final String TAG = "Encontrar amigos";
	private UiLifecycleHelper uiHelper;
	
	/* Google+ */
	public static final int REQUEST_CODE_RESOLVE_ERR = 9000;
	
    private ProgressDialog mConnectionProgressDialog;
    private PlusClient mPlusClient;
    private ConnectionResult mConnectionResult;
	
    boolean fb_info_ready,gp_info_ready;
    String facebook_id , gplus_id;
    Activity act;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.find_friends_welcome);
		act = this;
		
        /* Buscar amigos con Facebook */
        uiHelper = new UiLifecycleHelper(this, callback);
        uiHelper.onCreate(savedInstanceState);
        final LoginButton authButton = (LoginButton) findViewById(R.id.authButton);
        authButton.setReadPermissions(Arrays.asList("user_friends"));
        
        Button facebook_button = (Button) findViewById(R.id.facebook_button);
        
        facebook_button.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				
				Session session = Session.getActiveSession();
			    if (session != null && (session.isOpened() || session.isClosed()) ) {
			        onSessionStateChange(session, session.getState(), null);
			    }
			    else{
			    	authButton.performClick();
			    }
				
			}
		});
        
        /* Buscar amigos con Google+ */
        
        mPlusClient =
        	    new PlusClient.Builder(this, this, this).setActions(
        	        "http://schemas.google.com/AddActivity", "http://schemas.google.com/BuyActivity")
        	        .build();
		
		mConnectionProgressDialog = new ProgressDialog(this);
		mConnectionProgressDialog.setMessage(getResources().getString(R.string.buscando_amigos));
		
		findViewById(R.id.gplus_button).setOnClickListener(new OnClickListener(){
			
			@Override
			public void onClick(View view) {
								
			    if (!mPlusClient.isConnected()) {
			        if (mConnectionResult == null) {
			            mConnectionProgressDialog.show();
			            mPlusClient.connect();
			        } else {
		                mConnectionResult = null;
		                mPlusClient.connect();
			        }
			    }
			}
		});
        
		
	}
	
    public void onStart(){
		super.onStart();
	}
	
	public void onStop(){
		super.onStop();
		//mPlusClient.disconnect();
	}
	
	@Override
	public void onResume() {
	    super.onResume();
	    
	    uiHelper.onResume();
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		
	    super.onActivityResult(requestCode, resultCode, data);
	    
		if (requestCode == REQUEST_CODE_RESOLVE_ERR && resultCode == Activity.RESULT_OK) {
			mConnectionResult = null;
	        mPlusClient.connect();
	    }
		
	    uiHelper.onActivityResult(requestCode, resultCode, data);
	}
	
	@Override
	public void onPause() {
	    super.onPause();
	    uiHelper.onPause();
	}
	
	@Override
	public void onDestroy() {
	    super.onDestroy();
	    uiHelper.onDestroy();
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
	    super.onSaveInstanceState(outState);
	    uiHelper.onSaveInstanceState(outState);
	}
    
	public void setFriendsList( String result ){
		
		Intent i = new Intent(this,FindFriendsList.class);
		i.putExtra("result", result);
		startActivity(i);
	}
	
	/* Metodos para amigos de Facebook */
	
	private void onSessionStateChange( final Session session, SessionState state, Exception exception) {
	    
		if (state.isOpened()) {
	        Log.i(TAG, "Logged in...");
	        
	        Request.newMeRequest(session, new GraphUserCallback(){

				@Override
				public void onCompleted(GraphUser user, Response response) {
					
					facebook_id = user.getId();
					
					Request friendRequest = Request.newMyFriendsRequest(session, new GraphUserListCallback(){
						
						@Override
						public void onCompleted(List<GraphUser> users, Response response) {
							
							if ( fb_info_ready ){
								fb_info_ready = false;
								return;
							}
							
							String ids[] = new String[users.size()];
							
							for ( int i = 0 ; i < users.size() ; ++i){
								
								String id = users.get(i).getId();
								ids[i] = id;
							}
							
							fb_info_ready = true;
							new FindFriends(act,ids,null).execute();
						}
					});
			        
			        Bundle params = new Bundle();
			        params.putString("fields", "id");
			        friendRequest.setParameters(params);
			        friendRequest.executeAsync();
					
				}
			}).executeAsync();
	        	        
	        
	    } else if (state.isClosed()) {
	        Log.i(TAG, "Logged out...");
	    }
	}
	
	private Session.StatusCallback callback = new Session.StatusCallback() {
	    @Override
	    public void call(Session session, SessionState state, Exception exception) {
	        onSessionStateChange(session, state, exception);
	    }
	};
	
	/* Metodos para amigos de Google+ */
	
	@Override
	public void onConnectionFailed(ConnectionResult result) {
		
		if (result.hasResolution()) {
            
			try {
                result.startResolutionForResult(this, REQUEST_CODE_RESOLVE_ERR);
            } catch (SendIntentException e) {
                mPlusClient.connect();
            }
        }
        // Guarda el resultado y resuelve el fallo de conexi�n con el clic de un usuario.
        mConnectionResult = result;
	}
	
	@Override
	public void onConnected(Bundle arg0) {

        mConnectionProgressDialog.dismiss();
		Log.i(TAG, "Conectado");
		mPlusClient.loadVisiblePeople(this, null);
	}
	
	@Override
	public void onPeopleLoaded(ConnectionResult status, PersonBuffer personBuffer, String nextPageToken) {
		
		if ( gp_info_ready )
			return;
		
		Person me = mPlusClient.getCurrentPerson();
		gplus_id = me.getId();
		
		if (status.getErrorCode() == ConnectionResult.SUCCESS) {
	        try {
	        	
	        	String ids[] = new String[personBuffer.getCount()];
	        	
	        	for ( int i = 0 ; i < personBuffer.getCount() ; ++i ){
	        		Person p = personBuffer.get(i);
	        		ids[i] = p.getId();
	        		//Log.d(TAG, "Display Name: " + p.getId());
	        	}
	        	
				new FindFriends(this,null,ids).execute();
	        	
	        } finally {
	            personBuffer.close();
	        }
	    } else {
	        Log.e(TAG, "Error listing people: " + status.getErrorCode());
	    }
		
        gp_info_ready = true;
	}

	@Override
	public void onDisconnected() {
		Log.i(TAG, "Desconectado");
	}
	
	/* Encontrar amigos en Woopit */
	public class FindFriends extends ServerConnection{

		Activity act;
		String[] facebook_ids;
		String[] gplus_ids;
		ProgressDialog dialog;
		
		public FindFriends( Activity act , String[] facebook_ids , String[] gplus_ids ){
			super();
			
			this.act = act;
			this.facebook_ids = facebook_ids;
			this.gplus_ids = gplus_ids;
			dialog = ProgressDialog.show(act, "",act.getResources().getString(R.string.buscando_amigos), true);
			
			String fb_ids = facebook_ids == null ? "" : TextUtils.join("#", facebook_ids);
			String gp_ids = gplus_ids == null ? "" : TextUtils.join("#", gplus_ids);
			
			init(act,"find_friends",new Object[]{ User.get(act).id , fb_ids , gp_ids });
		}
		
		@Override
		public void onComplete(String result) {
			
			dialog.dismiss();
				
			if ( result != null && result.length() > 0 ){
				Log.d(TAG,result);
				
				int num_users;
				try {
					num_users = new JSONArray(result).length();
				} catch (JSONException e) {
					num_users = 0;
				}
				
				Utils.onFriendsSearch(getApplicationContext(), facebook_ids == null ? "Google+" : "Facebook", num_users);
				setFriendsList(result);
				new SetSocialIds(getApplicationContext()).execute();
			}
			else{
				Toast.makeText(act, act.getResources().getString(R.string.error_de_conexion),Toast.LENGTH_SHORT).show();
			}
		}
		
	}
	
	/* Si no existian mis ids sociales, los coloco */
	public class SetSocialIds extends ServerConnection{

		Context con;
		
		public SetSocialIds( Context con ){
			super();
			
			if ( facebook_id == null )
				facebook_id = "";
			
			if ( gplus_id == null )
				gplus_id = "";
			
			init(con,"set_social_ids",new Object[]{ User.get(con).id , facebook_id , gplus_id });
		}
		
		@Override
		public void onComplete(String result) {
			
		}
		
	}
	
}
