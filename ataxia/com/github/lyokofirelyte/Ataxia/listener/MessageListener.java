package com.github.lyokofirelyte.Ataxia.listener;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import org.apache.commons.io.FileUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.github.lyokofirelyte.Ataxia.Ataxia;
import com.github.lyokofirelyte.Ataxia.cooldown.CooldownDuration;
import com.github.lyokofirelyte.Ataxia.cooldown.CooldownType;
import com.github.lyokofirelyte.Ataxia.data.LocalData;
import com.github.lyokofirelyte.Ataxia.data.Role;
import com.github.lyokofirelyte.Ataxia.message.Channel;
import com.github.lyokofirelyte.Ataxia.message.MessageHandler;
import com.github.lyokofirelyte.Ataxia.message.Voice_Channel;

import lombok.SneakyThrows;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.util.audio.AudioPlayer;

public class MessageListener {
	
	private Ataxia main;
	private IUser client;
	private String[] args;
	private String channelID;
	
	private Map<String, String[]> effects = new HashMap<>();
	
	public MessageListener(Ataxia i, IUser c, String[] args, String channelID){
		main = i;
		client = c;
		this.args = args;
		effects.put("airhorn", new String[]{ "mlg", "3000" });
		effects.put("triple", new String[]{ "babyatriple", "8000" });
		effects.put("toilet", new String[]{ "toilet_flush", "15000" });
		effects.put("suspense", new String[]{ "suspense", "3000" });
		effects.put("bombito", new String[]{ "bombito", "7000" });
		this.channelID = channelID;
	}
	
	public String ping(){
		return "<@" + client.getID() + "> ";
	}
	
	@SneakyThrows
	@MessageHandler(aliases = { "airhorn", "triple", "toilet", "suspense", "bombito" }, usage = "!ax:airhorn", desc = "MLG")
	public void onAirhorn(){
		if (main.cd.handleCooldown(client.getID(), CooldownType.ATAXIA_AIRHORN, CooldownDuration.MINUTES, 5)){
			File file = new File("data/" + effects.get(args[0].split(":")[1])[0] + ".mp3");
			try {
				main.client.getVoiceChannelByID("199665266764939265").join();
				AudioPlayer.getAudioPlayerForGuild(main.client.getGuilds().get(0)).queue(file);
			} catch (Exception e) {
				e.printStackTrace();
				main.sendMessage("Error playing airhorn! :3", channelID);
			}
			main.doLater(() -> {
				main.client.getVoiceChannelByID("199665266764939265").leave();
			}, Long.parseLong(effects.get(args[0].split(":")[1])[1]));
		} else {
			main.sendMessage(ping() + " You can use this command again in " + main.cd.minutesLeft(client.getID(), CooldownType.ATAXIA_AIRHORN) + " minutes.", channelID);
		}
	}
	
	/*@MessageHandler(aliases = { "4chan", "4c" }, usage = "!ax:4c <board>", desc = "4chan latest lookup")
	public void on4C(){
		try {
			if (main.cd.handleCooldown(client.getID(), CooldownType.ATAXIA_4C, CooldownDuration.MINUTES, 5)){
				if (args.length == 2){
					JSONArray array = getHTMLAsArray("http://a.4cdn.org/" + args[1] + "/catalog.json");
					JSONObject obj = (JSONObject) array.get(0);
					JSONArray threads = (JSONArray) obj.get("threads");
					String complete = "```xl\nLatest 4Chan Results (Board: /" + args[1] + "/)```\n";
					int max = 0;
					for (Object o : threads){
						JSONObject thread = (JSONObject) o;
						if (thread.containsKey("last_replies")){
							String subName = "`" + (String) thread.get("sub") + "`\n";
							if (thread.containsKey("tim")){
								String picture = "http://i.4cdn.org/" + args[1] + "/" + (long) thread.get("tim") + (String) thread.get("ext");
								subName += picture;
							}
							complete += subName;
							JSONArray replies = (JSONArray) thread.get("last_replies");
							for (Object replyObject : replies){
								JSONObject reply = (JSONObject) replyObject;
								long number = (long) reply.get("no");
								String date = (String) reply.get("now");
								String comment = reply.containsKey("com") ? (String) reply.get("com") : "N/A";
								String picture = reply.containsKey("tim") ? "http://i.4cdn.org/" + args[1] + "/" + (long) reply.get("tim") + (String) reply.get("ext") : "N/A";
								complete += "\n**#** " + number + " @ " + date + "\n";
								complete += "**Comment** " + Jsoup.parse(comment).text() + "\n";
								if (!picture.equals("N/A")){
									complete += picture;
								}
								complete += "\n";
							}
							main.sendMessage(complete, channelID);
							complete = "";
							Thread.sleep(1000);
							max++;
							if (max >= 3){
								break;
							}
						}
					}
				} else {
					main.sendMessage(ping() + " You must provide a board ID. (a, hm, b, cm, mlp, etc)", channelID);
				}
			} else {
				
			}
		} catch (Exception e){
			main.sendMessage(ping() + " Something went wrong. You probably provided an invalid board ID.", channelID);
			e.printStackTrace();
		}
	}*/
	
