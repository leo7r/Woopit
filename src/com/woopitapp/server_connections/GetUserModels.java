package com.woopitapp.server_connections;

import android.content.Context;
import com.woopitapp.services.ServerConnection;

/* User models */
public abstract class GetUserModels extends ServerConnection{
	
	int user_id;
	Context con;
	
	public GetUserModels( Context con , int user_id ){
		super();
		
		this.con = con;
		this.user_id = user_id;
		
		init(con,"get_user_models",new Object[]{ ""+user_id });
	}
	
	public abstract void onComplete( String result );
}

