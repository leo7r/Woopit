package com.woopitapp;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.format.Time;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;


public class Utils {

	public static void cross(float[] p1, float[] p2, float[] result) {
		result[0] = p1[1]*p2[2]-p2[1]*p1[2];
		result[1] = p1[2]*p2[0]-p2[2]*p1[0];
		result[2] = p1[0]*p2[1]-p2[0]*p1[1];
	    }
	public static void scalarMultiply(float[] vector, float scalar) {
		for (int i=0;i<vector.length;i++)
		    vector[i] *= scalar;
	}
	public static float magnitude(float[] vector) {
		return (float)Math.sqrt(vector[0]*vector[0]+
					vector[1]*vector[1]+
					vector[2]*vector[2]);
	}
	public static void normalize(float[] vector) {
		scalarMultiply(vector, 1/magnitude(vector));
	}
	
	public static float[] convertFloats(Float[] floats)
	{
		float[] r = new float[floats.length];
	    for (int i=0; i < floats.length; i++)
	    {
	        r[i] = floats[i];
	    }
	    return r;
	}
	public static float[] convertFloats(double[] floats)
	{
		float[] r = new float[floats.length];
	    for (int i=0; i < floats.length; i++)
	    {
	        r[i] = (float)floats[i];
	    }
	    return r;
	}
	
	public static int parseInt(String val) {
		
		if (val.length() == 0) {
			return -1;
		}
		return Integer.parseInt(val);
	}
	
	public static int[] parseIntTriple(String face) {
			
			int ix = face.indexOf("/");
			if (ix == -1)
				return new int[] {Integer.parseInt(face)-1};
			else {
				int ix2 = face.indexOf("/", ix+1);
				if (ix2 == -1) {
					return new int[] 
					               {Integer.parseInt(face.substring(0,ix))-1,
							Integer.parseInt(face.substring(ix+1))-1};
				}
				else {
					return new int[] 
					               {parseInt(face.substring(0,ix))-1,
							parseInt(face.substring(ix+1,ix2))-1,
							parseInt(face.substring(ix2+1))-1
					               };
				}
			}
		}

	/** One second (in milliseconds) */
	private static final int _A_SECOND = 1000;
	/** One minute (in milliseconds) */
	private static final int _A_MINUTE = 60 * _A_SECOND;
	/** One hour (in milliseconds) */
	private static final int _AN_HOUR = 60 * _A_MINUTE;
	/** One day (in milliseconds) */
	private static final int _A_DAY = 24 * _AN_HOUR;
	
	private static boolean ads_on = true;
	
	public static void closeKeyboard( EditText e , Context c ){

		// Close keyboard
		InputMethodManager imm = (InputMethodManager) c.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(e.getWindowToken(), 0);
	}
	
	public static void openKeyboard( EditText e , Context c ){

		e.requestFocus();
		// Open keyboard
		InputMethodManager imm = (InputMethodManager) c.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.showSoftInput(e, 0);
	}
	
	public static Bitmap round(Bitmap bitmap, int pixels) {
		
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap
                .getHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = pixels;
        
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        
        bitmap.recycle();
        
        return output;
    }
	
	public static int dpToPx(float dp, Context context){
		
		if ( context != null){
			Resources resources = context.getResources();
		    DisplayMetrics metrics = resources.getDisplayMetrics();
		    float px = dp * (metrics.densityDpi / 160f);
		    return (int)px;
		}
	    
		return 0;
	}
	
	public static int pxToDp(float px, Context context){
	    Resources resources = context.getResources();
	    DisplayMetrics metrics = resources.getDisplayMetrics();
	    float dp = px / (metrics.densityDpi / 160f);
	    return (int) dp;
	}
	
	public static int getScreenOrientation( Activity act ) {
	    int rotation = act.getWindowManager().getDefaultDisplay().getRotation();
	    DisplayMetrics dm = new DisplayMetrics();
	    act.getWindowManager().getDefaultDisplay().getMetrics(dm);
	    int width = dm.widthPixels;
	    int height = dm.heightPixels;
	    int orientation;
	    // if the device's natural orientation is portrait:
	    if ((rotation == Surface.ROTATION_0
	            || rotation == Surface.ROTATION_180) && height > width ||
	        (rotation == Surface.ROTATION_90
	            || rotation == Surface.ROTATION_270) && width > height) {
	        switch(rotation) {
	            case Surface.ROTATION_0:
	                orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
	                break;
	            case Surface.ROTATION_90:
	                orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
	                break;
	            case Surface.ROTATION_180:
	                orientation =
	                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
	                break;
	            case Surface.ROTATION_270:
	                orientation =
	                    ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
	                break;
	            default:
	                Log.e("", "Unknown screen orientation. Defaulting to " +
	                        "portrait.");
	                orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
	                break;              
	        }
	    }
	    // if the device's natural orientation is landscape or if the device
	    // is square:
	    else {
	        switch(rotation) {
	            case Surface.ROTATION_0:
	                orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
	                break;
	            case Surface.ROTATION_90:
	                orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
	                break;
	            case Surface.ROTATION_180:
	                orientation =
	                    ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
	                break;
	            case Surface.ROTATION_270:
	                orientation =
	                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
	                break;
	            default:
	                Log.e("", "Unknown screen orientation. Defaulting to " +
	                        "landscape.");
	                orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
	                break;              
	        }
	    }

	    return orientation;
	}

