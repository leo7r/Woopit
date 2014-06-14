package com.woopitapp;

public class FriendRequest {

	int id, from_user;
	String name,username;
	
	public FriendRequest( int id , int from_user , String username , String name ){
		this.id = id;
		this.from_user = from_user;
		this.username = username;
		this.name = name;
	}
	
}
