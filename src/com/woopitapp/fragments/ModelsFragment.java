package com.woopitapp.fragments;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;
import com.scythe.bucket.BucketListAdapter;
import com.woopitapp.R;
import com.woopitapp.activities.ModelPreviewActivity;
import com.woopitapp.activities.SearchModelsActivity;
import com.woopitapp.entities.Model;
import com.woopitapp.entities.User;
import com.woopitapp.services.ServerConnection;
import com.woopitapp.services.Utils;

public class ModelsFragment extends Fragment {

	ListView models_list;
	ModelAdapter mAdapter;
	int page = 0;
	boolean loadingMore;
	LinearLayout list_loading;
	EditText search;
	JSONArray saved_models;
	
	
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	    	
    	View view = (LinearLayout)inflater.inflate(R.layout.models_fragment, container, false);
        
        models_list = (ListView) view.findViewById(R.id.models_list);
        list_loading = (LinearLayout) View.inflate(getActivity(), R.layout.list_footer_loading, null);
        models_list.addFooterView(list_loading);
        saved_models = new JSONArray();
        
        PauseOnScrollListener listener = new PauseOnScrollListener(Utils.getImageLoader(getActivity()), true, true, new OnScrollListener(){
        	
			@Override
			public void onScroll(AbsListView list, int firstVisible, int visibleItems, int totalItems) {
				
				int lastInScreen = firstVisible + visibleItems;
				
				if( lastInScreen == totalItems && totalItems != 0 && !loadingMore && mAdapter != null ){
					
					if ( getActivity() != null ){
						page++;
        	        	new GetUserModels( getActivity() , page ).execute();
    					loadingMore = true;
    				}
				}
			}

			@Override
			public void onScrollStateChanged(AbsListView arg0, int arg1) {}
		});
        
        models_list.setOnScrollListener(listener);
        
       
        
        search = (EditText) view.findViewById(R.id.search_models);
        search.setOnEditorActionListener(new OnEditorActionListener(){

			@Override
			public boolean onEditorAction(TextView tv, int actionId, KeyEvent key) {
				
				String query = search.getText().toString();
				
				if ( actionId == EditorInfo.IME_ACTION_SEARCH && query.length() > 3 ){
					
					Intent i = new Intent( getActivity() , SearchModelsActivity.class );
					i.putExtra("query", query );
					startActivity(i);
					
					Utils.onModelSearch(getActivity(), "ModelsFragment", query);
					
					return true;
				}
				
				return false;
			}
		});
        return view;
    }
    
    public void onStart(){
    	
    	super.onStart();
    	if(mAdapter == null){
    		new GetUserModels( getActivity() , page ).execute();
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
    	Log.e("chao", "chao");
    }
    
    public void invalidateModels(){
    	
    	mAdapter = null;
    	page = 0;
        new GetUserModels( getActivity() , page ).execute();
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
			Utils.setModelImage(getActivity(), image, model.id);
			
			convertView.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View arg0) {
					
					Intent i = new Intent(getActivity(),ModelPreviewActivity.class);
					i.putExtra("modelId", model.id);
					i.putExtra("enable", model.enable);
					startActivity(i);
					
					Utils.onModelOpen(getActivity(), "ModelsFragment", model.id);		
				}
			});
			
			return convertView;
		}

    }
    
    public void setModelList( ArrayList<Model> list ){
    	
    	if ( getActivity() != null ){
	        mAdapter = new ModelAdapter(getActivity(), list , 2 );
	        models_list.setAdapter(mAdapter);
    	}
	}
    
    public void refresh(){
    	
    	((ImageView) getView().findViewById(R.id.notSignalImage)).setVisibility(View.GONE);
		((TextView) getView().findViewById(R.id.reload_button)).setVisibility(View.GONE);	
		
		if(mAdapter == null){
    		new GetUserModels( getActivity() , page ).execute();
    	}
    }
    
    class GetUserModels extends ServerConnection{
    	
    	Context con;
    	int user_id;
    	int page;
    	ProgressBar loader;
    	
    	public GetUserModels( Context con , int page ){
    		super();
    		
    		this.con = con;
    		this.user_id = User.get(con).id;
    		this.page = page;

    		loader = (ProgressBar) getActivity().findViewById(R.id.loaderModel);	

    		if(mAdapter == null){
    			loader.setVisibility(View.VISIBLE);
    		}
        		
    		((ImageView) getView().findViewById(R.id.notSignalImage)).setVisibility(View.GONE);
    		((TextView)  getView().findViewById(R.id.reload_button)).setVisibility(View.GONE);	
    		
    		init(con,"get_user_models",new Object[]{ user_id+"" , page });
    	}
    	
		@Override
		public void onComplete(String result) {
		
			loader.setVisibility(View.GONE);
			
			if ( getView() != null ){
				((Button) getView().findViewById(R.id.send_image)).setVisibility(View.VISIBLE);
			}
			
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
						saved_models.put(model);
					}
					
					if ( mAdapter != null ){
						
						for (Model m : models_list){
							mAdapter.add(m);
						}
						
						//mAdapter.addAll(models_list);
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
				ImageView notSiganlIcon = ((ImageView) getView().findViewById(R.id.notSignalImage));
				TextView reload = (TextView) getView().findViewById(R.id.reload_button);		
				reload.setVisibility(View.VISIBLE);
				notSiganlIcon.setVisibility(View.VISIBLE);
				if(mAdapter != null){
					mAdapter.clear();
				}
				if(list_loading != null){
					list_loading.setVisibility(View.GONE);
				}
				notSiganlIcon.setOnClickListener(new View.OnClickListener() {
				    public void onClick(View v) {
				    	refresh();
				    }
				});
				reload.setOnClickListener(new View.OnClickListener() {
				    public void onClick(View v) {
				    	refresh();
				    }
				});
			}
			
		}
	    
		public void updateContent(){

	    	page = 0;
	    	mAdapter = null;
	    	new GetUserModels( getActivity() , page ).execute();
	       
	    }
	    
    	
    }
        
}