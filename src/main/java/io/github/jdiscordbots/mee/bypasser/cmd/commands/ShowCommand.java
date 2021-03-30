package io.github.jdiscordbots.mee.bypasser.cmd.commands;

import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import io.github.jdiscordbots.mee.bypasser.DataBaseController;
import io.github.jdiscordbots.mee.bypasser.model.db.GuildInformation;
import io.github.jdiscordbots.mee.bypasser.model.jda.wrappers.ReceivedCommand;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.Command.OptionType;

public class ShowCommand implements Command {

	private DataBaseController database;

	public ShowCommand(DataBaseController database) {
		this.database = database;
	}

	@Override
	public String[] getNames() {
		return new String[] { "show", "list" };
	}

	@Override
	public String getDescription() {
		return "lists roles";
	}
	
	@Override
	public void execute(ReceivedCommand cmd) {
		GuildInformation guildInfo = database.loadGuildInformation(cmd.getGuild().getId());
		cmd.reply("" + guildInfo.getRoles().stream().map(roleInfo -> {
			final Role role = cmd.getGuild().getRoleById(roleInfo.getRoleId());
			if (role != null) {
				return "Level " + roleInfo.getLevel() + " is assigned to role " + role.getAsMention();
			}
			return "";
		}).collect(Collectors.joining("\n")) + "\nRoles will " + ((guildInfo.isAutoRemoveLevels()) ? "" : "**not** ")
				+ "be removed if someone reaches a higher role.");
	}

	@Override
	public List<Entry<String, OptionType>> getExpectedArguments() {
		return Collections.emptyList();
	}

}