	@SneakyThrows
	@MessageHandler(aliases = { "image" }, usage = "!ax:image <search>", desc = "Bing Image Search")
	public void onImageSearch(){
		if (main.cd.handleCooldown("system", CooldownType.ATAXIA_IMAGE, CooldownDuration.SECONDS, 6)){
			if (args.length >= 2){
				try {
					if (main.cd.handleCooldown(client.getID(), CooldownType.ATAXIA_IMAGE, CooldownDuration.MINUTES, 5)){
						String search = "";
						for (int i = 1; i < args.length; i++){
							search += search.equals("") ? args[i] : " " + args[i];
						}
						List<String> srcs = main.getImagesFromSearch(search, args[1].equals("-random"));
						int i = 0;
						for (String src : srcs){
							if (!src.startsWith("data:image")){
								try {
									src = src.startsWith("//") ? src.replaceFirst("//", "") : src;
								    src = (src.contains("http") ? "" : "http://") + src;
								    File theFile = new File("data/temp/ax_image_" + i + ".jpg");
								    FileUtils.copyURLToFile(new URL(src), theFile);
								    main.client.getChannelByID(channelID).sendFile(theFile);
								    i++;
								    if (i >= 5){
								    	main.doLater(() -> {
								    		List<File> toDelete = new ArrayList<File>();
									    	for (File f : new File("data/temp/").listFiles()){
									    		if (f.getName().startsWith("ax_image_")){
									    			toDelete.add(f);
									    		}
									    	}
											for (File f : toDelete){
									    		f.delete();
									    	}
								    	}, 6000L);
								    	break;
								    }
								} catch (Exception skip){}
							}
						}
					} else {
						main.sendMessage(ping() + " You can use this command again in " + main.cd.minutesLeft(client.getID(), CooldownType.ATAXIA_IMAGE) + " minutes.", Channel.TIKI_LOUNGE);
					}
				} catch (Exception e){
					main.sendMessage(ping() + " Something went wrong with that image, try again!", channelID);
					main.cd.endCooldown(client.getID(), CooldownType.ATAXIA_IMAGE);
				}
			} else {
				main.sendMessage(ping() + " You must provide a search!", channelID);
			}
		} else {
			main.sendMessage(ping() + " Global cooldown of 5 seconds on this command!", channelID);
		}
	}
	
	@MessageHandler(channel = Channel.MOVIES, aliases = { "imdb" }, usage = "!ax:imdb <search>, !ax:imdb -a <search>", desc = "IMDB Lookup")
	public void onIMDB(){
		if (args.length >= 2){
			String search = "";
			String toSay = "";
			for (int i = args[1].equals("-noplot") || args[1].equals("-a") ? 2 : 1; i < args.length; i++){
				search += search.equals("") ? args[i] : " " + args[i];
			}
			if (args[1].equals("-a")){
				int i = 1;
				try {
					JSONObject obj = main.getHTML("http://www.imdb.com/xml/find?json=1&nr=1&nm=on&q=" + search.replace(" ", "%20"));
					JSONArray array = new JSONArray();
					try {
						array = (JSONArray) obj.get("name_approx");
					} catch (Exception e){
						array = (JSONArray) obj.get("name_exact");
					}
					for (Object thing : array){
						JSONObject newObj = (JSONObject) thing;
						toSay += "\n**Result " + i + "** `" + newObj.get("description") + "`";
						i++;
					}
					JSONArray pic = (JSONArray) obj.get("name_popular");
					JSONObject picOb = (JSONObject) pic.get(0);
					toSay += "\nhttp://www.imdb.com/name/" + (String) picOb.get("id");
				} catch (Exception e){
					toSay = "No results found!";
				}
			} else {
				JSONObject obj = main.getHTML("http://www.omdbapi.com/?t=" + search.replace(" ", "%20"));
				try {
					if (((String) obj.get("Title")).equals("null") || obj.get("Title") == null){
						toSay = "No results found!";
					} else {
						toSay = "I found a result!\n**Title** `" + (String) obj.get("Title") + "`";
						String[] things = new String[]{ "Year", "Rating", "Runtime", "Genre", "Director", "Actors", "Plot", "Language", "Country", "Metascore", "imdbRating", "imdbVotes"};
						for (String thing : things){
							toSay += "\n**" + thing + "** `" + (obj.containsKey(thing) ? ((thing.equals("Plot") && args[1].equals("-noplot") ? "N/A" : (String) obj.get(thing))) : "N/A") + "`";
						}
						toSay += "\n" + (String) obj.get("Poster");
					}
				} catch (Exception e){
					toSay = "No results found!";
				}
			}
			main.sendMessage(toSay, Channel.MOVIES);
		} else {
			main.sendMessage(ping() + " You must provide a title to search for!", Channel.MOVIES);
		}
	}
	
