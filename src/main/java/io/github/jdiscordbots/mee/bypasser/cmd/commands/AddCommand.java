package io.github.jdiscordbots.mee.bypasser.cmd.commands;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import io.github.jdiscordbots.mee.bypasser.DataBaseController;
import io.github.jdiscordbots.mee.bypasser.model.db.GuildInformation;
import io.github.jdiscordbots.mee.bypasser.model.db.RoleInformation;
import io.github.jdiscordbots.mee.bypasser.model.jda.wrappers.ReceivedCommand;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.Command.OptionType;

public class AddCommand implements Command{

	private DataBaseController database;
	
	public AddCommand(DataBaseController database) {
		this.database = database;
	}

	@Override
	public String[] getNames() {
		return new String[]{"add"};
	}
	
	@Override
	public String getDescription() {
		return "Adds a role";
	}

	@Override
	public void execute(ReceivedCommand cmd) {
//		if (args.length < 2) {//TODO
//			cmd.reply("Error - missing args");
//			return;
//		}
		int level;
		try {
			level = cmd.getArgument("level").getAsInt();
		} catch (NumberFormatException e) {
			cmd.reply("Error - level required");
			return;
		}
		Role role;
		try {
			role = cmd.getArgument("role").getAsRole();
		} catch (NumberFormatException e) {
			cmd.reply("Error - Role not found");
			return;
		}
		String roleId = role.getId();
		database.executeInTransaction(()->{
			GuildInformation info = database.loadGuildInformation(cmd.getGuild().getId());
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
		
		cmd.reply("added Role " + role.getName() + " for level " + level);
	}

	@Override
	public List<Entry<String, OptionType>> getExpectedArguments() {
		return Arrays.asList(new AbstractMap.SimpleEntry<>("level", OptionType.INTEGER),
				new AbstractMap.SimpleEntry<>("role", OptionType.ROLE));
	}

}
