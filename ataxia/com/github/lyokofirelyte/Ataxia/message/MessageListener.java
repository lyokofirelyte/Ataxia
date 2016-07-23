package com.github.lyokofirelyte.Ataxia.message;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.jsoup.Jsoup;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.tidy.Tidy;

import com.github.lyokofirelyte.Ataxia.Ataxia;
import com.github.lyokofirelyte.Ataxia.LocalData;
import com.github.lyokofirelyte.Ataxia.cooldown.CooldownDuration;
import com.github.lyokofirelyte.Ataxia.cooldown.CooldownType;

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
	public JSONObject getHTML(String urlToRead){
	      String result = getHTMLString(urlToRead);
	      JSONParser parser = new JSONParser();
	      JSONObject json = (JSONObject) parser.parse(result.toString());
	      return json;
	}
	
	@SneakyThrows
	public JSONArray getHTMLAsArray(String urlToRead){
		return (JSONArray) new JSONParser().parse(getHTMLString(urlToRead));
	}
	
	@SneakyThrows
	public String getHTMLString(String urlToRead){
		StringBuilder result = new StringBuilder();
	      URL url = new URL(urlToRead);
	      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	      conn.setRequestMethod("GET");
	      BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	      String line;
	      while ((line = rd.readLine()) != null) {
	         result.append(line);
	      }
	      rd.close();
	      return result.toString();
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
				main.sendMessage("Error playing airhorn! :3", Channel.TIKI_LOUNGE);
			}
	
			new Timer().schedule(new TimerTask(){
				@Override
				public void run(){
					main.client.getVoiceChannelByID("199665266764939265").leave();
				}
			}, Long.parseLong(effects.get(args[0].split(":")[1])[1]));
		} else {
			main.sendMessage(ping() + " You can use this command again in " + main.cd.minutesLeft(client.getID(), CooldownType.ATAXIA_AIRHORN) + " minutes.", channelID);
		}
	}
	
	@SneakyThrows
	public String shorten(String link){
		URL url = new URL("http://tinyurl.com/api-create.php?url=" + link);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        if (conn.getResponseCode() != 200) {
            return link;
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(
            (conn.getInputStream())));
        String output;
        String results = "";
        while ((output = br.readLine()) != null) {
        	results += output;
        }
        conn.disconnect();
        return results;
	}
	
	@SneakyThrows
	public String resolveURL(String base){
		URL url = new URL(base);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        InputStream is = conn.getInputStream();
        String toReturn = conn.getURL().toString();
        is.close();
        conn.disconnect();
        main.log(toReturn);
        return toReturn;
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
		if (args.length >= 2){
			try {
				if (main.cd.handleCooldown(client.getID(), CooldownType.ATAXIA_IMAGE, CooldownDuration.MINUTES, 5)){
					String search = "";
					String toSay = "";
					for (int i = 1; i < args.length; i++){
						search += search.equals("") ? args[i] : " " + args[i];
					}
					search = args[1].equals("-random") ? resolveURL("http://imgur.com/random") : "http://www.bing.com/images/search?q=" + search.replace(" ", "%20") + "&go=Search&qs=n&form=QBILPG&pq=cookies&sc=8-7&sp=-1&sk=";
					InputStream input = new URL(search).openStream();
					Document document = new Tidy().parseDOM(input, null);
					NodeList imgs = document.getElementsByTagName("img");
					List<String> srcs = new ArrayList<String>();
		
					for (int i = 0; i < imgs.getLength(); i++) {
					    srcs.add(imgs.item(i).getAttributes().getNamedItem("src").getNodeValue());
					}
					
					int i = 0;
		
					for (String src : srcs){
						if (!src.startsWith("data:image")){
							src = src.startsWith("//") ? src.replaceFirst("//", "") : src;
						    toSay += (!args[1].equals("-random") ? shorten(src) : "http://" + src) + "\n";
						    i++;
						    if (i >= 5){
						    	break;
						    }
						}
					}
					main.sendMessage(toSay.replace("ERROR", ""), channelID);
				} else {
					main.sendMessage(ping() + " You can use this command again in " + main.cd.minutesLeft(client.getID(), CooldownType.ATAXIA_IMAGE) + " minutes.", Channel.TIKI_LOUNGE);
				}
			} catch (Exception e){
				main.sendMessage(ping() + " Something went wrong with that image, try again!", channelID);
				main.cd.endCooldown(client.getID(), CooldownType.ATAXIA_IMAGE);
			}
		} else {
			main.sendMessage(ping() + " you must provide a search!", channelID);
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
					JSONObject obj = getHTML("http://www.imdb.com/xml/find?json=1&nr=1&nm=on&q=" + search.replace(" ", "%20"));
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
				JSONObject obj = getHTML("http://www.omdbapi.com/?t=" + search.replace(" ", "%20"));
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
	
	@SneakyThrows
	@MessageHandler(role = Role.ADMIN, aliases = "moveall", usage = "!ax:moveall <channel>", desc = "Mass move command")
	public void onMassMove(){
		if (args.length >= 2){
			IVoiceChannel channel = main.client.getVoiceChannelByID(Voice_Channel.valueOf(args[1].toUpperCase().replace(" ", "_")).getId());
			if (channel != null){
				for (IUser user : client.getClient().getUsers()){
					user.moveToVoiceChannel(channel);
				}
			}
		}
	}
	
	@MessageHandler(role = Role.ADMIN, aliases = "channel", usage = "!ax:channel -help", desc = "Channel Command")
	public void onChannel(){
		
	}
	
	@MessageHandler(aliases = { "who" }, usage = "!ax:who", desc = "Whois command")
	public void onWho(){
		String toSend = "";
		for (IUser u : client.getClient().getChannelByID(channelID).getUsersHere()){
			String setup = u.getName() + " (" + u.getID() + ")" + "\n" + u.getAvatarURL();
			toSend += toSend.equals("") ? setup : "\n- - -\n" + setup;
		}
		main.sendMessage(toSend, channelID);
	}
	
	@MessageHandler(aliases = { "pic" }, usage = "!ax:pic <user>", desc = "Profile Picture Command")
	public void onPic(){
		if (args.length == 2){
			for (IUser u : client.getClient().getChannelByID(channelID).getUsersHere()){
				if (u.getName().contains(args[1])){
					main.sendMessage(u.getAvatarURL(), channelID);
					return;
				}
			}
			main.sendMessage("No user found by that name.", channelID);
		}
	}
	
	@SneakyThrows
	@MessageHandler(aliases = { "rel" }, usage = "!ax:rel", desc = "Reload", noHelp = true)
	public void onReload(){
		if (client.getID().contains(LocalData.OWNER_CHAT_ID.getData("keys", main).asString())){
			main.sendMessage("I'll be right back!", Channel.TIKI_LOUNGE);
			main.save();
			System.exit(0);
		} else {
			main.sendMessage(ping() + "You don't have permissions for that, sorry love!", channelID);
		}
	}
}