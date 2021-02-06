package io.github.jdiscordbots.mee.bypasser.model;

import java.util.Objects;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name="roles",uniqueConstraints = {@UniqueConstraint(columnNames = {"guild","level"},name = "unq_levelperguild")/*,@UniqueConstraint(columnNames = "roleId",name="unq_roleperguild")*/})
public class RoleInformation {
	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
	
	@Column(name="level",nullable = false)
	private int level;
	
	@Column(name="roleid",nullable = false)
	private String roleId;
	
	@ManyToOne(cascade = CascadeType.REMOVE)
	@JoinColumn(name = "guild",foreignKey = @ForeignKey(name="fk_roles_guild"),nullable = false)
	private GuildInformation guild;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public String getRoleId() {
		return roleId;
	}

	public void setRoleId(String roleId) {
		this.roleId = roleId;
	}

	public GuildInformation getGuild() {
		return guild;
	}
	
	public void setGuild(GuildInformation guild) {
		this.guild = guild;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(id, level, roleId);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RoleInformation other = (RoleInformation) obj;
		return Objects.equals(id, other.id) && level == other.level && Objects.equals(roleId, other.roleId);
	}
}
