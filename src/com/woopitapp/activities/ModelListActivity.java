package com.woopitapp.activities;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.emilsjolander.components.stickylistheaders.StickyListHeadersListView;
import com.woopitapp.R;
import com.woopitapp.WoopitActivity;
import com.woopitapp.entities.Model;
import com.woopitapp.entities.User;

public class ModelListActivity extends WoopitActivity {
	GridView model_list;
	ListAdapter mAdapter;
	int userId;
	String userName;
	ArrayList<Model> models_list;
	EditText search_model;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.model_message_list);
		
		Bundle extras = getIntent().getExtras();
		
		userId = extras.getInt("userId");
		userName = extras.getString("userName");
		
		new getUserModelsList(getApplicationContext(),User.get(getApplicationContext()).id).execute();
		
		model_list = (GridView) findViewById(R.id.models_list);
	    
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
	    
	}
	
	public class ListAdapter extends ArrayAdapter<Model> {
		
		ArrayList<Model> l_items;
		Context context;
		Filter filter;
		LayoutInflater infalInflater;
		
		public ListAdapter(Context context, int textViewResourceId, ArrayList<Model> objects) {
			
			super(context, textViewResourceId, objects);
			this.l_items = objects;
			this.context = context;
			infalInflater = (LayoutInflater) context.getSystemService("layout_inflater");
		}
		
		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {

			final Model model = getItem(position);
			
			if ( convertView == null ){
				convertView = infalInflater.inflate(R.layout.model_item, null);
			}

			ImageView image = (ImageView) convertView.findViewById(R.id.image);
			TextView name = (TextView) convertView.findViewById(R.id.name);
			TextView price = (TextView) convertView.findViewById(R.id.price);
			
			image.setImageResource(R.drawable.model_image);
			
			name.setText(model.name);
			price.setText(model.price);
	        convertView.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View arg0) {
					goToMessage(userId,model);
				}

			});
			
			return convertView;
		}
		
		@Override
		public int getCount() {

			return l_items != null ? l_items.size() : 0;
		}
		
		public Model getItem( int pos ){
			return l_items.get(pos);
		}
		
	}
		
	public void goToMessage(int userId , Model m) {
		
    	Intent i = new Intent(getApplicationContext(),ModelPreviewActivity.class);
		i.putExtra("userId", userId);
		i.putExtra("userName",userName);
		i.putExtra("modelId", m.id);
		i.putExtra("enable", m.enable);
		startActivity(i);
	}
	
	public class getUserModelsList extends com.woopitapp.server_connections.GetUserModels{
		
		Context con;
		
		public getUserModelsList(Context con, int user_id) {
			super(con, user_id);
			this.con = con;
		}

		@Override
		public void onComplete(String result) {
			//ProgressBar loading = (ProgressBar) findViewById(R.id.loading);
			//loading.setVisibility(View.GONE);
			if ( result != null && result.length() > 0 ){
								
				try {
					JSONArray models = new JSONArray(result);
					models_list = new ArrayList<Model>();
					
					for ( int i = 0 ; i < models.length() ; ++i ){
						
						JSONObject model = models.getJSONObject(i);
						int id = model.getInt("i");
						String name = model.getString("n");
						String price = model.getString("p");
						boolean is_enable = model.getInt("e") == 1;
						
						Model m = new Model(id,name,price,"",is_enable);
						
						models_list.add(m);
					}
					
				}catch(Exception e){
					e.printStackTrace();
				}
			}
			
		    mAdapter = new ListAdapter(con, R.id.models_list, models_list);
		    
		    model_list.setAdapter(mAdapter);
		}
			
	}
	
}
