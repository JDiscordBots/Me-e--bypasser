package io.github.jdiscordbots.mee.bypasser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;

public class MeeAPI {
	private static final String URL="https://mee6.xyz/api/plugins/levels/leaderboard/";
	
	private MeeAPI() {
		//prevent instantiation
	}
	
	private static JSONObject getLeaderBoardData(String gId) throws IOException {
		URLConnection con = new URL(URL+gId).openConnection();
		con.setRequestProperty("User-Agent", UUID.randomUUID().toString());
		try(BufferedReader reader=new BufferedReader(new InputStreamReader(con.getInputStream(),StandardCharsets.UTF_8))){
			return new JSONObject(reader.lines().collect(Collectors.joining(" ")));
		}
	}
	public static int getLevel(String guildId,String uID) throws IOException {
		JSONArray players = getLeaderBoardData(guildId).getJSONArray("players");
		for (int i = 0; i < players.length(); i++) {
			JSONObject player = players.getJSONObject(i);
			if(uID.equals(player.getString("id"))) {
				return player.getInt("level");
			}
		}
		return 0;
	}
}
