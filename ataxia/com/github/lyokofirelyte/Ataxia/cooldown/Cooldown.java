package com.github.lyokofirelyte.Ataxia.cooldown;

import java.util.HashMap;

import com.github.lyokofirelyte.Ataxia.Ataxia;

public class Cooldown {

	private Ataxia main;
	
	public Cooldown(Ataxia main){
		this.main = main;
	}
	
	public boolean handleCooldown(String userID, CooldownType type, CooldownDuration duration, long amount){
		if (isCooldownFinished(userID, type)){
			addCooldown(userID, type, duration, amount);
			return true;
		}
		return false;
	}
	
	public void addCooldown(String userID, CooldownType type, CooldownDuration duration, long amount){
		if (!main.cooldowns.containsKey(userID)){
			main.cooldowns.put(userID, new HashMap<Integer, Long>());
		}
		
		main.cooldowns.get(userID).put(type.getType(), duration.getDuration(amount));
	}
	
	public boolean isCooldownFinished(String userID, CooldownType type){
		return !main.cooldowns.containsKey(userID) || !main.cooldowns.get(userID).containsKey(type.getType()) || main.cooldowns.get(userID).get(type.getType()) <= System.currentTimeMillis();
	}
	
	public void endCooldown(String userID, CooldownType type){
		if (!main.cooldowns.containsKey(userID)){
			main.cooldowns.put(userID, new HashMap<Integer, Long>());
		}
		main.cooldowns.get(userID).remove(type.getType());
	}
	
	public void endAllCooldowns(String userID){
		main.cooldowns.remove(userID);
	}
	
	public long secondsLeft(String userID, CooldownType cooldown){
		return isCooldownFinished(userID, cooldown) ? 0 : (main.cooldowns.get(userID).get(cooldown.getType()) - System.currentTimeMillis()) / 1000;
	}
	
	public long minutesLeft(String userID, CooldownType cooldown){
		return isCooldownFinished(userID, cooldown) ? 0 : ((main.cooldowns.get(userID).get(cooldown.getType()) - System.currentTimeMillis()) / 1000) / 60;
	}
}