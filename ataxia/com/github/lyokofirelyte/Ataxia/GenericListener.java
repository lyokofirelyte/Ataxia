package com.github.lyokofirelyte.Ataxia;

import java.lang.reflect.Method;
import java.util.Locale;

import lombok.SneakyThrows;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.impl.events.UserVoiceChannelJoinEvent;
import sx.blah.discord.handle.obj.IMessage;

import com.github.lyokofirelyte.Ataxia.message.Channel;
import com.github.lyokofirelyte.Ataxia.message.MessageHandler;
import com.github.lyokofirelyte.Ataxia.message.MessageListener;
import com.google.code.chatterbotapi.ChatterBot;
import com.google.code.chatterbotapi.ChatterBotFactory;
import com.google.code.chatterbotapi.ChatterBotSession;
import com.google.code.chatterbotapi.ChatterBotType;

public class GenericListener implements AtaxiaListener {

	private Ataxia main;
	
	public GenericListener(Ataxia main){
		this.main = main;
	}
	
	@EventSubscriber
	public void onReady(ReadyEvent e){
		main.sendMessage("All systems ready!", Channel.TIKI_LOUNGE);
	}
	
	@EventSubscriber
	public void onVoiceJoin(UserVoiceChannelJoinEvent e){
		
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
			} else if (e.getMessage().toString().contains("201916819357958144")){
				handleChat(e.getMessage());
			}
		}
		
		if (e.getMessage().getChannel().getID().equals(Channel.MINECRAFT.getId())){
			
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
		main.sendMessage("<@" + message.getAuthor().getID() + "> " + sesh.think(message.toString()), Channel.TIKI_LOUNGE);
	}
}