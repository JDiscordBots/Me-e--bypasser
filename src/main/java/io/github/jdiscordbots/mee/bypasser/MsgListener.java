package io.github.jdiscordbots.mee.bypasser;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.ShutdownEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.jdiscordbots.mee.bypasser.model.GuildInformation;
import io.github.jdiscordbots.mee.bypasser.model.RoleInformation;

import java.io.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

public class MsgListener extends ListenerAdapter {

	private static final String PREFIX = "mb!";
	private static final long RATE_LIMIT = 1000L * 60;
	private static final Logger LOG = LoggerFactory.getLogger(MsgListener.class);

	private DataBaseController database;

	public MsgListener() {
		try {
			database = new DataBaseController();
		} catch (ClassNotFoundException | IOException e) {
			LOG.error("Could not load role database", e);
		}
	}

	private static boolean hasModPerm(Member member) {
		return member.hasPermission(Permission.MANAGE_ROLES);
	}

	public static class Holder<T> {
		private T val;

		public Holder(T val) {
			this.val = val;
		}

		public T get() {
			return val;
		}

		public void set(T val) {
			this.val = val;
		}
	}

	@Override
	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
		String msgContent = event.getMessage().getContentRaw();
		Guild g = event.getGuild();
		if (event.getMessage().getMember() == null || event.getMessage().getMember().getUser().isBot()) {
			return;
		}
		if (msgContent.startsWith("!rank")) {
			try {
				final Member member;
				if (event.getMessage().getMentionedMembers().isEmpty()) {
					member = event.getMember();
				} else {
					member = event.getMessage().getMentionedMembers().get(0);
				}

				if (member == null) {
					return;
				}

				final int level = MeeAPI.getLevel(event.getGuild().getId(), member.getId());
				final GuildInformation guildInfo = database.loadGuildInformation(event.getGuild().getId());
				if (guildInfo.getLastRankCall() + RATE_LIMIT > System.currentTimeMillis()) {
					return;
				}
				guildInfo.setLastRankCall(System.currentTimeMillis());
				database.save(guildInfo);
				guildInfo.getRoleIdsAtLevel(level).forEach((roleId, add) -> {
					final Role role = event.getGuild().getRoleById(roleId);
					if (role != null) {
						if (hasModPerm(event.getGuild().getSelfMember())
								&& event.getGuild().getSelfMember().canInteract(role)) {
							if (add.booleanValue()) {
								if (!member.getRoles().contains(role)) {
									event.getGuild().addRoleToMember(member, role).queue();
								}
							} else {
								if (member.getRoles().contains(role)) {
									event.getGuild().removeRoleFromMember(member, role).queue();
								}
							}
						} else if (member.hasPermission(Permission.ADMINISTRATOR)) {
							event.getChannel()
									.sendMessage("Cannot assign role " + role.getName() + " as "
											+ event.getGuild().getSelfMember().getEffectiveName()
											+ " does not have the necessary permissions.")
									.queue();
						}
					}
				});
			} catch (JSONException e) {
				if (LOG.isErrorEnabled()) {
					LOG.error("Cannot load leveling data from the Mee API in guild {}", g.getName(), e);
				}
			} catch (IOException e) {
				if (e.getMessage() != null
						&& e.getMessage().startsWith("Server returned HTTP response code: 401 for URL: ")) {
					final Member member = event.getMember();

					if (member != null && member.hasPermission(Permission.ADMINISTRATOR)) {
						event.getChannel().sendMessage(
								"The server leaderboard needs to be public for automatic role assigning to work. This can be configured on https://mee6.xyz/dashboard/"
										+ event.getGuild().getId() + "/leaderboard")
								.queue();
					}
					if (LOG.isInfoEnabled()) {
						LOG.info("Server leaderboard is not public for guild {}", event.getGuild().getName());
					}
				} else {
					if (LOG.isErrorEnabled()) {
						LOG.error("Cannot load leveling data from the Mee API in guild {}", event.getGuild().getName(),
								e);
					}
				}
			}
		} else if (event.getMessage().getContentRaw().startsWith(PREFIX)) {
			final Member member = event.getMember();

			if (member == null || !hasModPerm(member))
				return;

			final String[] split = event.getMessage().getContentRaw().split(" ");
			final String[] args = Arrays.copyOfRange(split, 1, split.length);
			final String command = split[0].substring(PREFIX.length()).toLowerCase();

			switch (command) {
			case "add": {
				if (args.length < 2) {
					event.getChannel().sendMessage("Error - missing args").queue();
					return;
				}
				int level;
				try {
					level = Integer.parseInt(args[0]);
				} catch (NumberFormatException e) {
					event.getChannel().sendMessage("Error - level required").queue();
					return;
				}
				Role role;
				try {
					role = event.getGuild().getRoleById(args[1]);
				} catch (NumberFormatException e) {
					role = null;
				}
				if (role == null) {
					event.getChannel().sendMessage("Error - Role not found").queue();
					return;
				}
				GuildInformation info = database.loadGuildInformation(event.getGuild().getId());
				String roleId = role.getId();
				info.getRoles().removeIf(toRemove -> roleId.equals(toRemove.getRoleId()));
				RoleInformation toAdd = new RoleInformation();
				toAdd.setLevel(level);
				toAdd.setRoleId(role.getId());
				toAdd.setGuild(info);
				info.getRoles().add(toAdd);
				database.save(toAdd, info);
				event.getChannel().sendMessage("added Role " + role.getName() + " for level " + level).queue();
				break;
			}
			case "remove": {
				int level;
				try {
					level = Integer.parseInt(args[0]);
				} catch (NumberFormatException e) {
					event.getChannel().sendMessage("Error - level required").queue();
					return;
				}
				String id = database.removeRole(event.getGuild().getId(), level);

				final Role role;

				if (id != null && (role = event.getGuild().getRoleById(id)) != null) {
					event.getChannel().sendMessage("removed id " + role.getAsMention() + " from level " + level)
							.allowedMentions(Collections.emptyList()).queue();
				} else {
					event.getChannel().sendMessage("no id for level " + level).allowedMentions(Collections.emptyList())
							.queue();
				}
				break;
			}
			case "show":
			case "list": {
				GuildInformation guildInfo = database.loadGuildInformation(event.getGuild().getId());
				event.getChannel().sendMessage("" + guildInfo.getRoles().stream().map(roleInfo -> {
					final Role role = event.getGuild().getRoleById(roleInfo.getRoleId());
					if (role != null) {
						return "Level " + roleInfo.getLevel() + " is assigned to role " + role.getAsMention();
					}
					return "";
				}).collect(Collectors.joining("\n")) + "\nRoles will "
						+ ((guildInfo.isAutoRemoveLevels()) ? "" : "**not** ")
						+ "be removed if someone reaches a higher role.").allowedMentions(Collections.emptyList())
						.queue();
				break;
			}
			case "toggle": {
				GuildInformation guildInfo = database.loadGuildInformation(event.getGuild().getId());
				boolean rem = !guildInfo.isAutoRemoveLevels();
				guildInfo.setAutoRemoveLevels(rem);
				database.save(guildInfo);
				event.getChannel().sendMessage(
						"Roles will now " + (rem ? "" : "**not** ") + "be removed if someone reaches a higher role.")
						.queue();
				break;
			}
			case "id": {
				if (args.length < 1) {
					event.getChannel().sendMessage("missing args").queue();
					return;
				}

				final String name = String.join(" ", args);
				final String title = "Roles of name `" + name + "`";
				final String desc = event.getGuild().getRolesByName(name, true).stream()
						.map(role -> role.getAsMention() + " (" + role.getId() + ")").collect(Collectors.joining("\n"));

				if (event.getGuild().getSelfMember().hasPermission(event.getChannel(),
						Permission.MESSAGE_EMBED_LINKS)) {
					final EmbedBuilder eb = new EmbedBuilder();
					eb.setTitle(title);
					eb.setDescription(desc);

					event.getChannel().sendMessage(eb.build()).queue();
				} else {
					event.getChannel().sendMessage("**" + title + "**\n" + desc).queue();
				}
				break;
			}
			default: {
				event.getChannel().sendMessage("Command not found: " + command).queue();
			}
			}
		}
	}

	@Override
	public void onShutdown(ShutdownEvent event) {
		try {
			database.close();
		} catch (Exception e) {
			LOG.error("cannot close database", e);
		}
	}

}
