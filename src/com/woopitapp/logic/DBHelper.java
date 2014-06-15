package com.woopitapp.logic;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper{
	
  static final int DATABASE_VERSION = 6;
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
  
  static final String FRIEND_REQUEST_TABLE = "friend_request";
  static final String fr_id = "id";
  static final String fr_from_user = "from_user";
  static final String fr_username = "username";
  static final String fr_name = "name";
  private static final String CREATE_FRIEND_REQUEST = "CREATE TABLE "+FRIEND_REQUEST_TABLE+" ( "+fr_id+" integer primary key, "+fr_from_user+" integer, "+fr_username+" text , "+fr_name+" text );";
  
  static final String MODEL_TABLE = "model";
  static final String model_id = "id";
  static final String model_name = "name";
  static final String model_price = "price";
  private static final String CREATE_MODEL = "CREATE TABLE "+MODEL_TABLE+" ( "+model_id+" integer primary key, "+model_name+" text, "+model_price+" text );";
  
  
  DBHelper(Context context){
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
  }

  public void onCreate(SQLiteDatabase db){
	  db.execSQL(CREATE_USER);
	  db.execSQL(CREATE_FRIEND);
	  db.execSQL(CREATE_FRIEND_REQUEST);
	  db.execSQL(CREATE_MODEL);
  }

  public void onUpgrade(SQLiteDatabase db, int oldV, int newV){
	  
	  
	  switch( oldV ){
	  
	  case 1:
		  db.execSQL(CREATE_FRIEND);
	  case 2:
		  db.execSQL("DROP TABLE IF EXISTS "+FRIEND_TABLE);
		  db.execSQL(CREATE_FRIEND);
	  case 3:
		  db.execSQL(CREATE_FRIEND_REQUEST);
	  case 4:
		  db.execSQL("DROP TABLE IF EXISTS "+FRIEND_REQUEST_TABLE);
		  db.execSQL(CREATE_FRIEND_REQUEST);
	  case 5:
		  db.execSQL(CREATE_MODEL);
		 
	  }
      
  }
  
}