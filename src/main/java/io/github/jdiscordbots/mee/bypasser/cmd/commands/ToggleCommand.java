package io.github.jdiscordbots.mee.bypasser.cmd.commands;

import java.util.Collections;
import java.util.List;
import io.github.jdiscordbots.command_framework.command.ArgumentTemplate;
import io.github.jdiscordbots.command_framework.command.Command;
import io.github.jdiscordbots.command_framework.command.CommandEvent;
import io.github.jdiscordbots.mee.bypasser.DataBaseController;
import io.github.jdiscordbots.mee.bypasser.model.db.GuildInformation;

@Command("toggle")
public class ToggleCommand extends AbstractCommand {

	private DataBaseController database;

	public ToggleCommand() {
		this.database = DataBaseController.getInstance();
	}

	@Override
	public String help() {
		return "toggles whether roles should be removed if someone reaches a higher role";
	}
	
	@Override
	public void action(CommandEvent event) {
		GuildInformation guildInfo = database.loadGuildInformation(event.getGuild().getId());
		boolean rem = !guildInfo.isAutoRemoveLevels();
		guildInfo.setAutoRemoveLevels(rem);
		database.save(guildInfo);
		event.reply("Roles will now " + (rem ? "" : "**not** ") + "be removed if someone reaches a higher role.").queue();
	}

	@Override
	public List<ArgumentTemplate> getExpectedArguments() {
		return Collections.emptyList();
	}

}
