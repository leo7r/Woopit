package com.woopitapp.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.woopitapp.R;
import com.woopitapp.activities.MainActivity;
import com.woopitapp.activities.ModelListActivity;
import com.woopitapp.entities.User;

public class GcmIntentService extends IntentService {
	
	private String TAG = "GCMIntent";

    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        
    	Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        
        String messageType = gcm.getMessageType(intent);
        
        if (!extras.isEmpty()) {
            
            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
            	Log.e(TAG, "Error "+messageType);
                
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
            	Log.e(TAG, "Error "+messageType);            
            	
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
            	
            	int type = Integer.parseInt(extras.getString("t"));
				int user_id = Integer.parseInt(extras.getString("i"));
				String user_name = extras.getString("n");
				String user_username = extras.getString("u");
				String extra = extras.getString("e");
				
				String description = "", title = "";
				Intent resultIntent = null;
				Class<?> intentClass = null;
            	
				switch ( type ){
				
				// New friend request
				case 1:
					title = user_name+" (@"+user_username+")";
					description = getResources().getString(R.string.notificacion_peticion_amigos);
					resultIntent = new Intent(getApplicationContext(), MainActivity.class);
					

			    	new User.GetFriendRequest(getApplicationContext()).execute();
					break;
					
				// New message
				case 2:
					title = user_name+" (@"+user_username+")";
					description = getResources().getString(R.string.notificacion_nuevo_mensaje);
					resultIntent = new Intent(getApplicationContext(), MainActivity.class);
					
					Utils.sendBroadcast(getApplicationContext(), R.string.broadcast_messages);
					break;
				
				// Friend request accepted
				case 3:
					title = user_name+" (@"+user_username+")";
					description = getResources().getString(R.string.notificacion_peticion_aceptada);
					resultIntent = new Intent(getApplicationContext(), ModelListActivity.class);
					intentClass = ModelListActivity.class;
					resultIntent.putExtra("userId", user_id);
					resultIntent.putExtra("userName", user_username);
					
			        new User.GetFriends(getApplicationContext(), User.get(getApplicationContext()).id).execute();
					break;
					
				// Woopit update
				case 4:
					title = getResources().getString(R.string.notificacion_woopit_actualizado);
					description = getResources().getString(R.string.notificacion_woopit_actualizado2);
					
					resultIntent = new Intent(Intent.ACTION_VIEW);
					resultIntent.setData(Uri.parse("market://details?id=com.woopitapp"));
					break;
					
				}
				
				setUserNotification(type,title,description,resultIntent,intentClass);
            }
        }
        
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }
    
    public void setUserNotification( int type , String title , String description , Intent goIntent , Class<?> intentClass ){
		
    	Uri notifSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
    	
		NotificationCompat.Builder mBuilder =
		        new NotificationCompat.Builder(this)
		        .setSmallIcon(R.drawable.notif_icon)
		        .setAutoCancel(true)
		        .setContentTitle(title)
		        .setContentText(description)
		        .setSound(notifSound);
		
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
    
}