package com.woopitapp.entities;

import java.util.Date;

public class Message {
	public int id,sender,model,receiver,status;
	public String title, text, name, modelName;
	public Date date;
	public double latitud,longitud;
	public String imagen;
	public Message(int id , int sender, int receiver, int model, String title , String text, Date d,double latitud,double longitud, int status,String name,String modelName,String imagen){
		this.id = id;
		this.sender = sender;
		this.receiver = receiver;
		this.model = model;
		this.title = title;
		this.text = text;
		this.date = d;
		this.status = status;
		this.name = name;
		this.latitud = latitud;
		this.longitud = longitud;
		this.modelName = modelName;
		this.imagen = imagen;
	}
	

}
