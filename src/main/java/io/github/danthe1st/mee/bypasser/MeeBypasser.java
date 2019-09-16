package io.github.danthe1st.mee.bypasser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.security.auth.login.LoginException;

import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

public class MeeBypasser {

	public static void main(String[] args) {
		File tokenFile=new File(".token");
		if(tokenFile.exists()) {
			try(BufferedReader reader=new BufferedReader(new InputStreamReader(new FileInputStream(tokenFile)))){
				JDABuilder builder=new JDABuilder(AccountType.BOT);
				builder.setToken(reader.readLine());
				builder.addEventListeners(new MsgListener());
				JDA jda=builder.build();
				jda.awaitReady();
			} catch (LoginException | IOException | InterruptedException e) {
				e.printStackTrace();
			}
		}else {
			try {
				tokenFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
