package com.XiaoHuiHui.minecraft.plugin.AutoChat;

import java.util.logging.Level;

import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

// AutoChat插件主类
public class ACMain extends JavaPlugin {
	//事件监听器
	ACListener listener = new ACListener(this);
	//传递给监听器的数据
	ACData data=ACData.initialize(this);
	//命令处理类
	ACCmd cmd=new ACCmd(this);
	//版本号
	public static final String version = "00.03.47";
	
	//getter and setter
	public ACListener getListener() {
		return listener;
	}

	public void setListener(ACListener listener) {
		this.listener = listener;
	}

	public static String getVersion() {
		return version;
	}

	@Override
	public void onEnable() {
		if (!getDataFolder().exists()) {
			getDataFolder().mkdir();
		}
		getServer().getPluginManager().registerEvents(listener, this);
		getServer().getPluginCommand("AutoChat").setExecutor(cmd);
		if(data.load()){
			getLogger().info("插件启动完毕！版本:" +getVersion() + " 制作:小灰灰");
		}else{
			getLogger().log(Level.SEVERE,"插件启动异常！版本:" +getVersion() + " 制作:小灰灰");
		}
		
	}
	@Override
	public void onDisable() {
		HandlerList.unregisterAll(this);
		getLogger().info("插件已经关闭了！版本:" +getVersion() + " 制作:小灰灰");
	}

}