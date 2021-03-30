package io.github.jdiscordbots.mee.bypasser.model.jda.wrappers.text;

import io.github.jdiscordbots.mee.bypasser.model.jda.wrappers.Argument;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;

public class TextArgument implements Argument{

	private Guild guild;
	private String text;
	
	public TextArgument(Guild guild, String text) {
		this.guild=guild;
		this.text=text;
	}

	@Override
	public String getAsString() {
		return text;
	}

	@Override
	public Role getAsRole() {
		return guild.getRoleById(text);
	}

	@Override
	public int getAsInt() {
		return Integer.parseInt(text);
	}

}
