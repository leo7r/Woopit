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
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;
import com.scythe.bucket.BucketListAdapter;
import com.woopitapp.R;
import com.woopitapp.WoopitActivity;
import com.woopitapp.entities.Model;
import com.woopitapp.entities.User;
import com.woopitapp.fragments.HomeFragment.get_messages;
import com.woopitapp.services.ServerConnection;
import com.woopitapp.services.Utils;

public class ModelListActivity extends WoopitActivity {

	private static final int REQUEST_SEND_MESSAGE = 0;
	ListView model_list;
	ModelAdapter mAdapter;
	int page = 0;
	boolean loadingMore;
	LinearLayout list_loading;
	int userId;
	String userName;
	ArrayList<Model> models_list;
	EditText search_model;
	Activity act;
	
	
	// Broadcast receivers
	ModelPurchaseReceiver m_receiver;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.model_message_list);
		
		Bundle extras = getIntent().getExtras();
		act = this;
		
		userId = extras.getInt("userId");
		userName = extras.getString("userName");
		
		TextView title = (TextView) findViewById(R.id.title);
		title.setText(getResources().getString(R.string.enviar_a,userName));
		model_list = (ListView) findViewById(R.id.models_list);
		list_loading = (LinearLayout) View.inflate(this, R.layout.list_footer_loading, null);
        model_list.addFooterView(list_loading);
		
        PauseOnScrollListener listener = new PauseOnScrollListener(Utils.getImageLoader(this), true, true, new OnScrollListener(){

			@Override
			public void onScroll(AbsListView list, int firstVisible, int visibleItems, int totalItems) {
				
				int lastInScreen = firstVisible + visibleItems;
				
				if( lastInScreen == totalItems && totalItems != 0 && !loadingMore && mAdapter != null ){
					
					page++;
    	        	new GetUserModels( getApplicationContext() , page ).execute();
					loadingMore = true;
				}
			}

			@Override
			public void onScrollStateChanged(AbsListView arg0, int arg1) {}
		});

        model_list.setOnScrollListener(listener);
        
		new GetUserModels(getApplicationContext(), page ).execute();
		
		search_model = (EditText) findViewById(R.id.search_models);
		search_model.setOnEditorActionListener(new OnEditorActionListener(){

			@Override
			public boolean onEditorAction(TextView tv, int actionId, KeyEvent key) {
				
				String query = search_model.getText().toString();
				
				if ( actionId == EditorInfo.IME_ACTION_SEARCH && query.length() > 3 ){
					
					Intent i = new Intent( getApplicationContext(), SearchModelsActivity.class );
					i.putExtra("query", query );
					startActivity(i);
					
					return true;
				}
				
				return false;
			}
		});
		
        /* Recibe si compraste un modelo */
        m_receiver = new ModelPurchaseReceiver();
        registerReceiver(m_receiver,new IntentFilter(this.getString(R.string.broadcast_model_purchase)));
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		if ( requestCode == REQUEST_SEND_MESSAGE ) {
			if (resultCode == RESULT_OK) {
				setResult(RESULT_OK);
				this.finish();
			}
		}
	}
	
    protected void onDestroy(){
    	super.onDestroy();
    	    	
    	if ( m_receiver != null ){
    		unregisterReceiver(m_receiver);
    	}
    	
    }
    
    public class ModelAdapter extends BucketListAdapter<Model> {

		LayoutInflater inflater;
		
    	public ModelAdapter(Activity ctx, ArrayList<Model> elements,Integer bucketSize) {
    		super(ctx, elements, bucketSize);

			inflater = (LayoutInflater) ctx.getSystemService("layout_inflater");
    	}
    	
		@Override
		protected View getBucketElement(int position, final Model model, View convertView) {
			
			if ( convertView == null ){
				convertView = inflater.inflate(R.layout.model_item, null);
			}
			
			TextView name = (TextView) convertView.findViewById(R.id.name);
			TextView price = (TextView) convertView.findViewById(R.id.price);
			ImageView image = (ImageView) convertView.findViewById(R.id.image);
			
			name.setText(model.name);
			price.setText(model.price);
			image.setImageResource(R.drawable.model_image);
			
			convertView.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View arg0) {
					goToMessage(userId,model);
					
				}
			});
			
			return convertView;
		}

    }

    public void invalidateModels(){

    	mAdapter = null;
    	page = 0;
        new GetUserModels( this , page ).execute();
    }
    
    public void setModelList( ArrayList<Model> list ){
        mAdapter = new ModelAdapter(act, list , 2 );
        model_list.setAdapter(mAdapter);
	}
    
	public void goToMessage(int userId , Model m) {
		
    	Intent i = new Intent(getApplicationContext(),ModelPreviewActivity.class);
		i.putExtra("userId", userId);
		i.putExtra("userName",userName);
		i.putExtra("modelId", m.id);
		i.putExtra("enable", m.enable);
		startActivityForResult(i, REQUEST_SEND_MESSAGE);
	}
    public void refresh(){
	    ((ImageView)findViewById(R.id.notSignalImage)).setVisibility(View.GONE);
		((TextView) findViewById(R.id.reload_button)).setVisibility(View.GONE);		
		if(mAdapter == null){
				
			new GetUserModels(getApplicationContext(), page ).execute();
	    		
	    }
	}
	public class GetUserModels extends ServerConnection{
    	
    	Context con;
    	int user_id;
    	int page;
    	ProgressBar loader;
    	public GetUserModels( Context con , int page ){
    		super();
    		this.con = con;
    		this.user_id = User.get(con).id;
    		this.page = page;
    		loader = (ProgressBar) findViewById(R.id.loaderModel);		
     		if(mAdapter == null){
    			loader.setVisibility(View.VISIBLE);
    		}
     		((ImageView)findViewById(R.id.notSignalImage)).setVisibility(View.GONE);
    		((TextView) findViewById(R.id.reload_button)).setVisibility(View.GONE);	
    		init(con,"get_user_models",new Object[]{ User.get(con).id+"" , page });
    	}
    	
		@Override
		public void onComplete(String result) {
			loader.setVisibility(View.GONE);
			if ( result != null && result.length() > 0 ){
				
				Log.i("Models", result);
				
				try {
					JSONArray models = new JSONArray(result);
					
					ArrayList<Model> models_list = new ArrayList<Model>();
					
					for ( int i = 0 ; i < models.length() ; ++i ){
						
						JSONObject model = models.getJSONObject(i);
						int id = model.getInt("i");
						String name = model.getString("n");
						String price = model.getString("p");
						boolean is_enable = model.getInt("e") == 1;
						
						models_list.add(new Model(id,name,price,id+"",is_enable));
					}
					
					if ( mAdapter != null ){
						
						for (Model m : models_list){
							mAdapter.add(m);	
						}
						mAdapter.notifyDataSetChanged();
					}
					else{
						setModelList(models_list);
					}
					
					if ( models_list.size() > 0 ){

			            loadingMore = false;
					}
					else{
						loadingMore = true;
						list_loading.setVisibility(View.GONE);
					}
					
					
				} catch (JSONException e) {	
					e.printStackTrace();
				}
			
			}
			else{
				((ImageView) findViewById(R.id.notSignalImage)).setVisibility(View.VISIBLE);
				TextView reload = (TextView) findViewById(R.id.reload_button);		
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
	
	public class ModelPurchaseReceiver extends BroadcastReceiver {
        
    	@Override
    	public void onReceive(Context context, Intent intent) {
    		invalidateModels();
    	}
      
    }
}
