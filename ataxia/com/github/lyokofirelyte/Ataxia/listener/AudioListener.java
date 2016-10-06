/*package com.github.lyokofirelyte.Ataxia.listener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.lyokofirelyte.Ataxia.Ataxia;
import com.google.common.primitives.Bytes;

import sx.blah.discord.handle.audio.IAudioReceiver;
import sx.blah.discord.handle.obj.IUser;

public class AudioListener implements IAudioReceiver {
	
	public Map<String, byte[]> userBytes = new HashMap<>();
	public Map<String, Long> recording = new HashMap<>();
	public List<String> processing = new ArrayList<String>();
	private Ataxia main;
	
	public AudioListener(Ataxia i){
		main = i;
	}
	
	@Override
	public void receive(byte[] incomingBytes, IUser user) {
		if (!processing.contains(user.getID()) && main.listenFor(user.getID())){
			recording.put(user.getID(), System.currentTimeMillis());
			try {
				if (!userBytes.containsKey(user.getID())){
					userBytes.put(user.getID(), incomingBytes);
				} else {
					byte[] c = Bytes.concat(userBytes.get(user.getID()), incomingBytes);
					userBytes.put(user.getID(), c);
				}
			} catch (Exception e){}
		}
	}
}*/