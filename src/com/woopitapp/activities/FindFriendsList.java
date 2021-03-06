package com.woopitapp.activities;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.woopitapp.R;
import com.woopitapp.WoopitActivity;
import com.woopitapp.entities.User;
import com.woopitapp.fragments.HomeFragment;
import com.woopitapp.server_connections.InsertCoins;
import com.woopitapp.services.ServerConnection;
import com.woopitapp.services.Utils;

public class FindFriendsList extends WoopitActivity {

	ListView user_list;
	UserAdapter uAdapter;
    boolean share_launched = false , share_clicked = false;
	private int SHARE_REQUEST_CODE = 1;
	
	public void onCreate( Bundle savedInstanceState ){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.find_friends_list);
		
		Bundle extras = getIntent().getExtras();
		
		if(extras != null){
        	
        	user_list = (ListView) findViewById(R.id.user_list);
            String result = extras.getString("result");
            
            try {
				JSONArray users = new JSONArray(result);
				
				if ( users.length() > 0 ){
					ArrayList<User> list = new ArrayList<User>();
					
					for ( int i = 0 ; i < users.length() ; ++i ){
						
						JSONObject user = users.getJSONObject(i);
						int id = user.getInt("i");
						String name = user.getString("n");
						String username = user.getString("u");
						String image = user.getString("m");
						int is_friend = user.getInt("f");
						
						User u = new User(id,username,name,image);
						u.request_status = is_friend;
						list.add(u);
					}
					
					uAdapter = new UserAdapter(this, R.id.user_list, list );
			        user_list.setAdapter(uAdapter);
				}
				else{
					((LinearLayout) findViewById(R.id.no_friends)).setVisibility(View.VISIBLE);
				}

			} catch (JSONException e) {					
				e.printStackTrace();
			}
        }
	}
	
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		if ( requestCode == SHARE_REQUEST_CODE && share_launched && share_clicked ){
			
			Utils.onShareWoopit(getApplicationContext(), "SlidingMenu", "Compartido");
			new InsertCoins(this , 1 , R.string.por_compartir ).execute();
		}
		
		share_launched = false;
		share_clicked = false;
	}
    	
    public class UserAdapter extends ArrayAdapter<User>{
    	
		ArrayList<User> items;
		Context context;
		Filter filter;
		LayoutInflater inflater;

		public UserAdapter(Context context, int textViewResourceId, ArrayList<User> objects){
			super(context, textViewResourceId, objects);
			this.items = objects;
			this.context = context;
			inflater = (LayoutInflater)this.context.getSystemService("layout_inflater");
		}

		public View getView(final int position, View convertView, ViewGroup parent){
			
			if ( convertView == null ){
				convertView = inflater.inflate(R.layout.friend_item, null);
			}
			
			final User user = getItem(position);

			ImageView image = (ImageView) convertView.findViewById(R.id.image);
			TextView name = (TextView) convertView.findViewById(R.id.name);
			TextView username = (TextView) convertView.findViewById(R.id.username);
			final ImageView add_friend = (ImageView) convertView.findViewById(R.id.add_friend);		
			final ProgressBar loading = (ProgressBar) convertView.findViewById(R.id.loading);
			
			Utils.setUserImage(context, image, user.id);
			name.setText(user.name);
			username.setText("@"+user.username);
			add_friend.setVisibility(View.VISIBLE);
			loading.setVisibility(View.GONE);
						
			switch( user.request_status ){
			
			// No hay pedido de ser amigos o fue rechazada
			case -1:
			case 2:
				add_friend.setImageResource(R.drawable.add_friend);
				break;
			// Solicitud sin responder o aceptada
			case 0:
			case 1:
				add_friend.setImageResource(R.drawable.friend_added);
				break;
			
			}
			
			add_friend.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View arg0) {
					
					add_friend.setVisibility(View.GONE);
					loading.setVisibility(View.VISIBLE);
					
					new AddOrRejectFriend(user.id).execute();
				}
			});
			
			return convertView;
		}
		
		public User getItem( int position ){
			return items.get(position);
		}
		
    }

    public void inviteFriends( View v ){
		
		Intent sendIntent = new Intent();
		sendIntent.setAction(Intent.ACTION_SEND);
		sendIntent.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.compartir_woopit_texto));
		sendIntent.setType("text/plain");
		
		startActivityForResult(Intent.createChooser(sendIntent, getResources().getString(R.string.compartir_woopit)),SHARE_REQUEST_CODE);
		
		Utils.onShareWoopit(getApplicationContext(), "FindFriendsList", "Entrar");
		share_launched = true;
		
	}
    
 // Si ya esta la amistad la rompe, si no esta crea un request, si ya habia un request lo acepta.
 	class AddOrRejectFriend extends ServerConnection{
 		
 		int to_user;
 		
 		public AddOrRejectFriend( int to_user ){
 			super();
 			
 			this.to_user = to_user;
 			init(getApplicationContext(),"add_or_reject_friend",new Object[]{ User.get(getApplicationContext()).id , to_user });
 		}

 		@Override
 		public void onComplete(String result) {
 			
 			if ( result != null && result.length() > 0 ){
 				
 				int new_status = Integer.parseInt(result);

				Utils.onFriendsAddOrReject(getApplicationContext(), "FindFriendsList", new_status == 1 ? "Agregar" : "Borrar" , to_user);
				
 				// Busco el usuario y cambio su request_status
 				for ( User u : uAdapter.items ){
 					if ( u.id == to_user ){
 						u.request_status = new_status;
 					}
 				}
 				
 				if ( new_status == -1 || new_status == 1 ){
 			        // Refresco lista de amigos
 			        new User.GetFriends(getApplicationContext(), User.get(getApplicationContext()).id).execute();
 				}
 				
 				uAdapter.notifyDataSetChanged();
 								
 				Log.i("Add friend", result);		
 			}
 			else{
 				Toast.makeText(getApplicationContext(), getResources().getString(R.string.error_de_conexion),Toast.LENGTH_SHORT).show();
 			}
 		}
 		
 	}   
    
}
        

