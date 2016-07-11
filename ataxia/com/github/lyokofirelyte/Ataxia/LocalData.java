package com.github.lyokofirelyte.Ataxia;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;

public enum LocalData {

	BOT_TOKEN("BOT_TOKEN"),
	BOT_ID("BOT_ID");
	
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