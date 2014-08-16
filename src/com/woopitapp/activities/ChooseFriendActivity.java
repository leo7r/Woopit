package com.woopitapp.activities;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.woopitapp.R;
import com.woopitapp.WoopitActivity;
import com.woopitapp.entities.User;
import com.woopitapp.services.Data;
import com.woopitapp.services.Utils;

public class ChooseFriendActivity extends WoopitActivity {

	int modelId;
	ListView user_list;
	ListAdapter uAdapter;
	ArrayList<String> images;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.choose_friend);
		
		Bundle extras = getIntent().getExtras();
		images = new ArrayList<String>();
		
		if ( extras.containsKey("modelId") ){
			
			modelId = extras.getInt("modelId");
		}
		else{
			modelId = -1;
			
			for ( int i = 0 ; i < 4 ; ++i ){
				if ( extras.containsKey("image"+i) ){
					images.add(extras.getString("image"+i));
				}
			}			
		}
		
		user_list = (ListView) findViewById(R.id.user_list);
		
		Data data = new Data(this);
		data.open();
		ArrayList<User> friends = data.getFriends();
		data.close();
		
		if ( friends.size() == 0 ){
			LinearLayout no_friends = (LinearLayout) findViewById(R.id.no_friends);
			no_friends.setVisibility(View.VISIBLE);
		}
		else{
			uAdapter = new ListAdapter(this, R.id.user_list, friends );
	        user_list.setAdapter(uAdapter);
		}
		
	}
	
    public class ListAdapter extends ArrayAdapter<User>{
		
		ArrayList<User> l_items;
		Context context;
		Filter filter;
		LayoutInflater infalInflater;

		public ListAdapter(Context context, int textViewResourceId, ArrayList<User> objects) {
			
			super(context, textViewResourceId, objects);
			this.l_items = objects;
			this.context = context;
			infalInflater = (LayoutInflater) context.getSystemService("layout_inflater");
		}
		
		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {

			final User user = getItem(position);
			
			if ( convertView == null ){
				convertView = infalInflater.inflate(R.layout.friend_item, null);
			}
			
			ImageView image = (ImageView) convertView.findViewById(R.id.image);
			TextView name = (TextView) convertView.findViewById(R.id.name);
			TextView username = (TextView) convertView.findViewById(R.id.username);
			final ImageView confirm_friend = (ImageView) convertView.findViewById(R.id.add_friend);
			confirm_friend.setVisibility(View.GONE);
			
			name.setText(user.name);
			username.setText(user.username);
			Utils.setUserImage(getApplicationContext(), image, user.id);
			
			convertView.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					
					if ( images.size() == 0 ){
						Intent i = new Intent(getApplicationContext(),ModelPreviewActivity.class);
						i.putExtra("userId", user.id);
						i.putExtra("userName",user.username);
						i.putExtra("modelId", modelId);
						i.putExtra("enable", true);
						startActivity(i);
					}
					else{
						Intent i = new Intent(getApplicationContext(),ImagePreviewActivity.class);
						i.putExtra("userId", user.id);
						i.putExtra("userName",user.username);
						
						for ( int j = 0 ; j < images.size() ; ++ j ){
							i.putExtra("image"+j, images.get(j));
						}
						
						startActivity(i);
						Utils.onMessageImageNew(getApplicationContext());
					}
					
					finish();
				}
			});
			
			return convertView;
		}
		
		@Override
		public int getCount() {

			return l_items != null ? l_items.size() : 0;
		}
		
		public User getItem( int pos ){
			return l_items.get(pos);
		}
		
	}
	
}