	@SneakyThrows
	@MessageHandler(aliases = { "radio" }, noHelp = true, role = Role.ADMIN)
	public void onRadio(){
		main.client.getVoiceChannelByID("199665266764939265").join();
		InputStream is = new URL("http://stream.dancewave.online/dance.mp3").openStream();
        BufferedInputStream bis = new BufferedInputStream(is);
        AudioInputStream sound = AudioSystem.getAudioInputStream(bis);
		AudioPlayer.getAudioPlayerForGuild(main.client.getGuilds().get(0)).queue(sound);
	}
	
	@SneakyThrows
	@MessageHandler(aliases = { "play" }, noHelp = true, role = Role.ADMIN, channel = Channel.TIKI_LOUNGE)
	public void onPlay(){
		if (args[1].equals("-stop")){
			AudioPlayer.getAudioPlayerForGuild(main.client.getGuilds().get(0)).skip();
			main.client.getVoiceChannelByID("199665266764939265").leave();
		} else {
			main.client.getVoiceChannelByID("199665266764939265").join();
			AudioPlayer.getAudioPlayerForGuild(main.client.getGuilds().get(0)).queue(new File("data/" + args[1]));
		}
	}
	
	public void onNotFound(){
		main.sendMessage(ping() + "Command not found! Try !ax:help", channelID);
	}

	@MessageHandler(aliases = { "help" }, usage = "!ax:help", desc = "Ataxia help command")
	public void onHelp(){
		String help = "";
		String aliases = "";
		for (Method m : getClass().getMethods()){
			if (m.getAnnotation(MessageHandler.class) != null){
				MessageHandler mh = m.getAnnotation(MessageHandler.class);
				if (!mh.noHelp()){
					for (String alias : mh.aliases()){
						aliases += aliases.equals("") ? alias : ", " + alias;
					}
					help += "**" + mh.aliases()[0] + "** " + (mh.aliases().length > 1 ? "{ " + aliases + " }" : "") + "\n";
					help += "*" + mh.usage() + ", " + mh.desc() + ", " + (mh.channel().equals(Channel.ANY) ? "Any channel" : "<#" + mh.channel().getId() + ">") + ", " + mh.role().toString() + "*";
					help += "\n\n";
					aliases = "";
				}
			}
		}
		main.sendMessage("```xl\nAtaxia Commands (usage, description, required channel, role needed)```\n" + help, main.client.getChannelByID(channelID).getID());
	}
	
	@MessageHandler(aliases = { "autogame" }, usage = "!ax:autogame", desc = "Toggle your autogame status")
	public void onAutoGame(){
		boolean isAuto = LocalData.AUTOGAME.getData("users/" + client.getID(), main).asBool();
		LocalData.AUTOGAME.setData("users/" + client.getID(), !isAuto + "", main);
		main.sendPrivateMessage(client.getID(), "Your autogame preference is now set to: " + !isAuto + ".");
	}
	
	@SneakyThrows
	@MessageHandler(role = Role.ADMIN, aliases = "moveall", usage = "!ax:moveall <channel>", desc = "Mass move command")
	public void onMassMove(){
		if (args.length >= 2){
			IVoiceChannel channel = main.client.getVoiceChannelByID(Voice_Channel.valueOf(args[1].toUpperCase().replace(" ", "_")).getId());
			if (channel != null){
				for (IUser user : client.getClient().getUsers()){
					if (!user.getName().equals("dJ")){
						main.addToDiscordQueue(() -> {
							user.moveToVoiceChannel(channel);
						});
					}
				}
			}
		}
	}
	
	@MessageHandler(role = Role.ADMIN, aliases = "channel", usage = "!ax:channel -help", desc = "Channel Command")
	public void onChannel(){
		
	}
	
