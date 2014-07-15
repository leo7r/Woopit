package com.woopitapp;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.GoogleAnalytics;
import com.woopitapp.activities.FindFriendsActivity;
import com.woopitapp.entities.User;
import com.woopitapp.services.Utils;

public class WoopitFragmentActivity extends FragmentActivity {
	
	public void onCreate( Bundle savedInstanceState ){
		super.onCreate(savedInstanceState);
	}
	
	public void onStart(){
		super.onStart();
		
		if ( User.get(getApplicationContext()) != null ){
			EasyTracker.getInstance(this).set("&uid", User.get(getApplicationContext()).id+"");
		}
		EasyTracker.getInstance(this).activityStart(this);
	}
	
	public void onStop(){
		super.onStop();
		EasyTracker.getInstance(this).activityStop(this);
	}
	
	/* Ir atras */
	public void goBack( View v ){
		this.onBackPressed();
	}

    /* Ir a encontrar amigos */
    public void goFindFriends( View v ){    	
    	Intent i = new Intent(this,FindFriendsActivity.class);
    	startActivity(i); 
    	
    	try {
			ActivityInfo info = this.getPackageManager().getActivityInfo(getComponentName(), 0);
			Utils.onFriendsSearchEnter(getApplicationContext(), info.loadLabel(getPackageManager()).toString());
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
    }
	
}
