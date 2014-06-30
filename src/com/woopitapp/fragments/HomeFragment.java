package com.woopitapp.fragments;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ParseException;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TextView.OnEditorActionListener;

import com.emilsjolander.components.stickylistheaders.StickyListHeadersAdapter;
import com.emilsjolander.components.stickylistheaders.StickyListHeadersListView;
import com.woopitapp.R;
import com.woopitapp.activities.MainActivity;
import com.woopitapp.activities.MessageActivity;
import com.woopitapp.activities.TestActivity;
import com.woopitapp.entities.Message;
import com.woopitapp.entities.Model;
import com.woopitapp.entities.User;
import com.woopitapp.fragments.FriendsFragment.AddOrRejectFriend;
import com.woopitapp.services.Data;
import com.woopitapp.services.FriendRequest;
import com.woopitapp.services.ServerConnection;
import com.woopitapp.services.Utils;

public class HomeFragment extends Fragment {
	
	private static final int REQUEST_DOWNLOAD_MODEL = 0;
	GridView message_list;
	ListAdapter mAdapter;
	TabHost tabHost;
	ArrayList<Object> messages_list;
	EditText search_message;
	
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	
        View view = (LinearLayout)inflater.inflate(R.layout.home_fragment, container, false);

        message_list = (GridView) view.findViewById(R.id.messages_list);
        search_message = (EditText) view.findViewById(R.id.search_users);
        
        /*search_message.setOnEditorActionListener(new OnEditorActionListener(){

			@Override
			public boolean onEditorAction(TextView tv, int actionId, KeyEvent key) {
				
				if ( actionId == EditorInfo.IME_ACTION_SEARCH ){
					//searchMessages(friend_message.getText().toString());
					return true;
				}
				
				return false;
			}
		});
        */
      

      /*  Button boton = (Button) view.findViewById(R.id.boton_prueba);
        boton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				
				Intent i = new Intent(getActivity(),TestActivity.class);
				startActivity(i);				
			}
		});*/
        new get_messages(this.getActivity().getApplicationContext(),User.get(this.getActivity().getApplicationContext()).id).execute();
        return view;
    }
    public class get_messages extends ServerConnection{
    	Context con;
    	int user_id;
    	public get_messages(Context con, int user_id){
    		this.con = con;
    		this.user_id = user_id;
    		init(con,"get_messages",new Object[]{ ""+user_id });
    	}

		@Override
		public void onComplete(String result) {
			if ( result != null && result.length() > 0 ){
				
				try {
					JSONArray messages = new JSONArray(result);
					messages_list = new ArrayList<Object>();
					
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
						SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
						Date date = null;
						try {
							date = formatter.parse(message.getString("d"));

						} catch (ParseException e) {
							e.printStackTrace();
						}
						
						int status = message.getInt("e");
						
						Message m = new Message(id,sender,recevier,model,title,text,date,latitud,longitud,status,name);
						
						messages_list.add(m);
					}
					
				}catch(Exception e){
					e.printStackTrace();
				}
			}
			
		    mAdapter = new ListAdapter(con, R.id.messages_list, messages_list);
		    Log.e("erorsss", ""+(message_list == null ));
		    message_list.setAdapter(mAdapter);
			
		}
    }
    @Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

	     if ( requestCode == REQUEST_DOWNLOAD_MODEL ) {
	          if (resultCode == Activity.RESULT_OK) {
	        	  int model = Integer.parseInt(data.getStringExtra("model"));
	        	  double latitud = Double.parseDouble(data.getStringExtra("latitud"));
	        	  double longitud = Double.parseDouble(data.getStringExtra("longitud"));
	        	  int messageId = Integer.parseInt(data.getStringExtra("message"));
	        	  int status = Integer.parseInt(data.getStringExtra("status"));
	        	  if(status == 0){
	        		  new UpdateMessageStatus(this.getActivity().getApplicationContext(), messageId).execute();
	        	  }
	        	  verMensaje(model,latitud,longitud);
	        	  
	          }
	      }
	 }
    public void verMensaje(int modelo, double latitud, double longitud){
    		
			Intent newMessagei =  new  Intent(this.getActivity().getApplicationContext(),MessageActivity.class);
			newMessagei.putExtra("latitud", latitud+"");
			newMessagei.putExtra("longitud",longitud+"");
			newMessagei.putExtra("modelo",modelo+"");			
			startActivity(newMessagei);
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
 			}else{
 				convertView.setOnClickListener(null);
 			}
 			
 			
 			ImageView imagen = (ImageView) convertView.findViewById(R.id.image);
 			if(item.receiver == user_id){
 				imagen.setImageResource(R.drawable.mensaje_recibido);
				convertView.setOnClickListener(new OnClickListener(){

					@Override
					public void onClick(View v) {
						Intent i = new Intent(getActivity(),TestActivity.class);
						i.putExtra("model",item.model+"");
						i.putExtra("latitud",item.latitud+"");
						i.putExtra("longitud", item.longitud+"");
						i.putExtra("message", item.id+"");
						i.putExtra("status", item.status+"");
						i.putExtra("caller", "HomeFragment");
						startActivityForResult(i,REQUEST_DOWNLOAD_MODEL);						
					}
				});

 				}else{
 				if(item.sender == user_id){ 
 				
 					if(item.status == 0){
 						imagen.setImageResource(R.drawable.mensaje_enviado);
 		
 					}else{
 						imagen.setImageResource(R.drawable.mensaje_leido);
 					}
 				}
 			}
 			TextView name = (TextView) convertView.findViewById(R.id.name);
 			TextView fecha = (TextView) convertView.findViewById(R.id.date);
 			final ImageView confirm_friend = (ImageView) convertView.findViewById(R.id.status);
 			confirm_friend.setVisibility(View.VISIBLE);

 			DateFormat pstFormat = new SimpleDateFormat("HH:mm  MM/dd/yyyy");
 			pstFormat.setTimeZone(TimeZone.getDefault());
 			String fechaVal = pstFormat.format(item.date);
 			
 			
 			name.setText(item.name);
 			fecha.setText(fechaVal);
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
        
