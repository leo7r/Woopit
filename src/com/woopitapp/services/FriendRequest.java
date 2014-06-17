package com.woopitapp.services;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.woopitapp.R;

public class FriendRequest {

	public int id, from_user;
	public String name, username, image;
	
	public FriendRequest( int id , int from_user , String username , String name ){
		this.id = id;
		this.from_user = from_user;
		this.username = username;
		this.name = name;
	}
	
	public Bitmap getImage( Context c ){
		
		Bitmap bm;
		
		if ( image != null && image.length() > 0 ){
	        bm = BitmapFactory.decodeResource(c.getResources(), R.drawable.user);
		}
		else{
	        bm = BitmapFactory.decodeResource(c.getResources(), R.drawable.user);			
		}
		
        return Utils.round(bm, Utils.dpToPx(100, c));
	}
	
}
