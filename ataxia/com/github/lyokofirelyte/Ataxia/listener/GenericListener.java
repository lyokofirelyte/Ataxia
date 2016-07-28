package com.github.lyokofirelyte.Ataxia.listener;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.io.FileUtils;
import org.json.simple.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.tidy.Tidy;

import com.github.lyokofirelyte.Ataxia.Ataxia;
import com.github.lyokofirelyte.Ataxia.data.LocalData;
import com.github.lyokofirelyte.Ataxia.message.Channel;
import com.github.lyokofirelyte.Ataxia.message.MessageHandler;
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
		main.sendMessage("All systems ready!", Channel.SERVER);
		main.ready();
	}
	
	@SneakyThrows
	@EventSubscriber
	public void onStatusChange(StatusChangeEvent e){
		if (e.getNewStatus().getStatusMessage() != null && !e.getNewStatus().getStatusMessage().contains("NONE")){
			String message = ":eight_pointed_black_star: " + e.getUser().getName() + " is now playing " + e.getNewStatus().getStatusMessage();
			File theFile = new File("data/covers/" + e.getNewStatus().getStatusMessage().replace(" ", "_").replaceAll("[^a-zA-Z0-9.-]", "_") + ".jpg");
			if (!theFile.exists()){
				String search = "http://www.bing.com/images/search?q=" + e.getNewStatus().getStatusMessage().replace(" ", "%20") + "%20game%20logo" + "&go=Search&qs=n&form=QBILPG&pq=cookies&sc=8-7&sp=-1&sk=";
				InputStream input = new URL(search).openStream();
				Document document = new Tidy().parseDOM(input, null);
				NodeList imgs = document.getElementsByTagName("img");
				for (int i = 2; i < imgs.getLength(); i++) {
				    String picture = imgs.item(i).getAttributes().getNamedItem("src").getNodeValue();
				    if (!picture.contains("data:image")){
				    	FileUtils.copyURLToFile(new URL((picture.contains("http") ? "" : "http://") + picture), theFile);
				    	break;
				    }
				}
			}
			String game = "#" + e.getNewStatus().getStatusMessage().toLowerCase();
			main.sendMessage(message, Channel.SERVER);
			main.client.getChannelByID(Channel.SERVER.getId()).sendFile(theFile);
			/*if (LocalData.AUTOGAME.getData("users/" + e.getUser().getID(), main).asBool() && e.getUser().getConnectedVoiceChannels().size() > 0){
				for (IVoiceChannel vc : main.client.getVoiceChannels()){
					if (vc.getName().equals(game)){
						e.getUser().moveToVoiceChannel(vc);
						return;
					}
				}
				IVoiceChannel vc = main.client.getVoiceChannelByID(Voice_Channel.TIKI_LOUNGE.getId());
				IVoiceChannel newChannel = main.client.getGuilds().get(0).createVoiceChannel(game);
				newChannel.changeBitrate(vc.getBitrate());
				newChannel.changePosition(vc.getPosition() + 1);
				e.getUser().moveToVoiceChannel(newChannel);
			}*/
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
		main.doLater(() -> {
			main.client.getVoiceChannelByID("199665266764939265").leave();
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
	
	@SneakyThrows
	@EventSubscriber
	public void onVoiceLeave(UserVoiceChannelLeaveEvent e){
		main.sendMessage(LocalData.VOICE_LEAVE.getData("messages", main).asString().replace(
			"%channel%", e.getChannel().getName()
		).replace(
			"%user_name%", e.getUser().getName()
		), Channel.SERVER);
		if (e.getChannel().getUsersHere().size() <= 0 && e.getChannel().getName().startsWith("#")){
			e.getChannel().delete();
		}
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
		if (e.getOldChannel().getUsersHere().size() <= 0 && e.getOldChannel().getName().startsWith("#")){
			e.getOldChannel().delete();
		}
	}
	
	@EventSubscriber @SneakyThrows
	public void onMessage(MessageReceivedEvent e){
		String message = e.getMessage().toString();
		String userID = e.getMessage().getAuthor().getID();
		if (!main.data.containsKey("users/" + userID)){
			JSONObject obj = new JSONObject();
			main.data.put("users/" + userID, obj);
		}
		String[] tempArgs = e.getMessage().toString().split(" ");
		cont:
		for (int x = tempArgs.length; x >= 0; x--){
			String tempMessage = "";
			for (int i = 0; i < x; i++){
				tempMessage += tempMessage.equals("") ? tempArgs[i] : " " + tempArgs[i];
			}
			if (main.binds.get(e.getMessage().getAuthor().getID()).hasBind(tempMessage)){
				message = main.binds.get(e.getMessage().getAuthor().getID()).getBind(tempMessage);
				try {
					String args = e.getMessage().toString().substring(tempMessage.length());
					message += args;
				} catch (Exception ee){}
				break cont;
			}
		}
		if (message.startsWith("!ax:")){
			String[] args = message.split(" ");
			MessageListener ml = new MessageListener(main, e.getMessage().getAuthor(), message.split(" "), e.getMessage().getChannel().getID());
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
		if (e.getMessage().getChannel().getID().contains(Channel.MINECRAFT.getId())){
			main.mc.sendMessage("chat" + "%split%" + e.getMessage().getAuthor().getName() + "%split%" + e.getMessage().toString());
		}
		/*try {
			URL url = new URL(e.getMessage().toString());
			String safeName = url.toString().replace(" ", "_").replaceAll("[^a-zA-Z0-9.-]", "_");
			if (ImageIO.write(ImageIO.read(url), "png", new File("./data/" + safeName))){
				e.getMessage().delete();
				e.getMessage().getChannel().sendFile(new File("./data/" + safeName));
				main.sendMessage("*image by " + e.getMessage().getAuthor().getName() + "*", e.getMessage().getChannel().getID());
			} else {
				main.log("Error with image!");
			}
		} catch (Exception ee){
			ee.printStackTrace();
		}*/
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