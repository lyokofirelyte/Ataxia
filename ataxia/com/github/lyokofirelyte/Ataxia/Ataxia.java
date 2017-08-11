package com.github.lyokofirelyte.Ataxia;

import java.io.BufferedReader;
import java.io.Console;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
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

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioSystem;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.tritonus.share.sampled.file.TAudioFileFormat;
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
import com.github.lyokofirelyte.Ataxia.message.Voice_Channel;
import com.google.code.chatterbotapi.ChatterBotSession;
import com.jcabi.ssh.Shell.Plain;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import lombok.SneakyThrows;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IPrivateChannel;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.RequestBuffer;
import sx.blah.discord.util.audio.AudioPlayer;

public class Ataxia {
	
	public static String VERSION = "1.0";
	public static String GUILD_ID = "199663127363715074";
	
	private List<AtaxiaListener> listeners = new ArrayList<AtaxiaListener>();
	private long currentDelay = 0L;
	private long startTime = 0;
	private int processed = 0;
	
	public List<String> savedMessages = new ArrayList<String>();
	public Map<String, JSONObject> data = new HashMap<>();
	public Map<String, ChatterBotSession> sesh = new HashMap<>();
	public Map<String, Bind> binds = new HashMap<>();
	public List<String> audioListeners = new ArrayList<String>();
	public IDiscordClient client;
	//public MinecraftChatHandler mc;
	//public AudioListener al;
	public Cooldown cd;
	public Plain plain;
	public IMessage queueMessage;
	

	public Map<String, String> queue = new HashMap<>();
	public Map<Integer, String> queueOrder = new HashMap<>();

	public static void main(String[] args){
		if (args.length > 0 && args[0].equalsIgnoreCase("sftp")){
			new Ataxia().sftp("ataxia_sftp.jar");
		} else {
			new Ataxia().start();
		}
	}
	
	@SneakyThrows
	public void ready(){
		//mc = new MinecraftChatHandler(this);
		//mc.register();
		//client.getVoiceChannelByID(Voice_Channel.TIKI_LOUNGE.getId()).join();
		//al = new AudioListener(this);
		//client.getGuildByID(GUILD_ID).getAudioManager().subscribeReceiver(al);
		//new Timer().scheduleAtFixedRate(new ProcessTask(this), 0L, 400L);
		//Shell shell = new SSH("worldscolli.de", 22, "wa", LocalData.SSH_PASSWORD.getData("keys", this).asString());
		//plain = new Shell.Plain(shell);
		sendMessage("All loaded up. (Took " + ((System.currentTimeMillis() - startTime) / 1000) + " seconds)", Channel.TIKI_LOUNGE);
		queueMessage = sendMessage("```xl\nAtaxia Music Queue (Updated Automatically)```\n" + 
		"**!ax:queue <song name>** `!ax:queue fireflies owl city`\n" +
		"**!ax:queue <direct link>** `!ax:queue https://www.youtube.com/watch?v=QP5fKMme2NU`\n" +
		"**!ax:queue remove**\n```xl\nNow Playing: [ nothing ]```\n%q%", Channel.MUSIC.getId());
		String extra = "";
		for (int i = 0; i < 10; i++){
			extra += "**" + (i < 10 ? "0" + i : i) + "** `Song, User, Status`\n\n";
		}
		extra += "\n```xl\nQueue Idle```";
		queueMessage.edit(queueMessage.getContent().replace("%q%", extra));
	}
	
