package com.woopitapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper{
	
  static final int DATABASE_VERSION = 3;
  static final String DATABASE_NAME = "woopit";
  Context context;
  
  static final String USER_TABLE = "user";
  static final String user_id = "id";
  static final String user_email = "email";
  static final String user_username = "username";
  static final String user_name = "name";
  static final String user_image = "image";
  static final String user_fb = "facebook_user";
  static final String user_gp = "gplus_user";
  private static final String CREATE_USER = "CREATE TABLE "+USER_TABLE+" ( "+user_id+" integer primary key, "+user_email+" text, "+user_username+" text, "+user_name+" text, "+user_image+" text, "+user_fb+" integer, "+user_gp+" integer );";
  
  static final String FRIEND_TABLE = "friend";
  static final String friend_id = "id";
  static final String friend_username = "username";
  static final String friend_name = "name";
  static final String friend_image = "image";
  private static final String CREATE_FRIEND = "CREATE TABLE "+FRIEND_TABLE+" ( "+friend_id+" integer primary key, "+friend_username+" text, "+friend_name+" text, "+friend_image+" text );";
  
  
  DBHelper(Context context){
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
  }

  public void onCreate(SQLiteDatabase db){
	  db.execSQL(CREATE_USER);
	  db.execSQL(CREATE_FRIEND);
  }

  public void onUpgrade(SQLiteDatabase db, int oldV, int newV){
	  
	  
	  switch( oldV ){
	  
	  case 1:
		  db.execSQL(CREATE_FRIEND);
	  case 2:
		  db.execSQL("DROP TABLE IF EXISTS "+FRIEND_TABLE);
		  db.execSQL(CREATE_FRIEND);
		  
	  }
      
  }
  
}