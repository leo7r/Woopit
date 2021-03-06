package com.woopitapp.activities;

import java.util.Arrays;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.facebook.Request;
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

public class SignupActivity extends Activity implements ConnectionCallbacks, OnConnectionFailedListener, OnPeopleLoadedListener {
    
	/* Facebook */
	private static final String TAG = "Signup";
	private UiLifecycleHelper uiHelper;
	
	/* Google+ */
	public static final int REQUEST_CODE_RESOLVE_ERR = 9000;

    private ProgressDialog mConnectionProgressDialog;
    private PlusClient mPlusClient;
    private ConnectionResult mConnectionResult;
    
    boolean fb_info_ready = false;
    boolean gp_info_ready = false;
    
    EditText email, password,rPassword, name;
    Activity act;
    
    public void onCreate( Bundle savedInstanceState ){
    	
    	super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_signup);
		act = this;
		
		/* Login con Facebook */
        uiHelper = new UiLifecycleHelper(this, callback);
        uiHelper.onCreate(savedInstanceState);
        LoginButton authButton = (LoginButton) findViewById(R.id.authButton);
        authButton.setReadPermissions(Arrays.asList("email","public_profile","user_friends"));
        
        
        /* Login con Google+ */
        
        mPlusClient =
        	    new PlusClient.Builder(this, this, this).setActions(
        	        "http://schemas.google.com/AddActivity", "http://schemas.google.com/BuyActivity")
        	        .build();
        
        
		mConnectionProgressDialog = new ProgressDialog(this);
		mConnectionProgressDialog.setMessage(getResources().getString(R.string.creando_cuenta));
		
		findViewById(R.id.sign_in_button).setOnClickListener(new OnClickListener(){
			
			@Override
			public void onClick(View view) {
				
				// Se tiene que mostrar esta barra de progreso si no se resuelve el fallo de conexi�n.
				
			    if (view.getId() == R.id.sign_in_button && !mPlusClient.isConnected()) {
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
		
		email = (EditText) findViewById(R.id.email);
		password = (EditText) findViewById(R.id.password);
		rPassword = (EditText) findViewById(R.id.repeatedPassword);
		name = (EditText) findViewById(R.id.name);
		
		TextWatcher validWatcher = new TextWatcher() {
	        @Override
	        public void afterTextChanged(Editable s) {
	        	showSignUpButton();
	        }

	        @Override
	        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

	        @Override
	        public void onTextChanged(CharSequence s, int start, int before, int count) {}
	    };
		
		email.addTextChangedListener(validWatcher);
		password.addTextChangedListener(validWatcher);
		rPassword.addTextChangedListener(validWatcher);
		
		WelcomeActivity.setGoogleEmail(email,this);
		
		Button signUp = (Button) findViewById(R.id.signUp);
		signUp.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				signUp(v);
			}
		});
		
    	
    }
    	
    public void onStart(){
		super.onStart();
	}
	
	public void onStop(){
		super.onStop();
		mPlusClient.disconnect();
	}
	
	@Override
	public void onResume() {
	    super.onResume();
	    
	    // For scenarios where the main activity is launched and user
	    // session is not null, the session state change notification
	    // may not be triggered. Trigger it if it's open/closed.
	    Session session = Session.getActiveSession();
	    if (session != null &&
	           (session.isOpened() || session.isClosed()) ) {
	        onSessionStateChange(session, session.getState(), null);
	    }
	    
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
	
	public void showSignUpButton(){
				
		Pattern pattern = Patterns.EMAIL_ADDRESS;
	    boolean email_valid = pattern.matcher(email.getText()).matches();
	    
		Button button = (Button) findViewById(R.id.signUp);
		
		if ( email_valid && password.getText().length() >= 6 && (password.getText()+"").equals(rPassword.getText()+"") ){
			button.setVisibility(View.VISIBLE);
		}
		else{
			button.setVisibility(View.GONE);
		}
	}
	
	public void signUp( View v ){
		new WelcomeActivity.NewUser( this , 
				email.getText().toString() , name.getText().toString() ,
				password.getText().toString() , null , null , true ).execute();
	}
	
	/* Metodos del login de Facebook */
	
	private void onSessionStateChange(Session session, SessionState state, Exception exception) {
	    
		if (state.isOpened()) {
	        Log.i(TAG, "Logged in...");
	        
	        Request.newMeRequest(session, new Request.GraphUserCallback() {
				
				@Override
				public void onCompleted(GraphUser user, Response response) {
					
					if ( fb_info_ready )
						return;
					
					String name = user.getFirstName() + " " + user.getLastName();
                    String id = user.getId();
                    String email = user.getProperty("email").toString();

                    Log.i("facebookid", id);
                    Log.i("Name", name);
                    Log.i("email", email);
                    
                    new WelcomeActivity.NewUser( act , email , name , null , id , null , true ).execute();
                    fb_info_ready = true;
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
	
	/* Metodos del login de Google+ */
	
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
		mPlusClient.loadPeople(this, "me");
	}
	
	@Override
	public void onPeopleLoaded(ConnectionResult status, PersonBuffer personBuffer, String nextPageToken) {
		
		Person person = personBuffer.get(0);
		String id = person.getId();
		String email = mPlusClient.getAccountName();
		String name = person.getDisplayName();
		personBuffer.close();

        new WelcomeActivity.NewUser( this , email , name , null , null , id , true ).execute();
        gp_info_ready = true;				
	}

	@Override
	public void onDisconnected() {
		Log.i(TAG, "Desconectado");
	}
	
}
