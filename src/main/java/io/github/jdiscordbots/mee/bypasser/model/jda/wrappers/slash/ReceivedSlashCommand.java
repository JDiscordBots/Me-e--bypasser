package io.github.jdiscordbots.mee.bypasser.model.jda.wrappers.slash;

import java.util.Collections;

import io.github.jdiscordbots.mee.bypasser.model.jda.wrappers.Argument;
import io.github.jdiscordbots.mee.bypasser.model.jda.wrappers.ReceivedCommand;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Webhook;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

public class ReceivedSlashCommand implements ReceivedCommand {
	
	private SlashCommandEvent evt;

	public ReceivedSlashCommand(SlashCommandEvent evt) {
		this.evt = evt;
	}

	@Override
	public void reply(String message) {
		evt.getHook().sendMessage(message).allowedMentions(Collections.emptyList()).queue();
	}

	@Override
	public void reply(MessageEmbed message) {
		evt.getHook().sendMessage(message).allowedMentions(Collections.emptyList()).queue();
	}
	
	@Override
	public Member getMember() {
		return evt.getMember();
	}
	
	@Override
	public Guild getGuild() {
		return evt.getGuild();
	}

	@Override
	public Argument getArgument(String name) {
		return new SlashArgument(evt.getOption(name));
	}

	@Override
	public boolean canUseEmbed() {
		return true;
	}
}
