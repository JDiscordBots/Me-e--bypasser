package io.github.jdiscordbots.mee.bypasser.model.jda.wrappers.slash;

import io.github.jdiscordbots.mee.bypasser.model.jda.wrappers.Argument;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent.OptionData;

public class SlashArgument implements Argument {
	
	private OptionData option;
	
	public SlashArgument(OptionData option) {
		this.option=option;
	}

	@Override
	public String getAsString() {
		return option.getAsString();
	}

	@Override
	public Role getAsRole() {
		return option.getAsRole();
	}

	@Override
	public int getAsInt() {
		return (int)option.getAsLong();
	}

}