	public static void expand(final View v) {
	    v.measure(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
	    final int targtetHeight = v.getMeasuredHeight();

	    v.getLayoutParams().height = 0;
	    v.setVisibility(View.VISIBLE);
	    Animation a = new Animation()
	    {
	        @Override
	        protected void applyTransformation(float interpolatedTime, android.view.animation.Transformation t) {
	            v.getLayoutParams().height = interpolatedTime == 1
	                    ? LayoutParams.WRAP_CONTENT
	                    : (int)(targtetHeight * interpolatedTime);
	            v.requestLayout();
	        }

	        @Override
	        public boolean willChangeBounds() {
	            return true;
	        }
	    };

	    // 1dp/ms
	    a.setDuration((int)(targtetHeight / v.getContext().getResources().getDisplayMetrics().density)*3);
	    v.startAnimation(a);
	}

	public static void collapse(final View v) {
	    final int initialHeight = v.getMeasuredHeight();

	    Animation a = new Animation()
	    {
	        @Override
	        protected void applyTransformation(float interpolatedTime, android.view.animation.Transformation t) {
	            if(interpolatedTime == 1){
	                v.setVisibility(View.GONE);
	            }else{
	                v.getLayoutParams().height = initialHeight - (int)(initialHeight * interpolatedTime);
	                v.requestLayout();
	            }
	        }

	        @Override
	        public boolean willChangeBounds() {
	            return true;
	        }
	    };

	    // 1dp/ms
	    a.setDuration((int)(initialHeight / v.getContext().getResources().getDisplayMetrics().density)*3);
	    v.startAnimation(a);
	}
	
	/*
	public static String getTimeAgo(long time, Context context) {
	    
		time*=1000;
		
	    Time now_t = new Time();
	    now_t.setToNow();
	    final long now = now_t.toMillis(false);
	    if (time > now || time <= 0) return "";

	    
	    final Resources res = context.getResources();
	    final long time_difference = now - time;
	    
	    if (time_difference < _A_MINUTE)
	        return res.getString(R.string.just_now);
	    else if (time_difference < 50 * _A_MINUTE){
	    	
	    	int minutes = (int) time_difference / _A_MINUTE;
	    	
	    	if ( minutes > 1 ){
	    		return res.getString(R.string.time_ago, res.getString(R.string.minutes,minutes));
	    	}
	    	else{
	    		return res.getString(R.string.time_ago, res.getString(R.string.minute));
	    	}
	    	
	    }
	    else if (time_difference < 24 * _AN_HOUR){
	    	
	    	int hours = (int) time_difference / _AN_HOUR;
	    	
	    	if ( hours > 1 ){
	    		return res.getString(R.string.time_ago, res.getString(R.string.hours,hours));
	    	}
	    	else{
	    		return res.getString(R.string.time_ago, res.getString(R.string.hour));
	    	}
	    	
	    }
	    else if (time_difference < 48 * _AN_HOUR)
	        return res.getString(R.string.yesterday);
	    else{
	    	
	    	int days = (int) time_difference / _A_DAY;
	    	if ( days > 1 ){
	    		
	    		if ( days > 7 ){
		    		return res.getString(R.string.time_ago, res.getString(R.string.weeks));
	    		}
	    		else{
		    		return res.getString(R.string.time_ago, res.getString(R.string.days,days));
	    		}
	    	}
	    	else{
	    		return res.getString(R.string.time_ago, res.getString(R.string.day));
	    	}
	    }
	}
	*/
	
	public static Object[] concat(Object[] A, Object[] B) {
		int aLen = A.length;
		int bLen = B.length;
		Object[] C = new Object[aLen + bLen];
		System.arraycopy(A, 0, C, 0, aLen);
		System.arraycopy(B, 0, C, aLen, bLen);

		return C;
	}
	
	public static void fixBackgroundRepeat(View view) {
	    Drawable bg = view.getBackground();
	    if (bg != null) {
	        if (bg instanceof BitmapDrawable) {
	            BitmapDrawable bmp = (BitmapDrawable) bg;
	            bmp.mutate(); // make sure that we aren't sharing state anymore
	            bmp.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
	        }
	    }
	}
	
	public static String getSongDuration(int millis) {

		int seconds = millis / 1000;
		
	    int hours = seconds / 3600;
	    int minutes = (seconds % 3600) / 60;
	    seconds = seconds % 60;

	    if ( hours > 0 ){
	    	return twoDigitString(hours) + ":" + twoDigitString(minutes) + ":" + twoDigitString(seconds);
	    }
	    
	    return twoDigitString(minutes) + ":" + twoDigitString(seconds);
	}

	public static String twoDigitString(int number) {

	    if (number == 0) {
	        return "00";
	    }

	    if (number / 10 == 0) {
	        return "0" + number;
	    }

	    return String.valueOf(number);
	}
	
	/* Google Analytics */
	/*
	public static void sendEvent( Context con , String category , String action , String label , int value ){
		sendEvent(  con ,  category ,  action ,  label ,  (long) value );
	}
	
	public static void sendEvent( Context con , String category , String action , String label , long value ){
		
		GoogleAnalytics mGaInstance = GoogleAnalytics.getInstance(con);
		Tracker mGaTracker = mGaInstance.getTracker(con.getResources().getString(R.string.google_analytics));
		mGaTracker.sendEvent(category, action, label , value);
	}
	*/
	
	/* Send feedback */
	/*
	public static FeedbackDialog getFeedbackDialog( Activity act ){
		
		FeedbackSettings feedbackSettings = new FeedbackSettings();
		Resources res = act.getResources();		
		
		//SUBMIT-CANCEL BUTTONS
		feedbackSettings.setCancelButtonText(res.getString(R.string.general_no));
		feedbackSettings.setSendButtonText(res.getString(R.string.add_song_send));

		//DIALOG TEXT
		feedbackSettings.setText(res.getString(R.string.feedback_text));
		feedbackSettings.setYourComments(res.getString(R.string.feedback_question_hint));
		feedbackSettings.setTitle(res.getString(R.string.feedback_title));

		//TOAST MESSAGE
		feedbackSettings.setToast(res.getString(R.string.feedback_thanks));

		//RADIO BUTTONS
		feedbackSettings.setBugLabel(res.getString(R.string.feedback_bug));
		feedbackSettings.setIdeaLabel(res.getString(R.string.feedback_idea));
		feedbackSettings.setQuestionLabel(res.getString(R.string.feedback_question));

		//RADIO BUTTONS ORIENTATION AND GRAVITY
		feedbackSettings.setOrientation(LinearLayout.HORIZONTAL); // Default
		//feedbackSettings.setOrientation(LinearLayout.VERTICAL);
		//feedbackSettings.setGravity(Gravity.RIGHT); // Default
		//feedbackSettings.setGravity(Gravity.LEFT);
		feedbackSettings.setGravity(Gravity.CENTER);

		//SET DIALOG MODAL
		feedbackSettings.setModal(true); //Default is false

		//DEVELOPER REPLIES
		feedbackSettings.setReplyTitle(res.getString(R.string.feedback_answer));
		feedbackSettings.setReplyCloseButtonText(res.getString(R.string.feedback_close));
		feedbackSettings.setReplyRateButtonText(res.getString(R.string.rate));

		return new FeedbackDialog(act, act.getResources().getString(R.string.feedback_api), feedbackSettings);
	}
	
	public static void setAd( final Context c , final View v ){

		if ( c == null )
			return;
		
		final AdView adView = (AdView) v.findViewById(R.id.adView);
        
        if ( adView.getVisibility() == View.GONE )
        	return;
        
        if ( ads_on )
        	adView.loadAd(new AdRequest());
        
        adView.setAdListener(new AdListener(){

			@Override
			public void onDismissScreen(Ad arg0) {}

			@Override
			public void onFailedToReceiveAd(Ad arg0, ErrorCode arg1) {}

			@Override
			public void onLeaveApplication(Ad arg0) {}

			@Override
			public void onPresentScreen(Ad arg0) {}

			@Override
			public void onReceiveAd(Ad arg0) {
				final ImageView closeAd = (ImageView) v.findViewById(R.id.closeAd);
		        closeAd.setVisibility(View.VISIBLE);
		        closeAd.setOnClickListener(new OnClickListener(){

					@Override
					public void onClick(View arg0) {
						adView.setVisibility(View.GONE);
						closeAd.setVisibility(View.GONE);
					}
				});
			}
		});
        
	}
	
	public static void setAd( final Activity act ){
				
        final AdView adView = (AdView) act.findViewById(R.id.adView);
        
        if ( adView.getVisibility() == View.GONE )
        	return;
        
        if ( ads_on )
        	adView.loadAd(new AdRequest());
        
        adView.setAdListener(new AdListener(){

			@Override
			public void onDismissScreen(Ad arg0) {}

			@Override
			public void onFailedToReceiveAd(Ad arg0, ErrorCode arg1) {}

			@Override
			public void onLeaveApplication(Ad arg0) {}

			@Override
			public void onPresentScreen(Ad arg0) {}

			@Override
			public void onReceiveAd(Ad arg0) {
				final ImageView closeAd = (ImageView) act.findViewById(R.id.closeAd);
		        closeAd.setVisibility(View.VISIBLE);
		        closeAd.setOnClickListener(new OnClickListener(){

					@Override
					public void onClick(View arg0) {
						//Toast.makeText(act, "CLOSE AD :D", Toast.LENGTH_SHORT).show();
						adView.setVisibility(View.GONE);
						closeAd.setVisibility(View.GONE);
					}
				});
			}
		});
        
	}
	*/
		
}


