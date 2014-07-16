package com.woopitapp.server_connections;

import android.content.Context;

import com.woopitapp.services.ServerConnection;

public abstract class InsertCoins  extends ServerConnection{
		int user_id;
		int cant_coins;
		Context con;
		
		public InsertCoins( Context con , int user_id, int cant_coins){
			super();
			
			this.con = con;
			this.user_id = user_id;
			this.cant_coins = cant_coins;
			init(con,"insert_user_coins",new Object[]{ ""+user_id, ""+cant_coins });
		}
		
		public abstract void onComplete( String result );
}


