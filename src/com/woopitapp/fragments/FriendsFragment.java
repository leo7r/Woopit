package com.woopitapp.fragments;

import java.util.ArrayList;

import com.woopitapp.R;
import com.woopitapp.activities.SearchUsers;
import com.woopitapp.logic.Data;
import com.woopitapp.logic.FriendRequest;
import com.woopitapp.logic.User;


import com.emilsjolander.components.stickylistheaders.StickyListHeadersAdapter;
import com.emilsjolander.components.stickylistheaders.StickyListHeadersListView;

import android.annotation.SuppressLint;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;

import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Filter;

import android.widget.ImageView;

import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TextView.OnEditorActionListener;

public class FriendsFragment extends Fragment {
		
	StickyListHeadersListView friend_list;
	ListAdapter fAdapter;
	
	ArrayList<User> friends;
	ArrayList<FriendRequest> friend_requests;
	ArrayList<Object> list_items;
	EditText search_users;
	
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	
        View view = (LinearLayout)inflater.inflate(R.layout.friends_fragment, container, false);

    	       
        friend_list = (StickyListHeadersListView) view.findViewById(R.id.friend_list);
        search_users = (EditText) view.findViewById(R.id.search_users);
        
        search_users.setOnEditorActionListener(new OnEditorActionListener(){

			@Override
			public boolean onEditorAction(TextView tv, int actionId, KeyEvent key) {
				
				if ( actionId == EditorInfo.IME_ACTION_SEARCH ){
					searchUsers(search_users.getText().toString());
					return true;
				}
				
				return false;
			}
		});
        
        Data data = new Data(getActivity());
        data.open();
        friends = data.getFriends();
        friend_requests = data.getFriendRequests();
        list_items = new ArrayList<Object>();
        list_items.addAll(friend_requests);
        list_items.addAll(friends);
        
        data.close();
        
        fAdapter = new ListAdapter(getActivity(), R.id.friend_list, list_items );
        friend_list.setAdapter(fAdapter);

        return view;
    }
    
    public void onStart(){
    	super.onStart();
    	
    	new User.GetFriendRequest(getActivity()).execute();
        //new User.GetFriends(getActivity(), User.get(getActivity()).id).execute();
    }
    
    public void searchUsers( String query ){
    	
		Intent i = new Intent(getActivity(),SearchUsers.class);
		
		i.putExtra("query", query);
		
		startActivity(i);
    }
    
    public void updateContent(){
    	
    	Data data = new Data(getActivity());
        data.open();
        friends = data.getFriends();
        friend_requests = data.getFriendRequests();
        list_items = new ArrayList<Object>();
        list_items.addAll(friend_requests);
        list_items.addAll(friends);
        data.close();

        fAdapter = new ListAdapter(getActivity(), R.id.friend_list, list_items );
        friend_list.setAdapter(fAdapter);
    	
        Toast.makeText(getActivity(), "Refrescado", Toast.LENGTH_SHORT).show();
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
				convertView = infalInflater.inflate(R.layout.friend_item, null);
			}

			ImageView image = (ImageView) convertView.findViewById(R.id.image);
			TextView name = (TextView) convertView.findViewById(R.id.name);
			TextView username = (TextView) convertView.findViewById(R.id.username);
			final ImageView confirm_friend = (ImageView) convertView.findViewById(R.id.add_friend);
			
			if ( item instanceof FriendRequest ){
				final FriendRequest fr = (FriendRequest) item;
				
				name.setText(fr.name);
				username.setText("@"+fr.username);
				confirm_friend.setImageResource(R.drawable.add_friend);
				confirm_friend.setVisibility(View.VISIBLE);
		        image.setImageBitmap(fr.getImage(getActivity()));
				
				confirm_friend.setOnClickListener(new OnClickListener(){

					@Override
					public void onClick(View arg0) {
						new AddOrRejectFriend(fr.from_user,fr.id).execute();
						confirm_friend.setImageResource(R.drawable.friend_added);
					}
				});
				
			}
			else{
				final User user = (User) item;

				name.setText(user.name);
				username.setText("@"+user.username);
				confirm_friend.setVisibility(View.GONE);
		        image.setImageBitmap(user.getImage(getActivity()));
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
				tv.setText(getResources().getString(R.string.solicitudes_de_amistad).toUpperCase());
			}
			else{
				tv.setText(getResources().getString(R.string.amigos).toUpperCase());
			}

			return tv;
		}

		@Override
		public long getHeaderId(int position) {

			int id = 0;
			
			if (getItem(position) instanceof FriendRequest) {
				id = 0;
			} else {
				if (getItem(position) instanceof User) {
					id = 1;
				}
			}
			
			return id;
		}

	}
	
	// Si ya esta la amistad la rompe, si no esta crea un request, si ya habia un request lo acepta.
	public class AddOrRejectFriend extends com.woopitapp.logic.ServerConnection{
			
			int to_user,friend_request;
			
			public AddOrRejectFriend( int to_user , int friend_request ){
				super();
				
				this.to_user = to_user;
				this.friend_request = friend_request;
				init(getActivity(),"add_or_reject_friend",new Object[]{ User.get(getActivity()).id , to_user });
			}

			@Override
			public void onComplete(String result) {
				
				if ( result != null && result.length() > 0 ){
					
					int new_status = Integer.parseInt(result);
										
					if ( new_status == -1 || new_status == 1 ){
				        // Refresco lista de amigos
						
						Data data = new Data(getActivity());
						data.open();
						data.deleteFriendRequest(friend_request);
						data.close();
						
				        new User.GetFriends(getActivity(), User.get(getActivity()).id).execute();
					}
				}
				else{
					Toast.makeText(getActivity(), getResources().getString(R.string.error_de_conexion),Toast.LENGTH_SHORT).show();
				}
			}
			
		}
	
}
        
