package com.github.lyokofirelyte.Ataxia;

import java.util.List;
import java.util.Random;
import java.util.TimerTask;

import com.github.lyokofirelyte.Ataxia.data.LocalData;
import com.github.lyokofirelyte.Ataxia.message.Channel;

public class AutoAnnouncer extends TimerTask {
	
	private Ataxia main;
	
	public AutoAnnouncer(Ataxia i){
		main = i;
	}

	@Override
	public void run(){
		List<String> messages = LocalData.MESSAGES.getData("messages", main).asListString();
		String nextMessage = messages.get(new Random().nextInt(messages.size()));
		main.sendMessage(nextMessage, Channel.TIKI_LOUNGE);
	}
}
