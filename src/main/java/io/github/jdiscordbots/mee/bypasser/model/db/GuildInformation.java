package io.github.jdiscordbots.mee.bypasser.model.db;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import io.github.jdiscordbots.mee.bypasser.MsgListener.Holder;

@Entity
@Table(name = "guilds")
public class GuildInformation {
	@Id
	private String guildId;

	@OneToMany(fetch = FetchType.EAGER,mappedBy = "guild",cascade = CascadeType.REMOVE)
	private Set<RoleInformation> roles=new HashSet<>();
	
	@Column(name="autoremovelevels",nullable = false)
	private boolean autoRemoveLevels=true;
	
	@Column(name="lastRankCall",nullable = false)
	private long lastRankCall=0;

	public String getGuildId() {
		return guildId;
	}

	public void setGuildId(String guildId) {
		this.guildId = guildId;
	}

	public Set<RoleInformation> getRoles() {
		return roles;
	}

	public void setRoles(Set<RoleInformation> roles) {
		this.roles = roles;
	}

	public boolean isAutoRemoveLevels() {
		return autoRemoveLevels;
	}

	public void setAutoRemoveLevels(boolean autoRemoveLevels) {
		this.autoRemoveLevels = autoRemoveLevels;
	}

	public long getLastRankCall() {
		return lastRankCall;
	}
	
	public void setLastRankCall(long lastRankCall) {
		this.lastRankCall = lastRankCall;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(autoRemoveLevels, guildId, roles);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GuildInformation other = (GuildInformation) obj;
		return autoRemoveLevels == other.autoRemoveLevels && Objects.equals(guildId, other.guildId)
				&& Objects.equals(roles, other.roles);
	}
	
	public Map<String,Boolean> getRoleIdsAtLevel(int level) {
		
		Set<String> rolesToRemove = new HashSet<>();
		Set<String> rolesToAdd = new HashSet<>();
		Holder<Integer> highestLevelRole = new Holder<>(0);

		for (RoleInformation roleInfo : roles) {
			if (level >= roleInfo.getLevel()) {
				if (autoRemoveLevels) {
					if (rolesToAdd.isEmpty()) {
						rolesToAdd.add(roleInfo.getRoleId());
						highestLevelRole.set(roleInfo.getLevel());
					} else if (roleInfo.getLevel() > highestLevelRole.get()) {
						for (String r : rolesToAdd) {
							rolesToRemove.add(r);
						}
						rolesToAdd.clear();
						rolesToAdd.add(roleInfo.getRoleId());
						highestLevelRole.set(roleInfo.getLevel());
					} else {
						rolesToRemove.add(roleInfo.getRoleId());
					}
				} else {
					rolesToAdd.add(roleInfo.getRoleId());
				}
			}
		}
		Map<String,Boolean> ret=new HashMap<>();
		for (String r : rolesToAdd) {
			ret.put(r, true);
		}
		for (String r : rolesToRemove) {
			ret.put(r, false);
		}
		return ret;
	}
}
