package com.woopitapp.server_connections;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Window;
import android.view.WindowManager;

import com.woopitapp.R;
import com.woopitapp.services.ServerConnection;

public abstract class InsertCoins  extends ServerConnection{
		int user_id;
		int cant_coins;
		Activity act;
		
		public InsertCoins( Activity act , int user_id, int cant_coins){
			super();
			
			this.act = act;
			this.user_id = user_id;
			this.cant_coins = cant_coins;
			init(act,"insert_user_coins",new Object[]{ ""+user_id, ""+cant_coins });
		}
		
		public void showDialog(){
			
			AlertDialog.Builder builder = new AlertDialog.Builder(act);
	        LayoutInflater inflater = act.getLayoutInflater();
	        builder.setView(inflater.inflate(R.layout.self_dismiss_dialog, null));
			AlertDialog dialog = builder.create();
			
			dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			WindowManager.LayoutParams wmlp = dialog.getWindow().getAttributes();

			wmlp.gravity = Gravity.TOP | Gravity.LEFT;
			wmlp.x = 100;   //x position
			wmlp.y = 100;   //y position

			dialog.show();
		}
		
		public void onComplete( String result ){
			
			if ( result != null && result.equals("ok")){
				showDialog();
			}
			
		}
}


