package com.github.lyokofirelyte.Ataxia.message;

public enum Channel {

	ANY("0"),
	FAQ("199663127363715074"),
	TIKI_LOUNGE("199737300471513088"),
	MINECRAFT("202230311273103362"),
	SERVER("199755588421419009"),
	MOVIES("199666762105618433");
	
	Channel(String id){
		this.id = id;
	}
	
	private String id;
	
	public String getId(){
		return id;
	}
}