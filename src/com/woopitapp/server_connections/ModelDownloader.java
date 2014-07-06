package com.woopitapp.server_connections;

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
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.woopitapp.R;
import com.woopitapp.services.ServerConnection;

abstract public class ModelDownloader extends AsyncTask<Void,Void,Boolean>{

	int modelId;
	String server_path = "http://"+ServerConnection.HOST+":7778/models/";
	String base_url = "Woopit/models/";
	
	int downloadedSize = 0;
    int totalSize = 0;
    ProgressDialog barProgressDialog;
    boolean canceled = false;
    final long min_size = 2048L;
    
	public ModelDownloader( Activity act , final int modelId ){
		this.modelId = modelId;

		File w_dir = Environment.getExternalStorageDirectory();
		File w_modelFile = new File(w_dir, base_url+modelId+".jet");
		
		//Log.i("Model", w_modelFile.length()+"");
		
		if ( w_modelFile.exists() && w_modelFile.length() > min_size ){

			canceled = true;
		}
		else{
			
			if ( w_modelFile.length() <= min_size ){
				w_modelFile.delete();
			}
			
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
	    
	}
	
	@Override
	protected Boolean doInBackground(Void... arg0) {
		
		if ( canceled ){
			return true;
		}
		
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
	
	abstract protected void onPostExecute( Boolean success );
}