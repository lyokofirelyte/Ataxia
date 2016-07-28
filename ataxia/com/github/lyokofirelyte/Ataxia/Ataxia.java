package com.github.lyokofirelyte.Ataxia;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
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

import com.github.lyokofirelyte.Ataxia.cooldown.Cooldown;
import com.github.lyokofirelyte.Ataxia.data.AtaxiaRunnable;
import com.github.lyokofirelyte.Ataxia.data.Bind;
import com.github.lyokofirelyte.Ataxia.data.LocalData;
import com.github.lyokofirelyte.Ataxia.data.Role;
import com.github.lyokofirelyte.Ataxia.listener.AtaxiaListener;
import com.github.lyokofirelyte.Ataxia.listener.GenericListener;
import com.github.lyokofirelyte.Ataxia.message.Channel;
import com.github.lyokofirelyte.Ataxia.message.MinecraftChatHandler;
import com.google.code.chatterbotapi.ChatterBotSession;

import lombok.SneakyThrows;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IPrivateChannel;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;

public class Ataxia {
	
	public static String VERSION = "1.0";
	public static String GUILD_ID = "199663127363715074";
	
	private List<AtaxiaListener> listeners = new ArrayList<AtaxiaListener>();
	private long currentDelay = 0L;
	private int processed = 0;
	
	public Map<String, JSONObject> data = new HashMap<>();
	public Map<String, ChatterBotSession> sesh = new HashMap<>();
	public Map<String, Bind> binds = new HashMap<>();
	public IDiscordClient client;
	public MinecraftChatHandler mc;
	public Cooldown cd;

	public static void main(String[] args){
		new Ataxia().start();
	}
	
	@SneakyThrows
	public void ready(){
		//new Timer().scheduleAtFixedRate(new AutoAnnouncer(this), 0L, 1200000L * 2L);
		mc = new MinecraftChatHandler(this);
		mc.register();
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
        log(toReturn);
        return toReturn;
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
	
	public Role getHighestRole(IUser u){
		Role highest = Role.MEMBER;
		for (IRole r : u.getRolesForGuild(client.getGuildByID(GUILD_ID))){
			try {
				if (highest.value(r.getID()).getRank() > highest.getRank()){
					highest = highest.value(r.getID());
				}
			} catch (Exception e){}
		}
		return highest;
	}
	
	@SneakyThrows
	public List<String> getImagesFromSearch(String search, boolean randomImage){
		search = randomImage ? resolveURL("http://imgur.com/random") : "http://www.bing.com/images/search?q=" + search.replace(" ", "%20") + "&go=Search&qs=n&form=QBILPG&pq=cookies&sc=8-7&sp=-1&sk=";
		InputStream input = new URL(search).openStream();
		Document document = new Tidy().parseDOM(input, null);
		NodeList imgs = document.getElementsByTagName("img");
		List<String> srcs = new ArrayList<String>();

		for (int i = 0; i < imgs.getLength(); i++) {
		    srcs.add(imgs.item(i).getAttributes().getNamedItem("src").getNodeValue());
		}
		
		return srcs;
	}
	
	public void addToDiscordQueue(AtaxiaRunnable run){
		currentDelay = processed >= 3 ? System.currentTimeMillis() + 3000L : currentDelay;
		processed = processed >= 3 ? 0 : processed;
		doLater(() -> { try { run.run(); } catch (Exception e){} }, currentDelay > System.currentTimeMillis() ? (currentDelay - System.currentTimeMillis()) : 0);
		processed++;
	}
	
	public void repeat(Runnable run, long initialDelay, long cycle){
		new Timer().scheduleAtFixedRate(new TimerTask(){
			public void run(){
				run.run();
			}
		}, initialDelay, cycle);
	}
	
	public void doLater(Runnable run, long mills){
		new Timer().schedule(new TimerTask(){
			public void run(){
				run.run();
			}
		}, mills);
	}
	
	@SneakyThrows
	public void sendMessage(String message, Channel channel){
		client.getChannelByID(channel.getId()).sendMessage(message);
	}
	
	@SneakyThrows
	public void sendMessage(String message, String channel){
		client.getChannelByID(channel).sendMessage(message);
	}
	
	@SneakyThrows
	public void sendPrivateMessage(String userID, String message){
	    IPrivateChannel channel = client.getOrCreatePMChannel(client.getUserByID(userID));
	    channel.sendMessage(message);
	}
	
	@SneakyThrows
	public void start(){
		log("Starting Ataxia v" + VERSION);
		load();
		client = getClient(LocalData.BOT_TOKEN.getData("keys", this).asString());
		cd = new Cooldown();
		for (AtaxiaListener a : new AtaxiaListener[]{ 
			new GenericListener(this)
		}){
			listeners.add(a);
			client.getDispatcher().registerListener(a);
		}
	}
	
	private void load(){
		for (String folder : new String[] { "./data/", "./data/users/" }){
			JSONParser parser = new JSONParser();
			File path = new File(folder);
			path.mkdirs();
			for (String file : path.list()){
				if (file.endsWith(".json")){
			        try {
			            Object obj = parser.parse(new FileReader(folder + file));
			            JSONObject jsonObject = (JSONObject) obj;
			            data.put((folder.contains("users") ? "users/" : "") + file.replace(".json", ""), jsonObject);
			            if (folder.contains("users")){
			            	binds.put(file.replace(".json", ""), new Bind(file.replace(".json", "")));
			            }
			        } catch (Exception e){
			        	e.printStackTrace();
			        }
				}
			}
		}
		loadBindsFromFile();
	}
	
	public void save(){
		saveBindsToFile();
		for (String player : data.keySet()){
			try (FileWriter file = new FileWriter("./data/" + player + ".json")) {
				file.write(data.get(player).toJSONString());
				file.close();
			} catch (Exception e){
				e.printStackTrace();
			}
		}
	}
	
	public void loadBindsFromFile(){
		for (String user : data.keySet()){
			if (user.startsWith("users/")){
				if (data.get(user).containsKey(LocalData.BINDS.toString())){
					user = user.replace("users/", "").replace(".json", "");
					JSONArray array = (JSONArray) data.get("users/" + user).get(LocalData.BINDS.toString());
					Bind bind = new Bind(user);
					for (Object thing : array){
						String[] spl = ((String) thing).split("%split%");
						bind.addBind(spl[0], spl[1]);
					}
					binds.put(user, bind);
				}
			}
		}
	}
	
	public void saveBindsToFile(){
		for (String userID : binds.keySet()){
			JSONArray array = new JSONArray();
			for (String activator : binds.get(userID).getBinds().keySet()){
				array.add(activator + "%split%" + binds.get(userID).getBinds().get(activator));
			}
			data.get("users/" + userID).put("BINDS", array);
		}
	}
	
	@SneakyThrows
	public static IDiscordClient getClient(String token) { // Returns an instance of the discord client
		 return new ClientBuilder().withToken(token).login();
	}
	
	public void log(String log){
		System.out.println(log);
	}
}