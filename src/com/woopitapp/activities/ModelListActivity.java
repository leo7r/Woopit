package com.woopitapp.activities;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TextView.OnEditorActionListener;

import com.emilsjolander.components.stickylistheaders.StickyListHeadersAdapter;
import com.emilsjolander.components.stickylistheaders.StickyListHeadersListView;
import com.woopitapp.R;
import com.woopitapp.entities.Model;
import com.woopitapp.entities.ModelToBuy;
import com.woopitapp.entities.User;
import com.woopitapp.server_connections.GetUserModels;
import com.woopitapp.services.Data;
import com.woopitapp.services.FriendRequest;

import android.widget.ArrayAdapter;

public class ModelListActivity extends Activity {
	StickyListHeadersListView model_list;
	ListAdapter fAdapter;
	int userId;
	String userName;
	ArrayList<Model> models;
	ArrayList<Model> models_tobuy;
	ArrayList<Object> list_items;
	EditText search_model;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.model_message_list);
		
		Bundle extras = getIntent().getExtras();
		
		userId = extras.getInt("userId");
		userName = extras.getString("userName");
		
		new getUserModelsList(getApplicationContext(),User.get(getApplicationContext()).id).execute();
		
		model_list = (StickyListHeadersListView) findViewById(R.id.model_list);
		search_model = (EditText) findViewById(R.id.search_models);
	    
	    search_model.setOnEditorActionListener(new OnEditorActionListener(){

		@Override
		public boolean onEditorAction(TextView tv, int actionId, KeyEvent key) {
				
			return false;}
		});
	        


	}
	
	public class ListAdapter extends ArrayAdapter<Object> implements StickyListHeadersAdapter {
		
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

			Object item = getItem(position);
			
			if ( convertView == null ){
				convertView = infalInflater.inflate(R.layout.model_message_item, null);
			}

			ImageView image = (ImageView) convertView.findViewById(R.id.image);
			TextView name = (TextView) convertView.findViewById(R.id.name);
			TextView price = (TextView) convertView.findViewById(R.id.price);
			
			if ( item instanceof ModelToBuy ){
				final ModelToBuy model = (ModelToBuy) item;
				
				name.setText(model.name);
				price.setText("$"+model.price);
		        image.setImageBitmap(model.getImage(this.getContext()));
				
			}
			else{
				final Model model = (Model) item;

				name.setText(model.name);
				price.setText(model.price);
			    image.setImageBitmap(model.getImage(this.getContext()));
		        convertView.setOnClickListener(new OnClickListener(){

					@Override
					public void onClick(View arg0) {
						goToMessage(userId,model.id);
					}


				});
			}
			
			return convertView;
		}
		
		@Override
		public int getCount() {

			return l_items != null ? l_items.size() : 0;
		}
		
		public Object getItem( int pos ){
			return l_items.get(pos);
		}
		
		@SuppressLint("DefaultLocale")
		@Override
		public View getHeaderView(int position, View convertView, ViewGroup parent) {

			TextView tv;
			tv = (TextView) infalInflater.inflate(R.layout.list_header,parent, false);
			
			if ( getItem( position ) instanceof FriendRequest ){
				tv.setText(getResources().getString(R.string.modelos_a_comprar).toUpperCase());
			}
			else{
				tv.setText(getResources().getString(R.string.mis_modelos).toUpperCase());
			}

			return tv;
		}

		@Override
		public long getHeaderId(int position) {

			int id = 0;
			
			if (getItem(position) instanceof ModelToBuy) {
				id = 0;
			} else {
				if (getItem(position) instanceof Model) {
					id = 1;
				}
			}
			
			return id;
		}
		
	}

	public void goToMessage(int userId , int modelId) {
		
    	Intent i = new Intent(getApplicationContext(),ModelPreviewActivity.class);
		i.putExtra("userId", userId);
		i.putExtra("userName",userName);
		i.putExtra("modelId", modelId);
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
			list_items = new ArrayList<Object>();
			if ( result != null && result.length() > 0 ){
								
				try {
					JSONArray models = new JSONArray(result);

					ArrayList<Model> list = new ArrayList<Model>();
					
					for ( int i = 0 ; i < models.length() ; ++i ){
						
						JSONObject model = models.getJSONObject(i);
						int id = model.getInt("i");
						String name = model.getString("n");
						String price = model.getString("p");
						boolean is_enable = model.getInt("e") == 1;
						
						Model m = new Model(id,name,price,"",is_enable);
						
						list.add(m);
					}
					list_items.addAll(list);
				}catch(Exception e){

				}
			}
			
		    fAdapter = new ListAdapter(con, R.id.model_list, list_items);
		    model_list.setAdapter(fAdapter);
		}
			
	}
}
