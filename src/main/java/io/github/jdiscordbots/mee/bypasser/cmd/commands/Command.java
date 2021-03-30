package io.github.jdiscordbots.mee.bypasser.cmd.commands;

import java.util.List;
import java.util.Map;

import io.github.jdiscordbots.mee.bypasser.model.jda.wrappers.ReceivedCommand;
import net.dv8tion.jda.api.entities.Command.OptionType;

public interface Command {
	String[] getNames();
	
	void execute(ReceivedCommand cmd);
	
	List<Map.Entry<String, OptionType>> getExpectedArguments();

	String getDescription();
}
