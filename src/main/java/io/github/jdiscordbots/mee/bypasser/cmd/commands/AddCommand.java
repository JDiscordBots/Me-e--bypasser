package io.github.jdiscordbots.mee.bypasser.cmd.commands;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import io.github.jdiscordbots.command_framework.command.ArgumentTemplate;
import io.github.jdiscordbots.command_framework.command.Command;
import io.github.jdiscordbots.command_framework.command.CommandEvent;
import io.github.jdiscordbots.mee.bypasser.DataBaseController;
import io.github.jdiscordbots.mee.bypasser.model.db.GuildInformation;
import io.github.jdiscordbots.mee.bypasser.model.db.RoleInformation;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.interactions.commands.OptionType;

@Command("add")
public class AddCommand extends AbstractCommand{

	private DataBaseController database;
	
	public AddCommand() {
		this.database = DataBaseController.getInstance();
	}

	@Override
	public List<ArgumentTemplate> getExpectedArguments() {
		return Arrays.asList(new ArgumentTemplate(OptionType.INTEGER, "level", "The level to add", true),
				new ArgumentTemplate(OptionType.ROLE, "role", "the role", true));
	}
	
	@Override
	public void action(CommandEvent event) {
		if(event.getArgs().size()<2) {
			event.reply("Error - missing args").queue();
			return;
		}
		int level;
		try {
			level = (int)event.getArgs().get(0).getAsLong();
		} catch (NumberFormatException e) {
			event.reply("Error - level required").queue();
			return;
		}
		Role role;
		try {
			role = event.getArgs().get(1).getAsRole();
		} catch (NumberFormatException e) {
			event.reply("Error - Role not found").queue();
			return;
		}
		String roleId = role.getId();
		database.executeInTransaction(()->{
			GuildInformation info = database.loadGuildInformation(event.getGuild().getId());
			info.setRoles(new HashSet<>(info.getRoles()));
			for(Iterator<RoleInformation> roleIter=info.getRoles().iterator();
					roleIter.hasNext();) {
				RoleInformation roleToCheck=roleIter.next();
				if(roleId.equals(roleToCheck.getRoleId())||level==roleToCheck.getLevel()) {
					database.removeRole(roleToCheck);
					roleIter.remove();
				}
			}
			RoleInformation toAdd = new RoleInformation();
			toAdd.setLevel(level);
			toAdd.setRoleId(roleId);
			toAdd.setGuild(info);
			info.getRoles().add(toAdd);
			database.save(info,toAdd);
		});
		
		event.reply("added Role " + role.getName() + " for level " + level).queue();
	}

	@Override
	public String help() {
		return "Adds a role";
	}
	
	@Override
	public boolean isAvailableToEveryone() {
		return false;
	}
}
