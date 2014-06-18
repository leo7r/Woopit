package com.woopitapp.fragments;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.woopitapp.R;
import com.woopitapp.activities.SearchUsers.UserAdapter;
import com.woopitapp.services.Data;
import com.woopitapp.services.User;
import com.woopitapp.services.Utils;
import com.woopitapp.server_connections.GetUserModels;
import com.woopitapp.services.Model;

public class ProfileFragment extends Fragment{

	GridView models_list;
	ModelAdapter mAdapter;
	
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	
        View view = (LinearLayout)inflater.inflate(R.layout.profile_fragment, container, false);
    	
        User user = User.get(getActivity());
        
        ImageView image = (ImageView) view.findViewById(R.id.image);
        TextView name = (TextView) view.findViewById(R.id.name);
        TextView username = (TextView) view.findViewById(R.id.username);
        
        image.setImageBitmap(user.getImage(getActivity()));
        name.setText(user.name);
        username.setText("@"+user.username);
        
        Data data = new Data(getActivity());
        data.open();
        ArrayList<Model> list = data.getModels();
        data.close();

        models_list = (GridView) view.findViewById(R.id.models_list);
        mAdapter = new ModelAdapter(getActivity(), R.id.models_list, list );
        models_list.setAdapter(mAdapter);
        
                
        return view;
    }
    
    public void onStart(){
    	super.onStart();
    	
    	new GetMyModels().execute();
    }

    public void updateContent(){

        Data data = new Data(getActivity());
        data.open();
        ArrayList<Model> list = data.getModels();
        data.close();

        mAdapter = new ModelAdapter(getActivity(), R.id.models_list, list );
        models_list.setAdapter(mAdapter);
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
    
    class GetMyModels extends GetUserModels{

		public GetMyModels() {
			super(getActivity(), User.get(getActivity()).id);
		}

		@Override
		public void onComplete(String result) {
			
			if ( result != null && result.length() > 0 ){
				
				Log.i("Models", result);
				
				Data data = new Data(getActivity());
				data.open();
				
				try {
					JSONArray models = new JSONArray(result);
					
					for ( int i = 0 ; i < models.length() ; ++i ){
						
						JSONObject model = models.getJSONObject(i);
						int id = model.getInt("i");
						String name = model.getString("n");
						String price = model.getString("p");
						
						data.insertModel(id, name, price);					
					}
					
					Utils.sendBroadcast(getActivity(), R.string.broadcast_profile_models_list);
					
				} catch (JSONException e) {
					
					e.printStackTrace();
				}
				finally{
					data.close();
				}
				
				
			}
			else{
				Toast.makeText(getActivity(), R.string.error_de_conexion, Toast.LENGTH_SHORT).show();
			}
			
		}
    	
    }
    
}