package io.github.jdiscordbots.mee.bypasser;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.json.JSONException;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class MsgListener extends ListenerAdapter{
	
	public MsgListener() {
		if(new File("roles.dat").exists()) {
			try {
				load();
			} catch (ClassNotFoundException | IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private Map<String, Map<Integer,String>> roles=new HashMap<>();
	private boolean hasModPerm(Member member) {
		return member.hasPermission(Permission.MANAGE_ROLES);
	}
	@Override
	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
		if(event.getMessage().getContentRaw().startsWith("!rank")) {
			try {
				Member member;
				if(event.getMessage().getMentionedMembers().isEmpty()) {
					member=event.getMember();
				}else {
					member=event.getMessage().getMentionedMembers().get(0);
				}
				int level = MeeAPI.getLevel(event.getGuild().getId(), member.getId());
				Map<Integer,String> gRoles=roles.get(event.getGuild().getId());
				if(gRoles!=null) {
					gRoles.forEach((k,v)->{
						if(k<=level) {
							event.getGuild().addRoleToMember(member, event.getGuild().getRoleById(v)).queue();
						}
					});
				}
			} catch (JSONException | IOException e) {
				e.printStackTrace();
			}
		}else if(event.getMessage().getContentRaw().startsWith("mb!")) {
			if(!hasModPerm(event.getMember())) {
				return;
			}
			String[] split=event.getMessage().getContentRaw().split(" ");
			String[] args=Arrays.copyOfRange(split, 1, split.length);
			String command=split[0].substring("mb!".length()).toLowerCase();
			switch(command){
				case "add":{
					if(args.length<2) {
						event.getChannel().sendMessage("Error - missing args").queue();
						return;
					}
					int level;
					try{
						level = Integer.parseInt(args[0]);
					}catch(NumberFormatException e) {
						event.getChannel().sendMessage("Error - level required").queue();
						return;
					}
					Role role=null;
					try {
						role=event.getGuild().getRoleById(args[1]);
					} catch (NumberFormatException e) {
					}
					if(role==null) {
						event.getChannel().sendMessage("Error - Role not found").queue();
						return;
					}
					Map<Integer,String> gRoles;
					if(roles.containsKey(event.getGuild().getId())) {
						gRoles=roles.get(event.getGuild().getId());
					}
					else {
						gRoles=new HashMap<>();
						roles.put(event.getGuild().getId(), gRoles);
					}
					gRoles.put(level, role.getId());
					try {
						save();
					} catch (IOException e) {
						e.printStackTrace();
					}
					event.getChannel().sendMessage("added Role "+role.getName()+" for level "+level).queue();
					break;
				}
				case "remove":{
					int level;
					try{
						level = Integer.parseInt(args[0]);
					}catch(NumberFormatException e) {
						event.getChannel().sendMessage("Error - level required").queue();
						return;
					}
					String role=null;
					if(roles.containsKey(event.getGuild().getId())) {
						Map<Integer,String> gRoles=roles.get(event.getGuild().getId());
						role=gRoles.remove(level);
					}
					if(role!=null) {
						event.getChannel().sendMessage("removed role "+role+" from level "+level).queue();
					}else {
						event.getChannel().sendMessage("no role for level"+level).queue();
					}
					try {
						save();
					} catch (IOException e) {
						e.printStackTrace();
					}
					break;
				}
				case "show":{
					if(roles.containsKey(event.getGuild().getId())) {
						event.getChannel().sendMessage(""+roles.get(event.getGuild().getId())).queue();
					}else {
						event.getChannel().sendMessage("no roles").queue();
					}
					break;
				}
				case "id":{
					if(args.length<1) {
						event.getChannel().sendMessage("missing args").queue();
						return;
					}
					String name=String.join(" ",args);
					event.getChannel().sendMessage("Roles of name "+name+": "+event.getGuild().getRolesByName(name, true)).queue();
					break;
				}
				case "list":{
					Map<Integer, String> gRoles = roles.get(event.getGuild().getId());
					if(gRoles==null) {
						event.getChannel().sendMessage("Mee-bypasser is not set up for this guild").queue();
					}else {
						event.getChannel().sendMessage(gRoles.entrySet().stream().map(entry->"Level: "+entry.getKey()+", Role: "+event.getJDA().getRoleById(entry.getValue()).getAsMention()).collect(Collectors.joining("\n"))).queue();
					}
					break;
				}
				default:{
					event.getChannel().sendMessage("Command not found: "+command).queue();
				}
			}
		}
	}
	private void save() throws IOException {
		try(ObjectOutputStream oos=new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream("roles.dat")))){
			oos.writeObject(roles);
		}
	}
	@SuppressWarnings("unchecked")
	private void load() throws IOException, ClassNotFoundException {
		try(ObjectInputStream ois=new ObjectInputStream(new BufferedInputStream(new FileInputStream("roles.dat")))){
			roles=(Map<String, Map<Integer, String>>) ois.readObject();
		}
	}
}
