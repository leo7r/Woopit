package com.woopitapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;

import com.woopitapp.R;
import com.woopitapp.WoopitFragmentActivity;
import com.woopitapp.fragments.FindFriendsList;
import com.woopitapp.fragments.FindFriendsWelcomeFragment;

public class FindFriendsActivity extends WoopitFragmentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.find_friends);
		
		Fragment fragment = new FindFriendsWelcomeFragment();
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		
		transaction.add(R.id.fragment_container, fragment);
		transaction.commit();
		
	}
	
	public void setFriendsList( String result ){

		Fragment fragment = new FindFriendsList();
		Bundle bundle = new Bundle();
		bundle.putString("result", result);
		fragment.setArguments(bundle);
		
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		
		transaction.replace(R.id.fragment_container, fragment);
		transaction.addToBackStack(null);
		transaction.commit();
		
	}
	
	public void inviteFriends( View v ){
		
		Intent sendIntent = new Intent();
		sendIntent.setAction(Intent.ACTION_SEND);
		sendIntent.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.compartir_woopit_texto));
		sendIntent.setType("text/plain");
		
		startActivity(Intent.createChooser(sendIntent, getResources().getString(R.string.compartir_woopit)));
	}
	
}
