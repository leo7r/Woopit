package com.woopitapp.activities;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import com.woopitapp.R;
import com.woopitapp.R.layout;
import com.woopitapp.R.string;
import com.woopitapp.services.ServerConnection;

public class TestActivity extends Activity {
	
	String base_url = "Woopit/models/";
	String caller;
	int modelId;
	double latitud;
	double longitud;
	int message;
	int status;
	Intent iResult;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.test);
		Intent i = this.getIntent();
		Log.e("EXTRA", "extra: " +i.getStringExtra("model"));
		String 
		
		caller = i.getStringExtra("caller");
		modelId = Integer.parseInt(i.getStringExtra("model"));
		if(caller.equalsIgnoreCase("HomeFragment")){
			latitud = Double.parseDouble(i.getStringExtra("latitud"));
			longitud = Double.parseDouble(i.getStringExtra("longitud"));
			message = Integer.parseInt(i.getStringExtra("message"));
			status = Integer.parseInt(i.getStringExtra("status"));
		}
		File dir = Environment.getExternalStorageDirectory();
		File modelFile = new File(dir, base_url+modelId+".jet");
		
		if ( modelFile.exists() ){
			// Cargo normalmente
			Log.i("Inicio", "Ya existe");
			iResult = new Intent();
			if(caller.equalsIgnoreCase("HomeFragment")){
				iResult.putExtra("model", modelId+"");
				iResult.putExtra("latitud", latitud+"");
				iResult.putExtra("longitud", longitud+"");
				iResult.putExtra("message", message+"");
				iResult.putExtra("status", status+"");
				setResult(RESULT_OK,iResult);

			}else{
				setResult(RESULT_OK);
			}
			finish();
		}
		else{
			// Lo descargo del server y luego cargo normalmente
			Log.i("Inicio", "NO EXISTE, descargando");
			new ModelDownloader( this, modelId ).execute();
		}
		
	}
	
	class ModelDownloader extends AsyncTask<Void,Void,Boolean>{

		int modelId;
		String server_path = "http://"+ServerConnection.HOST+":7778/models/";
		
		int downloadedSize = 0;
	    int totalSize = 0;
	    ProgressDialog barProgressDialog;
	    boolean canceled = false;
	    
		
		public ModelDownloader( Activity act , final int modelId ){
			this.modelId = modelId;
			
		    barProgressDialog = new ProgressDialog(act);
		    barProgressDialog.setTitle(R.string.descargando_modelo);
		    barProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		    barProgressDialog.setProgress(0);
		    barProgressDialog.setMax(100);
		    barProgressDialog.setCancelable(true);
		    barProgressDialog.setCanceledOnTouchOutside(false);
		    barProgressDialog.setOnCancelListener(new OnCancelListener(){

				@Override
				public void onCancel(DialogInterface dialog) {
					canceled = true;
		    		
				}
			});
		    barProgressDialog.show();
		}
		
		@Override
		protected Boolean doInBackground(Void... arg0) {
						
			try {
	            URL url = new URL(server_path+modelId+".jet");
	            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
	 
	            urlConnection.setRequestMethod("GET");
	            urlConnection.setDoOutput(true);
	            urlConnection.connect();
	            
	    		File dir = Environment.getExternalStorageDirectory();
	    		
	    		File woopitDir = new File(dir,"Woopit");
	    		woopitDir.mkdir();
	    		
	    		File modelsDir = new File(woopitDir,"models");
	    		modelsDir.mkdir();
	    		
	    		File modelFile = new File(modelsDir, modelId+".jet");
	    		boolean created = modelFile.createNewFile();
	    		
	    		if ( !created ){
	    			Log.e("error","Error creando archivo "+modelId+".jet");
	    			return false;
	    		}
	    		
	            FileOutputStream fileOutput = new FileOutputStream(modelFile);
	 
	            //Stream used for reading the data from the internet
	            InputStream inputStream = urlConnection.getInputStream();
	 
	            //this is the total size of the file which we are downloading
	            totalSize = urlConnection.getContentLength();
	            
	            //create a buffer...
	            byte[] buffer = new byte[1024];
	            int bufferLength = 0;
	 
	            while ( (bufferLength = inputStream.read(buffer)) > 0 ) {
	            	
	            	if ( canceled ){
	            		inputStream.close();
	            		fileOutput.close();
	            		modelFile.delete();
	            		return false;
	            	}
	            	
	                fileOutput.write(buffer, 0, bufferLength);
	                downloadedSize += bufferLength;
	                
                    float per = ((float)downloadedSize/totalSize) * 100;
                    barProgressDialog.setProgress((int) per);
	            }
	            
	            //close the output stream when complete
	            fileOutput.close();
	            barProgressDialog.dismiss();
	         
	        } catch (final MalformedURLException e) {
	            e.printStackTrace();
	        } catch (final IOException e) {   
	            e.printStackTrace();
	        }
	        catch (final Exception e) {
	        	e.printStackTrace();
	        }       
			
			
			return true;
		}

		@Override
		protected void onPostExecute( Boolean success ){
			
			if ( success ){
			
				Log.i("Download", "LISTO :D");
				iResult = new Intent();
				if(caller.equalsIgnoreCase("HomeFragment")){
					iResult.putExtra("model", modelId+"");
					iResult.putExtra("latitud", latitud+"");
					iResult.putExtra("longitud", longitud+"");
					iResult.putExtra("message", message+"");
					iResult.putExtra("status", status+"");
					setResult(RESULT_OK,iResult);

				}else{
					setResult(RESULT_OK);
				}
			}
			else{
				Log.i("Download", "Cancelado o error");
			}
			
		}
	}


}
