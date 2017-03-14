package com.XiaoHuiHui.minecraft.plugin.AutoChat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

/*插件主体、命令处理类、事件监听器之间的信息交互使用本类对象
 * 采用信息独立机制，即本类专门负责与config.yml的数据交互
 * 所有数据设置在这里后统一写入
 * 并统一在这里读取，已保证时效性
 */
public class ACData {
	//开启单例模式！
	static ACData data=new ACData();
	//有没有初始化的flag
	static boolean flag=false;
	public static ACData getInstance() throws Exception{
		if(!flag)throw new Exception("你丫还没初始化呢！！！！");
		return data;
	}
	//真·Constructor
	public static ACData initialize(ACMain main) {
		data.main=main;
		data.configFile=new File(main.getDataFolder(),"config.yml");
		flag=true;
		return data;
	}
	//配置文件要求的版本
	public static final String configVersion="1.2";
	//是否使用首字符验证，只有以变量c开头的字符串会被机器人识别
	boolean isChar;
	//首字符验证使用的字符
	char c;
	//是否开启忽略权限，具有权限的玩家消息被机器人忽略
	boolean hasPermission;
	//忽略权限的权限名称，使用 插件.权限 的格式
	String permission;
	//是否开启公屏模式，即机器人的消息全部显示在公屏
	boolean isPublic;
	//字段，所有配置中配置好的字段都将存入这里，但未做处理
	List<String> Messages;
	//字段，所有配置中配置好的字段都将转换后存入这里
	Map<String,String> msgs=new HashMap<String,String>();
	//机器人发送消息时显示的名称
	String name;
	//机器人回答的延迟时间
	long tick;
	//是否开启命令替换功能
	boolean isReplaceCommand;
	//命令替换功能的字段组
	Map<String,String> cmds=new HashMap<String,String>();

	ACMain main;
	/* 配置文件路径，因为主类的saveConfig()不抛错，
	  * 所以只能写点多余的辣鸡代码让它抛错，这才是我的风格！
	  * 这个对象就是多余的，因为还需要重新定义
	  * 本来主类定义了的，但是用的private
	  * 我真是x了狗了(误)
	  */
	File configFile;
	FileConfiguration config;
	
	//Constructor
	private ACData(){
	}

	//getter and setter
	public boolean isUseChar() {
		return isChar;
	}

	public void setUseChar(boolean isChar) {
		this.isChar = isChar;
	}

	public char getCharactor() {
		return c;
	}

	public void setCharactor(char c) {
		this.c = c;
	}

	public boolean hasPermission() {
		return hasPermission;
	}

	public void setHasPermission(boolean hasPermission) {
		this.hasPermission = hasPermission;
	}

	public String getPermission() {
		return permission;
	}

	public void setPermission(String permission) {
		this.permission = permission;
	}

	public boolean isPublic() {
		return isPublic;
	}

	public void setIsPublic(boolean isPublic) {
		this.isPublic = isPublic;
	}

	public List<String> getMessages() {
		return Messages;
	}

	public void setMessages(List<String> messages) {
		Messages = messages;
	}

	public Map<String,String> getMsgs() {
		return msgs;
	}

