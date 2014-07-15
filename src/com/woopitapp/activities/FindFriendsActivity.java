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
import com.woopitapp.services.Utils;

public class FindFriendsActivity extends WoopitFragmentActivity {
	
	private int SHARE_REQUEST_CODE = 1;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.find_friends);
		
		Fragment fragment = new FindFriendsWelcomeFragment();
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		
		transaction.add(R.id.fragment_container, fragment);
		transaction.commit();
		
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		if ( requestCode == SHARE_REQUEST_CODE && resultCode == RESULT_OK ){
			
			Utils.onShareWoopit(getApplicationContext(), "FindFriendsActivity", "Compartido");
		}
		
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
		

		Utils.onShareWoopit(getApplicationContext(), "FindFriendsActivity", "Entrar");
		
		Intent sendIntent = new Intent();
		sendIntent.setAction(Intent.ACTION_SEND);
		sendIntent.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.compartir_woopit_texto));
		sendIntent.setType("text/plain");
		
		startActivityForResult(Intent.createChooser(sendIntent, getResources().getString(R.string.compartir_woopit)) , SHARE_REQUEST_CODE );
	}
	
}
