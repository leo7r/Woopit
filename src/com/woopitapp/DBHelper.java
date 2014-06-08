package com.woopitapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper{
	
  static final int DATABASE_VERSION = 1;
  static final String DATABASE_NAME = "woopit";
  Context context;
  
  static final String USER_TABLE = "user";
  static final String user_id = "id";
  static final String user_email = "email";
  static final String user_name = "name";
  static final String user_image = "image";
  static final String user_fb = "facebook_user";
  static final String user_gp = "gplus_user";
  private static final String CREATE_USER = "CREATE TABLE "+USER_TABLE+" ( "+user_id+" integer primary key, "+user_email+" text, "+user_name+" text, "+user_image+" text, "+user_fb+" integer, "+user_gp+" integer );";
  
  
  DBHelper(Context context){
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
  }

  public void onCreate(SQLiteDatabase db){
	  db.execSQL(CREATE_USER);
  }

  public void onUpgrade(SQLiteDatabase db, int oldV, int newV){
	  
	  /*
	  switch( oldV ){
	  
	  case 1:
		  db.execSQL(CREATE_USER);
	  }
      */
  }
  
}