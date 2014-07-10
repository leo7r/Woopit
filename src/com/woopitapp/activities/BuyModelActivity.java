package com.woopitapp.activities;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.Toast;

import com.android.vending.billing.IInAppBillingService;
import com.woopitapp.R;
import com.woopitapp.entities.User;
import com.woopitapp.services.ServerConnection;
import com.woopitapp.services.Utils;

public class BuyModelActivity extends Activity {
	
	IInAppBillingService mService;
	String TAG = "In-app billing";
	private int BUY_REQUEST_CODE = 777;
	private String security_token;
	private int id_model;
	Activity act;
	private final int error_notification_id = 77;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.test);
		act = this;
		
		Bundle extras = getIntent().getExtras();
		
		if ( extras.containsKey("modelId") ){
			id_model = extras.getInt("modelId");
			bindService(new Intent("com.android.vending.billing.InAppBillingService.BIND"), mServiceConn, Context.BIND_AUTO_CREATE);
		}
		else{
			finish();
		}
		
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == BUY_REQUEST_CODE) {           
			int responseCode = data.getIntExtra("RESPONSE_CODE", 0);
			String purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA");
			String dataSignature = data.getStringExtra("INAPP_DATA_SIGNATURE");
			
			if (resultCode == RESULT_OK) {
				try {
					JSONObject jsonData = new JSONObject(purchaseData);
					
					String sToken = jsonData.getString("developerPayload");
					
					if ( sToken.equals(security_token) ){
						Log.i(TAG, jsonData.toString());
						Log.i(TAG,dataSignature);
						Log.i(TAG,responseCode+"");
						
						 String purchase_token = jsonData.getString("purchaseToken");
						 String order_id = jsonData.getString("orderId");
						 long purchase_time = jsonData.getLong("purchaseTime");
						 String product_id = jsonData.getString("productId");
						
						//new ConsumeModel().execute();
						new SavePurchase(this,id_model,purchase_token,order_id,purchase_time,product_id,true).execute();
					}
					else{
						Log.e(TAG, "Error, fallaron los token");
					}
					
				}
				catch (JSONException e) {
					Log.i(TAG,"Failed to parse purchase data.");
					e.printStackTrace();
					
					Toast.makeText(getApplicationContext(), R.string.error_compra, Toast.LENGTH_SHORT).show();
					setResult(RESULT_CANCELED);
					finish();
				}
			}
			else{
				Toast.makeText(getApplicationContext(), R.string.error_compra, Toast.LENGTH_SHORT).show();
				setResult(resultCode);
				finish();
			}
		}
	}
	
	protected void onDestroy() {
	    super.onDestroy();
	    
	    if (mService != null) {
	        unbindService(mServiceConn);
	    }   
	}
	
	ServiceConnection mServiceConn = new ServiceConnection() {
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
			mService = null;
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mService = IInAppBillingService.Stub.asInterface(service);
			Log.i(TAG,"Connected");
			
			new VerifyItemDisponibility().execute();
		}
	};
	
	class VerifyItemDisponibility extends AsyncTask<Void,Void,Boolean>{
		
		Bundle skuDetails;
		
		@Override
		protected Boolean doInBackground(Void... params) {
			
			ArrayList<String> skuList = new ArrayList<String> ();
			
			skuList.add("5_corazon");
			
			Bundle querySkus = new Bundle();
			querySkus.putStringArrayList("ITEM_ID_LIST", skuList);
			
			try {
				skuDetails = mService.getSkuDetails(3, getPackageName(), "inapp", querySkus);
				
				return true;
				
			} catch (RemoteException e) {
				
				e.printStackTrace();
			}
			
			return false;
		}
		
		protected void onPostExecute( Boolean success ){
			
			if ( success ){
				
				int response = skuDetails.getInt("RESPONSE_CODE");
				
				if (response == 0) {
				   ArrayList<String> responseList = skuDetails.getStringArrayList("DETAILS_LIST");
				   
				   if ( responseList.size() == 0 ){
					   Toast.makeText(getApplicationContext(), R.string.error_disponibilidad_modelo, Toast.LENGTH_SHORT).show();
					   finish();
				   }
				   else{
						new VerifyItemNotOwned( act ).execute();
				   }
				   
				}
				
			}
			else{
				Toast.makeText(getApplicationContext(), R.string.error_desconocido, Toast.LENGTH_SHORT).show();
			}
			
		}
		
	}
	
	class VerifyItemNotOwned extends ServerConnection{

		ProgressDialog dialog;
		
		public VerifyItemNotOwned( Activity act ){
			super();
			
			dialog = ProgressDialog.show(act, "",act.getResources().getString(R.string.verificando_disponibilidad), true);
    		dialog.setCancelable(false);
    		
			init(act,"verify_not_owned",new Object[]{ User.get(act).id+"" , id_model+"" });
		}
		
		@Override
		public void onComplete(String result) {
			
			dialog.dismiss();
			
			if ( result != null ){
				
				if ( result.equals("ok") ){

					new BuyModel().execute();
				}
				else{
					Toast.makeText(getApplicationContext(), R.string.error_desconocido, Toast.LENGTH_SHORT).show();
				}			
			}
			else{
				Toast.makeText(getApplicationContext(), R.string.error_de_conexion, Toast.LENGTH_SHORT).show();
			}
			
		}
		
	}
	
	class BuyModel extends AsyncTask<Void,Void,Boolean>{

		@Override
		protected Boolean doInBackground(Void... arg0) {
			
			try {
				
				security_token = Utils.randomToken();
				
				Bundle buyIntentBundle = mService.getBuyIntent(3, getPackageName(), "android.test.purchased", "inapp", security_token);
				int response_code = buyIntentBundle.getInt("RESPONSE_CODE");
				
				if ( response_code == 0  ){
					
					PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");
					
					startIntentSenderForResult(pendingIntent.getIntentSender(), BUY_REQUEST_CODE , new Intent(), Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(0) );
					
					return true;
				}
				else{
					
					// Item already owned
					if ( response_code == 7 ){
						new ConsumeModel().execute();
					}
					
					Log.e(TAG, "Error response code "+response_code);
					return false;
				}
				
				
			} catch (RemoteException e) {
				
				e.printStackTrace();
			} catch (SendIntentException e) {
				
				e.printStackTrace();
			}
			
			return false;
		}
		
	}
	
	class ConsumeModel extends AsyncTask<Void,Void,Boolean>{
		
		@Override
		protected Boolean doInBackground(Void... arg0) {
			
			String purchaseToken = "inapp:"+getPackageName()+":android.test.purchased";
			try {
				int response = mService.consumePurchase(3, getPackageName(),purchaseToken);
				
				if ( response == 0 ){
					return true;
				}
				
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			
			return false;
		}
		
		public void onPostExecute( Boolean b ){
			
			if ( b ){
				Toast.makeText(getApplicationContext(), "Listo, consumido", Toast.LENGTH_SHORT).show();
			}
			else{
				Toast.makeText(getApplicationContext(), "NO FUE consumido", Toast.LENGTH_SHORT).show();
			}
			
		}
		
	}
	
	class SavePurchase extends ServerConnection{

		Activity act;
		int id_model;
		String purchase_token, order_id, product_id;
		long purchase_time;
		ProgressDialog dialog;
		int count = 10;
		SavePurchase sp;
    	
    	public SavePurchase( Activity act , int id_model , String purchase_token , String order_id , long purchase_time , String product_id , final boolean show_dialog ){
    		super();
    		
    		this.act = act;
    		this.id_model = id_model;
    		this.purchase_token = purchase_token;
    		this.order_id = order_id;
    		this.purchase_time = purchase_time;
    		this.product_id = product_id;
    		
    		if ( show_dialog ){
    			dialog = ProgressDialog.show(act, "",act.getResources().getString(R.string.guardando_compra), true);
        		dialog.setCancelable(true);        		
        		dialog.setOnCancelListener(new OnCancelListener(){
        			
    				@Override
    				public void onCancel(DialogInterface arg0) {    					
    					Toast.makeText(getApplicationContext(), R.string.error_compra_reintentando, Toast.LENGTH_LONG).show();
    					onError();
    				}
    			});
    		}
    		
    		init(act,"save_purchase",new Object[]{ User.get(act).id+"" , id_model+"" , purchase_token , order_id , purchase_time+"" , product_id });
    	}
    	
    	public void setNotification(){
    		NotificationCompat.Builder mBuilder =
    		        new NotificationCompat.Builder(act)
    		        .setSmallIcon(R.drawable.launcher_logo)
    		        .setContentTitle(getResources().getString(R.string.conectando_con_servidor))
    		        .setContentText(getResources().getString(R.string.para_completar_compra));
    		// Creates an explicit intent for an Activity in your app
    		Intent resultIntent = new Intent(act, ModelPreviewActivity.class);
    		resultIntent.putExtra("modelId", id_model);

    		// The stack builder object will contain an artificial back stack for the
    		// started Activity.
    		// This ensures that navigating backward from the Activity leads out of
    		// your application to the Home screen.
    		TaskStackBuilder stackBuilder = TaskStackBuilder.create(act);
    		// Adds the back stack for the Intent (but not the Intent itself)
    		stackBuilder.addParentStack(ModelPreviewActivity.class);
    		// Adds the Intent that starts the Activity to the top of the stack
    		stackBuilder.addNextIntent(resultIntent);
    		PendingIntent resultPendingIntent =
    		        stackBuilder.getPendingIntent(
    		            0,
    		            PendingIntent.FLAG_UPDATE_CURRENT
    		        );
    		mBuilder.setContentIntent(resultPendingIntent);
    		NotificationManager mNotificationManager =
    		    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    		// mId allows you to update the notification later on.
    		mNotificationManager.notify(error_notification_id, mBuilder.build());
    	}
		
    	public void cancelNotification(){
    		NotificationManager mNotificationManager =
        		    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    		
    		mNotificationManager.cancel(error_notification_id);
    	}
    	
    	public void onError(){
    		
			sp = new SavePurchase( this.act , this.id_model , this.purchase_token , this.order_id , this.purchase_time , this.product_id , false  );
			sp.count = count-1;
			
			if ( sp.count > 0 ){
	    		setNotification();
				new Handler().postDelayed(new Runnable(){

					@Override
					public void run() {
						sp.execute();
					}}, 1000*10);
				
				act.finish();
			}
    	}
    	
		@Override
		public void onComplete(String result) {
			
			if ( dialog != null ){
				dialog.dismiss();
			}
			
			if ( result != null ){
				
				if ( result.equals("ok")){
					
					Toast.makeText(getApplicationContext(), R.string.compra_hecha, Toast.LENGTH_LONG).show();
					Utils.sendBroadcast(getApplicationContext(), R.string.broadcast_model_purchase);
					cancelNotification();
					
					setResult(RESULT_OK);
					finish();
				}
				else{
					Toast.makeText(getApplicationContext(), R.string.error_desconocido, Toast.LENGTH_SHORT).show();
				}
			}
			else{
				Log.e(TAG, "Error result null");
				//Toast.makeText(getApplicationContext(), R.string.error_de_conexion, Toast.LENGTH_SHORT).show();
				Toast.makeText(getApplicationContext(), R.string.error_compra_reintentando, Toast.LENGTH_SHORT).show();
				onError();
			}
			
		}
		
	}
	
}