	public void setMsgs(Map<String,String> msgs) {
		this.msgs = msgs;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public long getTick() {
		return tick;
	}
	
	public void setTick(long tick) {
		this.tick = tick;
	}
	
	public boolean isReplaceCommand() {
		return isReplaceCommand;
	}
	
	public void setReplaceCommand(boolean isReplaceCommand) {
		this.isReplaceCommand = isReplaceCommand;
	}
	
	public Map<String, String> getCmds() {
		return cmds;
	}
	
	public void setCmds(Map<String, String> cmds) {
		this.cmds = cmds;
	}
	
	public static String getConfigversion() {
		return configVersion;
	}

	//分割获取的字段数据
	private void split() {
		List<String> temp1=getMessages();
		for(int i=0;i<temp1.size();i++){
			String temp2[]=temp1.get(i).split("——");
			if(temp2.length==1)
				throw new RuntimeException("字段信息有误！错误行数："+(i+1)+"未检测到\"——\"！");
			String temp3[]=temp2[0].split(",");
			if(temp3.length==1){
				msgs.put(temp2[0], temp2[1]);
			}else{
				for(int i1=0;i1<temp3.length;i1++){
					msgs.put(temp3[i1], temp2[1]);
				}
			}
		}
	}

	//读取config的数据，出问题就抛异常
	private void load0() throws Exception{
		main.saveDefaultConfig();
		main.reloadConfig();
		config=main.getConfig();
		String version=config.getString("Version");
		if(version==null)version=config.getString("version");
		if(!verifyVersion(version)){
			updateVersion(version);
		}
		setName(config.getString("name"));
		setUseChar(config.getBoolean("isChar"));
		setCharactor(config.getString("char").charAt(0));
		setHasPermission(config.getBoolean("hasPermission"));
		setPermission(config.getString("permission"));
		setIsPublic(config.getBoolean("isPublic"));
		setTick(config.getLong("tick"));
		setMessages(config.getStringList("field"));
		split();
		setReplaceCommand(config.getBoolean("replaceCommand.enable"));
		loadReplaceCmds();
	}
	
	//自动更新配置文件
	private void updateVersion(String version)throws Exception {
		main.getLogger().info("config.yml版本已过时，尝试自动更新成新版本...");
		if(version==null){
			throw new IllegalArgumentException("config.yml版本过旧！无法自动更新！请手动更新为新版本！");
		}
		long tick=4L;
		switch(version){
		case "1.1":
			tick=config.getLong("Tick");
		case "1.0":
			String name=config.getString("Name");
			boolean isChar=config.getBoolean("IsChar");
			char c=config.getString("Char").charAt(0);
			boolean hasPermission=config.getBoolean("HasPermission");
			String permission=config.getString("Permission");
			boolean isPublic=config.getBoolean("IsPublic");
			List<String> messages=config.getStringList("Field");
			try{
				configFile.delete();
				main.saveDefaultConfig();
				main.reloadConfig();
				config=main.getConfig();
				config.set("name", name);
				config.set("isChar", isChar);
				config.set("char",c);
				config.set("hasPermission",hasPermission);
				config.set("permission",permission);
				config.set("isPublic",isPublic);
				config.set("tick",tick);
				config.set("field", messages);
				config.save(configFile);
			}catch (IOException e) {
				throw new IllegalArgumentException("改动配置文件出错！请确定参数无误，磁盘未满且未被写保护！");
			}
		}
		main.getLogger().info("config.yml更新完毕！");
	}
	
	//读取替换命令字段列表的方法
	private void loadReplaceCmds() {
		if(isReplaceCommand()){
			List<String> temp1=config.getStringList("replaceCommand.list");
			for(int i=0;i<temp1.size();i++){
				String temp2=temp1.get(i);
				String temp3[]=temp2.split(":");
				if(temp3.length<2){
					throw new IllegalArgumentException("命令转换列表有误！请检查配置！");
				}
				cmds.put(temp3[0], temp3[1]);
			}
		}
	}

	//检查配置文件的版本
	private boolean verifyVersion(String version) {
		if(version==null)return false;
		return version.equalsIgnoreCase(getConfigversion());
	}

	//封装异常，更友好的显示，有参数表示控制台或者玩家执行命令
	public boolean load(CommandSender sender) {
		try{
			load0();
		}catch (Exception e) {
			sender.sendMessage("§4读取配置文件信息出错！请结合错误代码进行问题判断！");
			sender.sendMessage(e.getMessage());
			e.printStackTrace();
			return false;
		}
		sender.sendMessage("§2读取配置文件信息成功！");
		return true;
	}

	//封装异常，更友好的显示，无参表示系统调用
	public boolean load() {
		try{
			load0();
		}catch (Exception e) {
			main.getLogger().log(Level.SEVERE,"读取配置文件信息出错！请结合错误代码进行问题判断！");
			main.getServer().getPluginManager().disablePlugin(main);
			e.printStackTrace();
			return false;
		}
		main.getLogger().info("读取配置文件信息成功！");
		return true;
	}

	//写到config.yml
	private void write0(){
		split();
		main.saveDefaultConfig();
		config.set("name", getName());
		config.set("isChar", isUseChar());
		config.set("char",getCharactor());
		config.set("hasPermission",hasPermission());
		config.set("permission",getPermission());
		config.set("isPublic",isPublic());
		config.set("tick",getTick());
		config.set("field", getMessages());
		config.set("replaceCommand.enable", isReplaceCommand());
		writeReplaceCmds();
	}
	
	//写入替换命令字段列表的方法
	private void writeReplaceCmds() {
		if(isReplaceCommand()){
			Map<String,String> temp1=getCmds();
			List<String> temp2=new ArrayList<String>();
			Set<String> temp3=temp1.keySet();
			for(String s:temp3){
				String temp4=temp1.get(s);
				temp2.add(s+":"+temp4);
			}
			config.set("replaceCommand.list", temp2);
		}
	}
	
	//写入之后保存，封装异常，友好的显示，无参表示系统调用
	public boolean write(){
		write0();
		main.getLogger().log(Level.WARNING,"配置文件的所有注解将会清除！");
		try{
			config.save(configFile);
		}catch(IOException e){
			main.getLogger().log(Level.SEVERE,"改动配置文件出错！请确定参数无误，磁盘未满且未被写保护！");
			e.printStackTrace();
			return false;
		}
		main.getLogger().info("已成功改动配置文件！");
		return true;
	}

	//写入之后保存，封装异常，友好的显示，有参数表示控制台或者玩家执行命令
	public boolean write(CommandSender sender){
		write0();
		sender.sendMessage("§4配置文件的所有注解将会清除！");
		try{
			config.save(configFile);
		}catch(IOException e){
			sender.sendMessage("§4改动配置文件出错！请确定参数无误，磁盘未满且未被写保护！");
			sender.sendMessage(e.getMessage());
			e.printStackTrace();
			return false;
		}
		sender.sendMessage("§2已成功改动配置文件！");
		return true;
	}
}
