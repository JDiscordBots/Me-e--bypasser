package io.github.jdiscordbots.mee.bypasser;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Map;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.jdiscordbots.mee.bypasser.model.db.GuildInformation;
import io.github.jdiscordbots.mee.bypasser.model.db.RoleInformation;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Persistence;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;

public class DataBaseController implements AutoCloseable {
	
	private static class InstanceHolder{
		private static final DataBaseController INSTANCE=new DataBaseController();
	}

	private static final String LEGACY_ROLES_FILE_NAME = "roles.dat";
	private static final Logger LOG = LoggerFactory.getLogger(DataBaseController.class);

	private EntityManagerFactory entityManagerFactory;
	private EntityManager entityManager;

	private Query deleteRoleByLevelStatement;
	private Query deleteRoleByIdStatement;

	private TypedQuery<String> loadRoleStatement;

	private DataBaseController() {
		try {
			load();
		} catch (ClassNotFoundException | IOException e) {
			throw new RuntimeException("Could not load role database",e);
		}
	}
	
	public static DataBaseController getInstance() {
		return InstanceHolder.INSTANCE;
	}

	@SuppressWarnings("unchecked")
	private void load() throws IOException, ClassNotFoundException {
		final DataStorage data;
		if (!new File("roles.mv.db").exists()) {
			if (new File(LEGACY_ROLES_FILE_NAME).exists()) {
				LOG.info("load legacy role storage");
				try (ObjectInputStream ois = new ObjectInputStream(
						new BufferedInputStream(new FileInputStream(LEGACY_ROLES_FILE_NAME)))) {
					Object readObj = ois.readObject();
					if (readObj instanceof Map) {
						data = new DataStorage((Map<String, Map<Integer, String>>) readObj);
					} else if (readObj instanceof DataStorage) {
						data = (DataStorage) readObj;
					} else if (readObj == null) {
						LOG.error("legacy role storage only null object");
						data = null;
					} else {
						data = null;
						LOG.error("deserialized role storage object has unexcepted data type {}",
								readObj.getClass().getName());
					}
				}
			} else {
				data = null;
			}
		} else {
			data = null;
		}
		entityManagerFactory = Persistence.createEntityManagerFactory("sqlite");
		entityManager = entityManagerFactory.createEntityManager();
		deleteRoleByLevelStatement = entityManager
				.createQuery("DELETE FROM RoleInformation WHERE guild.guildId = :guild AND level = :level");
		deleteRoleByIdStatement = entityManager
				.createQuery("DELETE FROM RoleInformation WHERE guild.guildId = :guild AND roleId = :role");
		loadRoleStatement = entityManager.createQuery(
				"SELECT i.roleId FROM RoleInformation i WHERE i.guild.guildId = :guild AND i.level = :level",
				String.class);
		if (data != null) {
			LOG.info("save legacy information to DB");
			executeInTransaction(() -> {
				data.getData().forEach((guildId, guildStorage) -> {
					GuildInformation guildInfo = new GuildInformation();
					guildInfo.setGuildId(guildId);
					guildInfo.setAutoRemoveLevels(guildStorage.isAutoRemoveLevels());
					guildStorage.getRolesToAssign().forEach((level, role) -> {
						RoleInformation roleInfo = new RoleInformation();
						roleInfo.setGuild(guildInfo);
						guildInfo.getRoles().add(roleInfo);
						roleInfo.setLevel(level);
						roleInfo.setRoleId(role);
						entityManager.persist(roleInfo);
					});
					entityManager.persist(guildInfo);
				});
			});
		}
	}

	public void executeInTransaction(Runnable toExecute) {
		executeInTransaction(() -> {
			toExecute.run();
			return null;
		});
	}

	public <T> T executeInTransaction(Supplier<T> toExecute) {
		if(entityManager.getTransaction().isActive()) {
			return toExecute.get();
		}
		try {
			entityManager.getTransaction().begin();
			T result = toExecute.get();
			entityManager.getTransaction().commit();
			return result;
		} catch (Exception e) {
			try {
				entityManager.getTransaction().rollback();
			} catch (RuntimeException ex) {
				e.addSuppressed(ex);
			}
			throw e;
		}
	}

	public GuildInformation loadGuildInformation(String guildId) {
		GuildInformation guildInfo = entityManager.find(GuildInformation.class, guildId);
		if (guildInfo == null) {
			guildInfo = new GuildInformation();
			guildInfo.setGuildId(guildId);
		}
		return guildInfo;
	}

	private static void runMultipleThrowExceptionsAfterAllExecuted(Runnable... toClose) {
		RuntimeException occured = null;
		for (Runnable cl : toClose) {
			try {
				cl.run();
			} catch (RuntimeException e) {
				if (occured == null) {
					occured = e;
				} else {
					occured.addSuppressed(e);
				}
			}
		}
		if (occured != null) {
			throw occured;
		}
	}

	@Override
	public void close() throws Exception {
		runMultipleThrowExceptionsAfterAllExecuted(entityManager::close, entityManagerFactory::close);
	}

	public void save(Object... toSave) {
		executeInTransaction(() -> {
			for (Object object : toSave) {
				entityManager.persist(object);
			}
		});
	}

	public String getRole(String id, int level) {
		loadRoleStatement.setParameter("guild", id);
		loadRoleStatement.setParameter("level", level);
		try {
			return loadRoleStatement.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	public String removeRole(String guildId, int level) {
		return executeInTransaction(() -> {
			GuildInformation guildInformation = loadGuildInformation(guildId);
			if (!guildInformation.getRoles().removeIf(role -> role.getLevel() == level)) {
				return null;
			}
			String ret = getRole(guildId, level);
			if (ret == null) {
				return null;
			}
			deleteRoleByLevelStatement.setParameter("guild", guildId);
			deleteRoleByLevelStatement.setParameter("level", level);
			int delCount=deleteRoleByLevelStatement.executeUpdate();
			if(delCount>0) {
				entityManager.persist(guildInformation);
				return ret;
			}
			return null;
		});
	}

	public void removeRole(String guildId, String roleId) {
		executeInTransaction(()->{
			deleteRoleByIdStatement.setParameter("guild", guildId);
			deleteRoleByIdStatement.setParameter("role", roleId);
			deleteRoleByIdStatement.executeUpdate();
		});
	}

	public void removeRole(RoleInformation toRemove) {
		removeRole(toRemove.getGuild().getGuildId(),toRemove.getRoleId());
		entityManager.remove(toRemove);
	}
}
