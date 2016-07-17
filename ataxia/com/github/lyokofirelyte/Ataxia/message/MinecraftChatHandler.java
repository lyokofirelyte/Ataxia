package com.github.lyokofirelyte.Ataxia.message;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import com.github.lyokofirelyte.Ataxia.Ataxia;

import lombok.SneakyThrows;

public class MinecraftChatHandler {

	private Socket socket;
	private PrintWriter pw;
	private Ataxia main;
	
	public MinecraftChatHandler(Ataxia i){
		main = i;
	}
	
	public void sendMessage(String message){
		if (pw == null){
			main.log("AAA");
		}
		pw.println(message);
		pw.flush();
	}
	
	@SneakyThrows
	public void register(){
		new Thread(() -> {
			try {
				socket = new Socket("144.76.184.51", 25579);
				main.log("Reading from Minecraft...");
				BufferedReader bis = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				String text = "";
				main.mc.pw = new PrintWriter(socket.getOutputStream());
				try {
					while ((text = bis.readLine()) != null){
						main.log("Received " + text);
						main.sendMessage(text, Channel.MINECRAFT);
					}
					main.log("End of stream");
				} catch (Exception e){
					main.log("End of Minecraft thread.");
				}
			} catch (Exception e){
				main.log("Could not connect to Minecraft.");
			}
		}).start();
	}
}