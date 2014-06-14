package com.woopitapp.logic;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.woopitapp.R;
import com.woopitapp.R.string;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

public class User {
	
	public int id;
	int facebook_user;
	int gplus_user;
	public String email;
	public String username;
	public String name;
	String image;
	
	// Solicitud enviada, rechazada o sin solicitud
	public int request_status;
	
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

		int id_user;
		Context con;
		
		public GetFriends( Context con, int id_user ){
			super();
			
			this.con = con;
			this.id_user = id_user;
			
			init(con,"get_friends",new Object[]{ ""+id_user });
		}

		@Override
		public void onComplete(String result) {
			
			if ( result != null ){

				Data data = new Data(con);
				data.open();
				
				try {
					JSONArray friends = new JSONArray(result);
					int[] ids = new int[friends.length()];
					
					for ( int i = 0 ; i < friends.length() ; ++i ){
						
						JSONObject friend = friends.getJSONObject(i);
						int id = friend.getInt("i");
						String name = friend.getString("n");
						String username = friend.getString("u");
						String image = friend.getString("m");
						
						data.insertFriend(id,username,name,image);
						ids[i] = id;
					}
					
					data.deleteFriendsNotIn(ids);

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

	/* Friend Requests */
	public static class GetFriendRequest extends ServerConnection{

		int id_user;
		Context con;
		
		public GetFriendRequest( Context con ){
			super();
			
			this.con = con;
			
			init(con,"get_friend_requests",new Object[]{ ""+User.get(con).id });
		}

		@Override
		public void onComplete(String result) {
			
			if ( result != null ){

				Data data = new Data(con);
				data.open();
				
				try {
					Log.i("FR", result);
					JSONArray requests = new JSONArray(result);
					
					for (int i = 0 ; i < requests.length() ; ++i ){
						JSONObject request = requests.getJSONObject(i);
						int id = request.getInt("i");
						int from_user = request.getInt("f");
						String username = request.getString("u");
						String name = request.getString("n");
						
						data.insertFriendRequest(id, from_user, username, name);
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
