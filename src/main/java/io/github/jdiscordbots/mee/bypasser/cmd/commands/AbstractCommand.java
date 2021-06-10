package io.github.jdiscordbots.mee.bypasser.cmd.commands;

import java.util.Collection;
import java.util.stream.Collectors;

import io.github.jdiscordbots.command_framework.command.CommandEvent;
import io.github.jdiscordbots.command_framework.command.ICommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.interactions.commands.privileges.CommandPrivilege;

public abstract class AbstractCommand implements ICommand {

	@Override
	public boolean isAvailableToEveryone() {
		return false;
	}

	@Override
	public Collection<CommandPrivilege> getPrivileges(Guild guild) {
		return guild.getRoles().stream().filter(role -> role.hasPermission(Permission.MANAGE_ROLES))
				.map(CommandPrivilege::enable).collect(Collectors.toList());
	}

	
	@Override
	public boolean allowExecute(CommandEvent event) {
		for (CommandPrivilege priv : getPrivileges(event.getGuild())) {
			if(event.getMember().getRoles().stream().map(Role::getId).anyMatch(priv.getId()::equals)) {
				return true;
			}
		}
		return false;
	}
}
