package io.github.jdiscordbots.mee.bypasser;

import io.github.jdiscordbots.mee.bypasser.model.db.GuildInformation;
import io.github.jdiscordbots.mee.bypasser.ocr.MeeImageRecognition;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed.AuthorInfo;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.ShutdownEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.sourceforge.tess4j.TesseractException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MsgListener extends ListenerAdapter {

	private static final long RATE_LIMIT = 1000L;
	private static final Logger LOG = LoggerFactory.getLogger(MsgListener.class);
	private static final Pattern rankPattern = Pattern.compile("!rank (?:(?<id1>\\d+)|[<][@][!]?(?<id2>\\d+)[>])");

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
	public void onMessageReceived(@NotNull MessageReceivedEvent event) {
		if(!event.isFromGuild()) {
			return;
		}
		if(event.getMember() == null) {
			return;
		}
		if(!event.getMember().getUser().isBot()
				|| event.getAuthor().getIdLong() != 159985870458322944L) { //check if message is from Mee6
			return;
		}
		if(event.getGuild().getSelfMember().hasPermission(event.getGuildChannel(), Permission.MESSAGE_HISTORY)) {
			Message.Interaction interaction = event.getMessage().getInteraction();
			if(interaction == null) {
				event.getGuildChannel().getHistory().retrievePast(2).queue(messages -> {
					if(messages.size() == 2) {
						Message message = messages.get(1);
						Matcher rankPatternMatcher = rankPattern.matcher(message.getContentRaw());
						if("!rank".equals(message.getContentRaw())) {
							if(message.getMember() == null) {
								event.getGuild().retrieveMemberById(message.getAuthor().getId()).queue(actualMember -> {
									updateRole(actualMember, event, actualMember);
								});
							} else {
								updateRole(message.getMember(), event, message.getMember());
							}
						} else if(rankPatternMatcher.matches()) {
							String id = rankPatternMatcher.group("id1");
							if(id == null) {
								id = rankPatternMatcher.group("id2");
							}
							event.getGuild().retrieveMemberById(id).queue(member -> updateRole(member, event, message.getMember()));
						}
					}
				});
			} else {
				if("rank".equals(interaction.getName())) {
					Member member = interaction.getMember();
					if(member != null) {
						event.getChannel().retrieveMessageById(event.getMessageId()).queueAfter(1, TimeUnit.SECONDS,
								msg -> updateRole(member, event, msg, member));
					}
				}

			}
		}
	}

	private void runIfMemberByPrefixFound(Guild g, AuthorInfo author, Consumer<Member> toRun) {
		g.retrieveMembersByPrefix(author.getName(), 1).onSuccess(members -> {
			if(!members.isEmpty()) {
				toRun.accept(members.get(0));
			}
		});
	}

	private void updateRole(Member member, MessageReceivedEvent event, Member author) {
		updateRole(member, event, event.getMessage(), author);
	}

	private void updateRole(Member member, MessageReceivedEvent event, Message msg, Member author) {
		final List<Message.Attachment> attachments = msg.getAttachments();
		if(attachments.size() != 1) {
			return;
		}
		final Message.Attachment attachment = attachments.get(0);
		if(attachment.getContentType() == null || !attachment.getContentType().startsWith("image/")) {
			return;
		}
		final GuildInformation guildInfo = database.loadGuildInformation(event.getGuild().getId());
		if(guildInfo.getLastRankCall() + RATE_LIMIT > System.currentTimeMillis()) {
			return;
		}
		guildInfo.setLastRankCall(System.currentTimeMillis());
		database.save(guildInfo);
		attachment.retrieveInputStream().thenAccept(is -> {
			try {
				MeeImageRecognition.LevelInfo levelInfo = MeeImageRecognition.loadDataFromImage(is);
				if(String.valueOf(levelInfo.getDiscrimminator()).equals(member.getUser().getDiscriminator())) {
					guildInfo.getRoleIdsAtLevel(levelInfo.getLevel()).forEach((roleId, add) -> {
						final Role role = member.getGuild().getRoleById(roleId);
						if(role != null) {
							if(hasModPerm(member.getGuild().getSelfMember())
									&& member.getGuild().getSelfMember().canInteract(role)) {
								if(add) {
									if(!member.getRoles().contains(role)) {
										member.getGuild().addRoleToMember(member, role).queue();
									}
								} else {
									if(member.getRoles().contains(role)) {
										member.getGuild().removeRoleFromMember(member, role).queue();
									}
								}
							} else if(author.hasPermission(Permission.ADMINISTRATOR)) {
								event.getChannel().sendMessage("Cannot assign role " + role.getName() + " as "
										+ member.getGuild().getSelfMember().getEffectiveName()
										+ " does not have the necessary permissions.").queue();
							}
						}
					});
				}
			} catch(IOException e) {
				LOG.atError()
						.addArgument(event::getGuild)
						.setCause(e)
						.log("An I/O error occurred trying to read the image in guild {}.");
			} catch(TesseractException e) {
				LOG.atInfo()
						.addArgument(event::getGuild)
						.setCause(e)
						.log("An error occurred trying to parse the image in guild {}.");
			} catch(UnsatisfiedLinkError e) {
				LOG.atError()
						.addArgument(event::getGuild)
						.setCause(e)
						.log("An error occured trying to load libraries during parsing the image in guild {}.");
			}
		}).exceptionally(e -> {
			LOG.atError()
					.addArgument(event::getGuild)
					.setCause(e)
					.log("An unknown error occurred trying to load levels in guild {}.");
			return null;
		});

	}

	@Override
	public void onShutdown(ShutdownEvent event) {
		try {
			database.close();
		} catch(Exception e) {
			LOG.error("cannot close database", e);
		}
	}

}
