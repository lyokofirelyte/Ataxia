package com.github.lyokofirelyte.Ataxia;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.SneakyThrows;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;

import com.github.lyokofirelyte.Ataxia.message.Channel;
import com.google.code.chatterbotapi.ChatterBotSession;

public class Ataxia {
	
	private static String version = "1.0";
	private List<AtaxiaListener> listeners = new ArrayList<AtaxiaListener>();
	
	public Map<String, JSONObject> data = new HashMap<>();
	public Map<String, ChatterBotSession> sesh = new HashMap<>();
	public IDiscordClient client;

	public static void main(String[] args){
		new Ataxia().start();
	}
	
	@SneakyThrows
	public void sendMessage(String message, Channel channel){
		client.getChannelByID(channel.getId()).sendMessage(message);
	}
	
	@SneakyThrows
	public void start(){
		log("Starting Ataxia v" + version);
		load();
		client = getClient(LocalData.BOT_TOKEN.getData("keys", this).asString());
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