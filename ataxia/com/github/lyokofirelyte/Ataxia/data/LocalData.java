package com.github.lyokofirelyte.Ataxia.data;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;

import com.github.lyokofirelyte.Ataxia.Ataxia;

public enum LocalData {

	BOT_TOKEN("BOT_TOKEN"),
	BOT_ID("BOT_ID"),
	OWNER_CHAT_ID("OWNER_CHAT_ID"),
	BOT_CHAT_ID("BOT_CHAT_ID"),
	MESSAGES("MESSAGES"),
	VOICE_JOIN("VOICE_JOIN"),
	VOICE_LEAVE("VOICE_LEAVE"),
	VOICE_MOVE("VOICE_MOVE"),
	TTS_KEY("TTS_KEY"),
	AUTOGAME("AUTOGAME"),
	BINDS("BINDS"),
	LISTEN("LISTEN"),
	API("API"),
	SSH_PASSWORD("SSH_PASSWORD"),
	IBM_USERNAME("IBM_USERNAME"),
	IBM_PASSWORD("IBM_PASSWORD");
	
	LocalData(String name){
		this.name = name;
	}
	
	public String getName(){
		return name;
	}
	
	public void setData(String key, Object value, Ataxia main){
		main.data.get(key).put(name, value);
	}
	
	public LocalData getData(String key, Ataxia main){
		if (main.data.containsKey(key) && main.data.get(key).containsKey(name)){
			a = main.data.get(key).get(getName());
		} else {
			a = "none";
		}
		
		return this;
	}
	
	public boolean asBool(){
		return asString().equals("true");
	}
	
	public String asString(){
		return (String) a;
	}
	
	public int asInt(){
		try {
			return Integer.parseInt("" + a);
		} catch (Exception e){
			return 0;
		}
	}
	
	public float asFloat(){
		try {
			return Float.parseFloat("" + a);
		} catch (Exception e){
			return 0;
		}
	}
	
	public List<String> asListString(){
		JSONArray array = (JSONArray) a;
		List<String> toReturn = new ArrayList<String>();
		for (Object o : array){
			toReturn.add(o.toString());
		}
		return toReturn;
	}
	
	public Object asObject(){
		return a;
	}
	
	private String name;
	private Object a;
}