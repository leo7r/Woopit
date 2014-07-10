package com.woopitapp.fragments;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.net.ParseException;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TabHost;
import android.widget.TextView;

import com.woopitapp.R;
import com.woopitapp.activities.MapUnMessageActivity;
import com.woopitapp.activities.MessageActivity;
import com.woopitapp.entities.Message;
import com.woopitapp.entities.User;
import com.woopitapp.services.ServerConnection;
import com.woopitapp.services.Utils;

public class HomeFragment extends Fragment {
	
	private static final int REQUEST_DOWNLOAD_MODEL = 0;
	private static final int REQUEST_MESSAGE = 1;
	ListView message_list;
	ListAdapter mAdapter;
	TabHost tabHost;
	ArrayList<Object> messages_list;
	EditText search_message;
	
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	
        View view = (LinearLayout)inflater.inflate(R.layout.home_fragment, container, false);

        message_list = (ListView) view.findViewById(R.id.messages_list);
        search_message = (EditText) view.findViewById(R.id.search_users);
        
        return view;
    }
    
    public void onStart(){
    	super.onStart();

        new get_messages(this.getActivity().getApplicationContext(),User.get(this.getActivity().getApplicationContext()).id).execute();
    }
    
    public class get_messages extends ServerConnection{
    	
    	Context con;
    	int user_id;
    	ProgressBar loader;
    	
    	public get_messages(Context con, int user_id){
    		this.con = con;
    		this.user_id = user_id;

    		if ( getView() != null ){
    			loader = (ProgressBar) getView().findViewById(R.id.loader);
    		}
    		init(con,"get_messages",new Object[]{ ""+user_id });
    	}

		@Override
		public void onComplete(String result) {
			
			if ( loader != null ){
				loader.setVisibility(View.GONE);
			}

			messages_list = new ArrayList<Object>();
			
			if ( result != null && result.length() > 0 ){

				try {
					JSONArray messages = new JSONArray(result);
					
					for ( int i = 0 ; i < messages.length() ; ++i ){
						
						JSONObject message = messages.getJSONObject(i);
						int id = message.getInt("i");
						int sender = Integer.parseInt(message.getString("s"));
						int recevier = Integer.parseInt(message.getString("r"));
						int model = Integer.parseInt(message.getString("m"));
						String title = message.getString("ti");
						String text = message.getString("te");
						double latitud = Double.parseDouble(message.getString("la"));
						double longitud = Double.parseDouble(message.getString("lo"));
						String name = message.getString("n");
						long timestamp = message.getLong("d");
						Date date = new Date(timestamp);
						
						int status = message.getInt("e");
						
						Message m = new Message(id,sender,recevier,model,title,text,date,latitud,longitud,status,name);
						
						messages_list.add(m);
						
					}
					
				}catch(Exception e){
					e.printStackTrace();
				}
			}
			
			if ( messages_list.size() == 0 ){
				Date date = new Date();
				Message m = new Message(0,1,User.get(con).id,1,"","Bienvenido a Woopit :D",date,500,500,0,"Woopit");
				messages_list.add(m);
				((TextView) getView().findViewById(R.id.welcome_message)).setVisibility(View.VISIBLE);
			}
			
		    mAdapter = new ListAdapter(con, R.id.messages_list, messages_list);
		    message_list.setAdapter(mAdapter);
			
		}
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if(requestCode == REQUEST_MESSAGE){
    		if(resultCode == this.getActivity().RESULT_OK){
    			 Intent i = new Intent(getActivity().getApplicationContext(),MapUnMessageActivity.class);
				 Bundle extras = data.getExtras();
				 double latitud = extras.getDouble("latitud");
				 double longitud  = extras.getDouble("longitud");
    			 i.putExtra("latitud",latitud);
				 i.putExtra("longitud",longitud);
				 startActivity(i);
    		}
    	}
	   
	 }
    
    public void verMensaje( Message message ){
    	
	    if( message.status == 0){
		  new UpdateMessageStatus(this.getActivity().getApplicationContext(), message.id).execute();
		}
		Intent newMessagei =  new  Intent(getActivity(),MessageActivity.class);
		newMessagei.putExtra("latitud", message.latitud+"");
		newMessagei.putExtra("longitud",message.longitud+"");
		newMessagei.putExtra("modelo",message.model);
		newMessagei.putExtra("text", message.text);
		
		startActivityForResult(newMessagei,REQUEST_MESSAGE);
	}
    
    class UpdateMessageStatus extends ServerConnection{

    	Context con;
    	int user_id;
    	
    	public UpdateMessageStatus( Context con ,int message_id ){
    		super();
    		
    		this.con = con;
    		this.user_id = user_id;
    		init(con,"update_message_status",new Object[]{ ""+ message_id });
    	}
    	
		@Override
		public void onComplete(String result) {
			
			if ( result != null ){
				
				/*try{
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
				}	*/
			}else{
				//Toast.makeText(getApplicationContext(), R.string.error_de_conexion, Toast.LENGTH_SHORT).show();
			}				
		}
    	
    }
    
    public class ListAdapter extends ArrayAdapter<Object>{
		
 		ArrayList<Object> l_items;
 		Context context;
 		Filter filter;
 		LayoutInflater infalInflater;

 		public ListAdapter(Context context, int textViewResourceId, ArrayList<Object> objects) {
 			
 			super(context, textViewResourceId, objects);
 			this.l_items = objects;
 			this.context = context;
 			infalInflater = (LayoutInflater) context.getSystemService("layout_inflater");
 		}
 		
 		@Override
 		public View getView(final int position, View convertView, ViewGroup parent) {
 			

 			int user_id = User.get(this.context).id;
 			final Message item = (Message)getItem(position);
 			
 			if ( convertView == null ){
 				convertView = infalInflater.inflate(R.layout.message_item, null);
 			}

 			
 			ImageView imagen = (ImageView) convertView.findViewById(R.id.image);
 			
 			if(item.receiver == user_id){
 				if(item.status == 0){
 					//aqui el otro icono de mensaje nuevo; 					
 				}
 				imagen.setImageResource(R.drawable.message_received);
				convertView.setOnClickListener(new OnClickListener(){

					@Override
					public void onClick(View v) {
						verMensaje(item);
					}
				});

 			}
 			else{
 				if(item.sender == user_id){ 
 				
 					if(item.status == 0){
 						imagen.setImageResource(R.drawable.message_sent);
 		
 					}else{
 						imagen.setImageResource(R.drawable.message_viewed);
 					}
 					convertView.setOnClickListener(null);
 				}
 			}
 			TextView name = (TextView) convertView.findViewById(R.id.name);
 			TextView fecha = (TextView) convertView.findViewById(R.id.date);
 			final ImageView confirm_friend = (ImageView) convertView.findViewById(R.id.status);
 			confirm_friend.setVisibility(View.VISIBLE);
 			 			
 			name.setText(item.name);
 			fecha.setText(Utils.getTimeAgo(item.date.getTime()/1000, context));
 			confirm_friend.setVisibility(View.GONE);

 			return convertView;
 		}
 		
 		@Override
 		public int getCount() {

 			return l_items != null ? l_items.size() : 0;
 		}
 		
 		public Object getItem( int pos ){
 			return l_items.get(pos);
 		}

	
 	}
	
}
        
