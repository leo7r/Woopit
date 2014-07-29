package com.woopitapp.server_connections;

import android.app.Activity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.woopitapp.R;
import com.woopitapp.entities.User;
import com.woopitapp.services.ServerConnection;

public class InsertCoins  extends ServerConnection{
		int user_id;
		int cant_coins;
		int reason_string_id;
		Activity act;
		
		public InsertCoins( Activity act , int cant_coins , int reason_string_id ){
			super();
			
			this.act = act;
			this.user_id = User.get(act).id;
			this.cant_coins = cant_coins;
			this.reason_string_id = reason_string_id;
			init(act,"insert_user_coins",new Object[]{ ""+user_id, ""+cant_coins });
		}
		
		public void onComplete( String result ){
			
			if ( result != null && result.equals("ok")){
				
				LayoutInflater inflater = act.getLayoutInflater();
				View layout = inflater.inflate(R.layout.coin_toast,null);
				
				((TextView)layout.findViewById(R.id.text)).setText(act.getResources().getString(R.string.ganaste_moneda,cant_coins+"", cant_coins > 1 ? "s":"" ,act.getResources().getString(reason_string_id)));
				
				Toast toast = new Toast(act);
				toast.setGravity(Gravity.BOTTOM, 0, 100);
				toast.setDuration(1000*20);
				toast.setView(layout);
				toast.show();
				
			}
			
		}
}


