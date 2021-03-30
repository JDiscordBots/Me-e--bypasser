package io.github.jdiscordbots.mee.bypasser.cmd;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import io.github.jdiscordbots.mee.bypasser.DataBaseController;
import io.github.jdiscordbots.mee.bypasser.cmd.commands.AddCommand;
import io.github.jdiscordbots.mee.bypasser.cmd.commands.Command;
import io.github.jdiscordbots.mee.bypasser.cmd.commands.IdCommand;
import io.github.jdiscordbots.mee.bypasser.cmd.commands.RemoveCommand;
import io.github.jdiscordbots.mee.bypasser.cmd.commands.ShowCommand;
import io.github.jdiscordbots.mee.bypasser.cmd.commands.ToggleCommand;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Command.OptionType;
import net.dv8tion.jda.api.requests.restaction.CommandUpdateAction.CommandData;
import net.dv8tion.jda.api.requests.restaction.CommandUpdateAction.OptionData;
import net.dv8tion.jda.api.sharding.ShardManager;

public class CommandManager {
	private Map<String, Command> commands=new HashMap<>();
	public CommandManager(ShardManager shardManager,DataBaseController database) {
		List<CommandData> slashCommands=new ArrayList<>();
		for (Command cmd : loadCommands(database)) {
			for (String alias : cmd.getNames()) {
				commands.put(alias, cmd);
				CommandData slashCommand = new CommandData(alias, cmd.getDescription());
				for (Entry<String, OptionType> arg : cmd.getExpectedArguments()) {
					slashCommand.addOption(new OptionData(arg.getValue(), arg.getKey(), arg.getKey()).setRequired(true));
				}
				slashCommands.add(slashCommand);
			}
		}
		for (JDA jda : shardManager.getShards()) {
			jda.updateCommands().addCommands(slashCommands).queue();
		}
	}
	
	private static Collection<Command> loadCommands(DataBaseController database){
		return Arrays.asList(new AddCommand(database),new IdCommand(),new RemoveCommand(database),new ShowCommand(database),new ToggleCommand(database));
	}
	
	public Command getCommand(String name) {
		return commands.get(name);
	}
}
