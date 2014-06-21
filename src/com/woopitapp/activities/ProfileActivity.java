package com.woopitapp.activities;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.woopitapp.R;
import com.woopitapp.entities.Model;
import com.woopitapp.entities.User;
import com.woopitapp.services.Data;
import com.woopitapp.services.ServerConnection;
import com.woopitapp.services.Utils;

public class ProfileActivity extends Activity {

	GridView models_list;
	ModelAdapter mAdapter;
	User current_user;
	
	boolean is_my_profile = false;

	// Broadcast receivers
	ProfileChangeReceiver p_receiver;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.profile);
		
		Bundle extras = getIntent().getExtras();
		User user = User.get(this);
		
		if ( extras.containsKey("id_user") ){
			
			ImageView edit_profile = (ImageView) findViewById(R.id.edit_profile);
			
			int id_user = extras.getInt("id_user");
			
			if ( id_user == user.id ){
				is_my_profile = true;
				current_user = user;
				setProfile();
				
				Data data = new Data(this);
		        data.open();
		        ArrayList<Model> list = data.getModels();
		        data.close();
		        
		        edit_profile.setVisibility(View.VISIBLE);
		        edit_profile.setOnClickListener(new OnClickListener(){

					@Override
					public void onClick(View v) {
						goEditProfile(v);
					}
				});
		        
		        setModelList( list );				
			}
			else{
				is_my_profile = false;
				
				edit_profile.setVisibility(View.INVISIBLE);
				
				new GetUserInfo(getApplicationContext(),id_user).execute();
			}
			
	        /* Recibe cambios en lista de amigos */
			p_receiver = new ProfileChangeReceiver();
	        registerReceiver(p_receiver,new IntentFilter(this.getString(R.string.broadcast_profile_update)));
			
		}
		
	}
	
	protected void onDestroy(){
		super.onDestroy();
		
		if ( p_receiver != null ){
			unregisterReceiver(p_receiver);
		}
	}
	
	public void setProfile(){
		
        ImageView image = (ImageView) findViewById(R.id.image);
        TextView name = (TextView) findViewById(R.id.name);
        TextView username = (TextView) findViewById(R.id.username);
        
        Utils.setUserImage(getApplicationContext(), image, current_user.id);
        
        name.setText(current_user.name);
        username.setText("@"+current_user.username);
                
        new GetUserModels( getApplicationContext() , current_user.id ).execute();
        
        RelativeLayout progress = (RelativeLayout) findViewById(R.id.loading_layout);
        progress.setVisibility(View.GONE);
	}
	
	public void setModelList( ArrayList<Model> list ){
        models_list = (GridView) findViewById(R.id.models_list);
        mAdapter = new ModelAdapter(this, R.id.models_list, list );
        models_list.setAdapter(mAdapter);
	}
	
	public void goEditProfile( View v ){
		
		Intent i = new Intent(this,EditProfileActivity.class);
		
		startActivity(i);
	}
	
    public class ModelAdapter extends ArrayAdapter<Model>{
    	
		ArrayList<Model> items;
		Context context;
		Filter filter;
		LayoutInflater inflater;

		public ModelAdapter(Context context, int textViewResourceId, ArrayList<Model> objects){
			super(context, textViewResourceId, objects);
			this.items = objects;
			this.context = context;
			inflater = (LayoutInflater)this.context.getSystemService("layout_inflater");
		}
		
		public View getView(final int position, View convertView, ViewGroup parent){
			
			if ( convertView == null ){
				convertView = inflater.inflate(R.layout.model_item, null);
			}
			
			final Model model = getItem(position);

			TextView name = (TextView) convertView.findViewById(R.id.name);
			TextView price = (TextView) convertView.findViewById(R.id.price);
			ImageView image = (ImageView) convertView.findViewById(R.id.image);
			
			name.setText(model.name);
			price.setText(model.price);
			image.setImageResource(R.drawable.model_image);
			
			return convertView;
		}
		
		public Model getItem( int position ){
			return items.get(position);
		}
		
    }
    
    class GetUserModels extends ServerConnection{
    	
    	Context con;
    	int user_id;
    	
    	public GetUserModels( Context con , int user_id ){
    		super();
    		
    		this.con = con;
    		this.user_id = user_id;
    		init(con,"get_created_models",new Object[]{ ""+user_id });
    	}

		@Override
		public void onComplete(String result) {
			
			if ( result != null && result.length() > 0 ){
				
				Log.i("Models", result);
				
				Data data = new Data(getApplicationContext());
				data.open();
				
				try {
					JSONArray models = new JSONArray(result);
					ArrayList<Model> models_list = new ArrayList<Model>();
					
					for ( int i = 0 ; i < models.length() ; ++i ){
						
						JSONObject model = models.getJSONObject(i);
						int id = model.getInt("i");
						String name = model.getString("n");
						String price = model.getString("p");
						
						if ( is_my_profile ){
							data.insertModel(id, name, price);
						}
						else{
							models_list.add(new Model(id,name,price,id+"",true));
						}
					}
					
					if ( is_my_profile ){
						models_list = data.getModels();
					}

					setModelList(models_list);
					
					//Utils.sendBroadcast(getApplicationContext(), R.string.broadcast_profile_models_list);
					
				} catch (JSONException e) {
					
					e.printStackTrace();
				}
				finally{
					data.close();
				}
				
				
			}
			else{
				Toast.makeText(getApplicationContext(), R.string.error_de_conexion, Toast.LENGTH_SHORT).show();
			}
			
		}
    	
    }

    class GetUserInfo extends ServerConnection{

    	Context con;
    	int user_id;
    	
    	public GetUserInfo( Context con , int user_id ){
    		super();
    		
    		this.con = con;
    		this.user_id = user_id;
    		init(con,"get_user_info",new Object[]{ ""+user_id });
    	}
    	
		@Override
		public void onComplete(String result) {
			
			if ( result != null ){
				
				try{
					JSONObject user = new JSONObject(result);
				
					int id = user.getInt("i");
					String name = user.getString("n");
					String username = user.getString("u");
					String image = user.getString("m");
					
					User u = new User(id,username,name,image);
					current_user = u;
					setProfile();
				}
				catch( Exception e ){
					e.printStackTrace()	;
				}	
			}
			else{
				Toast.makeText(getApplicationContext(), R.string.error_de_conexion, Toast.LENGTH_SHORT).show();
			}				
		}
    	
    }
    
    /* Broadcasts receivers */
    public class ProfileChangeReceiver extends BroadcastReceiver {
        
    	@Override
    	public void onReceive(Context context, Intent intent) {
    		
    		if ( is_my_profile ){
    			
        		Data data = new Data(context);
        		data.open();
        		current_user = User.get(context);
        		data.close();
        		setProfile();
    		}
    	}
      
    }
     
}
