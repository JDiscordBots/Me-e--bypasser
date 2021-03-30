package io.github.jdiscordbots.mee.bypasser.model.jda.wrappers;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;

public interface ReceivedCommand {
	void reply(String message);
	void reply(MessageEmbed message);
	Member getMember();
	Argument getArgument(String name);
	Guild getGuild();
	boolean canUseEmbed();
}
