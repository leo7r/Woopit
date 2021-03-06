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
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.vending.billing.IInAppBillingService;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.woopitapp.R;
import com.woopitapp.WoopitActivity;
import com.woopitapp.entities.User;
import com.woopitapp.services.ServerConnection;
import com.woopitapp.services.Utils;

public class BuyCoinActivity extends WoopitActivity {
	
	IInAppBillingService mService;
	String TAG = "In-app billing";
	private int BUY_REQUEST_CODE = 777;
	private String security_token;
	private int id_model;
	Activity act;
	private final int error_notification_id = 77;
	int user_coins;
	
	ListView package_list;
	PackageAdapter mAdapter;
	
	private final String coins_20 = "com.woopitapp.coins.20";
	private final String coins_50 = "com.woopitapp.coins.50";
	private final String coins_130 = "com.woopitapp.coins.130";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.buy_coins);
		act = this;
		
		Bundle extras = getIntent().getExtras();
		id_model = extras.getInt("modelId");
		
		package_list = (ListView) findViewById(R.id.package_list);
		ArrayList<BuyPackage> pkgs = new ArrayList<BuyPackage>();
		pkgs.add(new BuyPackage(coins_20,getResources().getString(R.string.monedas_20),getResources().getString(R.string.info_monedas),R.drawable.coins_20,"0.99",20));
		pkgs.add(new BuyPackage(coins_50,getResources().getString(R.string.monedas_50),getResources().getString(R.string.info_monedas),R.drawable.coins_50,"1.99",50));
		pkgs.add(new BuyPackage(coins_130,getResources().getString(R.string.monedas_130),getResources().getString(R.string.info_monedas),R.drawable.coins_130,"4.99",130));
				
		mAdapter = new PackageAdapter(this,R.id.package_list,pkgs);
		package_list.setAdapter(mAdapter);
		
		bindService(new Intent("com.android.vending.billing.InAppBillingService.BIND"), mServiceConn, Context.BIND_AUTO_CREATE);
		
		new GetUserCoins().execute();
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
						
						new ConsumePurchase(act,purchase_token,order_id,purchase_time,product_id).execute();
												
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
			
		}
	};
	
	class BuyPackage{
		
		String id;
		String name,description,price;
		int image_resource;
		int coins;
		
		public BuyPackage( String id , String name , String description , int image_resource , String price , int coins ){
			
			this.id = id;
			this.name = name;
			this.description = description;
			this.image_resource = image_resource;
			this.price = price;
			this.coins = coins;
		}
		
	}
	
    public class PackageAdapter extends ArrayAdapter<BuyPackage>{
    	
		ArrayList<BuyPackage> items;
		Context context;
		Filter filter;
		LayoutInflater inflater;

		public PackageAdapter(Context context, int textViewResourceId, ArrayList<BuyPackage> objects){
			super(context, textViewResourceId, objects);
			this.items = objects;
			this.context = context;
			inflater = (LayoutInflater)this.context.getSystemService("layout_inflater");
		}

		public View getView(final int position, View convertView, ViewGroup parent){
			
			if ( convertView == null ){
				convertView = inflater.inflate(R.layout.coin_package_item, null);
			}
			
			final BuyPackage item = getItem(position);
			
			TextView name = (TextView) convertView.findViewById(R.id.name);
			TextView description = (TextView) convertView.findViewById(R.id.description);
			TextView price = (TextView) convertView.findViewById(R.id.price);
			ImageView image = (ImageView) convertView.findViewById(R.id.image);
			
			image.setImageResource(item.image_resource);
			name.setText(item.name);
			description.setText(item.description);
			price.setText("$"+item.price);
			
			convertView.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View arg0) {
					
					new VerifyItemDisponibility( item.id ).execute();
				}
			});
			
			return convertView;
		}
		
		public BuyPackage getItem( int position ){
			return items.get(position);
		}
		
    }
    
    class GetUserCoins extends ServerConnection{
    	
    	public GetUserCoins(){
    		super();
    		
    		init(getApplicationContext(),"get_user_coins",new Object[]{ User.get(getApplicationContext()).id });
    	}
    	
		@Override
		public void onComplete(String result) {
			
			((RelativeLayout)findViewById(R.id.loading)).setVisibility(View.GONE);
			
			if ( result != null ){
				
				try {
					JSONObject coins = new JSONObject(result);
					
					user_coins = coins.getInt("c");
					((TextView)findViewById(R.id.user_coins)).setText(user_coins+"");
					
				} catch (JSONException e) {
					e.printStackTrace();
				}
				
			}
			else{
				Toast.makeText(getApplicationContext(), R.string.error_de_conexion, Toast.LENGTH_SHORT).show();
			}
			
		}
    	
    }
	
	/* Buy methods */
	
	class VerifyItemDisponibility extends AsyncTask<Void,Void,Boolean>{
		
		Bundle skuDetails;
		String id;
		
		public VerifyItemDisponibility( String id ){
			super();
			
			this.id = id;
		}
		
		@Override
		protected Boolean doInBackground(Void... params) {
			
			ArrayList<String> skuList = new ArrayList<String> ();
			
			skuList.add(id);
			
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
						new BuyModel(id).execute();
					   //new VerifyItemNotOwned( act ).execute();
				   }
				   
				}
				
			}
			else{
				Toast.makeText(getApplicationContext(), R.string.error_desconocido, Toast.LENGTH_SHORT).show();
			}
			
		}
		
	}
	
	/*
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
	*/
	
	class BuyModel extends AsyncTask<Void,Void,Boolean>{

		String id;
		
		public BuyModel( String id ){
			super();
			this.id = id;
		}
		
		@Override
		protected Boolean doInBackground(Void... arg0) {
			
			try {
				
				security_token = Utils.randomToken();
				
				Bundle buyIntentBundle = mService.getBuyIntent(3, getPackageName(), id, "inapp", security_token);
				int response_code = buyIntentBundle.getInt("RESPONSE_CODE");
				
				if ( response_code == 0  ){
					
					PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");
					
					startIntentSenderForResult(pendingIntent.getIntentSender(), BUY_REQUEST_CODE , new Intent(), Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(0) );
					
					Utils.onCoinsBuy(getApplicationContext(), "BuyCoinActivity", "Comprar", id, user_coins);
					
					return true;
				}
				else{
					
					// Item already owned
					if ( response_code == 7 ){
						Log.e("Item","Item ya adquirido");
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
		
		protected void onPostExecute( Boolean b ){
			
			if ( !b ){
				Toast.makeText(getApplicationContext(), R.string.error_compra, Toast.LENGTH_SHORT).show();
			}
			
		}
		
	}
	
	class ConsumePurchase extends AsyncTask<Void,Void,Boolean>{
		
		String purchase_token,order_id,product_id;
		long purchase_time;
		Activity act;
		
		public ConsumePurchase( Activity act , String purchase_token , String order_id , long purchase_time , String product_id ){
			this.act = act;
			this.purchase_token = purchase_token;
    		this.order_id = order_id;
    		this.purchase_time = purchase_time;
    		this.product_id = product_id;
		}
		
		@Override
		protected Boolean doInBackground(Void... arg0) {
			
			try {
				int response = mService.consumePurchase(3, getPackageName(),purchase_token);
				
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
				Log.i("Item","Consumido");
				new SavePurchase(act,purchase_token,order_id,purchase_time,product_id,true).execute();
				//Toast.makeText(getApplicationContext(), "Listo, consumido", Toast.LENGTH_SHORT).show();
			}
			else{
				Log.e("Item","NO Consumido");
				/*
				if ( retry_count > 0 ){

					Log.e("Item","Intentando de nuevo");
					new Handler().postDelayed(new Runnable(){
						
						@Override
						public void run() {
							new ConsumePurchase(id,retry_count-1).execute();
						}
					}, 60*1000/retry_count);
					
					
				}
				*/
				//Toast.makeText(getApplicationContext(), "NO FUE consumido", Toast.LENGTH_SHORT).show();
			}
			
		}
		
	}
	
	class SavePurchase extends ServerConnection{

		Activity act;
		//int id_model;
		String purchase_token, order_id, product_id;
		long purchase_time;
		ProgressDialog dialog;
		int count = 10;
		SavePurchase sp;
    	
    	public SavePurchase( Activity act , String purchase_token , String order_id , long purchase_time , String product_id , final boolean show_dialog ){
    		super();
    		
    		this.act = act;
    		//this.id_model = id_model;
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
    		
    		init(act,"save_purchase",new Object[]{ User.get(act).id+"" , purchase_token , order_id , purchase_time+"" , product_id });
    	}
    	
    	public void setNotification(){
    		NotificationCompat.Builder mBuilder =
    		        new NotificationCompat.Builder(act)
    		        .setSmallIcon(R.drawable.notif_icon)
    		        .setContentTitle(getResources().getString(R.string.conectando_con_servidor))
    		        .setContentText(getResources().getString(R.string.para_completar_compra));
    		// Creates an explicit intent for an Activity in your app
    		Intent resultIntent;
    		
    		if ( id_model != -1 ){
        		resultIntent = new Intent(act, ModelPreviewActivity.class);
        		resultIntent.putExtra("modelId", id_model);
    		}
    		else{
        		resultIntent = new Intent(act, MainActivity.class);
    		}

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
    		
			sp = new SavePurchase( this.act , this.purchase_token , this.order_id , this.purchase_time , this.product_id , false  );
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
					
					Toast.makeText(getApplicationContext(), R.string.compra_monedas_hecha, Toast.LENGTH_LONG).show();
					Utils.sendBroadcast(getApplicationContext(), R.string.broadcast_model_purchase);
					cancelNotification();
					
					Utils.onCoinsBuy(getApplicationContext(), "BuyCoinActivity", "Comprado", product_id, user_coins);
					
					EasyTracker easyTracker = EasyTracker.getInstance(act);
					
					double revenue = 0.0;
					
					if ( product_id.equals(coins_20) ){
						revenue = 0.99;
					}
					else{
						if ( product_id.equals(coins_50) ){
							revenue = 1.99;
						}
						else{
							if ( product_id.equals(coins_130) ){
								revenue = 4.99;
							}
						}
					}
					
					easyTracker.send( MapBuilder.createTransaction(order_id, "In-app store", revenue, 0.0, 0.0, "USD").build() );
					
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
				Utils.onCoinsBuyError(getApplicationContext(), "BuyCoinActivity", product_id);
			}
			
		}
		
	}
	
}
