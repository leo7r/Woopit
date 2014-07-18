package com.woopitapp.activities;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;
import com.scythe.bucket.BucketListAdapter;
import com.woopitapp.R;
import com.woopitapp.WoopitActivity;
import com.woopitapp.entities.Model;
import com.woopitapp.entities.User;
import com.woopitapp.services.ServerConnection;
import com.woopitapp.services.Utils;

public class SearchModelsActivity extends WoopitActivity {

	String query;

	ListView models_list;
	ModelAdapter mAdapter;
	int page = 0;
	boolean loadingMore;
	LinearLayout list_loading;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search_models);
		
		Bundle extras = getIntent().getExtras();
		
		if ( extras.containsKey("query") ){
			
			query = extras.getString("query");
			
			TextView title = (TextView) findViewById(R.id.title);
			title.setText(getResources().getString(R.string.busqueda_modelos,query));
			
			models_list = (ListView) findViewById(R.id.models_list);
	        list_loading = (LinearLayout) View.inflate(this, R.layout.list_footer_loading, null);
	        models_list.addFooterView(list_loading);
	                
	        PauseOnScrollListener listener = new PauseOnScrollListener(Utils.getImageLoader(this), true, true, new OnScrollListener(){

				@Override
				public void onScroll(AbsListView list, int firstVisible, int visibleItems, int totalItems) {
					
					int lastInScreen = firstVisible + visibleItems;
					
					if( lastInScreen == totalItems && totalItems != 0 && !loadingMore && mAdapter != null ){
						
						page++;
        	        	new GetUserModels( getApplicationContext()  , page ).execute();
    					loadingMore = true;
					}
				}

				@Override
				public void onScrollStateChanged(AbsListView arg0, int arg1) {}
			});
	        
	        models_list.setOnScrollListener(listener);
	        
	        new GetUserModels( this , page ).execute();
		}
		else{
			finish();
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
			Utils.setModelImage(getApplicationContext(), image, model.id);
			
			convertView.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View arg0) {
					
					Intent i = new Intent(ctx,ModelPreviewActivity.class);
					i.putExtra("modelId", model.id);
					i.putExtra("enable", model.enable);
					startActivity(i);
					
				}
			});
			
			return convertView;
		}

    }
	
    public void setModelList( ArrayList<Model> list ){
        mAdapter = new ModelAdapter(this, list , 2 );
        models_list.setAdapter(mAdapter);
	}
	
    class GetUserModels extends ServerConnection{
    	
    	Context con;
    	int user_id;
    	int page;
    	
    	public GetUserModels( Context con , int page ){
    		super();
    		
    		this.con = con;
    		this.user_id = User.get(con).id;
    		this.page = page;
    		
    		init(con,"get_user_models",new Object[]{ user_id+"" , page , query });
    	}
    	
		@Override
		public void onComplete(String result) {
			
			if ( result != null && result.length() > 0 ){
				
				//Log.i("Models", result);
				
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
				Toast.makeText(con, R.string.error_de_conexion, Toast.LENGTH_SHORT).show();
			}
			
		}
    	
    }
    
}
