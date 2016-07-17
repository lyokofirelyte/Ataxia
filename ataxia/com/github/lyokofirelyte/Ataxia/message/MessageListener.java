package com.github.lyokofirelyte.Ataxia.message;

import java.io.File;

import com.github.lyokofirelyte.Ataxia.Ataxia;
import com.github.lyokofirelyte.Ataxia.LocalData;

import lombok.SneakyThrows;
import sx.blah.discord.handle.obj.IUser;

public class MessageListener {
	
	private Ataxia main;
	private IUser client;
	private String[] args;
	
	private String help = 
		"```" +
		"- - - Ataxia Help Menu - - - \n" +
		"!ax:who <-> Lists everyone w/ avatar\n" +
		"!ax:rel <-> David's reload command" +
		"```"
	;
	
	public MessageListener(Ataxia i, IUser c, String[] args){
		main = i;
		client = c;
		this.args = args;
	}
	
	public String ping(){
		return "<@" + client.getID() + "> ";
	}
	
	@SneakyThrows
	@MessageHandler(aliases = { "play" })
	public void a(){
		
	}
	
	public void onNotFound(){
		main.sendMessage(ping() + "Command not found! Try !ax:help", Channel.TIKI_LOUNGE);
	}

	@MessageHandler(aliases = { "help" }, usage = "!ax:help", desc = "Ataxia help command")
	public void onHelp(){
		main.sendMessage(ping() + "\n" + help, Channel.TIKI_LOUNGE);
	}
	
	@MessageHandler(aliases = { "who" }, usage = "!ax:who", desc = "Whois command")
	public void onWho(){
		String toSend = "";
		for (IUser u : client.getClient().getChannelByID(Channel.TIKI_LOUNGE.getId()).getUsersHere()){
			String setup = u.getName() + " (" + u.getID() + ")" + "\n" + u.getAvatarURL();
			toSend += toSend.equals("") ? setup : "\n- - -\n" + setup;
		}
		main.sendMessage(toSend, Channel.TIKI_LOUNGE);
	}
	
	@MessageHandler(aliases = { "rel" }, usage = "!ax:rel", desc = "Reload")
	public void onReload(){
		if (client.getID().contains(LocalData.OWNER_CHAT_ID.getData("keys", main).asString())){
			main.sendMessage("I'll be right back!", Channel.TIKI_LOUNGE);
			main.save();
			System.exit(0);
		} else {
			main.sendMessage(ping() + "You don't have permissions for that, sorry love!", Channel.TIKI_LOUNGE);
		}
	}
}