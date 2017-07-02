package com.github.lyokofirelyte.Ataxia.listener;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import org.apache.commons.io.FileUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.tidy.Tidy;

import com.github.lyokofirelyte.Ataxia.Ataxia;
import com.github.lyokofirelyte.Ataxia.cooldown.CooldownDuration;
import com.github.lyokofirelyte.Ataxia.cooldown.CooldownType;
import com.github.lyokofirelyte.Ataxia.data.LocalData;
import com.github.lyokofirelyte.Ataxia.data.Role;
import com.github.lyokofirelyte.Ataxia.message.Channel;
import com.github.lyokofirelyte.Ataxia.message.MessageHandler;
import com.github.lyokofirelyte.Ataxia.message.QueueMessage;
import com.github.lyokofirelyte.Ataxia.message.Voice_Channel;

import lombok.SneakyThrows;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.impl.obj.Embed;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IEmbed;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.Image;
import sx.blah.discord.util.RequestBuffer;
import sx.blah.discord.util.RequestBuffer.RequestFuture;
import sx.blah.discord.util.audio.AudioPlayer;

public class MessageListener {
	
	private Ataxia main;
	private IUser client;
	private String[] args;
	private String channelID;
	private Timer audioTimer = new Timer();
	private IMessage message;
	private Map<String, String[]> effects = new HashMap<>();
	private QueueMessage queueMessage;
	
