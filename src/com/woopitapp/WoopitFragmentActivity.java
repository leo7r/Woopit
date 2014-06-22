package com.woopitapp;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.view.View;

import com.woopitapp.activities.FindFriendsActivity;

public class WoopitFragmentActivity extends FragmentActivity {

	/* Ir atras */
	public void goBack( View v ){
		this.onBackPressed();
	}
	

    /* Ir a encontrar amigos */
    public void goFindFriends( View v ){    	
    	Intent i = new Intent(this,FindFriendsActivity.class);
    	startActivity(i);    	
    }
	
}
