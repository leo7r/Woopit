package com.woopitapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

public class NewVersionDialog extends Activity {

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.new_version_dialog);
		
		if ( Build.VERSION.SDK_INT >= 11 ){
			this.setFinishOnTouchOutside(false);
		}
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
	    Rect dialogBounds = new Rect();
	    getWindow().getDecorView().getHitRect(dialogBounds);

	    if ( !dialogBounds.contains((int) ev.getX(), (int) ev.getY())
	    		&& ev.getAction() == MotionEvent.ACTION_DOWN ) {
	        // Tapped outside so we finish the activity
	        //this.finish();
	    }
	    return super.dispatchTouchEvent(ev);
	}
	
	public void goToMarket( View v ){
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setData(Uri.parse("market://details?id=com.woopitapp"));
		
		startActivity(i);
		finish();
	}
	
}