	public MessageListener(Ataxia i, IUser c, String[] args, String channelID, IMessage message){
		main = i;
		client = c;
		this.args = args;
		effects.put("airhorn", new String[]{ "mlg", "3000" });
		effects.put("triple", new String[]{ "babyatriple", "8000" });
		effects.put("toilet", new String[]{ "toilet_flush", "15000" });
		effects.put("suspense", new String[]{ "suspense", "3000" });
		effects.put("bombito", new String[]{ "bombito", "7000" });
		this.channelID = channelID;
		this.message = message;
		this.queueMessage = new QueueMessage(main);
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
				main.sendMessage("Error playing airhorn!", channelID);
			}
			main.doLater(() -> {
				main.client.getVoiceChannelByID("199665266764939265").leave();
			}, Long.parseLong(effects.get(args[0].split(":")[1])[1]));
		} else {
			main.sendMessage(ping() + " You can use this command again in " + main.cd.minutesLeft(client.getID(), CooldownType.ATAXIA_AIRHORN) + " minutes.", channelID);
		}
	}
	
	@SneakyThrows
	public JSONArray getHTMLAsArray(String urlToRead){
		return (JSONArray) new JSONParser().parse(getHTMLString(urlToRead));
	}
	
	public String getHTMLString(String urlToRead){
		try {
			StringBuilder result = new StringBuilder();
		      URL url = new URL(urlToRead);
		      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		      conn.setRequestMethod("GET");
		      conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_5) " + "AppleWebKit/537.31 (KHTML, like" + " " + "Gecko)" + " Chrome/26.0.1410.65 " + "Safari/537.31");
		      BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		      String line;
		      while ((line = rd.readLine()) != null) {
		         result.append(line);
		      }
		      rd.close();
		      return result.toString();
		} catch (Exception e){
			e.printStackTrace();
		}
		return "null";
	}
	
	@SneakyThrows
	@MessageHandler(aliases = { "playing" }, usage = "!ax:playing <game>", desc = "Who's playing what?")
	public void onPlaying(){
		String fin = "";
		if (args.length == 2){
			for (IUser u : main.client.getUsers()){
				if (u.getPresence().getPlayingText().isPresent() && u.getPresence().getPlayingText().get().toLowerCase().contains(args[1].toLowerCase())){
					fin += fin.equals("") ? u.getName() : ", " + u.getName();
				}
			}
			File theFile = new File("temp.png");
			//if (!theFile.exists()){
				String search = "http://www.bing.com/images/search?q=" + args[1] + "%20game%20logo" + "&go=Search&qs=n&form=QBILPG&pq=cookies&sc=8-7&sp=-1&sk=";
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
			//}
			main.client.getChannelByID(Channel.TIKI_LOUNGE.getId()).sendFile(theFile);
			main.sendMessage("Users playing " + args[1] + ":", channelID);
			main.sendMessage("```\n" + (fin.equals("") ? "Literally nobody." : fin) + "\nThat's " + (fin.equals("") ? "0" : fin.split(", ").length) + " / " + main.client.getUsers().size() + "!\n```", channelID);
			theFile.delete();
		} else {
			main.sendMessage("Usage: !ax:playing <name>", channelID);
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

	@MessageHandler(aliases = { "avatar" }, usage = "!ax:avatar", desc = "Bot Avatar Command", role = Role.DEVELOPER)
	public void onAvatar(){
		main.sendMessage("Attemping to change avatar as " + args[1], channelID);
        Image image = Image.forUrl(args[1], args[2]);
        main.client.changeAvatar(image);
		main.sendMessage("Complete.", channelID);
	}
	
	/*@MessageHandler(aliases = { "ssh" }, usage = "!ax:ssh", desc = "SSH Command", role = Role.DEVELOPER)
	public void onSSH(){
		try {
			if (args.length == 2 && args[1].equals("-o")){
				Shell shell = new SSH("worldscolli.de", 22, "wa", LocalData.SSH_PASSWORD.getData("keys", main).asString());
				main.plain = new Shell.Plain(shell);
				main.sendMessage("Connected!", channelID);
			} else if (args.length == 2 && args[1].equals("-c")){
				main.plain = null;
				main.sendMessage("Closed!", channelID);
			} else if (args.length > 1) {
				String arg = "";
				int i = 0;
				for (String a : args){
					i++;
					if (i == 1){
						continue;
					}
					arg += arg.equals("") ? a : " " + a;
				}
				String stdout = "Success:\n---\n```\n" + main.plain.exec(arg) + "\n```";
				main.sendMessage(stdout, channelID);
			}
		} catch(Exception e){
			e.printStackTrace();
		}
	}*/
	
	/*@SneakyThrows
	@MessageHandler(aliases = { "listen" }, usage = "!ax:listen", desc = "Listen Toggle")
	public void onListen(){
		if (main.listenFor(client.getID())){
			main.audioListeners.remove(client.getID());
			main.sendMessage("I won't listen for your voice anymore, " + ping(), channelID);
		} else {
			main.audioListeners.add(client.getID());
			main.sendMessage("Listening to you now, " + ping(), channelID);
		}
	}*/
	
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
		InputStream is = new URL("http://proxima.shoutca.st:9313/;stream.mp3").openStream();
        BufferedInputStream bis = new BufferedInputStream(is);
        AudioInputStream sound = AudioSystem.getAudioInputStream(bis);
		AudioPlayer.getAudioPlayerForGuild(main.client.getGuilds().get(0)).queue(sound);
	}
	
	@SneakyThrows
	@MessageHandler(aliases = { "queue" }, usage = "!ax:queue <query>", desc="Play Audio", role = Role.MEMBER, channel = Channel.MUSIC)
	public void onQueue(){
		main.client.getVoiceChannelByID("199665266764939265").join();
		message.delete();
		if (main.cd.handleCooldown("system", CooldownType.ATAXIA_QUEUE, CooldownDuration.SECONDS, 5)){
			if (main.queue.size() < 10){
				if (args.length >= 2){
					if (args[1].equals("remove")){
						if (!main.queue.containsKey(client.getName())){
							queueMessage.updateFeedback(client.getName() + ", you don't have anything in the main.queue.");
						} else {
							if (main.queueOrder.get(0).equals(client.getName())){
								AudioPlayer.getAudioPlayerForGuild(main.client.getGuilds().get(0)).skip();
								main.queue.remove(client.getName());
								for (int i = 0; i < main.queueOrder.size() - 1; i++){
									main.queueOrder.put(i, main.queueOrder.get(i+1));
								}
								main.queueOrder.remove(main.queueOrder.size() - 1);
							} else {
								for (int i = 0; i < main.queueOrder.size(); i++){
									if (main.queueOrder.get(i).equals(client.getName())){
										AudioPlayer.getAudioPlayerForGuild(main.client.getGuilds().get(0)).getPlaylist().remove(i);
										for (int x = i; x < main.queueOrder.size() - 1; x++){
											main.queueOrder.put(x, main.queueOrder.get(x+1));
										}
										main.queueOrder.remove(main.queueOrder.size() - 1);
										main.queue.remove(client.getName());
										break;
									}
								}
							}
							queueMessage.updateFeedback(client.getName() + ", your submission has been removed.");
						}
					} else if (!main.queue.containsKey(client.getName())){
						main.queue.put(client.getName(), main.restOfString(args, 1));
						int max = -1;
						for (int i : main.queueOrder.keySet()){
							max = (i > max ? i : max);
						}
						main.queueOrder.put(max+1, client.getName());
						queueMessage.updateFeedback(client.getName() + ", your submission has been added!");
						if (new File(client.getID()).exists()){
							for (File f : new File(client.getID()).listFiles()){
								f.delete();
							}
						} else {
							new File(client.getID()).mkdirs();
						}
						Process p = null;
						if (args[1].equals("-link")){
							p = Runtime.getRuntime().exec("youtube-dl -o \"" + client.getID() + "/%(title)s.%(ext)s\" --extract-audio --audio-format mp3 " + args[2]);
						} else {
							p = Runtime.getRuntime().exec("youtube-dl -o " + client.getID() + "/%(title)s.%(ext)s ytsearch:\"" +  main.restOfString(args, 2) + "\" --extract-audio --audio-format mp3");
						}
						p.waitFor();
						for (File f : new File(client.getID()).listFiles()){
							if (f.getName().endsWith(".mp3")){
								AudioPlayer.getAudioPlayerForGuild(main.client.getGuilds().get(0)).queue(f);
								break;
							}
						}
					} else {
						queueMessage.updateFeedback(client.getName() + ", you already have something in the queue!");
					}
				} else {
					queueMessage.updateFeedback(client.getName() + ", please use !ax:queue <query>");
				}
				String newMessage = queueMessage.msg.getContent();
				for (int i = 0; i < main.queueOrder.size(); i++){
					newMessage = queueMessage.edit(i, main.queue.get(main.queueOrder.get(i)), main.queueOrder.get(i), "Waiting...", newMessage);
				}
				if (main.queueOrder.size() < 10){
					for (int i = main.queueOrder.size(); i < 10; i++){
						newMessage = queueMessage.edit(i, "Song", "User", "Status", newMessage);
					}
				}
				main.queueMessage.edit(newMessage);
			} else {
				queueMessage.updateFeedback(client.getName() + ", the queue is full!");
			}
		} else if (main.cd.handleCooldown(client.getName(), CooldownType.ATAXIA_QUEUE, CooldownDuration.SECONDS, 5)){
			queueMessage.updateFeedback(client.getName() + ", please wait for the global cooldown (5 seconds).");
		}
	}
	
	@SneakyThrows
	@MessageHandler(aliases = { "play" }, usage = "!ax:play [-link, -search] <query>", desc="Play Audio", role = Role.MEMBER, channel = Channel.MUSIC)
	public void onPlay(){
		queueMessage.updateFeedback(client.getName() + ", please use !ax:queue <query>");
		/*
		if (args[1].equals("-stop")){
			AudioPlayer.getAudioPlayerForGuild(main.client.getGuilds().get(0)).skip();
			main.client.getVoiceChannelByID("199665266764939265").leave();
			audioTimer.cancel();
		} else if ((args[1].equals("-link") || args[1].equals("-search")) && args.length >= 3){
			try {
				audioTimer.cancel();
				AudioPlayer.getAudioPlayerForGuild(main.client.getGuilds().get(0)).clear();
				for (String f : new File(".").list()){
					if (f.endsWith(".mp3")){
						new File(f).delete();
					}
				}
				Process p = null;
				if (args[1].equals("-link")){
					p = Runtime.getRuntime().exec("youtube-dl --extract-audio --audio-format mp3 " + args[2]);
				} else {
					p = Runtime.getRuntime().exec("youtube-dl ytsearch:\"" +  main.restOfString(args, 2) + "\" --extract-audio --audio-format mp3");
				}
				IMessage message = main.sendMessage(ping() + " Downloading!", channelID);
				BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
				BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
				String s = null;
				long next = 0;
				while ((s = stdInput.readLine()) != null) {
					 String localSplz = new String(s);
					 if (System.currentTimeMillis() >= next){
						 RequestBuffer.request(() -> {
							 message.edit(localSplz);
						 });
						 next = System.currentTimeMillis() + 2000L;
					 }
				}
				while ((s = stdError.readLine()) != null) {
					System.out.println(s);
				}
				p.waitFor();
				main.sendMessage(ping() + " Playing!", channelID);
				main.client.getVoiceChannelByID("199665266764939265").join();
				String duration = "";
				long actualDuration = 0L;
				for (File f : new File(".").listFiles()){
					if (f.getName().endsWith(".mp3")){
						duration = main.getAudioDuration(f)[0];
						actualDuration = Long.parseLong(main.getAudioDuration(f)[1]);
						AudioPlayer.getAudioPlayerForGuild(main.client.getGuilds().get(0)).queue(f);
						break;
					}
				}
				final long actualDuration2 = new Long(actualDuration);
				final String actualDuration3 = new String(duration);
				audioTimer = main.repeat(() -> {
					try {
						 long elapsed = AudioPlayer.getAudioPlayerForGuild(main.client.getGuilds().get(0)).getCurrentTrack().getCurrentTrackTime() / 1000;
						 long result = (elapsed / (actualDuration2 / 1000))*100;
						 String lines = "[";
						 for (int i = 0; i < 100; i++){
							 lines += i <= result ? "|" : " ";
						 }
						 lines += "]";
						 final String linez = new String(lines);
						 RequestBuffer.request(() -> {
							 long sec = (elapsed) % 60;
							 long min = (elapsed) / 60;
							 String amin = min < 10 ? "0" + min : min + "";
							 message.edit("Now Playing: " + main.restOfString(args, 2) + "\n" + amin + ":" + sec + " / " + actualDuration3 + " " + linez);
						 });
						} catch (Exception eee){
							eee.printStackTrace();
						}
					}, 3000L, 3000L);
			} catch (Exception e){
				audioTimer.cancel();
				main.sendMessage(ping() + " Something went totally wrong with that. Yikes!", channelID);
				e.printStackTrace();
			}
		} else if (args[1].equals("-elapsed")){
			main.sendMessage(AudioPlayer.getAudioPlayerForGuild(main.client.getGuilds().get(0)).getCurrentTrack().getCurrentTrackTime() / 1000 + "s", channelID);
		} else {
			audioTimer.cancel();
			main.client.getVoiceChannelByID("199665266764939265").join();
			AudioPlayer.getAudioPlayerForGuild(main.client.getGuilds().get(0)).queue(new File("data/" + args[1]));
		}*/
	}
	
	public void onNotFound(){
		main.sendMessage(ping() + "Command not found! Try !ax:help", channelID);
	}
	
	@MessageHandler(aliases = { "vol" }, usage = "!ax:vol <float>", desc = "Volume Command (0 - 1)", role = Role.ADMIN)
	public void onVol(){
		AudioPlayer.getAudioPlayerForGuild(main.client.getGuilds().get(0)).setVolume(Float.parseFloat(args[1]));
	}
	
	/*@SneakyThrows
	@MessageHandler(aliases = { "web" }, usage = "!ax:web <url>", desc = "View a Website", role = Role.MEMBER)
	public void onWeb(){
		if (main.cd.handleCooldown("system", CooldownType.ATAXIA_WHO, CooldownDuration.SECONDS, 10)){
			if (args.length == 2){
				String html = Jsoup.connect(args[1]).get().html();
				FileUtils.writeStringToFile(new File("download.html"), html);
				Process p = Runtime.getRuntime().exec("C:/Users/David/Desktop/Assets/phantomjs-2.1.1-windows/bin/phantomjs ./html/test2.js");
				p.waitFor();
				main.client.getChannelByID(channelID).sendFile(new File("image2.png"));
			} else {
				main.sendMessage(ping() + "you must provide a URL!", channelID);
			}
		} else {
			main.sendMessage("Global cooldown of 10 seconds on this command!", channelID);
		}
	}*/

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
	
	@MessageHandler(aliases = { "autogame" }, usage = "!ax:autogame", desc = "Toggle your autogame status", noHelp = true)
	public void onAutoGame(){
		// boolean isAuto = LocalData.AUTOGAME.getData("users/" + client.getID(), main).asBool();
		// LocalData.AUTOGAME.setData("users/" + client.getID(), !isAuto + "", main);
		// main.sendPrivateMessage(client.getID(), "Your autogame preference is now set to: " + !isAuto + ".");
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
	
	/*@MessageHandler(role = Role.ADMIN, aliases = "channel", usage = "!ax:channel -help", desc = "Channel Command")
	public void onChannel(){
		
	}*/
	
	@SneakyThrows
	@MessageHandler(aliases = { "who" }, usage = "!ax:who", desc = "Whois command")
	public void onWho(){
		if (main.cd.handleCooldown("system", CooldownType.ATAXIA_WHO, CooldownDuration.SECONDS, 6)){
			main.sendMessage("This may take awhile...", channelID);
			List<String> html = Files.readAllLines(Paths.get("./html/site/src", "template.html"));
			List<String> newHTML = new ArrayList<String>();
			List<IUser> users = new ArrayList<IUser>();
			Map<IUser, Integer> userMap = new HashMap<>();
			String toReplace = "";
			List<String> sectionDefaults = Files.readAllLines(Paths.get("./html/site/src", "section.txt"));
			int count = 0;
			for (IUser u : client.getClient().getChannelByID(channelID).getUsersHere()){
				userMap.put(u, main.getHighestRole(u).getRank());
			}
			for (int i = Role.values().length - 1; i > -1; i--){
				for (IUser u : userMap.keySet()){
					if (userMap.get(u) == i){
						users.add(u);
					}
				}
			}
			for (IUser u : users){
				List<String> superSectionDefaults = new ArrayList<String>(sectionDefaults);
				for (String supers : superSectionDefaults){
					toReplace += supers.replace("%id%", u.getID()).replace("%username%", u.getName()).replace("%role%", main.getHighestRole(u).toString());
				}
				count++;
				if (count == 5){
					toReplace += "</tr><tr>";
					count = 0;
				}
			}
			for (String s : html){
				newHTML.add(s.contains("%replace%") ? toReplace : s);
			}
			new File("./html/site/src/index.html").delete();
			new File("./html/site/src/index.html").createNewFile();
			PrintWriter pw = new PrintWriter(new FileWriter("./html/site/src/index.html"));
			for (String s : newHTML){
				pw.println(s);
			}
			pw.close();
			List<String> css = Files.readAllLines(Paths.get("./html/site/src/css", "template.css"));
			toReplace = "";
			newHTML = new ArrayList<String>();
			for (IUser u : users){
				toReplace += "section.content > table div.avatar#id_" + u.getID() + "{ background-image: url('" + (u.getAvatarURL().contains("null") ? "default_avatar.png" : u.getAvatarURL().replace(".webp", ".png")) + "'); }";
			}
			for (String s : css){
				newHTML.add(s.contains("%replace%") ? toReplace : s);
			}
			pw = new PrintWriter(new FileWriter("./html/site/src/css/index.css"));
			for (String s : newHTML){
				pw.println(s);
			}
			pw.close();
			Runtime.getRuntime().exec("C:/Users/David/Desktop/Assets/phantomjs-2.1.1-windows/bin/phantomjs ./html/test.js");
			main.doLater(() -> {
				try {
					main.client.getChannelByID(channelID).sendFile(new File("image.png"));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}, 3000L);
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
	@MessageHandler(aliases = { "pic" }, usage = "!ax:pic <user> [as <ext>]", desc = "Profile Picture Command")
	public void onPic(){
		if (args.length >= 2){
			String argz = "";
			for (String a : args){
				argz += argz.equals("") ? a : " " + a;
			}
			String[] query = argz.split(" ");
			boolean found = false;
			for (IUser u : client.getClient().getChannelByID(channelID).getUsersHere()){
				for (String q : query){
					if (!q.equals("as") && (q.equals("*") || u.getName().toLowerCase().contains(q.toLowerCase()))){
						if (!u.getAvatarURL().contains("null")){
							if (u.getAvatarURL().endsWith(".gif")){
								main.sendMessage(u.getAvatarURL(), channelID);
								continue;
							}
							 final HttpURLConnection connection = (HttpURLConnection) new URL(u.getAvatarURL().replace(".webp", ".png")).openConnection();
							 connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_5) " + "AppleWebKit/537.31 (KHTML, like" + " " + "Gecko)" + " Chrome/26.0.1410.65 " + "Safari/537.31");
							 BufferedImage img = ImageIO.read(connection.getInputStream());
							 if (args.length >= 4 && args[3].equals("fucc")){
								 img = colorImage(img);
								 args[3] = "jpg";
							 }
							 ImageIO.write(img, "png", new File(u.getID() + ".png"));
							 if (args.length >= 4 && args[2].equalsIgnoreCase("as")){
								 File inputFile = new File(u.getID() + ".png");
								 File outputFile = new File(u.getID() + "." + args[3].toLowerCase());
								 outputFile.createNewFile();
								 try (InputStream is = new FileInputStream(inputFile)) {
								     BufferedImage image = ImageIO.read(is);
								     try (OutputStream os = new FileOutputStream(outputFile)) {
								         ImageIO.write(image, args[3].toLowerCase(), os);
								     } catch (Exception exp) {
								         exp.printStackTrace();
								     }
								 } catch (Exception exp) {
								     exp.printStackTrace();
								 }
								main.client.getGuildByID(Ataxia.GUILD_ID).getChannelByID(Channel.TIKI_LOUNGE.getId()).sendFile(new File(u.getID() + "." + args[3].toLowerCase()));
								return;
							 } else if (!args[2].equalsIgnoreCase("as")) {
								main.client.getGuildByID(Ataxia.GUILD_ID).getChannelByID(Channel.TIKI_LOUNGE.getId()).sendFile(new File(u.getID() + ".png"));
							 }
						} else {
							main.client.getGuildByID(Ataxia.GUILD_ID).getChannelByID(Channel.TIKI_LOUNGE.getId()).sendFile(new File("./html/default_avatar.png"));
						}
						found = true;
						continue;
					}
				}
			}
			if (!found){
				main.sendMessage("No user found by that name.", channelID);
			}
		} else {
			if (!client.getAvatarURL().contains("null")){
				main.sendMessage(client.getAvatarURL(), channelID);
			} else {
				main.client.getGuildByID(Ataxia.GUILD_ID).getChannelByID(Channel.TIKI_LOUNGE.getId()).sendFile(new File("./html/default_avatar.png"));
			}
		}
	}
	
   private static BufferedImage colorImage(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        WritableRaster raster = image.getRaster();
        Random rand = new Random();

        for (int xx = 0; xx < width; xx++) {
            for (int yy = 0; yy < height; yy++) {
            	int yo = rand.nextInt(100);
            	int yo2 = rand.nextInt(100);
            	int yo3 = rand.nextInt(100);
            	Color originalColor = new Color(image.getRGB(xx, yy), true);
                int[] pixels = raster.getPixel(xx, yy, (int[]) null);
                pixels[0] = (originalColor.getRed() + yo) <= 255 ? originalColor.getRed() + yo : originalColor.getRed() - yo;
                pixels[1] = (originalColor.getGreen() + yo2) <= 255 ? originalColor.getGreen() + yo2 : originalColor.getGreen() - yo2;
                pixels[2] = (originalColor.getBlue() + yo3) <= 255 ? originalColor.getBlue() + yo3 : originalColor.getBlue() - yo3;
                raster.setPixel(xx, yy, pixels);
            }
        }
        return image;
    }
	
	@MessageHandler(aliases = { "del" }, usage = "!ax:del <amount> [-s (skip)]", desc = "Delete Command", role = Role.ADMIN)
	public void onDel(){
		if (args.length >= 2){
			if (args[1].equals("-r")){
				main.sendMessage("RESTORED MESSAGES: \n\n", channelID);
				for (String msg : main.savedMessages){
					String sender = msg.split("%")[0];
					String message = msg.split("%")[1];
					main.sendMessage("*" + sender + "* " + message + "\n", channelID);
				}
				main.savedMessages.clear();
			} else {
				List<Integer> skips = new ArrayList<Integer>();
				List<String> toDel = new ArrayList<String>();
				int amount = Integer.parseInt(args[1]);
				boolean started = false;
				for (String s : args){
					if (started){
						skips.add(Integer.parseInt(s));
					} else if (s.equals("-s")){
						started = true;
					}
				}
				IChannel chan = main.client.getChannelByID(channelID);
				int x = 0;
				for (int i = 0; i < amount; i++){
					x++;
					if (!skips.contains(x)){
						main.savedMessages.add(new String(chan.getMessages().get(i).getAuthor().getName() + "%" + chan.getMessages().get(i).getContent()));
						toDel.add(chan.getMessages().get(i).getID());
						main.log("Added for del: " + chan.getMessages().get(i).getContent());
					}
				}
				long waitTime = 0L;
				for (String del : toDel){
					waitTime += 250L;
					main.doLater(() -> {
						try {
							chan.getMessageByID(del).delete();
						} catch (Exception e){
							e.printStackTrace();
						}
					}, waitTime);
				}
			}
		}
	}
	
	@MessageHandler(aliases = { "info" }, usage = "!ax:info <user>", desc = "Google information about their avatar!", role = Role.MEMBER, channel = Channel.TIKI_LOUNGE)
	public void onInfo(){
		if (args.length == 2){
			if (main.cd.handleCooldown(client.getID(), CooldownType.ATAXIA_INFO, CooldownDuration.SECONDS, 10)){
				for (IUser u : main.client.getChannelByID(Long.parseLong(Channel.TIKI_LOUNGE.getId())).getUsersHere()){
					if (u.getName().toLowerCase().contains(args[1].toLowerCase())){
						String link = "https://www.google.com/searchbyimage?site=search&sa=X&image_url=" + u.getAvatarURL().replace(".webp", ".png");
						org.jsoup.nodes.Document doc = Jsoup.parse(getHTMLString(link));
						String result = "";
						String[] splits = doc.html().split("\n");
						for (int i = 0; i < splits.length; i++){
							String curr = splits[i];
							if (curr.contains("Best guess")){
								result = splits[i+1].substring(splits[i+1].indexOf('>')+1).replace("</a>", "");
								break;
							}
						}
						String imagez = "";
						Elements images = doc.select(".notranslate");
						int i = 0;
						for (Element el : images) {
							org.json.JSONObject json = new org.json.JSONObject(el.html());
						    imagez += "\n" + json.getString("ou");
							EmbedObject embed = new EmbedBuilder().withColor(Color.WHITE).withThumbnail(u.getAvatarURL().replace(".webp", ".png")).withFooterText("Lookup by " + client.getName()).withFooterIcon(client.getAvatarURL().replace(".webp", ".png")).withDesc("Result #" + (i+1) + " for " + result).withUrl(json.getString("ru")).withImage(json.getString("ou")).withTitle(json.getString("pt")).build();
							main.client.getChannelByID(Channel.TIKI_LOUNGE.getId()).sendMessage(embed);
							i++;
							if (i >= 4){
								break;
							}
						}
						break;
					}
				}
			} else {
				main.sendMessage(ping() + "Please wait 10 seconds between each lookup!", channelID);
			}
		} else {
			main.sendMessage(ping() + "!ax:info <player>", channelID);
		}
	}
	
	@SneakyThrows
	@MessageHandler(aliases = { "shows" }, usage = "!ax:shows", desc = "Shows Registration Command", role = Role.MEMBER)
	public void onShows(){
		if (main.cd.handleCooldown(client.getID(), CooldownType.ATAXIA_SHOWS, CooldownDuration.SECONDS, 3)){
			try {
				String avatarUrl = client.getAvatarURL().replace(".webp", ".png");
				String clientName = client.getName();
				String clientID = client.getID().toString();
				String role = main.getHighestRole(client).toString();
				String ref = clientID + "_" + (10000 + new Random().nextInt(100000));
				
				String url = "http://shows.tikilounge.co";  
				URL obj = new URL(url);
				URLConnection con = obj.openConnection();
			    // activate the output
			    con.setDoOutput(true);
			    PrintStream ps = new PrintStream(con.getOutputStream());
			    // send your parameters to your site
			    ps.print("avatar=" + avatarUrl);
			    ps.print("&client_name=" + clientName);
			    ps.print("&client_id=" + clientID);
			    ps.print("&role=" + role);
			    ps.print("&ref=" + ref);
			    
			    if (args.length >= 2){
					if (args[1].equals("-reset")){
						ps.print("&reset=true");
					}
			    }
			    BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			    String line = null;
			    
			    while ((line = in.readLine()) != null) {
			    	System.out.println(line);
			    }
			    ps.close();
				
				main.sendPrivateMessage(client.getID(), "Your shows link is http://shows.tikilounge.co?ref=" + ref);
				if (args.length >= 2 && args[1].equals("-reset")){
					main.sendPrivateMessage(client.getID(), "You will need to log out of the website first!");
				}
			} catch (Exception e){
				e.printStackTrace();
				main.sendPrivateMessage(client.getID(), "There was an error. The shows site is probably down.");
			}
		} else {
			main.sendPrivateMessage(client.getID(), "Please wait at least 3 seconds between each command.");
		}
	}
	
	@SneakyThrows
	@MessageHandler(aliases = { "wave", "testf" }, usage = "!ax:wave", desc = "Fun!", noHelp = true, role = Role.DEVELOPER)
	public void onWave(){
		new Thread(() -> {
			List<IRole> toDelete = new ArrayList<>();

			for (IRole r : client.getClient().getGuildByID(main.GUILD_ID).getRoles()){
				if (r.getName().matches("[0-9]+")){
					toDelete.add(r);
				}
			}
			
			for (IRole r : toDelete){
				 RequestFuture<Void> f = RequestBuffer.request(() -> {
					 r.delete();
				 });
				 while (!f.isDone()){}
			}
		}).start();
	}
	
	@SneakyThrows
	@MessageHandler(aliases = { "rel" }, usage = "!ax:rel", desc = "Reload", noHelp = true)
	public void onReload(){
		if (client.getID().contains(LocalData.OWNER_CHAT_ID.getData("keys", main).asString())){
			main.sendMessage("Going down for a couple seconds, brb!", Channel.TIKI_LOUNGE);
			main.save();
			System.exit(0);
		} else {
			main.sendMessage(ping() + "You don't have permissions for that, sorry love!", channelID);
		}
	}
}