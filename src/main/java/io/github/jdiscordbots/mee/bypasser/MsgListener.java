package io.github.jdiscordbots.mee.bypasser;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.jdiscordbots.mee.bypasser.model.db.GuildInformation;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageEmbed.AuthorInfo;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.ShutdownEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class MsgListener extends ListenerAdapter {

	private static final long RATE_LIMIT = 1000L * 60;
	private static final Logger LOG = LoggerFactory.getLogger(MsgListener.class);

	private DataBaseController database;

	public MsgListener() {
		database = DataBaseController.getInstance();
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
		if (event.getMember() == null) {
			return;
		}
		if (event.getMember().getUser().isBot()) {
			if (event.getAuthor().getIdLong() == 159985870458322944L //check if message is from Mee6
					&& event.getGuild().getSelfMember().hasPermission(event.getChannel(), Permission.MESSAGE_HISTORY)) {
				event.getChannel().retrieveMessageById(event.getMessageId()).queueAfter(1, TimeUnit.SECONDS,
						msg -> runIfMemberFound(msg, member -> updateRole(member, event.getChannel(), null)));
			}
		} else if (event.getMessage().getContentRaw().startsWith("!rank")) { //check if message is rank query
			final Member member;
			if (event.getMessage().getMentionedMembers().isEmpty()) {
				member = event.getMember();
			} else {
				member = event.getMessage().getMentionedMembers().get(0);
			}
			updateRole(member, event.getChannel(), null);
		}
	}

	private void runIfMemberFound(Message message, Consumer<Member> toRun) {
		List<MessageEmbed> embeds = message.getEmbeds();
		if (embeds.size() != 1) {
			return;
		}
		MessageEmbed embed = embeds.get(0);
		AuthorInfo author = embed.getAuthor();
		if (author == null) {
			return;
		}
		String iconUrl = author.getIconUrl();
		if (iconUrl == null) {
			return;
		}
		if (iconUrl.startsWith("https://cdn.discordapp.com/embed/avatars/")) {
			runIfMemberByPrefixFound(message.getGuild(), author, toRun);
			return;
		}
		String[] splittedURL = iconUrl.split("/");
		if (splittedURL.length != 6) {
			return;
		}
		message.getGuild().retrieveMemberById(splittedURL[4]).queue(toRun,
				t -> runIfMemberByPrefixFound(message.getGuild(), author, toRun));
	}

	private void runIfMemberByPrefixFound(Guild g, AuthorInfo author, Consumer<Member> toRun) {
		g.retrieveMembersByPrefix(author.getName(), 1).onSuccess(members -> {
			if (!members.isEmpty()) {
				toRun.accept(members.get(0));
			}
		});
	}

	private void updateRole(Member member, TextChannel channel, Member author) {
		try {
			if (member == null) {
				return;
			}
			final GuildInformation guildInfo = database.loadGuildInformation(member.getGuild().getId());
			if (guildInfo.getLastRankCall() + RATE_LIMIT > System.currentTimeMillis()) {
				return;
			}
			final int level = MeeAPI.getLevel(member.getGuild().getId(), member.getId());
			guildInfo.setLastRankCall(System.currentTimeMillis());
			database.save(guildInfo);
			guildInfo.getRoleIdsAtLevel(level).forEach((roleId, add) -> {
				final Role role = member.getGuild().getRoleById(roleId);
				if (role != null) {
					if (hasModPerm(member.getGuild().getSelfMember())
							&& member.getGuild().getSelfMember().canInteract(role)) {
						if (add.booleanValue()) {
							if (!member.getRoles().contains(role)) {
								member.getGuild().addRoleToMember(member, role).queue();
							}
						} else {
							if (member.getRoles().contains(role)) {
								member.getGuild().removeRoleFromMember(member, role).queue();
							}
						}
					} else if (member.hasPermission(Permission.ADMINISTRATOR)) {
						channel.sendMessage("Cannot assign role " + role.getName() + " as "
								+ member.getGuild().getSelfMember().getEffectiveName()
								+ " does not have the necessary permissions.").queue();
					}
				}
			});
		} catch (JSONException e) {
			if (LOG.isErrorEnabled()) {
				LOG.error("Cannot load leveling data from the Mee API in guild {}", member.getGuild().getName(), e);
			}
		} catch (IOException e) {
			if (e.getMessage() != null
					&& e.getMessage().startsWith("Server returned HTTP response code: 401 for URL: ")) {
				if (author != null && author.hasPermission(Permission.ADMINISTRATOR)) {
					channel.sendMessage(
							"The server leaderboard needs to be public for automatic role assigning to work. This can be configured on https://mee6.xyz/dashboard/"
									+ member.getGuild().getId() + "/leaderboard")
							.queue();
				}
				if (LOG.isInfoEnabled()) {
					LOG.info("Server leaderboard is not public for guild {}", member.getGuild().getName());
				}
			} else {
				if (LOG.isErrorEnabled()) {
					LOG.error("Cannot load leveling data from the Mee API in guild {}", member.getGuild().getName(), e);
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
