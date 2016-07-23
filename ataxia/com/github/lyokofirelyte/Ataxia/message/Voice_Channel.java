package com.github.lyokofirelyte.Ataxia.message;

public enum Voice_Channel {

	TIKI_LOUNGE("199665266764939265"),
	MOVIES("199666781432840192"),
	GAMING("199758337934491648"),
	BOARD_ROOM("199665716599717888"),
	AFK("199664973738278912");
	
	Voice_Channel(String id){
		this.id = id;
	}
	
	private String id;
	
	public String getId(){
		return id;
	}
}