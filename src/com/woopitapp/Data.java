package com.woopitapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

public class Data{
	
	private SQLiteDatabase database;
	private DBHelper dbHelper;
  
	private String user_colls[] = new String[]{ DBHelper.user_id , DBHelper.user_email , DBHelper.user_username , DBHelper.user_name, DBHelper.user_image, DBHelper.user_fb, DBHelper.user_gp };
	private String friend_colls[] = new String[]{ DBHelper.friend_id , DBHelper.friend_username , DBHelper.friend_name, DBHelper.friend_image };
	private Context context;
  
	/* Database methods */
  
	public Data(Context context){
		this.context = context;
	    this.dbHelper = new DBHelper(context);
	}

	public void open() throws SQLException {
		this.database = this.dbHelper.getWritableDatabase();
	}

	public void close() {
		this.dbHelper.close();
	}
	
	
	/* Usuario logeado en Woopit */
	
	public User insertUser( int id , String email , String username , String name , String image , int facebook_user , int gplus_user ){
		
		Cursor cursor = this.database.query(DBHelper.USER_TABLE, 
				user_colls,
				DBHelper.user_email+" = ?",
				new String[]{email }, null, null, null);
				
		if ( !cursor.moveToFirst() ){
			
			ContentValues values = new ContentValues();
			
			values.put(DBHelper.user_id, id);
			values.put(DBHelper.user_email, email);
			values.put(DBHelper.user_name, name);
			values.put(DBHelper.user_image, image);
			values.put(DBHelper.user_fb, facebook_user);
			values.put(DBHelper.user_gp, gplus_user);
			
			if ( username != null ){
				values.put(DBHelper.user_username, username);
			}
			
			long res = database.insert(DBHelper.USER_TABLE, null, values);
			
			if ( res != -1 ){
				return new User( id , email , username , name , image , facebook_user , gplus_user );
			}
		}
		else{
			database.delete(DBHelper.USER_TABLE, null, null);
			return this.insertUser( id , email , username , name , image , facebook_user , gplus_user );
		}
		
		return null;
	}
	
	public User getUser(){
		
		Cursor cursor = this.database.query(DBHelper.USER_TABLE, 
				user_colls,
				null,null, null, null, null);
		
		if ( cursor.moveToFirst() ){
			
			int id = cursor.getInt(cursor.getColumnIndex(DBHelper.user_id));
			String email = cursor.getString(cursor.getColumnIndex(DBHelper.user_email));
			String username = cursor.getString(cursor.getColumnIndex(DBHelper.user_username));
			String name = cursor.getString(cursor.getColumnIndex(DBHelper.user_name));
			String image = cursor.getString(cursor.getColumnIndex(DBHelper.user_image));
			int facebook_user = cursor.getInt(cursor.getColumnIndex(DBHelper.user_fb));
			int gplus_user = cursor.getInt(cursor.getColumnIndex(DBHelper.user_gp));
			
			return new User( id , email , username, name , image , facebook_user , gplus_user );
		}
		
		return null;
	}
	
	public boolean updateUser( String username , String name , String image , int facebook_user , int gplus_user ){
		
		Cursor cursor = this.database.query(DBHelper.USER_TABLE, 
				user_colls,
				null,null, null, null, null);
		
		if ( cursor.moveToFirst() ){

			ContentValues values = new ContentValues();
			
			if ( username != null )
				values.put(DBHelper.user_username, username);

			if ( name != null )
				values.put(DBHelper.user_name, name);

			if ( image != null )
				values.put(DBHelper.user_image, image);

			if ( facebook_user != -1 )
				values.put(DBHelper.user_fb, facebook_user);

			if ( gplus_user != -1 )
				values.put(DBHelper.user_gp, gplus_user);
			
			String email = cursor.getString(cursor.getColumnIndex(DBHelper.user_email));
			
			int res = database.update(DBHelper.USER_TABLE, values, DBHelper.user_email+" = ?", new String[]{ email });
			
			return res != -1;
		}
		
		
		return false;
	}
	
	/* Amigos */
	
	public User insertFriend( int id , String username , String name , String image ){
		
		Cursor cursor = this.database.query(DBHelper.FRIEND_TABLE, 
				friend_colls,
				DBHelper.friend_id+" = ?",
				new String[]{ id+"" }, null, null, null);
				
		if ( !cursor.moveToFirst() ){
			
			ContentValues values = new ContentValues();
			
			values.put(DBHelper.friend_id, id);
			values.put(DBHelper.friend_username, username);
			values.put(DBHelper.friend_name, name);
			values.put(DBHelper.friend_image, image);
			
			long res = database.insert(DBHelper.FRIEND_TABLE, null, values);
			
			if ( res != -1 ){
				return new User( id , username , name , image );
			}
		}
		else{
			return getFriend(id);
		}
		
		return null;
	}
	
	public User getFriend( int id ){
		
		Cursor cursor = this.database.query(DBHelper.FRIEND_TABLE, 
				friend_colls,DBHelper.friend_id+" = ?",new String[]{ id+"" }, null, null, null);
		
		if ( cursor.moveToFirst() ){
			
			String username = cursor.getString(cursor.getColumnIndex(DBHelper.friend_username));
			String name = cursor.getString(cursor.getColumnIndex(DBHelper.friend_name));
			String image = cursor.getString(cursor.getColumnIndex(DBHelper.friend_image));
			
			return new User( id , username, name , image);
		}
		
		return null;
	}
	
	public boolean updateFriend( int id , String username , String name , String image ){
		
		Cursor cursor = this.database.query(DBHelper.FRIEND_TABLE, 
				friend_colls,DBHelper.friend_id+" = ?",new String[]{ id+"" }, null, null, null);
		
		if ( cursor.moveToFirst() ){

			ContentValues values = new ContentValues();
			
			if ( username != null )
				values.put(DBHelper.friend_username, username);

			if ( name != null )
				values.put(DBHelper.friend_name, name);
			
			if ( image != null )
				values.put(DBHelper.friend_image, image);
						
			int res = database.update(DBHelper.FRIEND_TABLE, values, DBHelper.friend_id+" = ?", new String[]{ id+"" });
			
			return res != -1;
		}
		
		
		return false;
	}
	
	public ArrayList<User> getFriends(){
		
		Cursor cursor = this.database.query(DBHelper.FRIEND_TABLE,null,null,null,null, null, null);
		cursor.moveToFirst();
		
		ArrayList<User> list = new ArrayList<User>();
		
		while ( !cursor.isAfterLast() ){
			
			int id = cursor.getInt(cursor.getColumnIndex(DBHelper.friend_id));
			String username = cursor.getString(cursor.getColumnIndex(DBHelper.friend_username));
			String name = cursor.getString(cursor.getColumnIndex(DBHelper.friend_name));
			String image = cursor.getString(cursor.getColumnIndex(DBHelper.friend_image));
			
			list.add(new User( id , username, name , image));
			cursor.moveToNext();
		}
		
		return list;
	}
	
}
