package io.github.jdiscordbots.mee.bypasser;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Deprecated
public class DataStorage implements Serializable {
	private static final long serialVersionUID = 3744043678519602689L;
	//<Guild id,data>
	private Map<String, GuildStorage> data = new HashMap<>();

	public DataStorage(){
		
	}
	
	@Deprecated
	public DataStorage(Map<String, Map<Integer, String>> legacyStorage) {
		legacyStorage.forEach((guild,gData)->{
			GuildStorage storage=new GuildStorage();
			storage.rolesToAssign=gData;
			data.put(guild, storage);
		});
	}
	
	
	Map<String, GuildStorage> getData() {
		return data;
	}

	@Deprecated
	static class GuildStorage implements Serializable{

		private static final long serialVersionUID = -1051298790406370626L;
		//<role level,role id>
		private Map<Integer, String> rolesToAssign = new HashMap<>();
		private boolean autoRemoveLevels = true;
		Map<Integer, String> getRolesToAssign() {
			return rolesToAssign;
		}
		boolean isAutoRemoveLevels() {
			return autoRemoveLevels;
		}
		
		
	}
}
