package io.github.jdiscordbots.mee.bypasser.cmd.commands;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import io.github.jdiscordbots.mee.bypasser.model.jda.wrappers.ReceivedCommand;
import io.github.jdiscordbots.mee.bypasser.model.jda.wrappers.text.ReceivedTextCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Command.OptionType;

public class IdCommand implements Command{

	@Override
	public String[] getNames() {
		return new String[]{"id"};
	}

	@Override
	public String getDescription() {
		return "gets the ID from a role name";
	}
	
	@Override
	public void execute(ReceivedCommand cmd) {
//		if (args.length < 1) {
//			event.getChannel().sendMessage("missing args").queue();
//			return;
//		}
		
		final String name;
		if(cmd instanceof ReceivedTextCommand) {
			name=startWithFirstSpace(((ReceivedTextCommand)cmd).getContentRaw().toString());
		}else {
			name=cmd.getArgument("role").getAsString();
		}
		final String title = "Roles of name `" + name + "`";
		final String desc = cmd.getGuild().getRolesByName(name, true).stream()
				.map(role -> role.getAsMention() + " (" + role.getId() + ")").collect(Collectors.joining("\n"));
		if (cmd.canUseEmbed()) {
			final EmbedBuilder eb = new EmbedBuilder();
			eb.setTitle(title);
			eb.setDescription(desc);

			cmd.reply(eb.build());
		} else {
			cmd.reply("**" + title + "**\n" + desc);
		}
	}
	private static String startWithFirstSpace(String txt) {
		return txt.substring(txt.indexOf(' '));
	}
	
	@Override
	public List<Entry<String, OptionType>> getExpectedArguments() {
		return Collections.singletonList(new AbstractMap.SimpleEntry<>("role", OptionType.STRING));
	}
}
