package com.XiaoHuiHui.minecraft.plugin.AutoChat;

import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class ACListener implements Listener {
	ACData data;
	ACMain main;

	//Constructor
	public ACListener(ACMain main){
		super();
		this.main=main;
		try {
			data=ACData.getInstance();
		} catch (Exception e) {
			data=ACData.initialize(main);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onChat(PlayerCommandPreprocessEvent event) {
		Player player=event.getPlayer();
		String msg=event.getMessage();
		msg=cmdCheck(msg,player);
		event.setMessage(msg);
		return;
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onChat(AsyncPlayerChatEvent event) {
		if(event.isCancelled()){
			return;
		}
		Player player=event.getPlayer();
		String msg=event.getMessage();
		if(data.isUseChar()){
			if(msg.charAt(0)!=data.getCharactor()){
				return;
			}
			msg=msg.substring(1, msg.length());
		}
		if(data.hasPermission()){
			if(player.hasPermission(data.getPermission())){
				return;
			}
		}
		Map<String,String> map=data.getMsgs();
		Set<String> set=map.keySet();
		String value=null;
		for(String s:set){
			if(msg.contains(s)){
				value=map.get(s);
				break;
			}
		}
		if(value==null){
			return;
		}
		if(value.charAt(0)=='/'){
			value=value.substring(1, value.length());
			value=value.replace("@p", player.getName());
			runAsCommand(value);
		}else{
			String name=data.getName();
			name=name.replace('&', '§');
			value=value.replace('&', '§');
			runAsMessage(name+value,player,data.isPublic());
		}
	}

	//如果是命令的话，会执行这个方法
	private String cmdCheck(String msg, Player player) {
		if(data.isReplaceCommand()){
			String temp1[]=msg.split(" ");
			String temp2=temp1[0];
			temp2=temp2.substring(1, temp2.length());
			String temp3=data.getCmds().get(temp2);
			if(temp3!=null){
				msg=msg.replaceFirst(temp2, temp3);
			}
		}
		return msg;
	}

	//如果是消息的话 会执行这个方法
	private void runAsMessage(final String value,final Player player,final boolean isPublic) {
		/* 因为最早用户反映这里机器人说话比人早，所以延迟0.2秒
		  * 不安全的延迟算法：
		  * Thread.sleep(200);
		  * 安全的延迟算法如下：
		  */
		new BukkitRunnable(){
			public void run(){
				if(isPublic){
					main.getServer().broadcastMessage(value);
				}else{
					player.sendMessage(value);
				}
			}
		/* 注意这里是tick！！刚开始我以为单位是毫秒我写了ms
		  * 20tick=1s，1000ms=1s
		  */
		}.runTaskLater(main, data.getTick());
	}

	//如果是命令的话 会执行这个方法
	private void runAsCommand(String value) {
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), value);
	}
}