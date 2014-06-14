package com.woopitapp.logic;

public class FriendRequest {

	public int id;
	public int from_user;
	public String name;
	public String username;
	
	public FriendRequest( int id , int from_user , String username , String name ){
		this.id = id;
		this.from_user = from_user;
		this.username = username;
		this.name = name;
	}
	
}
