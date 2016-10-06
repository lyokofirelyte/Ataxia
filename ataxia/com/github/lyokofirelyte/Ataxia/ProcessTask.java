/*package com.github.lyokofirelyte.Ataxia;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.TimerTask;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import com.github.lyokofirelyte.Ataxia.data.LocalData;
import com.github.lyokofirelyte.Ataxia.listener.MessageListener;
import com.github.lyokofirelyte.Ataxia.message.Channel;
import com.google.code.chatterbotapi.ChatterBot;
import com.google.code.chatterbotapi.ChatterBotFactory;
import com.google.code.chatterbotapi.ChatterBotSession;
import com.google.code.chatterbotapi.ChatterBotType;
import com.ibm.watson.developer_cloud.http.ServiceCall;
import com.ibm.watson.developer_cloud.speech_to_text.v1.SpeechToText;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.RecognizeOptions;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.RecognizeOptions.Builder;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.SpeechAlternative;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.SpeechResults;

import lombok.SneakyThrows;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.IVoiceChannel;

public class ProcessTask extends TimerTask {
	
	private Ataxia main;
	String host = "speech.googleapis.com";
	int port = 443;
	int sampling = 44100;
	
	public ProcessTask(Ataxia i){
		main = i;
	}

	@Override
	public void run(){
		for (String user : main.audioListeners){
			if (main.al.recording.containsKey(user)){
				if (main.al.recording.get(user) < System.currentTimeMillis() - 500L){
					main.al.recording.remove(user);
					if (!main.al.processing.contains(user)){
						processFor(user, main.al.userBytes.get(user));
						main.al.processing.add(user);
					}
				}
			}
		}
	}
	
	public static ByteArrayOutputStream saveWavByteArrayOutputStream(byte[] byte_array, AudioFormat audioFormat) {
		ByteArrayOutputStream baos = null;
		try {
			long length = (long)(byte_array.length / audioFormat.getFrameSize());
			ByteArrayInputStream bais = new ByteArrayInputStream(byte_array);
			AudioInputStream audioInputStreamTemp = new AudioInputStream(bais, audioFormat, length);
			AudioFileFormat.Type fileType = AudioFileFormat.Type.WAVE;
			if (AudioSystem.isFileTypeSupported(fileType, audioInputStreamTemp)) {
				baos = new ByteArrayOutputStream();
				AudioSystem.write(audioInputStreamTemp, fileType, baos);
			}
		} catch(Exception e) { }
		return baos;
	}
	
	@SneakyThrows
	public void processFor(String ID, byte[] bytes){
		main.log(ID + " bytes " + bytes.length + "");
		if (bytes.length >= 84000 && bytes.length <= 2000000){
			new Thread(() -> {
				try {
					FileOutputStream fos = new FileOutputStream("./data/pcm_" + ID + ".pcm");
					fos.write(bytes);
					fos.close();
					Process p = Runtime.getRuntime().exec("C:/Users/David/Desktop/Assets/sox-14-4-2/sox -t raw -b 16 -e signed-integer -B -c1 -r 16k " + "./data/pcm_" + ID + ".pcm ./data/pcm_" + ID + ".wav speed 6");
					p.waitFor();
					//main.al.userBytes.remove(ID);
					
					//// GOOGLE
					p = Runtime.getRuntime().exec("C:/Program Files/Java/jdk1.8.0_73/bin/java.exe -cp grpc.jar com.examples.cloud.speech.SyncRecognizeClient --host=speech.googleapis.com --port=443 --uri=./data/pcm_" + ID + ".wav --sampling=16000");

					BufferedReader in = new BufferedReader(new InputStreamReader(p.getErrorStream()));
					String line;
					List<String> results = new ArrayList<String>();
					while ((line = in.readLine()) != null){
						results.add(line);
						//main.log(line);
					}
					p.waitFor();
					in.close();
					
					String message = "";
					
					for (String result : results){
						if (result.contains("transcript:")){
							message = result.replace("transcript: ", "").replace("\"", "");
							break;
						}
					}
					
					SpeechToText service = new SpeechToText();
					service.setUsernameAndPassword(LocalData.IBM_USERNAME.getData("keys", main).asString(), LocalData.IBM_PASSWORD.getData("keys", main).asString());
				    ServiceCall<SpeechResults> transcript =  service.recognize(new File("./data/pcm_" + ID + ".wav"));
				    SpeechResults r = transcript.execute();
				   	SpeechAlternative alt = r.getResults().get(0).getAlternatives().get(0);
				   	String message = alt.getTranscript().toLowerCase();
					boolean didSomething = false;
					
					if (!message.equals("")){
						//main.sendMessage("You probably said " + message + ", <@" + ID + ">", Channel.TIKI_LOUNGE);
						main.log(message);
						if (message.startsWith("text") || message.contains("taxi") || message.contains("ataxia") || message.contains("sexy") || message.contains("a taxi") || message.contains("a taxia")){ // respond
							
							List<String> pictureSyns = new ArrayList<String>(Arrays.asList(
								"avatar", "profile", "picture", "pic"
							));
							
							List<String> moveSyns = new ArrayList<String>(Arrays.asList(
								"move", "transfer"
							));
							
							String[] spl = message.split(" ");
							
							for (String m : spl){
								if (m.equalsIgnoreCase("also")){
									
								}
							}
							
							String[] returnedArgs = searchForCommand(spl, pictureSyns);
							List<String> allVoiceChannels = new ArrayList<String>();
							
							for (IVoiceChannel channel : main.client.getGuildByID(Ataxia.GUILD_ID).getVoiceChannels()){
								allVoiceChannels.add(channel.getName());
							}
							
							if (!returnedArgs[0].equals("none")){
								List<IUser> calledUsers = findUsers(returnedArgs, ID);
								for (IUser u : calledUsers){
									main.addToDiscordQueue(() -> { // Just in case there's a shit load.
										new MessageListener(main, u, new String[]{ "!ax:pic", u.getName() }, Channel.TIKI_LOUNGE.getId()).onPic();
									});
								}
								if (calledUsers.size() <= 0){
									main.sendMessage("I couldn't find the avatar you requested, <@" + ID + ">", Channel.TIKI_LOUNGE.getId());
								}
								didSomething = true;
							}
							
							if (!didSomething){
								returnedArgs = searchForCommand(spl, moveSyns);
								if (!returnedArgs[0].equals("none")){
									returnedArgs = searchForCommand(spl, new ArrayList<String>(Arrays.asList("channel")));
									if (!returnedArgs[0].equals("none")){
										IVoiceChannel moveTo = findChannel(returnedArgs);
										if (moveTo != null){
											returnedArgs = searchForCommand(returnedArgs, allVoiceChannels);
											List<IUser> calledUsers = findUsers(returnedArgs, ID);
											for (IUser u : calledUsers){
												if (u.getConnectedVoiceChannels().size() > 0){
													main.addToDiscordQueue(() -> { 
														u.moveToVoiceChannel(moveTo);
													});
												} else {
													main.sendMessage("<@" + ID + "> " + u.getName() + " can't be moved at this time.", Channel.TIKI_LOUNGE);
												}
											}
											if (calledUsers.size() <= 0){
												main.sendMessage("I couldn't find anyone to move, <@" + ID + ">", Channel.TIKI_LOUNGE.getId());
											}
											didSomething = true;
										}
									}
								}
							}
							
							if (!didSomething){
								ChatterBotFactory factory = new ChatterBotFactory();
								ChatterBot bot = factory.create(ChatterBotType.CLEVERBOT);
								if (!main.sesh.containsKey(ID)){
									main.sesh.put(ID, bot.createSession(Locale.ENGLISH));
								}
								ChatterBotSession sesh = main.sesh.get(ID);
								String response = sesh.think(message.toString());
								main.sendMessage("<@" + ID + "> " + response, Channel.TIKI_LOUNGE);
							}
						}
						
					} else {
						//main.sendMessage("I'm not sure what you said, <@" + ID + ">", Channel.TIKI_LOUNGE);
					}
				} catch (Exception e){
					e.printStackTrace();
				}
				main.al.processing.remove(ID);
			}).start();
		}
	}
	
	private String[] searchForCommand(String[] args, List<String> command){
		for (int i = 0; i < args.length; i++){
			if (command.contains(args[i])){
				String[] toReturn = new String[args.length - (i+1)];
				for (int x = 0; x < toReturn.length; x++){
					toReturn[x] = args[x + i + 1];
				}
				return toReturn;
			}
		}
		return new String[]{ "none" };
	}
	
	private IVoiceChannel findChannel(String[] args){
		for (String m : args){
			for (IVoiceChannel u : main.client.getVoiceChannels()){
				if (u.getName().equalsIgnoreCase(m) || (m.length() >= 4 && u.getName().toLowerCase().contains(m.toLowerCase()))){
					return u;
				}
			}
		}
		return null;
	}
	
	private List<IUser> findUsers(String[] args, String ID){
		List<IUser> ul = new ArrayList<IUser>();
		for (String m : args){
			for (IUser u : main.client.getUsers()){
				if (u.getName().equalsIgnoreCase(m) || (m.length() >= 4 && u.getName().toLowerCase().contains(m.toLowerCase()))){
					ul.add(u);
				}
			}
			if (m.equals("me") || m.equals("myself")){
				ul.add(main.client.getUserByID(ID));
			}
		}
		return ul;
	}
}*/