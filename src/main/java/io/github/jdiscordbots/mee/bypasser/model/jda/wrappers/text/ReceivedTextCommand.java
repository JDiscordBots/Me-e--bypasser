package io.github.jdiscordbots.mee.bypasser.model.jda.wrappers.text;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import io.github.jdiscordbots.mee.bypasser.cmd.commands.Command;
import io.github.jdiscordbots.mee.bypasser.model.jda.wrappers.Argument;
import io.github.jdiscordbots.mee.bypasser.model.jda.wrappers.ReceivedCommand;
import net.dv8tion.jda.api.entities.Command.OptionType;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class ReceivedTextCommand implements ReceivedCommand {
	private Map<String, Argument> args = new HashMap<>();
	private GuildMessageReceivedEvent evt;

	public ReceivedTextCommand(GuildMessageReceivedEvent evt, Command cmd, String[] args) {
		this.evt = evt;
		for (int i = 0; i < Math.min(cmd.getExpectedArguments().size(), args.length); i++) {
			Entry<String, OptionType> entry = cmd.getExpectedArguments().get(i);
			this.args.put(entry.getKey(), new TextArgument(evt.getGuild(), args[i]));
		}
	}

	@Override
	public void reply(String message) {
		evt.getChannel().sendMessage(message).allowedMentions(Collections.emptyList()).queue();
	}

	@Override
	public void reply(MessageEmbed message) {
		evt.getChannel().sendMessage(message).allowedMentions(Collections.emptyList()).queue();
	}

	@Override
	public Guild getGuild() {
		return evt.getGuild();
	}
	
	@Override
	public Member getMember() {
		return evt.getMember();
	}

	@Override
	public Argument getArgument(String name) {
		return args.get(name);
	}

	public CharSequence getContentRaw() {
		return evt.getMessage().getContentRaw();
	}

	@Override
	public boolean canUseEmbed() {
		return getGuild().getSelfMember().hasPermission(evt.getChannel(),
				Permission.MESSAGE_EMBED_LINKS);
	}

}
