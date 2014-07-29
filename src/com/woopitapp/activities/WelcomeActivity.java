package com.woopitapp.activities;

import java.util.Locale;
import java.util.regex.Pattern;

import org.json.JSONObject;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.woopitapp.R;
import com.woopitapp.entities.User;
import com.woopitapp.fragments.WelcomeFragment;
import com.woopitapp.services.Data;
import com.woopitapp.services.Preferences;
import com.woopitapp.services.ServerConnection;
import com.woopitapp.services.Utils;

public class WelcomeActivity extends FragmentActivity {
	
	Fragment currentFragment;
	private int SIGNUP_LOGIN_REQUEST = 1;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if ( !Preferences.isFirstTime(this) && User.get(this) != null && User.get(this).username != null ){
			
			Intent i = new Intent(this,MainActivity.class);
			startActivity(i);
			finish();
		}
		
		setContentView(R.layout.welcome);
		
		Fragment fragment = new WelcomeFragment();
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		
		transaction.add(R.id.fragment_container, fragment);
		transaction.commit();
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		if ( requestCode == SIGNUP_LOGIN_REQUEST && resultCode == RESULT_OK ){
			finish();
		}
		
	}
	
	public void goLogin( View v ){

		Intent i = new Intent( this , LoginActivity.class );
		startActivityForResult(i , SIGNUP_LOGIN_REQUEST  );
	}
	
	public void goSignup( View v ){
		
		Intent i = new Intent( this , SignupActivity.class );
		startActivityForResult(i , SIGNUP_LOGIN_REQUEST  );
		
	}
	
	/* Coloca un email del usuario de una vez */
	public static void setGoogleEmail( EditText editT , Context context ){
		
		Pattern emailPattern = Patterns.EMAIL_ADDRESS;
		Account[] accounts = AccountManager.get(context).getAccounts();
		for (Account account : accounts) {
		    
			if (emailPattern.matcher(account.name).matches()) {
		        String possibleEmail = account.name;
		        editT.setText(possibleEmail);
		        break;
		    }
		}
		
	}
	
	public static class NewUser extends ServerConnection{

		Activity act;
		String email;
		String name;
		String password;
		String facebook_hash;
		String gplus_hash;
		boolean automaticLogin;
		
		ProgressDialog dialog;
		
		public NewUser( Activity act , String email , String name , String password , String facebook_hash , String gplus_hash , boolean automaticLogin ){
			super();
			
			if ( facebook_hash != null || gplus_hash != null ){
				Utils.onRegister(act, "WelcomeActivity", facebook_hash == null ? "Google+" : "Facebook" );
			}
			else{
				Utils.onRegister(act, "WelcomeActivity", "Ninguna" );
			}
			
			this.act = act;
			this.email = email;
			this.name = name;
			this.password = password != null ? password : "";
			this.facebook_hash = facebook_hash != null ? facebook_hash : "";
			this.gplus_hash = gplus_hash != null ? gplus_hash : "";
			this.automaticLogin = automaticLogin;
			this.dialog = ProgressDialog.show(act, "",act.getResources().getString(R.string.creando_cuenta), true);
			
			init(act,"new_user", new Object[]{ email , name , this.password , this.facebook_hash , this.gplus_hash } );
			
		}
		
		@Override
		public void onComplete(String result) {
			
			dialog.dismiss();
			
			if ( result == null ){
				Toast.makeText(act, R.string.error_de_conexion, Toast.LENGTH_SHORT).show();
				return;
			}
			
			if ( result.toLowerCase(Locale.getDefault()).equals("ok")){
				
				if ( automaticLogin ){
					new LoginTask( act, this.email , name , this.password , this.facebook_hash , this.gplus_hash ).execute();
				}
				
			}
			else{
				if ( result.toLowerCase(Locale.getDefault()).equals("already_registered") ){
					
					if ( this.facebook_hash.length() > 0 || this.gplus_hash.length() > 0 ){
						new LoginTask( act, this.email , name , this.password , this.facebook_hash , this.gplus_hash ).execute();						
					}
					else{
						Toast.makeText(act, act.getResources().getString(R.string.correo_ya_registrado),Toast.LENGTH_SHORT).show();
					}
					
				}
				else{
					Toast.makeText(act, act.getResources().getString(R.string.error_desconocido),Toast.LENGTH_SHORT).show();
				}
			}
				
		}
		
	}
	
	public static class LoginTask extends ServerConnection{

		Activity act;
		String email;
		String name;
		String password;
		String facebook_hash;
		String gplus_hash;
		ProgressDialog dialog;
		
		public LoginTask( Activity act , String email , String name , String password , String facebook_hash , String gplus_hash ){
			super();
			
			if ( facebook_hash != null || gplus_hash != null ){
				Utils.onLogin(act, "WelcomeActivity", facebook_hash == null ? "Google+" : "Facebook" ); 
			}
			else{
				Utils.onLogin(act, "WelcomeActivity", "Ninguna" );
			}
			
			this.act = act;
			this.email = email;
			this.name = name;
			this.password = password != null ? password : "";
			this.facebook_hash = facebook_hash != null ? facebook_hash : "";
			this.gplus_hash = gplus_hash != null ? gplus_hash : "";
			dialog = ProgressDialog.show(act, "",act.getResources().getString(R.string.entrando), true);
		
			init(act,"login",new Object[]{ email , this.password , this.facebook_hash , this.gplus_hash });
		}

		@Override
		public void onComplete(String result) {
			
			dialog.dismiss();
			
			if ( result != null && !result.equals("invalid") && !result.equals("not_registered") && !result.equals("error") ){
				
				try{
					JSONObject userInfo = new JSONObject(result);
					Log.i("Log in", userInfo.toString());
					
					int id = userInfo.getInt("i");
					String username = userInfo.getString("u");
					String name = userInfo.getString("n");
					String image = userInfo.getString("m");
					int facebook_user = userInfo.getInt("f");
					int gplus_user = userInfo.getInt("g");
					
					if ( username.length() == 0 ){
						username = null;
					}
					
					Data data = new Data(act);
					data.open();
					data.insertUser(id, email, username , name, image, facebook_user, gplus_user);
					data.close();

					act.setResult(RESULT_OK);
					
					if ( username == null ){
						Intent i = new Intent(act,ChooseUsernameActivity.class);
						act.startActivity(i);
						act.finish();
						return;
					}
					
					Preferences.setFirstTime(act, false);
					Intent i = new Intent(act,MainActivity.class);
					act.startActivity(i);
					act.finish();
				}
				catch ( Exception e ){
					e.printStackTrace();
				}
			}
			else{
				
				if ( result == null ){
					Toast.makeText(act, act.getResources().getString(R.string.error_de_conexion),Toast.LENGTH_SHORT).show();
				}
				else if ( result.equals("not_registered") && (facebook_hash.length() > 0 || gplus_hash.length() > 0) ){
					
					if ( facebook_hash.length() > 0 ){
						new WelcomeActivity.NewUser( act , email , name, null , facebook_hash , null , true ).execute();
					}
					else{
						new WelcomeActivity.NewUser( act , email , name, null , null , gplus_hash , true ).execute();
					}
					
				}
				else{
					
					if ( result.equals("invalid") ){
						Toast.makeText(act, act.getResources().getString(R.string.error_iniciar_sesion),Toast.LENGTH_SHORT).show();
					}
					else if ( result.equals("error") ){
						Toast.makeText(act, act.getResources().getString(R.string.error_desconocido),Toast.LENGTH_SHORT).show();
					}
					
				}
			}
		}
		
	}
	
	public static class ResetPassword extends ServerConnection{

		Context c;
		String email;
		ProgressDialog dialog;
		
		public ResetPassword( Context c , String email ){
			super();
			this.c = c;
			this.email = email;
			dialog = ProgressDialog.show(c, "",c.getResources().getString(R.string.enviando_correo_restauracion), true);
			
			init(c,"request_password_reset", new Object[]{ email });
		}
		
		@Override
		public void onComplete(String result) {
			
			dialog.dismiss();
			
			if ( result != null ){
				
				if ( result.equals("ok")){
					
					Toast.makeText(c, c.getResources().getString(R.string.enviado_correo_restauracion,email), Toast.LENGTH_SHORT).show();
				}
				else{
					Toast.makeText(c, R.string.error_desconocido, Toast.LENGTH_SHORT).show();
				}
				
			}
			else{
				Toast.makeText(c, R.string.error_de_conexion, Toast.LENGTH_SHORT).show();
			}
			
		}
		
	}
	
}
