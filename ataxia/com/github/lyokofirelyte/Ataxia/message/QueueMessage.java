package com.github.lyokofirelyte.Ataxia.message;

import com.github.lyokofirelyte.Ataxia.Ataxia;

import sx.blah.discord.handle.obj.IMessage;

public class QueueMessage {

	private Ataxia main;
	public IMessage msg;
	private String playing;
	
	public QueueMessage(Ataxia i){
		this.main = i;
		msg = main.queueMessage;
	}
	
	public String edit(int lineNumber, String song, String user, String totalDuration, String oldMessage){
		String newMessage = new String(oldMessage);
		for (String s : newMessage.split("\n")){
			if (s.startsWith("**0" + lineNumber)){
				newMessage = newMessage.replace(s, "**0" + lineNumber + "** `" + song + " " + user + " " + totalDuration + "`");
				break;
			}
		}
		return newMessage;
	}
	
	public void editNowPlaying(String song){
		this.playing = song;
		String newMessage = new String(msg.getContent());
		for (String s : newMessage.split("\n")){
			if (s.contains("Now Playing")){
				newMessage = newMessage.replace(s, "Now Playing: " + song + "```");
				break;
			}
		}
		msg.edit(newMessage);
	}
	
	public void updateElapsed(String minutes){
		String newMessage = new String(msg.getContent());
		for (String s : newMessage.split("\n")){
			if (s.contains("Now Playing")){
				newMessage = newMessage.replace(s, "```Now Playing: " + playing + " [" + minutes + "]```");
				break;
			}
		}
		msg.edit(newMessage);
	}
	
	public void updateFeedback(String message){
		msg.edit(msg.getContent().replace(msg.getContent().split("\n")[msg.getContent().split("\n").length - 1], message + "```"));
	}
}