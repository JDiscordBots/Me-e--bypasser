package io.github.jdiscordbots.mee.bypasser.cmd.commands;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import io.github.jdiscordbots.mee.bypasser.DataBaseController;
import io.github.jdiscordbots.mee.bypasser.model.jda.wrappers.ReceivedCommand;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.Command.OptionType;

public class RemoveCommand implements Command {

	private DataBaseController database;

	public RemoveCommand(DataBaseController database) {
		this.database = database;
	}

	@Override
	public String[] getNames() {
		return new String[]{"remove"};
	}
	
	@Override
	public String getDescription() {
		return "removes a role";
	}

	@Override
	public void execute(ReceivedCommand cmd) {
		int level;
		try {
			level = cmd.getArgument("level").getAsInt();
		} catch (NumberFormatException e) {
			cmd.reply("Error - level required");
			return;
		}
		String id = database.removeRole(cmd.getGuild().getId(), level);

		final Role role;

		if (id != null && (role = cmd.getGuild().getRoleById(id)) != null) {
			cmd.reply("removed id " + role.getAsMention() + " from level " + level);
		} else {
			cmd.reply("no id for level " + level);
		}
	}

	@Override
	public List<Entry<String, OptionType>> getExpectedArguments() {
		return Collections.singletonList(new AbstractMap.SimpleEntry<>("level", OptionType.INTEGER));
	}

}
