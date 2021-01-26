package io.github.jdiscordbots.mee.bypasser;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import io.github.jdiscordbots.mee.bypasser.MsgListener.Holder;

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
	
	public Map<String,Boolean> getRoleIdsForGuildAtLevel(String guildId, int level) {
		GuildStorage gData = data.get(guildId);
		if(gData==null) {
			return Collections.emptyMap();
		}
		Set<String> rolesToRemove = new HashSet<>();
		Set<String> rolesToAdd = new HashSet<>();
		Holder<Integer> highestLevelRole = new Holder<>(0);

		gData.rolesToAssign.forEach((roleLevel, role) -> {

			if (level >= roleLevel) {
				if (gData.autoRemoveLevels) {
					if (rolesToAdd.isEmpty()) {
						rolesToAdd.add(role);
						highestLevelRole.set(roleLevel);
					} else if (roleLevel > highestLevelRole.get()) {
						for (String r : rolesToAdd) {
							rolesToRemove.add(r);
						}
						rolesToAdd.clear();
						rolesToAdd.add(role);
						highestLevelRole.set(roleLevel);
					} else {
						rolesToRemove.add(role);
					}
				} else {
					rolesToAdd.add(role);
				}
			}
		});
		Map<String,Boolean> ret=new HashMap<>();
		for (String r : rolesToAdd) {
			ret.put(r, true);
		}
		for (String r : rolesToRemove) {
			ret.put(r, false);
		}
		return ret;
	}
	public void setRole(String gId,int level,String roleId) {
		GuildStorage gRoles;
		if(data.containsKey(gId)) {
			gRoles=data.get(gId);
		} else {
			gRoles=new GuildStorage();
			data.put(gId, gRoles);
		}
		gRoles.rolesToAssign.put(level, roleId);
	}
	public String removeRole(String gId,int level) {
		String id=null;
		if(data.containsKey(gId)) {
			GuildStorage gRoles=data.get(gId);
			if(gRoles!=null) {
				id = gRoles.rolesToAssign.remove(level);
				if(gRoles.rolesToAssign.isEmpty()) {
					data.remove(gId);
				}
			}
		}
		return id;
	}
	public Map<Integer, String> getRoles(String gId) {
		GuildStorage gData = data.get(gId);
		if(gData==null) {
			return new HashMap<>();
		}else {
			return gData.rolesToAssign;
		}
	}
	public boolean isAutoRemoveLevels(String gId) {
		GuildStorage gData = data.get(gId);
		if(gData==null) {
			return true;
		}else {
			return gData.autoRemoveLevels;
		}
	}
	public void setAutoRemoveLevels(String gId,boolean autoRemoveLevels){
		GuildStorage gData = data.get(gId);
		if(gData==null) {
			gData=new GuildStorage();
			data.put(gId,gData);
		}
		gData.autoRemoveLevels=autoRemoveLevels;
	}

	private static class GuildStorage implements Serializable{

		private static final long serialVersionUID = -1051298790406370626L;
		//<role level,role id>
		private Map<Integer, String> rolesToAssign = new HashMap<>();
		private boolean autoRemoveLevels = true;
	}
}
