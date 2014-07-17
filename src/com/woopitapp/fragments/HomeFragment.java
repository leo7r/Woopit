package com.woopitapp.fragments;

import java.util.ArrayList;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TabHost;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;
import com.woopitapp.R;
import com.woopitapp.activities.MapSentMessageActivity;
import com.woopitapp.activities.MapUnMessageActivity;
import com.woopitapp.activities.MessageActivity;
import com.woopitapp.entities.Message;
import com.woopitapp.entities.User;
import com.woopitapp.services.ServerConnection;
import com.woopitapp.services.Utils;

public class HomeFragment extends Fragment {
	
	private static final int REQUEST_DOWNLOAD_MODEL = 0;
	public static final int REQUEST_MESSAGE = 1;
	ListView message_list;
	ListAdapter mAdapter;
	TabHost tabHost;
	ArrayList<Message> messages_list;
	EditText search_message;
	int page = 0;
	boolean loadingMore;
	LinearLayout list_loading;
	LayoutInflater li;
	ViewGroup viewGroup;
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	li = inflater;
    	viewGroup = container;
        View view = (LinearLayout)inflater.inflate(R.layout.home_fragment, container, false);
		list_loading = (LinearLayout) View.inflate(getActivity(), R.layout.list_footer_loading, null);
		message_list = (ListView) view.findViewById(R.id.messages_list);
		message_list.addFooterView(list_loading);

		PauseOnScrollListener listener = new PauseOnScrollListener(Utils.getImageLoader(getActivity().getApplicationContext()), true, true, new OnScrollListener(){

			@Override
			public void onScroll(AbsListView list, int firstVisible, int visibleItems, int totalItems) {
				
				int lastInScreen = firstVisible + visibleItems;
				
				
				if( lastInScreen == totalItems && totalItems != 0 && !loadingMore && mAdapter != null ){
					
					page++;
					Context c = getActivity().getApplicationContext();
					list_loading.setVisibility(View.VISIBLE);
					new get_messages( c,User.get(c).id, page ).execute();
					loadingMore = true;
					
				}
			}

			@Override
			public void onScrollStateChanged(AbsListView arg0, int arg1) {}
		});

        message_list.setOnScrollListener(listener);
        
