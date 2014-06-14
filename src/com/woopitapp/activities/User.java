package com.woopitapp.activities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.woopitapp.Data;
import com.woopitapp.R;
import com.woopitapp.Utils;
import com.woopitapp.R.string;

import android.app.Activity;
import android.content.Context;

public class User {
	
	public int id;
	int facebook_user;
	int gplus_user;
	public String email;
	public String username;
	public String name;
	String image;
	
	// Solicitud enviada, rechazada o sin solicitud
	int request_status;
	
	public User( int id , String email , String username, String name , String image , int facebook_user , int gplus_user ){
		
		this.id = id;
		this.email = email;
		this.username = username;
		this.name = name;
		this.image = image;
		this.facebook_user = facebook_user;
		this.gplus_user = gplus_user;
	}
	
	/* Constructor para amigos */
	public User( int id , String username, String name , String image ){
		
		this.id = id;
		this.username = username;
		this.name = name;
		this.image = image;
	}
	
	public static User get( Context c ){
		
		Data data = new Data(c);
		data.open();
		User u = data.getUser();
		data.close();
				
		return u;
	}
	
	public void update_username( Context c , String username ){
		
		Data data = new Data(c);
		data.open();
		
		data.updateUser(username, null, null, -1, -1);		
		data.close();
	}
	
	/* Friends */

	public static class GetFriends extends ServerConnection{

		Activity act;
		int id_user;
		
		public GetFriends( Activity act , int id_user ){
			super();
			
			this.act = act;
			this.id_user = id_user;
			
			init(act,"get_friends",new Object[]{ ""+id_user });
		}

		@Override
		void onComplete(String result) {
			
			if ( result != null ){

				Data data = new Data(act);
				data.open();
				
				try {
					JSONArray friends = new JSONArray(result);

					
					for ( int i = 0 ; i < friends.length() ; ++i ){
						
						JSONObject friend = friends.getJSONObject(i);
						int id = friend.getInt("i");
						String name = friend.getString("n");
						String username = friend.getString("u");
						String image = friend.getString("m");
						
						data.insertFriend(id,username,name,image);
					}

				} catch (JSONException e) {					
					e.printStackTrace();
				}
				finally{
					data.close();
					Utils.sendBroadcast(con, R.string.broadcast_friends_list);
				}
			}
			
		}
	}

	
}
