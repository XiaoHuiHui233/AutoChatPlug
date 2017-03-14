package com.XiaoHuiHui.minecraft.plugin.AutoChat;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ACCmd implements CommandExecutor{
	ACMain main;
	ACData data;
	
	//Constructor
	public ACCmd(ACMain main) {
			this.main=main;
			try {
				this.data=ACData.getInstance();
			} catch (Exception e) {
				data=ACData.initialize(main);
			}
	 }

	//设置配置项，对应命令的setflag
	private void setflag(String arg1, String arg2, CommandSender sender) {
		String temp=arg1.toLowerCase();
		boolean b=Boolean.parseBoolean(arg2);
		switch(temp){
		case "ischar":
			data.setUseChar(b);
			break;
		case "haspermission":
			data.setHasPermission(b);
			break;
		case "ispublic":
			data.setIsPublic(b);
			break;
		case "replacecommand.enable":
			data.setReplaceCommand(b);
			break;
		case "permission":
			data.setPermission(arg2);
			break;
		case "char":
			data.setCharactor(arg2.charAt(0));
			break;
		case "name":
			data.setName(arg2);
			break;
		case "tick":
			long l;
			try{
				l=Long.parseLong(arg2);
			}catch(NumberFormatException e){
				sender.sendMessage(arg2+"§4不是有效数字！");
				return;
			}
			if(l<=0){
				sender.sendMessage(l+"§4不在合法的区间内！该数值必须大于0！");
				return;
			}
			data.setTick(l);
		default:
			sender.sendMessage("§4无效的配置项！");
			return;
		}
		data.write(sender);
	}

	//添加一个字段，对应命令的add
	private void add(String arg1, String arg2, CommandSender sender) {
		arg1=arg1.replace("%_"," ");
		arg2=arg2.replace("%_"," ");
		data.getMessages().add(arg1+"——"+arg2);
		data.write(sender);
	}
	
	//添加一个命令替换的字段，对应命令的addcmds
	private void addCmds(String arg1, String arg2, CommandSender sender) {
		if(!data.isReplaceCommand()){
			sender.sendMessage("§4命令替换功能暂未开启！请先开启该功能！");
			sender.sendMessage("§4可以使用/autoc setflag replacecommand.enable true命令开启该功能！");
			return;
		}
		data.getCmds().put(arg1,arg2);
		data.write(sender);
	}

	//删除一个字段，对应命令的remove
	private void remove(String arg, CommandSender sender) {
		int i;
		try{
			i= Integer.parseInt(arg);
		}catch(NumberFormatException e){
			sender.sendMessage(arg+"§4不是有效数字！");
			return;
		}
		try{
			data.getMessages().remove(i-1);
		}catch(IndexOutOfBoundsException e){
			sender.sendMessage(i
					+"§4不在合法的区间内！该数值必须大于0并且小于或等于当前字段数："
					+data.getMessages().size());
			return;
		}
		data.write(sender);
	}

	//列出所有字段，对应命令的list
	private void list(CommandSender player) {
		List<String> temp2=data.getMessages();
		int temp1=temp2.size();
		for (int i = 0; i < temp1; i++)
			player.sendMessage("§a" + (i + 1) + "§f:§2" + temp2.get(i));
	}

	//帮助
	private void help(CommandSender player) {
		player.sendMessage("§a/autochat list查看关键字列表");
		player.sendMessage("§a/autochat add <Field> <Message>设置关键字以及对应的消息");
		player.sendMessage("§a使用/autochat add添加字段时空格由%_替换，可以用于关键字和对应的消息或命令");
		player.sendMessage("§a/autochat remove <Index>删除列表中第Index个关键字和消息");
		player.sendMessage("§a/autochat setflag <Flag> <Value>设置配置文件config.yml中某Flag的值");
		player.sendMessage("§a/autochat addcmds <NewCmd> <OldCmd>添加一个命令替换，把/NewCmd替换为/OldCmd");
		player.sendMessage("§a使用/autochat addcmds添加一个命令替换时如果/NewCmd已经有实际的意义，则它的意义会被覆盖");
		player.sendMessage("§a并且该命令只对命令有效，之后的参数/子命令无法被改动，未来版本可能添加关于参数/子命令的定义");
		player.sendMessage("§a/autochat reload重载配置文件config.yml");
		player.sendMessage("§4注意！用命令操作配置文件会造成配置文件内的注释丢失！");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		int leng=args.length;
		String childCmdName;
		String arg1;
		String arg2;
		switch(leng){
		case 0:
			help(sender);
			return true;
		case 1:
			childCmdName=args[0].toLowerCase();
			switch(childCmdName){
			case "list":
				list(sender);
				return true;
			case "reload":
				data.load(sender);
				return true;
			case "help":
				help(sender);
				return true;
				default:
					return false;
			}
		case 2:
			childCmdName=args[0].toLowerCase();
			arg1=args[1].toLowerCase();
			if(childCmdName.equals( "remove")){
				remove(arg1,sender);
				return true;
			}else{
				return false;
			}
		case 3:
			childCmdName=args[0].toLowerCase();
			arg1=args[1].toLowerCase();
			arg2=args[2].toLowerCase();
			switch(childCmdName){
			case "add":
				add(arg1,arg2,sender);
				return true;
			case "addcmds":
				addCmds(arg1,arg2,sender);
				return true;
			case "setflag":
				setflag(arg1,arg2,sender);
				return true;
			default:
				return false;
			}
		}
	return false;
	}
}
