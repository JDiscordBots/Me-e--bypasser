package io.github.jdiscordbots.mee.bypasser.cmd.commands;

import java.util.Collections;
import java.util.List;

import io.github.jdiscordbots.command_framework.command.ArgumentTemplate;
import io.github.jdiscordbots.command_framework.command.Command;
import io.github.jdiscordbots.command_framework.command.CommandEvent;
import io.github.jdiscordbots.mee.bypasser.DataBaseController;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.interactions.commands.OptionType;

@Command("remove")
public class RemoveCommand extends AbstractCommand {

	private DataBaseController database;

	public RemoveCommand() {
		this.database = DataBaseController.getInstance();
	}

	
	@Override
	public String help() {
		return "removes a role";
	}

	@Override
	public void action(CommandEvent event) {
		if(event.getArgs().isEmpty()) {
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
		String id = database.removeRole(event.getGuild().getId(), level);

		final Role role;

		if (id != null && (role = event.getGuild().getRoleById(id)) != null) {
			event.reply("removed id " + role.getAsMention() + " from level " + level).queue();
		} else {
			event.reply("no id for level " + level).queue();
		}
	}

	@Override
	public List<ArgumentTemplate> getExpectedArguments() {
		return Collections.singletonList(new ArgumentTemplate(OptionType.INTEGER, "level", "the level to remove the reward from", false));
	}
	
}
