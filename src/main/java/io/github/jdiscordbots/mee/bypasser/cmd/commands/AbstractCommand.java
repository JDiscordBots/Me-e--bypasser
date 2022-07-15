package io.github.jdiscordbots.mee.bypasser.cmd.commands;

import java.util.Collections;
import java.util.Set;

import io.github.jdiscordbots.command_framework.command.CommandEvent;
import io.github.jdiscordbots.command_framework.command.ICommand;
import net.dv8tion.jda.api.Permission;

public abstract class AbstractCommand implements ICommand {

	@Override
	public boolean isAvailableToEveryone() {
		return false;
	}

	@Override
	public Set<Permission> getRequiredPermissions() {
		return Collections.singleton(Permission.MANAGE_ROLES);
	}
	
	@Override
	public boolean allowExecute(CommandEvent event) {
		for(Permission perm : getRequiredPermissions()){
			if(event.getMember().hasPermission(perm)){
				return true;
			}
		}
		return false;
	}
}
