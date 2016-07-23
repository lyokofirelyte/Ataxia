package com.github.lyokofirelyte.Ataxia.cooldown;

import java.util.HashMap;
import java.util.Map;

public class Cooldown {

	private Map<String, Map<Integer, Long>> cooldowns = new HashMap<>();
	
	public boolean handleCooldown(String userID, CooldownType type, CooldownDuration duration, long amount){
		if (isCooldownFinished(userID, type)){
			addCooldown(userID, type, duration, amount);
			return true;
		}
		return false;
	}
	
	public void addCooldown(String userID, CooldownType type, CooldownDuration duration, long amount){
		if (!cooldowns.containsKey(userID)){
			cooldowns.put(userID, new HashMap<Integer, Long>());
		}
		
		cooldowns.get(userID).put(type.getType(), duration.getDuration(amount));
	}
	
	public boolean isCooldownFinished(String userID, CooldownType type){
		return !cooldowns.containsKey(userID) || !cooldowns.get(userID).containsKey(type.getType()) || cooldowns.get(userID).get(type.getType()) <= System.currentTimeMillis();
	}
	
	public void endCooldown(String userID, CooldownType type){
		if (!cooldowns.containsKey(userID)){
			cooldowns.put(userID, new HashMap<Integer, Long>());
		}
		cooldowns.get(userID).remove(type.getType());
	}
	
	public void endAllCooldowns(String userID){
		cooldowns.remove(userID);
	}
	
	public long secondsLeft(String userID, CooldownType cooldown){
		return isCooldownFinished(userID, cooldown) ? 0 : (cooldowns.get(userID).get(cooldown.getType()) - System.currentTimeMillis()) / 1000;
	}
	
	public long minutesLeft(String userID, CooldownType cooldown){
		return isCooldownFinished(userID, cooldown) ? 0 : ((cooldowns.get(userID).get(cooldown.getType()) - System.currentTimeMillis()) / 1000) / 60;
	}
}