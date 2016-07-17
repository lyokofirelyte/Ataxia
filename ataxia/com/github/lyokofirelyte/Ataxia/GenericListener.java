package com.github.lyokofirelyte.Ataxia;

import java.lang.reflect.Method;
import java.util.Locale;

import com.github.lyokofirelyte.Ataxia.message.Channel;
import com.github.lyokofirelyte.Ataxia.message.MessageHandler;
import com.github.lyokofirelyte.Ataxia.message.MessageListener;
import com.google.code.chatterbotapi.ChatterBot;
import com.google.code.chatterbotapi.ChatterBotFactory;
import com.google.code.chatterbotapi.ChatterBotSession;
import com.google.code.chatterbotapi.ChatterBotType;

import lombok.SneakyThrows;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.impl.events.UserVoiceChannelJoinEvent;
import sx.blah.discord.handle.impl.events.UserVoiceChannelLeaveEvent;
import sx.blah.discord.handle.impl.events.UserVoiceChannelMoveEvent;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

public class GenericListener implements AtaxiaListener {

	private Ataxia main;
	
	public GenericListener(Ataxia main){
		this.main = main;
	}
	
	public String ping(IUser client){
		return "<@" + client.getID() + "> ";
	}
	
	@EventSubscriber
	public void onReady(ReadyEvent e){
		main.sendMessage("All systems ready!", Channel.TIKI_LOUNGE);
		main.ready();
	}
	
	@EventSubscriber
	public void onVoiceJoin(UserVoiceChannelJoinEvent e){
		main.sendMessage(LocalData.VOICE_JOIN.getData("messages", main).asString().replace(
			"%channel%", e.getChannel().getName()
		).replace(
			"%user_name%", e.getUser().getName()
		), Channel.SERVER);
	}
	
	@EventSubscriber
	public void onVoiceJoin(UserVoiceChannelLeaveEvent e){
		main.sendMessage(LocalData.VOICE_LEAVE.getData("messages", main).asString().replace(
			"%channel%", e.getChannel().getName()
		).replace(
			"%user_name%", e.getUser().getName()
		), Channel.SERVER);;
	}
	
	@EventSubscriber
	public void onVoiceJoin(UserVoiceChannelMoveEvent e){
		main.sendMessage(LocalData.VOICE_MOVE.getData("messages", main).asString().replace(
			"%new_channel%", e.getNewChannel().getName()
		).replace(
			"%old_channel%", e.getOldChannel().getName()
		).replace(
			"%user_name%", e.getUser().getName()
		), Channel.SERVER);
	}
	
	@EventSubscriber @SneakyThrows
	public void onMessage(MessageReceivedEvent e){
		if (e.getMessage().getChannel().getID().equals(Channel.TIKI_LOUNGE.getId())){
			if (e.getMessage().toString().startsWith("!ax:")){
				String[] args = e.getMessage().toString().split(" ");
				MessageListener ml = new MessageListener(main, e.getMessage().getAuthor(), e.getMessage().toString().split(" "));
				for (Method m : ml.getClass().getMethods()){
					if (m.getAnnotation(MessageHandler.class) != null){
						MessageHandler mh = (MessageHandler) m.getAnnotation(MessageHandler.class);
						for (String alias : mh.aliases()){
							if (alias.equalsIgnoreCase(args[0].split(":")[1])){
								m.invoke(ml);
								return;
							}
						}
					}
				}
				ml.onNotFound();
			} else if (e.getMessage().toString().contains(LocalData.BOT_CHAT_ID.getData("keys", main).asString())){
				handleChat(e.getMessage());
			}
		}
		
		if (e.getMessage().getChannel().getID().contains(Channel.MINECRAFT.getId())){
			main.mc.sendMessage("chat" + "%split%" + e.getMessage().getAuthor().getName() + "%split%" + e.getMessage().toString());
		}
	}

	@SneakyThrows
	private void handleChat(IMessage message){
		ChatterBotFactory factory = new ChatterBotFactory();
		ChatterBot bot = factory.create(ChatterBotType.CLEVERBOT);
		if (!main.sesh.containsKey(message.getAuthor().getID())){
			main.sesh.put(message.getAuthor().getID(), bot.createSession(Locale.ENGLISH));
		}
		ChatterBotSession sesh = main.sesh.get(message.getAuthor().getID());
		String response = sesh.think(message.toString());
		main.sendMessage("<@" + message.getAuthor().getID() + "> " + response, Channel.TIKI_LOUNGE);
	}
}