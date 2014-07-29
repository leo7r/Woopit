package com.woopitapp.dialogs;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.woopitapp.R;
import com.woopitapp.activities.BuyCoinActivity;
import com.woopitapp.entities.User;
import com.woopitapp.services.ServerConnection;
import com.woopitapp.services.Utils;

public class BuyModelDialog extends Activity {

	int modelId;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.buy_model_dialog);
		
		Bundle extras = getIntent().getExtras();
		
		if ( extras != null && extras.containsKey("modelId") ){
			modelId = extras.getInt("modelId");
			new GetBuyModelInfo().execute();
		}
		else{
			finish();
		}
		
	}
	
	class GetBuyModelInfo extends ServerConnection{

		public GetBuyModelInfo(){
			super();
			
			init(getApplicationContext(),"get_buy_model_info",new Object[]{ User.get(getApplicationContext()).id , modelId });
		}
		
		@Override
		public void onComplete(String result) {
			
			((RelativeLayout)findViewById(R.id.loading)).setVisibility(View.GONE);
			((LinearLayout)findViewById(R.id.content)).setVisibility(View.VISIBLE);
			
			if ( result != null ){
				
				try {
					JSONObject info = new JSONObject(result);
					
					int model_price = info.getInt("p");
					int user_coins = info.getInt("c");
					
					ImageView image = (ImageView) findViewById(R.id.user_image);
					TextView mPrice = (TextView) findViewById(R.id.model_price);
					TextView uCoins = (TextView) findViewById(R.id.user_coins);
					Button buy_button = (Button) findViewById(R.id.buy_model_button);
					
					mPrice.setText(getResources().getString(R.string.comprar_precio_modelo,model_price));
					uCoins.setText(getResources().getString(R.string.comprar_monedas_usuario,user_coins));
					Utils.setUserImage(getApplicationContext(), image, User.get(getApplicationContext()).id);
					
					if ( model_price <= user_coins ){
						buy_button.setText(R.string.comprar_ahora);
						buy_button.setOnClickListener(new OnClickListener(){
							
							@Override
							public void onClick(View arg0) {
								new BuyModel().execute();
							}
						});
					}
					else{
						buy_button.setText(R.string.comprar_monedas);
						buy_button.setOnClickListener(new OnClickListener(){

							@Override
							public void onClick(View arg0) {
								Intent i = new Intent(getApplicationContext(),BuyCoinActivity.class);
								i.putExtra("modelId", modelId);
								startActivity(i);
								finish();
							}
						});
					}
					
				} catch (JSONException e) {
					e.printStackTrace();
				}
				
			}
			else{
				Toast.makeText(getApplicationContext(), R.string.error_de_conexion, Toast.LENGTH_SHORT).show();
				finish();
			}
			
		}
		
	}
	
	class BuyModel extends ServerConnection{
		
		public BuyModel( ){
			super();
			
			((RelativeLayout)findViewById(R.id.loading)).setVisibility(View.VISIBLE);
			((LinearLayout)findViewById(R.id.content)).setVisibility(View.GONE);

			Utils.onModelBuy(getApplicationContext(), "BuyModelDialog", "Comprar", modelId);
			
			init(getApplicationContext(),"buy_model",new Object[]{ User.get(getApplicationContext()).id , modelId });
		}
		
		@Override
		public void onComplete(String result) {
			
			((RelativeLayout)findViewById(R.id.loading)).setVisibility(View.GONE);
			((LinearLayout)findViewById(R.id.content)).setVisibility(View.VISIBLE);
			
			if ( result != null ){
				
				if ( result.equals("ok") ){
					Toast.makeText(getApplicationContext(), R.string.compra_hecha, Toast.LENGTH_SHORT).show();
					
					Utils.sendBroadcast(getApplicationContext(), R.string.broadcast_model_purchase);
					setResult(RESULT_OK);
					finish();
				}
				else{
					Toast.makeText(getApplicationContext(), R.string.error_compra_reintentando, Toast.LENGTH_SHORT).show();
				}
				
			}
			else{
				Toast.makeText(getApplicationContext(), R.string.error_de_conexion, Toast.LENGTH_SHORT).show();
			}
			
		}
		
	}
	
}
