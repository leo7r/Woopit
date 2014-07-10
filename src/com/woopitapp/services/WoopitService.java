package com.woopitapp.services;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.TextUtils;
import android.util.Log;

import com.woopitapp.R;
import com.woopitapp.activities.MainActivity;
import com.woopitapp.activities.ModelListActivity;
import com.woopitapp.entities.User;

public class WoopitService extends Service{
	
	/* Server updates */
	private final static int INTERVAL = 1000 * 60 * 5;
	Handler mHandler;
	
	private final IBinder mBinder = new LocalBinder();
	
	public void onCreate(){
		
		mHandler = new Handler();
		startRepeatingTask();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		return super.onStartCommand(intent,flags,startId);
	  }
	
	public class LocalBinder extends Binder {
        public WoopitService getService() {
        	
            return WoopitService.this;
        }
    }
	
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    /* Metodos del servicio */
    
    class GetNotifications extends ServerConnection{
		
		public GetNotifications( ){
			super();
			init( getApplicationContext() , "get_notifications", new Object[]{ User.get(getApplicationContext()).id });
		}
		
		@Override
		public void onComplete(String result) {
			
			try{
				
				JSONArray notifications = new JSONArray(result);
				
				String[] ids = new String[notifications.length()];
				
				for ( int i = 0 ; i < notifications.length() ; i++ ){
					
					JSONObject notif = notifications.getJSONObject(i);

					int id = notif.getInt("i");
					int type = notif.getInt("t");
					String user_name = notif.getString("n");
					String user_username = notif.getString("u");
					int user_id = notif.getInt("q");
					String extra = notif.getString("e");
					
					String description = "", title = "";
					Intent resultIntent = null;
					Class<?> intentClass = null;
					Intent intent = null;
					
					ids[i] = id+"";
					
					switch ( type ){
					
					// New friend request
					case 1:
						title = user_name+" (@"+user_username+")";
						description = getResources().getString(R.string.notificacion_peticion_amigos);
						resultIntent = new Intent(getApplicationContext(), MainActivity.class);				
						break;
					
					// New message
					case 2:
						title = user_name+" (@"+user_username+")";
						description = getResources().getString(R.string.notificacion_nuevo_mensaje);
						resultIntent = new Intent(getApplicationContext(), MainActivity.class);		
						break;
					
					// Friend request accepted
					case 3:
						title = user_name+" (@"+user_username+")";
						description = getResources().getString(R.string.notificacion_peticion_aceptada);
						intent = new Intent(getApplicationContext(), ModelListActivity.class);
						intentClass = ModelListActivity.class;
						intent.putExtra("userId", user_id);
						intent.putExtra("userName", user_username);
						resultIntent = intent;
						break;
						
					// Woopit update
					case 4:
						title = getResources().getString(R.string.notificacion_woopit_actualizado);
						description = getResources().getString(R.string.notificacion_woopit_actualizado2);
						
						intent = new Intent(Intent.ACTION_VIEW);
						intent.setData(Uri.parse("market://details?id=com.woopitapp"));
						resultIntent = intent;
						break;
						
					}
					
					
					setUserNotification(type,title,description,resultIntent,intentClass);
				}
				
				if ( ids.length > 0 ){
					new SetNotificationsAsViewed(ids).execute();
				}
			}
			catch( Exception e ){
				Log.e("Error notifications", ""+result);
			}
			
		}
		
	}
    
    public void setUserNotification( int type , String title , String description , Intent goIntent , Class<?> intentClass ){
		
		NotificationCompat.Builder mBuilder =
		        new NotificationCompat.Builder(this)
		        .setSmallIcon(R.drawable.launcher_logo)
		        .setAutoCancel(true)
		        .setContentTitle(title)
		        .setContentText(description);
		
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		
		if ( intentClass != null ){
			stackBuilder.addParentStack(intentClass);
		}
		
		stackBuilder.addNextIntent(goIntent);
		PendingIntent resultPendingIntent = stackBuilder.getPendingIntent( 0, PendingIntent.FLAG_UPDATE_CURRENT );
		mBuilder.setContentIntent(resultPendingIntent);
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		
		mNotificationManager.notify(100+type, mBuilder.build());
	}
    
    class SetNotificationsAsViewed extends ServerConnection{
		
		public SetNotificationsAsViewed( String[] ids_list ){
			super();
			
			init(getApplicationContext(),"set_notifications_viewed",new Object[]{ TextUtils.join(",", ids_list) , User.get(getApplicationContext()).id });
		}

		@Override
		public void onComplete(String result) {}
		
	}
    
	Runnable mHandlerTask = new Runnable(){
	     @Override 
	     public void run() {
	    	 new GetNotifications().execute();
	    	 mHandler.postDelayed(mHandlerTask, INTERVAL);
	     }
	};

	void startRepeatingTask()
	{
	    mHandlerTask.run(); 
	}

	void stopRepeatingTask()
	{
	    mHandler.removeCallbacks(mHandlerTask);
	}
    
}
