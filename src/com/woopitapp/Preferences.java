package com.woopitapp;

import android.content.Context;
import android.content.SharedPreferences;

public class Preferences {

	static SharedPreferences prefs;
	final static String pkg = "com.woopitapp";
	final static String FIRST_TIME = pkg+".firsttime";
	
	static public boolean isFirstTime( Context c ){
		prefs = c.getSharedPreferences(pkg, Context.MODE_PRIVATE);
		return prefs.getBoolean(FIRST_TIME, true);	    
	}
	
	static public void setFirstTime( Context c , boolean b ){
		prefs = c.getSharedPreferences(pkg, Context.MODE_PRIVATE);
		prefs.edit().putBoolean(FIRST_TIME, b).commit();
	}
		
}
