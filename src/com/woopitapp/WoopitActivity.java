package com.woopitapp;

import com.woopitapp.activities.FindFriendsActivity;

import android.app.Activity;
import android.content.Intent;
import android.view.View;

public class WoopitActivity extends Activity {

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
