package com.github.lyokofirelyte.Ataxia.cooldown;

public enum CooldownDuration {

	MILLISECONDS(1),
	SECONDS(1000),
	MINUTES(1000 * 60),
	HOURS(1000 * 60 * 60),
	DAYS(1000 * 60 * 60 * 24);
	
	CooldownDuration(long duration){
		this.duration = duration;
	}
	
	private long duration;
	
	public long getDuration(long val){
		return System.currentTimeMillis() + (duration * val);
	}
}