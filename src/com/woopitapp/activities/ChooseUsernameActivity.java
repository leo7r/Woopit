package com.woopitapp.activities;
import com.woopitapp.R;
import com.woopitapp.entities.User;
import com.woopitapp.services.Preferences;
import com.woopitapp.services.ServerConnection;
import com.woopitapp.services.Utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ChooseUsernameActivity extends Activity {
	
	EditText edit_username;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.choose_username);
		
		edit_username = (EditText) findViewById(R.id.username);
		
		TextWatcher validWatcher = new TextWatcher() {
	        @Override
	        public void afterTextChanged(Editable s) {
	        	showNextButton();
	        }

	        @Override
	        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

	        @Override
	        public void onTextChanged(CharSequence s, int start, int before, int count) {}
	    };
		
	    edit_username.addTextChangedListener(validWatcher);
		
	}
	
	public void showNextButton(){
		
		Button button = (Button) findViewById(R.id.next);
		
		if ( edit_username.getText().length() >= 3 ){
			button.setVisibility(View.VISIBLE);
		}
		else{
			button.setVisibility(View.GONE);
		}
	}
	
	public void setUsername( View v ){
		
		new SetUsername(this,edit_username.getText().toString()).execute();
	}
	
	class SetUsername extends ServerConnection{

		Activity act;
		String username;
		User user;
		ProgressDialog dialog;
		
		public SetUsername( Activity act , String username ){
			super();
			
			this.act = act;
			this.username = username.toLowerCase();
			this.user = User.get(getApplicationContext());
						
			dialog = ProgressDialog.show(act, "",act.getResources().getString(R.string.verificando_usuario), true);
			
			init(act,"set_username",new Object[]{ user.email , this.username });
		}

		@Override
		public
		void onComplete(String result) {
			
			dialog.dismiss();
			Utils.closeKeyboard(edit_username, getApplicationContext());
			
			if ( result == null ){
				Toast.makeText(act, R.string.error_de_conexion, Toast.LENGTH_SHORT).show();
				return;
			}
			
			if ( result.equals("ok") ){
				user.update_username(getApplicationContext(), this.username);
				
				Preferences.setFirstTime(act, false);
				Intent i = new Intent(act,MainActivity.class);
				act.startActivity(i);
				act.finish();
			}
			else{
				
				if ( result.equals("taken") ){
					Toast.makeText(act, getResources().getString(R.string.nombre_usuario_ya_tomado,username), Toast.LENGTH_SHORT).show();
				}
				else{
					Toast.makeText(act, R.string.error_desconocido, Toast.LENGTH_SHORT).show();
				}
				
			}
			
		}
	}

}
