package com.github.lyokofirelyte.Ataxia.message;

public enum Channel {

	TIKI_LOUNGE("199737300471513088");
	
	Channel(String id){
		this.id = id;
	}
	
	private String id;
	
	public String getId(){
		return id;
	}
}