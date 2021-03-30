package io.github.jdiscordbots.mee.bypasser.model.jda.wrappers;

import net.dv8tion.jda.api.entities.Role;

public interface Argument {
	String getAsString();
	Role getAsRole();
	int getAsInt();
}
