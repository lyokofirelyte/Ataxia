package com.github.lyokofirelyte.Ataxia.message;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import lombok.SneakyThrows;

import com.github.lyokofirelyte.Ataxia.Ataxia;

public class MinecraftChatHandler {

	private ServerSocket listener;
	private Ataxia main;
	
	public MinecraftChatHandler(Ataxia i){
		main = i;
	}
	
	@SneakyThrows
	public void register(){
		listener = new ServerSocket(25570);
		new Thread(() -> {
			while (true){
				try {
					Socket minecraftServer = listener.accept();
					main.log("Connection success, chats linked!");
					PrintWriter pw = new PrintWriter(minecraftServer.getOutputStream());
					pw.println("chat system Linked to discord!");
					BufferedReader bis = new BufferedReader(new InputStreamReader(minecraftServer.getInputStream()));
					new Thread(() -> {
						String text = "";
						try {
							while ((text = bis.readLine()) != null){
								
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}).start();
				} catch (Exception e){
					main.log("There was an error linking the chats.");
				}
			}
		}).start();
	}
}