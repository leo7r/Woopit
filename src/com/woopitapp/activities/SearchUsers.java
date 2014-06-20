package com.woopitapp.activities;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.woopitapp.R;
import com.woopitapp.R.drawable;
import com.woopitapp.R.id;
import com.woopitapp.R.layout;
import com.woopitapp.R.string;
import com.woopitapp.entities.User;
import com.woopitapp.services.ServerConnection;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class SearchUsers extends Activity {

	String query;
	ListView user_list;
	UserAdapter uAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
						
		Bundle extras = getIntent().getExtras();
		setContentView(R.layout.search_users);
		
		user_list = (ListView) findViewById(R.id.user_list);
		
		if ( extras.containsKey("query") ){
			
			query = extras.getString("query");

			TextView title = (TextView) findViewById(R.id.title);
			title.setText(getResources().getString(R.string.buscar_amigos,query));
			
			new SearchUserTask(query).execute();
		}
		
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
			ImageView add_friend = (ImageView) convertView.findViewById(R.id.add_friend);			
			
			image.setImageBitmap(user.getImage(getApplicationContext()));
			name.setText(user.name);
			username.setText("@"+user.username);
			add_friend.setVisibility(View.VISIBLE);
						
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
					new AddOrRejectFriend(user.id).execute();
				}
			});
			
			return convertView;
		}
		
		public User getItem( int position ){
			return items.get(position);
		}
		
    }
    
	class SearchUserTask extends ServerConnection{

		String query;
		
		public SearchUserTask( String query ){
			super();
			
			this.query = query;
		
			init(getApplicationContext(),"search_users",new Object[]{ User.get(getApplicationContext()).id , query });
		}

		@Override
		public
		void onComplete(String result) {
			
			ProgressBar loading = (ProgressBar) findViewById(R.id.loading);
			loading.setVisibility(View.GONE);
			
			if ( result != null && result.length() > 0 ){
								
				try {
					JSONArray users = new JSONArray(result);

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
					
					uAdapter = new UserAdapter(getApplicationContext(), R.id.user_list, list );
			        user_list.setAdapter(uAdapter);	

				} catch (JSONException e) {					
					e.printStackTrace();
				}
				
			}
			else{
				Toast.makeText(getApplicationContext(), getResources().getString(R.string.error_de_conexion),Toast.LENGTH_SHORT).show();
			}
		}
		
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