        return view;
    }
    
    public void onStart(){
    	super.onStart();
    	if(mAdapter == null){
    		Context c = this.getActivity().getApplicationContext();
    		new get_messages(c,User.get(c).id,page).execute();
    	}

    	
    
    }
    
    public void onStop(){
    	super.onStop();


    }
    
    public void onDestroyView(){
    	super.onDestroyView();
    	page = 0;
    	if(mAdapter != null){
    		mAdapter.clear();
    	}
    	mAdapter = null;
    	

    }
    
    public void refresh(){
    	((ImageView) getView().findViewById(R.id.notSignalImage)).setVisibility(View.GONE);
		((TextView) getView().findViewById(R.id.reload_button)).setVisibility(View.GONE);		
		if(mAdapter == null){
			
    		Context c = this.getActivity().getApplicationContext();
    		new get_messages(c,User.get(c).id,page).execute();
    		
    	}
    }

    public class get_messages extends ServerConnection{
    	
    	Context con;
    	int user_id;
    	ProgressBar loader;
    	int page;
    	
    	public get_messages(Context con, int user_id, int page){
    		this.con = con;
    		this.user_id = user_id;
    		this.page = page;

    		loader = (ProgressBar) getView().findViewById(R.id.loader);
    		if(mAdapter == null){
    			loader.setVisibility(View.VISIBLE);
    		}
    		((ImageView) getView().findViewById(R.id.notSignalImage)).setVisibility(View.GONE);
    		((TextView)  getView().findViewById(R.id.reload_button)).setVisibility(View.GONE);	
    		init(con,"get_messages",new Object[]{ ""+user_id,""+ page });
    	}

		@Override
		public void onComplete(String result) {
			
		
			loader.setVisibility(View.GONE);
			

			messages_list = new ArrayList<Message>();
			
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
						String modelName = message.getString("mn");
						int status = message.getInt("e");
						
						Message m = new Message(id,sender,recevier,model,title,text,date,latitud,longitud,status,name,modelName);
						
						messages_list.add(m);
						
					}

					if ( messages_list.size() > 0 ){

			            loadingMore = false;
					}
					else{
						loadingMore = true;
						list_loading.setVisibility(View.GONE);
					}
				}catch(Exception e){
					e.printStackTrace();
				}
				
				if(mAdapter == null){
				    mAdapter = new ListAdapter(con, R.id.messages_list, messages_list);
				    message_list.setAdapter(mAdapter);
				}else{
					
					for (Message m : messages_list){
						mAdapter.add(m);
					}
					
					mAdapter.notifyDataSetChanged();
				}
				
				if ( messages_list.size() == 0 && page == 0){
					Date date = new Date();
					Message m = new Message(0,1,User.get(con).id,1,"","Bienvenido a Woopit :D",date,500,500,0,"Woopit","Welcome Woop");
					messages_list.add(m);
		
					if ( getView() != null ){
						((TextView) getView().findViewById(R.id.welcome_message)).setVisibility(View.VISIBLE);
					}
					if ( loader != null ){
						loader.setVisibility(View.GONE);
					}
					if ( list_loading != null ){
						list_loading.setVisibility(View.GONE);
					}
					mAdapter = null;
					
				}
			
			
			
			
			}else{
				((ImageView) getView().findViewById(R.id.notSignalImage)).setVisibility(View.VISIBLE);
				TextView reload = (TextView) getView().findViewById(R.id.reload_button);		
				reload.setVisibility(View.VISIBLE);
				if(mAdapter != null){
					mAdapter.clear();
				}
				if(list_loading != null){
					list_loading.setVisibility(View.GONE);
				}
				reload.setOnClickListener(new View.OnClickListener() {
				    public void onClick(View v) {
				    	refresh();
				    }
				});
			}
				
			
			
		   
			
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
				 String nombre = extras.getString("nombre");
    			 i.putExtra("latitud",latitud);
				 i.putExtra("longitud",longitud);
				 i.putExtra("nombre", nombre);
				 startActivity(i);
    		}
    	}
	   
	 }
    
    public void updateContent(){

    	page = 0;
    	mAdapter = null;
    	
        Context c = this.getActivity().getApplicationContext();
        new get_messages(c,User.get(c).id,page).execute();
    }
    
    public void verMensajeRecibido( Message message ){
    	
	    if( message.status == 0){
		  new UpdateMessageStatus(this.getActivity().getApplicationContext(), message.id).execute();
		}
		Intent newMessagei =  new  Intent(getActivity(),MessageActivity.class);
		newMessagei.putExtra("latitud", message.latitud+"");
		newMessagei.putExtra("longitud",message.longitud+"");
		newMessagei.putExtra("modelo",message.model);
		newMessagei.putExtra("text", message.text);
		newMessagei.putExtra("nombre", message.name);
		getActivity().startActivityForResult(newMessagei,REQUEST_MESSAGE);
		
		Utils.onMessageView(getActivity(), message, "HomeFragment");
	}
  
    public void verMensajeEnviado( Message message ){
    	Log.e("MAPA", "MAPA");
		Intent newMessagei =  new  Intent(getActivity(),MapSentMessageActivity.class);
		newMessagei.putExtra("latitud", message.latitud);
		newMessagei.putExtra("longitud",message.longitud);
		newMessagei.putExtra("modelName",message.modelName+"");
		newMessagei.putExtra("nombre", message.name);
		startActivity(newMessagei);
		
		Utils.onMessageView(getActivity(), message, "HomeFragment");
	}
    
    class UpdateMessageStatus extends ServerConnection{

    	Context con;
    	int user_id;
    	int page;
    	
    	public UpdateMessageStatus( Context con ,int message_id){
    		super();
    		
    		this.con = con;
    		this.user_id = user_id;
    		this.page = page;
    		init(con,"update_message_status",new Object[]{ ""+ message_id});
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
    
    public class ListAdapter extends ArrayAdapter<Message>{
		
 		ArrayList<Message> l_items;
 		Context context;
 		Filter filter;
 		LayoutInflater infalInflater;

 		public ListAdapter(Context context, int textViewResourceId, ArrayList<Message> objects) {
 			
 			super(context, textViewResourceId, objects);
 			this.l_items = objects;
 			this.context = context;
 			infalInflater = (LayoutInflater) context.getSystemService("layout_inflater");
 		}
 		
 		@Override
 		public View getView(final int position, View convertView, ViewGroup parent) {
 			

 			int user_id = User.get(this.context).id;
 			final Message item = getItem(position);
 			
 			if ( convertView == null ){
 				convertView = infalInflater.inflate(R.layout.message_item, null);
 			}
 			convertView.setOnClickListener(null);
 			
 			ImageView imagen = (ImageView) convertView.findViewById(R.id.image);
 			
 			if(item.receiver == user_id){
 				if(item.status == 0){
 					//aqui el otro icono de mensaje nuevo; 					
 				}
 				imagen.setImageResource(R.drawable.message_received);
				convertView.setOnClickListener(new OnClickListener(){

					@Override
					public void onClick(View v) {
						verMensajeRecibido(item);
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
					convertView.setOnClickListener(new OnClickListener(){

							@Override
							public void onClick(View v) {
								verMensajeEnviado(item);
							}
						});
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
 		
 		public Message getItem( int pos ){
 			return l_items.get(pos);
 		}

	
 	}
	
}
        