	@SneakyThrows
	@MessageHandler(aliases = { "who" }, usage = "!ax:who", desc = "Whois command")
	public void onWho(){
		if (main.cd.handleCooldown("system", CooldownType.ATAXIA_WHO, CooldownDuration.SECONDS, 6)){
			List<String> html = Files.readAllLines(Paths.get("./html", "template.html"));
			List<String> newHTML = new ArrayList<String>();
			List<IUser> users = new ArrayList<IUser>();
			Map<IUser, Integer> userMap = new HashMap<>();
			String toReplace = "";
			for (IUser u : client.getClient().getChannelByID(channelID).getUsersHere()){
				userMap.put(u, main.getHighestRole(u).getRank());
			}
			for (IUser u : users){
				main.log((u.getAvatarURL().contains("null") ? "default_avatar.png" : u.getAvatarURL()));
				toReplace += "<div class='profile'>" +
								"<img src='" + (u.getAvatarURL().contains("null") ? "default_avatar.png" : u.getAvatarURL()) + "' />" +
								"<div class='profile_info'>" +
									"<div class='name'>" + u.getName() + "</div>" +
									"<div class='id'>" + "(#" + u.getID() + ")</div>" + 
									"<div class='role'>" + main.getHighestRole(u).toString() + "</div>" + 
									"</div>" +
								"</div>" +
							"</div>";
			}
			for (String s : html){
				newHTML.add(s.contains("%replace%") ? toReplace : s);
			}
			new File("./html/page.html").delete();
			new File("./html/page.html").createNewFile();
			PrintWriter pw = new PrintWriter(new FileWriter("./html/page.html"));
			for (String s : newHTML){
				pw.println(s);
			}
			pw.close();
			Runtime.getRuntime().exec("C:/Users/David/Desktop/Assets/phantomjs-2.1.1-windows/bin/phantomjs", new String[] { "./html/test.js" } );
		} else {
			main.sendMessage(ping() + " Global cooldown on this command of 5 seconds!", channelID);
		}
	}
	
	@MessageHandler(aliases = { "bind", "b" }, usage = "!ax:bind [-add, -remove, -list] <\"keys\"> <\"other keys\">", desc = "Bind Command")
	public void onBind(){
		try {
			switch (args[1]){
				case "-add":
					String complete = "";
					for (int i = 2; i < args.length; i++){
						complete += complete.equals("") ? args[i] : " " + args[i];
					}
					List<String> matchList = new ArrayList<String>();
					Pattern regex = Pattern.compile("[^\\s\"']+|\"[^\"]*\"|'[^']*'");
					Matcher regexMatcher = regex.matcher(complete);
					while (regexMatcher.find()) {
					    matchList.add(regexMatcher.group().replace("\"", ""));
					}
					main.binds.get(client.getID()).addBind(matchList.get(0), matchList.get(1));
					main.sendMessage(ping() + " I have remapped `" + matchList.get(0) + "` to `" + matchList.get(1) + "` for you.", channelID);
				break;
				case "-remove": case "-delete": case "-rem": case "-del":
					complete = "";
					for (int i = 2; i < args.length; i++){
						complete += complete.equals("") ? args[i] : " " + args[i];
					}
					if (main.binds.get(client.getID()).hasBind(complete)){
						main.binds.get(client.getID()).removeBind(complete);
						main.sendMessage(ping() + " I have removed the bind for you!", channelID);
					} else {
						main.sendMessage(ping() + " I don't seem to have that bind registered. Try !ax:bind -list", channelID);
					}
				break;
				case "-list": case "-show":
					String bindList = "";
					for (String item : main.binds.get(client.getID()).list()){
						bindList += bindList.equals("") ? item : "\n" + item;
					}
					main.sendMessage("Here are your binds, " + ping() + "\n" + bindList, channelID);
				break;
			}

			
		} catch (Exception e){
			main.sendMessage(ping() + " Something went wrong! Example: !ax:bind -add \"!ax:help\" \"h\"", channelID);
		}
	}
	
	@SneakyThrows
	@MessageHandler(aliases = { "pic" }, usage = "!ax:pic <user>", desc = "Profile Picture Command")
	public void onPic(){
		if (args.length == 2){
			for (IUser u : client.getClient().getChannelByID(channelID).getUsersHere()){
				if (u.getName().toLowerCase().contains(args[1].toLowerCase())){
					if (!u.getAvatarURL().contains("null")){
						main.sendMessage(u.getAvatarURL(), channelID);
					} else {
						main.client.getGuildByID(Ataxia.GUILD_ID).getChannelByID(Channel.TIKI_LOUNGE.getId()).sendFile(new File("./html/default_avatar.png"));
					}
					return;
				}
			}
			main.sendMessage("No user found by that name.", channelID);
		} else {
			if (!client.getAvatarURL().contains("null")){
				main.sendMessage(client.getAvatarURL(), channelID);
			} else {
				main.client.getGuildByID(Ataxia.GUILD_ID).getChannelByID(Channel.TIKI_LOUNGE.getId()).sendFile(new File("./html/default_avatar.png"));
			}
		}
	}
	
	@SneakyThrows
	@MessageHandler(aliases = { "rel" }, usage = "!ax:rel", desc = "Reload", noHelp = true)
	public void onReload(){
		if (client.getID().contains(LocalData.OWNER_CHAT_ID.getData("keys", main).asString())){
			main.sendMessage("I'll be right back!", Channel.SERVER);
			main.save();
			System.exit(0);
		} else {
			main.sendMessage(ping() + "You don't have permissions for that, sorry love!", channelID);
		}
	}
}