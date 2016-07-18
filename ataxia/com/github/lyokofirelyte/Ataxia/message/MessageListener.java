package com.github.lyokofirelyte.Ataxia.message;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.tidy.Tidy;

import com.github.lyokofirelyte.Ataxia.Ataxia;
import com.github.lyokofirelyte.Ataxia.LocalData;
import com.github.lyokofirelyte.Ataxia.cooldown.CooldownDuration;
import com.github.lyokofirelyte.Ataxia.cooldown.CooldownType;

import lombok.SneakyThrows;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.audio.AudioPlayer;

public class MessageListener {
	
	private Ataxia main;
	private IUser client;
	private String[] args;
	
	private String help = 
		"```" +
		"- - - Ataxia Help Menu - - - \n" +
		"!ax:who // Lists everyone w/ avatar\n" +
		"!ax:rel // David's reload command\n" +
		"!ax:pic <user> // Displays the avatar for <user>\n" +
		"!ax:imdb [-a] <serach> // Search IMDB for movie/tv info, -a for actor\n" +
		"!ax:airhorn // MLG Pro\n" + 
		"!ax:triple // OH BABY A TRIPLE\n" +
		"!ax:image <search> // Search for an image on Bing" +
		"```"
	;
	
	private Map<String, String[]> effects = new HashMap<>();
	
	public MessageListener(Ataxia i, IUser c, String[] args){
		main = i;
		client = c;
		this.args = args;
		effects.put("airhorn", new String[]{ "mlg", "3000" });
		effects.put("triple", new String[]{ "babyatriple", "8000" });
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
	@MessageHandler(aliases = { "airhorn", "triple" }, usage = "!ax:airhorn", desc = "DO IT BITCH")
	public void onAirhorn(){
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
	
	@SneakyThrows
	@MessageHandler(aliases = { "image" }, usage = "!ax:image <search>", desc = "Bing Image Search")
	public void onImageSearch(){
		if (args.length >= 2){
			try {
				if (main.cd.handleCooldown(client.getID(), CooldownType.ATAXIA_IMAGE, CooldownDuration.HOURS, 1)){
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
					main.sendMessage(toSay.replace("ERROR", ""), Channel.TIKI_LOUNGE);
				} else {
					main.sendMessage(ping() + " you can use this command again in " + main.cd.minutesLeft(client.getID(), CooldownType.ATAXIA_IMAGE), Channel.TIKI_LOUNGE);
				}
			} catch (Exception e){
				main.sendMessage(ping() + " Something went wrong with that image, try again!", Channel.TIKI_LOUNGE);
				main.cd.endCooldown(client.getID(), CooldownType.ATAXIA_IMAGE);
			}
		} else {
			main.sendMessage(ping() + " you must provide a search!", Channel.TIKI_LOUNGE);
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
			main.sendMessage(toSay, Channel.TIKI_LOUNGE);
		} else {
			main.sendMessage(ping() + " You must provide a title to search for!", Channel.TIKI_LOUNGE);
		}
	}
	
	@SneakyThrows
	@MessageHandler(aliases = { "play" })
	public void a(){
		main.client.getVoiceChannelByID("199665266764939265").join();
		AudioPlayer.getAudioPlayerForGuild(main.client.getGuilds().get(0)).queue(new File("data/twp.mp3"));
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
	
	@MessageHandler(aliases = { "pic" }, usage = "!ax:pic <user>", desc = "Profile Picture Command")
	public void onPic(){
		if (args.length == 2){
			for (IUser u : client.getClient().getChannelByID(Channel.TIKI_LOUNGE.getId()).getUsersHere()){
				if (u.getName().contains(args[1])){
					main.sendMessage(u.getAvatarURL(), Channel.TIKI_LOUNGE);
					return;
				}
			}
			main.sendMessage("No user found by that name.", Channel.TIKI_LOUNGE);
		}
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