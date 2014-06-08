package com.woopitapp;

import android.content.Context;

public class User {

	int id, facebook_user, gplus_user;
	String email, name, image;
	
	public User( int id , String email , String name , String image , int facebook_user , int gplus_user ){
		
		this.id = id;
		this.name = name;
		this.image = image;
		this.facebook_user = facebook_user;
		this.gplus_user = gplus_user;
	}
	
	public static User get( Context c ){
		
		Data data = new Data(c);
		data.open();
		User u = data.getUser();
		data.close();
		
		return u;
	}
	
	
}
