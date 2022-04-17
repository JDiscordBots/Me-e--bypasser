package io.github.jdiscordbots.mee.bypasser.cmd.commands;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import io.github.jdiscordbots.command_framework.command.ArgumentTemplate;
import io.github.jdiscordbots.command_framework.command.Command;
import io.github.jdiscordbots.command_framework.command.CommandEvent;
import io.github.jdiscordbots.mee.bypasser.DataBaseController;
import io.github.jdiscordbots.mee.bypasser.model.db.GuildInformation;
import io.github.jdiscordbots.mee.bypasser.model.db.RoleInformation;
import net.dv8tion.jda.api.entities.Role;

@Command({"show","list"})
public class ShowCommand extends AbstractCommand {

	private DataBaseController database;

	public ShowCommand() {
		this.database = DataBaseController.getInstance();
	}

	@Override
	public String help() {
		return "lists roles";
	}
	
	@Override
	public void action(CommandEvent event) {
		GuildInformation guildInfo = database.loadGuildInformation(event.getGuild().getId());
		event.reply("" + guildInfo.getRoles().stream().sorted(Comparator.comparing(RoleInformation::getLevel)).map(roleInfo -> {
			final Role role = event.getGuild().getRoleById(roleInfo.getRoleId());
			if (role != null) {
				return "Level " + roleInfo.getLevel() + " is assigned to role " + role.getAsMention();
			}
			return "";
		}).collect(Collectors.joining("\n")) + "\nRoles will " + ((guildInfo.isAutoRemoveLevels()) ? "" : "**not** ")
				+ "be removed if someone reaches a higher role.").queue();
	}

	@Override
	public List<ArgumentTemplate> getExpectedArguments() {
		return Collections.emptyList();
	}

}
