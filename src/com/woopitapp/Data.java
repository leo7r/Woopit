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

public class Data{
	
	private SQLiteDatabase database;
	private DBHelper dbHelper;
  
	private String user_colls[] = new String[]{ DBHelper.user_id , DBHelper.user_email, DBHelper.user_name, DBHelper.user_image, DBHelper.user_fb, DBHelper.user_gp };
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
	
	public User insertUser( int id , String email , String name , String image , int facebook_user , int gplus_user ){
		
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
			
			long res = database.insert(DBHelper.USER_TABLE, null, values);
			
			if ( res != -1 ){
				return new User( id , email , name , image , facebook_user , gplus_user );
			}
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
			String name = cursor.getString(cursor.getColumnIndex(DBHelper.user_name));
			String image = cursor.getString(cursor.getColumnIndex(DBHelper.user_image));
			int facebook_user = cursor.getInt(cursor.getColumnIndex(DBHelper.user_fb));
			int gplus_user = cursor.getInt(cursor.getColumnIndex(DBHelper.user_gp));
			
			return new User( id , email , name , image , facebook_user , gplus_user );
		}
		
		return null;
	}
	
  /*
  public long insertSong( int track , int idA , String name , String path , int idAlbum , int duration ){
	  
		Cursor cursor = this.database.query(DBHelper.TABLE_NAME2, 
				s_allCol,
				DBHelper.s_PATH+" = ? OR ( "+DBHelper.s_NAME+" = ? AND "+DBHelper.s_ARTIST+" = ? AND "+DBHelper.s_ALBUM+" = ? )",
				new String[]{ path , name , idA+"" , idAlbum+"" }, null, null, null);
		long id_song = -1;
		
		if ( !cursor.moveToFirst() ){
			ContentValues values = new ContentValues();
			
			if ( track > 1000 ){
				track = track-1000;
			}
			
			values.put(DBHelper.s_TRACK, track);
			values.put(DBHelper.s_ARTIST, idA);
			values.put(DBHelper.s_NAME, name);
			values.put(DBHelper.s_PATH, path);
			values.put(DBHelper.s_ALBUM, idAlbum);
			values.put(DBHelper.s_FAVORITE, 0);
			values.put(DBHelper.s_PLAY_COUNT, 0);
			values.put(DBHelper.s_DURATION, duration);
			values.put(DBHelper.s_STATUS, 0);
			
			id_song = this.database.insert(DBHelper.TABLE_NAME2, null, values);
		}
		else{
			id_song = cursor.getInt(0);
		}
		
		cursor.close();
		
		return id_song;
	  }

  public Song[] getFavoritesSongs(){
	  
	  Cursor cursor = this.database.query(DBHelper.TABLE_NAME1+" a"+" , "+DBHelper.TABLE_NAME2+" s", 
			  new String[]{"a."+DBHelper.a_NAME+" as a_name","s.*"},
			  "s."+DBHelper.s_ARTIST+" = a."+DBHelper.a_ID+" AND "+DBHelper.s_FAVORITE+" = 1"+" AND s."+DBHelper.s_STATUS+" = 0",
			  null, null, null, "s."+DBHelper.s_NAME);
	  
	  Song[] ret = new Song[cursor.getCount()];
	  
	  int i=0;
	  cursor.moveToFirst();

		while (!cursor.isAfterLast()) {
			
			int id = cursor.getInt(cursor.getColumnIndex(DBHelper.s_ID));
			int track = cursor.getInt(cursor.getColumnIndex(DBHelper.s_TRACK));
			String name = cursor.getString(cursor.getColumnIndex(DBHelper.s_NAME));
			int artist_id = cursor.getInt(cursor.getColumnIndex(DBHelper.s_ARTIST));
			String artist_name = cursor.getString(cursor.getColumnIndex("a_name"));
			int album_id = cursor.getInt(cursor.getColumnIndex(DBHelper.s_ALBUM));
			String path = cursor.getString(cursor.getColumnIndex(DBHelper.s_PATH));
			int play_count = cursor.getInt(cursor.getColumnIndex(DBHelper.s_PLAY_COUNT));
			int duration = cursor.getInt(cursor.getColumnIndex(DBHelper.s_DURATION));
			int status = cursor.getInt(cursor.getColumnIndex(DBHelper.s_STATUS));
			
			ret[i] = new Song( id , track , name , artist_id , artist_name , album_id ,
					path , true , play_count , duration , status );
			
			cursor.moveToNext();
			i++;
		}
		
		cursor.close();
		
		return ret;	  
}

  public void deleteSong( int id_song ){
	  
	  Song song = this.getSongsById(id_song);
	  
	  ContentValues args = new ContentValues();
	  args.put(DBHelper.s_STATUS , -1);
	  database.update(DBHelper.TABLE_NAME2, args, DBHelper.s_ID+" = "+id_song, null);
	  
	  Song[] songs = this.getSongs(song.artist_id);
	  
	  if ( songs.length == 0 ){
		  database.delete(DBHelper.TABLE_NAME1, DBHelper.a_ID+" = "+song.artist_id, null);
	  }
  }
  */
  
}
