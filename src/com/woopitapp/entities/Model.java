package com.woopitapp.entities;

import com.woopitapp.R;
import com.woopitapp.services.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class Model {
	
	public int id;
	public String name, price, preview;
	public boolean enable;
	
	public Model( int id , String name , String price, String preview , boolean enable){
		this.id = id;
		this.name = name;
		this.price = price;
		this.preview = preview;
		this.enable = enable;
	}

	public Bitmap getImage( Context c ){
		
		Bitmap bm;
		
		if ( preview != null && preview.length() > 0 ){
	        bm = BitmapFactory.decodeResource(c.getResources(), R.drawable.model_image);
		}
		else{
	        bm = BitmapFactory.decodeResource(c.getResources(), R.drawable.model_image);			
		}
		
        return Utils.round(bm, Utils.dpToPx(100, c));
	}
	
}
