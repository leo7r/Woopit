package com.woopitapp.services;

import java.util.Random;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import android.text.format.Time;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.Tracker;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.suredigit.inappfeedback.FeedbackDialog;
import com.suredigit.inappfeedback.FeedbackSettings;
import com.woopitapp.R;
import com.woopitapp.entities.Message;
import com.woopitapp.entities.User;


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
	
	/* Emision de informacion dentro de Woopit */
	
	public static void sendBroadcast( Context con , int string_id ){

		Intent i = new Intent(con.getResources().getString(string_id));
		con.sendBroadcast(i);	
	}
	
	static ImageLoader imageLoader;
	
	public static ImageLoader getImageLoader( Context con ){
		
		if ( imageLoader == null ){
			imageLoader = ImageLoader.getInstance();
			imageLoader.init(ImageLoaderConfiguration.createDefault(con));
				        
	        com.nostra13.universalimageloader.utils.L.disableLogging();
		}
		
		return imageLoader;
	}
	
	public static String getUserImageURI( int id ){
		return "http://"+ServerConnection.HOST+"/users/images/"+id+".jpg";
	}

	public static String getModelImageURI( int id ){
		return "http://"+ServerConnection.HOST+"/models/previews/"+id+".jpg";
	}
	
	public static void setUserImage( Context con , ImageView iv , int id_user ){
		
		DisplayImageOptions options = new DisplayImageOptions.Builder()
        .showStubImage(R.drawable.user)
        .showImageForEmptyUri(R.drawable.user)
        .showImageOnFail(R.drawable.user)
        .cacheOnDisc()
        .displayer(new RoundedBitmapDisplayer(Utils.dpToPx(80, con)))
        .build();
		
		imageLoader = getImageLoader(con);		
		imageLoader.displayImage(getUserImageURI(id_user), iv, options );
	}

	public static void setModelImage( Context con , ImageView iv , int model_id ){
		
		DisplayImageOptions options = new DisplayImageOptions.Builder()
        .showStubImage(R.drawable.model_image)
        .showImageForEmptyUri(R.drawable.model_image)
        .showImageOnFail(R.drawable.model_image)
        .cacheOnDisc()
        .displayer(new RoundedBitmapDisplayer(Utils.dpToPx(80, con)))
        .build();
		
		imageLoader = getImageLoader(con);		
		imageLoader.displayImage(getModelImageURI(model_id), iv, options );
	}
	
	/* Send feedback */
	public static FeedbackDialog getFeedbackDialog( Activity act ){
		
		FeedbackSettings feedbackSettings = new FeedbackSettings();
		Resources res = act.getResources();		
		
		//SUBMIT-CANCEL BUTTONS
		feedbackSettings.setCancelButtonText(res.getString(R.string.no));
		feedbackSettings.setSendButtonText(res.getString(R.string.enviar));

		//DIALOG TEXT
		feedbackSettings.setText(res.getString(R.string.feedback_texto));
		feedbackSettings.setYourComments(res.getString(R.string.feedback_hint_pregunta));
		feedbackSettings.setTitle(res.getString(R.string.feedback_titulo));

		//TOAST MESSAGE
		feedbackSettings.setToast(res.getString(R.string.feedback_gracias));

		//RADIO BUTTONS
		feedbackSettings.setBugLabel(res.getString(R.string.feedback_error));
		feedbackSettings.setIdeaLabel(res.getString(R.string.feedback_idea));
		feedbackSettings.setQuestionLabel(res.getString(R.string.feedback_pregunta));

		//RADIO BUTTONS ORIENTATION AND GRAVITY
		feedbackSettings.setOrientation(LinearLayout.HORIZONTAL); // Default
		//feedbackSettings.setOrientation(LinearLayout.VERTICAL);
		//feedbackSettings.setGravity(Gravity.RIGHT); // Default
		//feedbackSettings.setGravity(Gravity.LEFT);
		feedbackSettings.setGravity(Gravity.CENTER);

		//SET DIALOG MODAL
		feedbackSettings.setModal(true); //Default is false

		//DEVELOPER REPLIES
		feedbackSettings.setReplyTitle(res.getString(R.string.feedback_respuesta));
		feedbackSettings.setReplyCloseButtonText(res.getString(R.string.feedback_cerrar));
		feedbackSettings.setReplyRateButtonText(res.getString(R.string.feedback_calificanos));
				
		return new FeedbackDialog(act, act.getResources().getString(R.string.feedback_key), feedbackSettings);
	}

	/* Random string for purchase security */
	
	public static String randomToken() {
		
		int len = 42;
		Random rnd = new Random();
		String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ_+";
		
		StringBuilder sb = new StringBuilder( len );
	   
		for( int i = 0; i < len ; i++ )
			sb.append( AB.charAt( rnd.nextInt(AB.length()) ) );
		
		return sb.toString();
	}
	
    
	/* Google analytics methods */    
	static Tracker getTracker( Context c ){
		
		Tracker t = GoogleAnalytics.getInstance(c).getTracker(c.getString(R.string.ga_trackingId));
		
		return t;
	}
	
    // Mensajes
    
    public static void onMessageNew( Context c , String from , int user_id ){
    	getTracker(c).send(MapBuilder
		    .createEvent("Mensajes", "Crear", User.get(c).id+"", null)
		    .build()
		); 
    }
    
    public static void onMessageNew( Context c , String from ){
    	getTracker(c).send(MapBuilder
		    .createEvent("Mensajes", "Crear", User.get(c).id+"", null)
		    .build()
		); 
    }
    
    public static void onMessageSent( Context c , String from , int modelId , String text , Double lat , Double lon ){
    	
    	getTracker(c).send(MapBuilder
		    .createEvent("Mensajes", "Envio", User.get(c).id+"" , null)
		    .build()
		);    	
    }
    
    public static void onMessageView( Context c , Message m , String from ){
    	
    	getTracker(c).send(MapBuilder
		    .createEvent("Mensajes", "Visto", User.get(c).id+"" , null)
		    .build()
		);    	
    }
    
    public static void onMessageImageNew( Context c , String from , int user_id ){
    	getTracker(c).send(MapBuilder
		    .createEvent("Mensajes con imagen", "Crear", User.get(c).id+"", null)
		    .build()
		); 
    }
    
    public static void onMessageImageNew( Context c , String from ){
    	getTracker(c).send(MapBuilder
		    .createEvent("Mensajes con imagen", "Crear", User.get(c).id+"", null)
		    .build()
		); 
    }
    
    public static void onMessageImageSent( Context c , String from , int modelId , String text , Double lat , Double lon ){
    	
    	getTracker(c).send(MapBuilder
		    .createEvent("Mensajes con imagen", "Envio", User.get(c).id+"" , null)
		    .build()
		);    	
    }
    
    public static void onMessageImageView( Context c , Message m , String from ){
    	
    	getTracker(c).send(MapBuilder
		    .createEvent("Mensajes con imagen", "Visto", User.get(c).id+"" , null)
		    .build()
		);    	
    }
        
    // Perfil
    public static void onEditProfileEnter( Context c , String from ){
    	getTracker(c).send(MapBuilder
		    .createEvent("Editar perfil", "Entrar", User.get(c).id+"", null)
		    .build()
		); 
    }
    
    public static void onEditProfileEdit( Context c , String type ){
    	getTracker(c).send(MapBuilder
		    .createEvent("Editar perfil", "Editar", User.get(c).id+"", null)
		    .build()
		); 
    }
    
    public static void onProfileCreateModel( Context c ){
    	getTracker(c).send(MapBuilder
		    .createEvent("Perfil", "Crear modelo", User.get(c).id+"", null)
		    .build()
		); 
    }
    
    // Compartir
    public static void onShareWoopit( Context c , String from , String action ){
    	getTracker(c).send(MapBuilder
		    .createEvent("Compartir Woopit", action , User.get(c).id+"", null)
		    .build()
		); 
    }
    
    // Enviar feedback
    public static void onSendFeedback( Context c , String from , String action ){
    	getTracker(c).send(MapBuilder
		    .createEvent("Enviar feedback", action , User.get(c).id+"", null)
		    .build()
		);
    }
    
    // Usuarios
    public static void onUserSearch( Context c , String from , String query ){
    	getTracker(c).send(MapBuilder
		    .createEvent("Usuarios", "Buscar" , User.get(c).id+"", null)
		    .build()
		);
    }
    
    public static void onUserAddOrReject( Context c , String from , String action , int user_id ){
    	getTracker(c).send(MapBuilder
		    .createEvent("Usuarios", action , User.get(c).id+"", null)
		    .build()
		);
    }
    
    public static void onUserProfileEnter( Context c , String from , int user_id ){
    	getTracker(c).send(MapBuilder
		    .createEvent("Usuarios", "Perfil" , User.get(c).id+"", null)
		    .build()
		);
    }
    
    // Encontrar amigos
    public static void onFriendsSearchEnter( Context c , String from ){
    	getTracker(c).send(MapBuilder
		    .createEvent("Encontrar amigos", "Entrar" , User.get(c).id+"", null)
		    .build()
		);
    }
    
    public static void onFriendsSearch( Context c , String from , int number_of_friends ){
    	getTracker(c).send(MapBuilder
		    .createEvent("Encontrar amigos", "Buscar" , User.get(c).id+"", null)
		    .build()
		);
    }
    
    public static void onFriendsAddOrReject( Context c , String from , String action , int user_id ){
    	getTracker(c).send(MapBuilder
		    .createEvent("Encontrar amigos", action , User.get(c).id+"", null)
		    .build()
		);
    }
    
    // Modelos
    public static void onModelOpen( Context c , String from , int model_id ){
    	getTracker(c).send(MapBuilder
		    .createEvent("Modelos", "Abrir" , User.get(c).id+"", null)
		    .build()
		);
    }
    
    public static void onModelSearch( Context c , String from , String query ){
    	getTracker(c).send(MapBuilder
		    .createEvent("Modelos", "Buscar" , User.get(c).id+"", null)
		    .build()
		);
    }
    
    public static void onModelBuy( Context c , String from , String action , int model_id ){
    	getTracker(c).send(MapBuilder
		    .createEvent("Comprar modelo", action , User.get(c).id+"", null)
		    .build()
		);
    }
    
    // Monedas
    public static void onCoinsEnter( Context c , String from ){
    	getTracker(c).send(MapBuilder
		    .createEvent("Monedas", "Entrar" , User.get(c).id+"", null)
		    .build()
		);
    }
    
    public static void onCoinsBuy( Context c , String from , String action , String coins_id , int user_coins ){
    	getTracker(c).send(MapBuilder
		    .createEvent("Monedas", action , User.get(c).id+"", null)
		    .build()
		);
    }
    
    public static void onCoinsBuyError( Context c , String from , String coins_id ){
    	getTracker(c).send(MapBuilder
		    .createEvent("Monedas", "Error" , User.get(c).id+"", null)
		    .build()
		);
    }
    
    // Login y registro
    public static void onLogin( Context c , String from , String social_network ){
    	getTracker(c).send(MapBuilder
		    .createEvent("Bienvenida", "Entrar" , social_network, null)
		    .build()
		);
    }
    
    public static void onRegister( Context c , String from , String social_network ){
    	getTracker(c).send(MapBuilder
		    .createEvent("Bienvenida", "Registrarse" , social_network, null)
		    .build()
		);
    }
	
}


