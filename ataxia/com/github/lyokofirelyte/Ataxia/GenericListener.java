package com.github.lyokofirelyte.Ataxia;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.tidy.Tidy;

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
import sx.blah.discord.handle.impl.events.StatusChangeEvent;
import sx.blah.discord.handle.impl.events.UserJoinEvent;
import sx.blah.discord.handle.impl.events.UserVoiceChannelJoinEvent;
import sx.blah.discord.handle.impl.events.UserVoiceChannelLeaveEvent;
import sx.blah.discord.handle.impl.events.UserVoiceChannelMoveEvent;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.audio.AudioPlayer;

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
	
	@SneakyThrows
	@EventSubscriber
	public void onStatusChange(StatusChangeEvent e){
		if (e.getNewStatus().getStatusMessage() != null && !e.getNewStatus().getStatusMessage().contains("NONE")){
			String message = ":eight_pointed_black_star: " + e.getUser().getName() + " is now playing " + e.getNewStatus().getStatusMessage();
			String search = "http://www.bing.com/images/search?q=" + e.getNewStatus().getStatusMessage().replace(" ", "%20") + "%20game%20logo" + "&go=Search&qs=n&form=QBILPG&pq=cookies&sc=8-7&sp=-1&sk=";
			InputStream input = new URL(search).openStream();
			Document document = new Tidy().parseDOM(input, null);
			NodeList imgs = document.getElementsByTagName("img");
			for (int i = 2; i < imgs.getLength(); i++) {
			    String picture = imgs.item(i).getAttributes().getNamedItem("src").getNodeValue();
			    if (!picture.contains("data:image")){
			    	message += "\n" + (picture.contains("http") ? "" : "http://") + picture;
			    	break;
			    }
			}
			main.sendMessage(message, Channel.SERVER);
		} else {
			main.sendMessage(":eight_pointed_black_star: " + e.getUser().getName() + " has stopped playing " + e.getOldStatus().getStatusMessage(), Channel.SERVER);
		}
	}
	
	@SneakyThrows
	@EventSubscriber
	public void onJoinServer(UserJoinEvent e){
		main.sendMessage("@everyone Welcome <@" + e.getUser().getID() + "> to the server!", Channel.TIKI_LOUNGE);
		File file = new File("data/welcome.mp3");
		main.client.getVoiceChannelByID("199665266764939265").join();
		AudioPlayer.getAudioPlayerForGuild(main.client.getGuilds().get(0)).queue(file);
		new Timer().schedule(new TimerTask(){
			@Override
			public void run(){
				main.client.getVoiceChannelByID("199665266764939265").leave();
			}
		}, 3000L);
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
		), Channel.SERVER);
	}
	
	@SneakyThrows
	@EventSubscriber
	public void onVoiceJoin(UserVoiceChannelMoveEvent e){
		main.sendMessage(LocalData.VOICE_MOVE.getData("messages", main).asString().replace(
			"%new_channel%", e.getNewChannel().getName()
		).replace(
			"%old_channel%", e.getOldChannel().getName()
		).replace(
			"%user_name%", e.getUser().getName()
		), Channel.SERVER);
		if (e.getOldChannel().getName().contains("afk rape dungeon")){
			main.client.getGuilds().get(0).setMuteUser(e.getUser(), false);
		}
	}
	
	@EventSubscriber @SneakyThrows
	public void onMessage(MessageReceivedEvent e){
		//if (e.getMessage().getChannel().getID().equals(Channel.TIKI_LOUNGE.getId())){
			if (e.getMessage().toString().startsWith("!ax:")){
				String[] args = e.getMessage().toString().split(" ");
				MessageListener ml = new MessageListener(main, e.getMessage().getAuthor(), e.getMessage().toString().split(" "), e.getMessage().getChannel().getID());
				for (Method m : ml.getClass().getMethods()){
					if (m.getAnnotation(MessageHandler.class) != null){
						MessageHandler mh = (MessageHandler) m.getAnnotation(MessageHandler.class);
						for (String alias : mh.aliases()){
							if (alias.equalsIgnoreCase(args[0].split(":")[1])){
								if (mh.channel().equals(Channel.ANY) || mh.channel().getId().equals(e.getMessage().getChannel().getID())){
									boolean perms = false;
									for (IRole role : e.getMessage().getAuthor().getRolesForGuild(main.client.getGuildByID("199663127363715074"))){
										if (mh.role().hasPermission(role.getID())){
											perms = true;
											break;
										}
									}
									if (perms){
										m.invoke(ml);
									} else {
										main.sendMessage(ping(e.getMessage().getAuthor()) + " You don't have the correct role for that!", e.getMessage().getChannel().getID());
										return;
									}
								} else {
									main.client.getChannelByID(e.getMessage().getChannel().getID()).sendMessage(ping(e.getMessage().getAuthor()) + " This command must be executed in #" + main.client.getChannelByID(mh.channel().getId()).getName());
								}
								return;
							}
						}
					}
				}
				ml.onNotFound();
			} else if (e.getMessage().toString().contains(LocalData.BOT_CHAT_ID.getData("keys", main).asString())){
				handleChat(e.getMessage());
			}
		//}
		
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