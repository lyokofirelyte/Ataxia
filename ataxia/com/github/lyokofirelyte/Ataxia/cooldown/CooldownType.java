package com.github.lyokofirelyte.Ataxia.cooldown;

public enum CooldownType {

	ATAXIA_IMAGE(0),
	ATAXIA_AIRHORN(1),
	ATAXIA_TRIPLE(2);
	
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