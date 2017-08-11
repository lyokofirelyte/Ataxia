package com.github.lyokofirelyte.Ataxia.cooldown;

public enum CooldownType {

	ATAXIA_IMAGE(0),
	ATAXIA_AIRHORN(1),
	ATAXIA_TRIPLE(2),
	ATAXIA_4C(3),
	ATAXIA_WHO(4),
	ATAXIA_SHOWS(5),
	ATAXIA_QUEUE(6),
	ATAXIA_INFO(7),
	ATAXIA_COLOR(8);
	
	CooldownType(int type){
		this.type = type;
	}
	
	private int type;
	
	public int getType(){
		return type;
	}
	
	public String getName(){
		return toString();
	}
}