package io.github.jdiscordbots.mee.bypasser.cmd.commands;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import io.github.jdiscordbots.command_framework.command.ArgumentTemplate;
import io.github.jdiscordbots.command_framework.command.Command;
import io.github.jdiscordbots.command_framework.command.CommandEvent;
import io.github.jdiscordbots.command_framework.command.text.MessageCommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.OptionType;

@Command("id")
public class IdCommand extends AbstractCommand{


	@Override
	public String help() {
		return "gets the ID from a role name";
	}
	
	@Override
	public void action(CommandEvent event) {
		if (event.getArgs().isEmpty()) {
			event.reply("missing args").queue();
			return;
		}
		
		final String name;
		if(event instanceof MessageCommandEvent) {
			name=startWithFirstSpace(((MessageCommandEvent)event).getEvent().getMessage().getContentRaw().toString());
		}else {
			name=event.getArgs().get(0).getAsString();
		}
		final String title = "Roles of name `" + name + "`";
		final String desc = event.getGuild().getRolesByName(name, true).stream()
				.map(role -> role.getAsMention() + " (" + role.getId() + ")").collect(Collectors.joining("\n"));
		if (event.getGuild().getSelfMember().hasPermission(Permission.MESSAGE_EMBED_LINKS)) {
			final EmbedBuilder eb = new EmbedBuilder();
			eb.setTitle(title);
			eb.setDescription(desc);
			event.reply(eb.build()).queue();
		} else {
			event.reply("**" + title + "**\n" + desc).queue();
		}
	}
	private static String startWithFirstSpace(String txt) {
		return txt.substring(txt.indexOf(' ')+1);
	}
	
	@Override
	public List<ArgumentTemplate> getExpectedArguments() {
		return Collections.singletonList(new ArgumentTemplate(OptionType.STRING, "role", "The role name to get the ID", true));
	}
	
}
