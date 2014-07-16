package com.woopitapp.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;

import com.woopitapp.R;
import com.woopitapp.WoopitFragmentActivity;
import com.woopitapp.fragments.FindFriendsList;
import com.woopitapp.fragments.FindFriendsWelcomeFragment;
import com.woopitapp.fragments.LoginFragment;
import com.woopitapp.fragments.SignupFragment;
import com.woopitapp.server_connections.InsertCoins;
import com.woopitapp.services.Utils;

public class FindFriendsActivity extends WoopitFragmentActivity {
	
	private int SHARE_REQUEST_CODE = 1;
    boolean share_launched = false , share_clicked = false;
    Fragment currentFragment;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.find_friends);
		
		currentFragment = new FindFriendsWelcomeFragment();
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		
		transaction.add(R.id.fragment_container, currentFragment);
		transaction.commit();
		
	}
	
	public void onStop(){
    	super.onStop();
    	
    	if ( share_launched ){
    		share_clicked = true;
    	}
    	
    }
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		if (requestCode == FindFriendsWelcomeFragment.REQUEST_CODE_RESOLVE_ERR && resultCode == Activity.RESULT_OK) {
			FindFriendsWelcomeFragment f = (FindFriendsWelcomeFragment) currentFragment;
			f.reconectGoogle();
	    }
		
		if ( requestCode == SHARE_REQUEST_CODE && share_launched && share_clicked ){
			
			Utils.onShareWoopit(getApplicationContext(), "FindFriendsActivity", "Compartido");
			new InsertCoins(this , 1 , R.string.por_compartir ).execute();			
		}
		
		share_launched = false;
		share_clicked = false;
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
		share_launched = true;
		
		Intent sendIntent = new Intent();
		sendIntent.setAction(Intent.ACTION_SEND);
		sendIntent.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.compartir_woopit_texto));
		sendIntent.setType("text/plain");
		
		startActivityForResult(Intent.createChooser(sendIntent, getResources().getString(R.string.compartir_woopit)) , SHARE_REQUEST_CODE );
	}
	
}
