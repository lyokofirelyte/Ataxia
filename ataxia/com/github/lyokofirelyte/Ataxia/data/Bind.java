package com.github.lyokofirelyte.Ataxia.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

public class Bind {

	@Getter @Setter
	private String userID;
	
	@Getter
	private Map<String, String> binds = new HashMap<>();
	
	public Bind(String userID){
		this.userID = userID;
	}
	
	public void addBind(String activator, String result){
		binds.put(activator, result);
	}
	
	public void removeBind(String activator){
		String toRemove = "";
		if (hasBind(activator)){
			all:
			for (String b : binds.values()){
				if (b.equals(activator)){
					for (String bb : binds.keySet()){
						if (binds.get(bb).equals(b)){
							toRemove = bb;
							break all;
						}
					}
				}
			}
			binds.remove(toRemove);
		}
	}
	
	public void clearBinds(){
		binds = new HashMap<>();
	}
	
	public boolean hasBind(String bind){
		for (String b : binds.values()){
			if (b.equals(bind)){
				return true;
			}
		}
		return false;
	}
	
	public String getTranslation(String bind){
		return binds.get(bind);
	}
	
	public String getBind(String bind){
		for (String b : binds.values()){
			if (b.equals(bind)){
				for (String bb : binds.keySet()){
					if (binds.get(bb).equals(b)){
						return bb;
					}
				}
			}
		}
		return "none";
	}
	
	public int size(){
		return binds.size();
	}
	
	public List<String> list(){
		List<String> list = new ArrayList<>();
		for (String activator : binds.keySet()){
			list.add("`" + activator + "` => `" + getTranslation(activator) + "`");
		}
		if (list.size() <= 0){
			list.add("No binds found!");
		}
		return list;
	}
}