	public File lastFileModified(String dir) {
	    File fl = new File(dir);
	    File[] files = fl.listFiles(new FileFilter() {          
	        public boolean accept(File file) {
	            return file.isFile();
	        }
	    });
	    long lastMod = Long.MIN_VALUE;
	    File choice = null;
	    for (File file : files) {
	        if (file.lastModified() > lastMod) {
	            choice = file;
	            lastMod = file.lastModified();
	        }
	    }
	    return choice;
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
	
	/**
	 * @author https://stackoverflow.com/questions/14830146/how-to-transfer-a-file-through-sftp-in-java
	 * @param fileName
	 */
	@SneakyThrows
	public void sftp(String fileName) {
		Console console = System.console();
        String SFTPHOST = "trey.tikilounge.co";
        int SFTPPORT = 22;
        String SFTPUSER = console.readLine("Username: ");
        char[] SFTPPASS = console.readPassword("Password: ");
        String SFTPWORKINGDIR = "/home/david/ataxia";
        Session session = null;
        com.jcraft.jsch.Channel channel = null;
        ChannelSftp channelSftp = null;
        String pass = "";
        for (char c : SFTPPASS){
        	pass += (c + "");
        }
        try {
            JSch jsch = new JSch();
            session = jsch.getSession(SFTPUSER, SFTPHOST, SFTPPORT);
            session.setPassword(pass);
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.connect();
            System.out.println("Host connected.");
            channel = session.openChannel("sftp");
            channel.connect();
            System.out.println("sftp channel opened and connected.");
            channelSftp = (ChannelSftp) channel;
            channelSftp.cd(SFTPWORKINGDIR);
            File f = new File(fileName);
            channelSftp.put(new FileInputStream(f), f.getName());
            System.out.println("File transfered successfully to host.");
        } catch (Exception ex) {
             System.out.println("Exception found while tranfer the response.");
        }
        finally{
            channelSftp.exit();
            System.out.println("sftp Channel exited.");
            channel.disconnect();
            System.out.println("Channel disconnected.");
            session.disconnect();
            System.out.println("Host Session disconnected.");
        }
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
	
	public Timer repeat(Runnable run, long initialDelay, long cycle){
		Timer t = new Timer();
		t.scheduleAtFixedRate(new TimerTask(){
			public void run(){
				run.run();
			}
		}, initialDelay, cycle);
		return t;
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
	public IMessage sendMessage(String message, String channel){
		return client.getChannelByID(channel).sendMessage(message);
	}
	
	@SneakyThrows
	public void sendPrivateMessage(String userID, String message){
	    IPrivateChannel channel = client.getOrCreatePMChannel(client.getUserByID(userID));
	    channel.sendMessage(message);
	}
	
	public boolean listenFor(String id){
		return audioListeners.contains(id);
	}
	
	@SneakyThrows
	public void start(){
		startTime = System.currentTimeMillis();
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
	
	@SneakyThrows
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
		client.getVoiceChannelByID(Voice_Channel.TIKI_LOUNGE.getId()).leave();
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
	
	@SneakyThrows
	public String[] getAudioDuration(File file){
	    AudioFileFormat fileFormat = AudioSystem.getAudioFileFormat(file);
	    if (fileFormat instanceof TAudioFileFormat) {
	        Map<?, ?> properties = ((TAudioFileFormat) fileFormat).properties();
	        String key = "duration";
	        Long microseconds = (Long) properties.get(key);
	        int mili = (int) (microseconds / 1000);
	        int sec = (mili / 1000) % 60;
	        int min = (mili / 1000) / 60;
	        return new String[]{min + ":" + sec, mili + ""};
	    }
	    return new String[]{ "N/A", "0" };
	}
	
	public void loadBindsFromFile(){
		for (String user : data.keySet()){
			if (user.startsWith("users/")){
				if (data.get(user).containsKey(LocalData.LISTEN.toString())){
					if (data.get(user).get(LocalData.LISTEN.toString()).equals("yes")){
						audioListeners.add(user.replace("users/", ""));
					}
				}
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
	
	public String restOfString(String[] args, int start){
		String fin = "";
		for (int i = start; i < args.length; i++){
			fin += fin.equals("") ? args[i] : " " + args[i];
		}
		return fin;
	}
	
	public void saveBindsToFile(){
		for (String userID : binds.keySet()){
			JSONArray array = new JSONArray();
			for (String activator : binds.get(userID).getBinds().keySet()){
				array.add(activator + "%split%" + binds.get(userID).getBinds().get(activator));
			}
			data.get("users/" + userID).put("BINDS", array);
		}
		for (String id : audioListeners){
			data.get("users/" + id).put("LISTEN", "yes");
		}
		List<String> toChange = new ArrayList<String>();
		for (String user : data.keySet()){
			if (user.startsWith("users/")){
				if (!audioListeners.contains(user.replace("users/", ""))){
					toChange.add(user);
				}
			}
		}
		for (String user : toChange){
			data.get(user).remove("LISTEN");
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