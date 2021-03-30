package io.github.jdiscordbots.mee.bypasser.cmd.commands;

import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import io.github.jdiscordbots.mee.bypasser.DataBaseController;
import io.github.jdiscordbots.mee.bypasser.model.db.GuildInformation;
import io.github.jdiscordbots.mee.bypasser.model.jda.wrappers.ReceivedCommand;
import net.dv8tion.jda.api.entities.Command.OptionType;

public class ToggleCommand implements Command {

	private DataBaseController database;

	public ToggleCommand(DataBaseController database) {
		this.database = database;
	}

	@Override
	public String[] getNames() {
		return new String[] { "toggle"};
	}

	@Override
	public String getDescription() {
		return "toggles whether roles should be removed if someone reaches a higher role";
	}
	
	@Override
	public void execute(ReceivedCommand cmd) {
		GuildInformation guildInfo = database.loadGuildInformation(cmd.getGuild().getId());
		boolean rem = !guildInfo.isAutoRemoveLevels();
		guildInfo.setAutoRemoveLevels(rem);
		database.save(guildInfo);
		cmd.reply("Roles will now " + (rem ? "" : "**not** ") + "be removed if someone reaches a higher role.");
	}

	@Override
	public List<Entry<String, OptionType>> getExpectedArguments() {
		return Collections.emptyList();
	}

}
