package com.woopitapp;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

import org.json.JSONArray;

import android.content.Context;
import android.os.AsyncTask;

public abstract class ServerConnection extends AsyncTask<Void,Void,Void> {

	final public static String HOST = "54.186.75.251";
	final int PORT = 7777; // PRUEBA! CAMBIAR A HTTPS en produccion
	
	Socket socket;
	DataOutputStream outStream;
	
	String result;
	String funName;
	String[] args;
	Context con;
	
	public void init( Context con , String funName , Object[] args ){
		this.funName = funName;
		this.con = con;
		this.args = new String[args.length];
		
		for ( int i = 0 ; i < args.length ; ++i ){
			this.args[i] = args[i].toString();
		}
	}
	
	@Override
	protected Void doInBackground(Void... arg0) {
		
		try {
			socket = new Socket( HOST , PORT );
			
			outStream = new DataOutputStream(socket.getOutputStream());
			BufferedReader buffer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			
			JSONArray message = new JSONArray();
			message.put(0, funName);
			
			JSONArray arg_list = new JSONArray();
			for ( String st : args ){
				arg_list.put(st);
			}
			
			message.put(1,con.getResources().getString(R.string.security_token));
			message.put(2,arg_list);

			outStream.writeBytes(message.toString());

			result = buffer.readLine();
			onBackground(result);
			
		} catch (UnknownHostException e) {
			
			e.printStackTrace();
		} catch (IOException e) {
			
			e.printStackTrace();
		} catch (Exception e) {
			
			e.printStackTrace();
		}
		
		return null;
	}
	
	protected void onPostExecute( Void v ){
		
		onComplete(result);
	}
	
	abstract void onComplete( String result );
	
	void onBackground( String result ){
		
	}

}

