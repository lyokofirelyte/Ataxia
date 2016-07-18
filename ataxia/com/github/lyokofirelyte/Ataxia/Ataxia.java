package com.github.lyokofirelyte.Ataxia;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.github.lyokofirelyte.Ataxia.cooldown.Cooldown;
import com.github.lyokofirelyte.Ataxia.message.Channel;
import com.github.lyokofirelyte.Ataxia.message.MinecraftChatHandler;
import com.google.code.chatterbotapi.ChatterBotSession;

import lombok.SneakyThrows;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IPrivateChannel;

public class Ataxia {
	
	private static String version = "1.0";
	private List<AtaxiaListener> listeners = new ArrayList<AtaxiaListener>();
	
	public Map<String, JSONObject> data = new HashMap<>();
	public Map<String, ChatterBotSession> sesh = new HashMap<>();
	public Map<String, Map<Integer, Long>> cooldowns = new HashMap<>();
	public IDiscordClient client;
	public MinecraftChatHandler mc;
	public Cooldown cd;

	public static void main(String[] args){
		new Ataxia().start();
	}
	
	public void ready(){
		new Timer().scheduleAtFixedRate(new AutoAnnouncer(this), 0L, 1200000L * 2L);
		mc = new MinecraftChatHandler(this);
		mc.register();
	}
	
	@SneakyThrows
	public void sendMessage(String message, Channel channel){
		client.getChannelByID(channel.getId()).sendMessage(message);
	}
	
	@SneakyThrows
	public void sendPrivateMessage(String userID, String message){
	    IPrivateChannel channel = client.getOrCreatePMChannel(client.getUserByID(userID));
	    channel.sendMessage(message);
	}
	
	@SneakyThrows
	public void start(){
		log("Starting Ataxia v" + version);
		load();
		client = getClient(LocalData.BOT_TOKEN.getData("keys", this).asString());
		cd = new Cooldown(this);
		for (AtaxiaListener a : new AtaxiaListener[]{ 
			new GenericListener(this)
		}){
			listeners.add(a);
			client.getDispatcher().registerListener(a);
		}
	}
	
	private void load(){
		JSONParser parser = new JSONParser();
		File path = new File("./data/");
		path.mkdirs();
		
		for (String file : path.list()){
			if (file.endsWith(".json")){
		        try {
		            Object obj = parser.parse(new FileReader("./data/" + file));
		            JSONObject jsonObject = (JSONObject) obj;
		            data.put(file.replace(".json", ""), jsonObject);
		        } catch (Exception e){
		        	e.printStackTrace();
		        }
			}
		}
	}
	
	public void save(){
		for (String player : data.keySet()){
			try (FileWriter file = new FileWriter("./data/" + player + ".json")) {
				file.write(data.get(player).toJSONString());
				file.close();
			} catch (Exception e){
				e.printStackTrace();
			}
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