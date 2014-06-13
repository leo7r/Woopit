package com.woopitapp;

import java.util.ArrayList;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class FriendsFragment extends Fragment {
	
	ListView friend_list;
	FriendAdapter fAdapter;
	ArrayList<User> friends;
	
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	
        View view = (LinearLayout)inflater.inflate(R.layout.friends_fragment, container, false);
    	       
        friend_list = (ListView) view.findViewById(R.id.friend_list);
        
        Data data = new Data(getActivity());
        data.open();
        friends = data.getFriends();
        data.close();
        
        fAdapter = new FriendAdapter(getActivity(), R.id.friend_list, friends );
        friend_list.setAdapter(fAdapter);
        
        // Busco en servidor por nuevos amigos
        new User.GetFriends(getActivity(), User.get(getActivity()).id).execute();
        
        return view;
    }
    
    public void updateContent(){
    	
    	Toast.makeText(getActivity(), "Renovando info", Toast.LENGTH_SHORT).show();
    }
    
    public class FriendAdapter extends ArrayAdapter<User>{
    	
		ArrayList<User> items;
		Context context;
		Filter filter;
		LayoutInflater inflater;

		public FriendAdapter(Context context, int textViewResourceId, ArrayList<User> objects){
			super(context, textViewResourceId, objects);
			this.items = objects;
			this.context = context;
			inflater = (LayoutInflater)this.context.getSystemService("layout_inflater");
		}

		public View getView(final int position, View convertView, ViewGroup parent){
			
			if ( convertView == null ){
				convertView = inflater.inflate(R.layout.friend_item, null);
			}
			
			User friend = getItem(position);
			
			TextView name = (TextView) convertView.findViewById(R.id.name);
			TextView username = (TextView) convertView.findViewById(R.id.username);
			
			name.setText(friend.name);
			username.setText("@"+friend.username);
			
			return convertView;
		}
		
		public User getItem( int position ){
			return items.get(position);
		}
		
    }

    
}
        
