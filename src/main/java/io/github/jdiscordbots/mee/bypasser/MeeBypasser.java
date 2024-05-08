package io.github.jdiscordbots.mee.bypasser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.EnumSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.jdiscordbots.command_framework.CommandFramework;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

public class MeeBypasser {

	private static final Logger LOG = LoggerFactory.getLogger(MeeBypasser.class);

	public static void main(String[] args) {
		File tokenFile = new File(".token");
		if(tokenFile.exists()){
			try(BufferedReader reader = new BufferedReader(
					new InputStreamReader(new FileInputStream(tokenFile), StandardCharsets.UTF_8)
			)){
				final DefaultShardManagerBuilder builder = DefaultShardManagerBuilder
					.createLight(reader.readLine(), EnumSet.of(GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT))
					.setAutoReconnect(true)
					.setStatus(OnlineStatus.ONLINE)
					.setMemberCachePolicy(MemberCachePolicy.NONE)
					.disableCache(EnumSet.allOf(CacheFlag.class))
					.setActivity(Activity.watching("https://github.com/JDiscordBots/Mee6-bypasser"))
					.setRequestTimeoutRetry(true);

				CommandFramework framework = new CommandFramework();
				framework.setSlashCommandsPerGuild(false);
				framework.setPrefix("mb!");
				builder.addEventListeners(new MsgListener(), framework.build());
				ShardManager manager = builder.build();

				for(JDA jda : manager.getShards()){
					jda.awaitReady();
				}
			}catch(IOException e){
				LOG.error("Cannot initialize bot", e);
			}catch(InterruptedException e){
				LOG.warn("The main thread was interruped while waiting for a shard to connect initially", e);
				Thread.currentThread().interrupt();
			}
		}else{
			try{
				Files.createFile(tokenFile.toPath());
			}catch(IOException e){
				LOG.error("Cannot create token file.", e);
			}
		}
	}

}
