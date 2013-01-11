/*******************************************************************************
 * Copyright 2012-2013 Trento RISE
 * 
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0
 * 
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 ******************************************************************************/
package eu.trentorise.smartcampus.vas.communitymanager.managers;

import it.unitn.disi.sweb.webapi.client.WebApiException;
import it.unitn.disi.sweb.webapi.model.smartcampus.social.User;
import it.unitn.disi.sweb.webapi.model.smartcampus.social.UserGroup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.trentorise.smartcampus.common.Constants;
import eu.trentorise.smartcampus.vas.communitymanager.converters.SocialEngineConverter;
import eu.trentorise.smartcampus.vas.communitymanager.model.Group;

/**
 * <i>GroupManager</i> manages the group functionalities of social network
 * 
 * @author mirko perillo
 * 
 */
@Component
public class GroupManager extends SocialEngineConnector {

	public GroupManager() throws IOException {
		super();
	}

	private static final Logger logger = Logger.getLogger(GroupManager.class);

	@Autowired
	private SocialEngineConverter socialConverter;

	@Autowired
	private UserManager userManager;

	/**
	 * returns the set of ids of users known by a user
	 * 
	 * @param socialUserId
	 *            social id of user
	 * @return the set of known users by given user
	 * @throws CommunityManagerException
	 */
	public Set<Long> getKnownUsers(long socialUserId)
			throws CommunityManagerException {
		User user;
		try {
			user = socialEngineClient.readUser(socialUserId);
		} catch (WebApiException e) {
			logger.error("Exception getting known users of user "
					+ socialUserId);
			throw new CommunityManagerException();
		}
		return user.getKnownUserIds();
	}

	/**
	 * returns the list of groups relative given list of ids
	 * 
	 * @param groupIds
	 *            list of group ids
	 * @return the list of group entities from given ids
	 * @throws CommunityManagerException
	 */
	public List<Group> getGroups(List<Long> groupIds)
			throws CommunityManagerException {
		try {
			return socialConverter.toGroup(socialEngineClient
					.readUserGroups(groupIds));
		} catch (Exception e) {
			logger.error("Exception retrieving social engine groups", e);
			throw new CommunityManagerException();
		}
	}

	/**
	 * returns the default group for given user
	 * 
	 * @param socialUserId
	 *            social id of the user
	 * @return default group containing all known users by given user
	 * @throws CommunityManagerException
	 */
	public Group getDefaultGroup(long socialUserId)
			throws CommunityManagerException {
		try {
			User user = socialEngineClient.readUser(socialUserId);
			user.getKnownUserIds();
			Group defaultGroup = new Group();
			defaultGroup.setName(Constants.MY_PEOPLE_GROUP_NAME);
			defaultGroup.setSocialId(Constants.MY_PEOPLE_GROUP_ID);
			defaultGroup.setUsers(userManager.getUsers(new ArrayList<Long>(user
					.getKnownUserIds())));
			return defaultGroup;
		} catch (Exception e) {
			logger.error("Exception getting default group for user "
					+ socialUserId);
			throw new CommunityManagerException();
		}
	}

	/**
	 * returns groups created by the user
	 * 
	 * @param socialUserId
	 *            social id of the user
	 * @return the list of user groups
	 * @throws CommunityManagerException
	 */
	public List<Group> getGroups(long socialUserId)
			throws CommunityManagerException {
		List<Group> groups = new ArrayList<Group>();
		// add default group
		Group defaultGroup = getDefaultGroup(socialUserId);
		if (defaultGroup != null) {
			groups.add(defaultGroup);
		}
		try {
			User user = socialEngineClient.readUser(socialUserId);
			Set<Long> groupIds = user.getUserGroupIds();
			for (Long groupId : groupIds) {
				groups.add(socialConverter.toGroup(socialEngineClient
						.readUserGroup(groupId)));
			}

		} catch (Exception e) {
			logger.error("Exception retrieving social engine groups", e);
			throw new CommunityManagerException();
		}
		return groups;
	}

	/**
	 * returns the group by given id
	 * 
	 * @param groupId
	 *            id of the group
	 * @return the group or null if it doesn't exist
	 * @throws CommunityManagerException
	 */
	public Group getGroup(long groupId) throws CommunityManagerException {
		try {
			return socialConverter.toGroup(socialEngineClient
					.readUserGroup(groupId));
		} catch (Exception e) {
			logger.error("Exception during delete sociale engine group "
					+ groupId, e);
			throw new CommunityManagerException();
		}
	}

	/**
	 * deletes a group
	 * 
	 * @param groupId
	 *            id of group to delete
	 * @return true if operation gone fine, false otherwise
	 * @throws CommunityManagerException
	 */
	public boolean delete(long groupId) throws CommunityManagerException {
		boolean removed = false;
		try {
			removed = socialEngineClient.deleteUserGroup(groupId);
		} catch (WebApiException e) {
			logger.error("Exception deleting social engine group " + groupId, e);
			throw new CommunityManagerException();
		}
		return removed;
	}

	/**
	 * create a group for given user
	 * 
	 * @param socialUserId
	 *            social id of the user owner of the group
	 * @param groupName
	 *            name of the group
	 * @return the id of created group
	 * @throws CommunityManagerException
	 */
	public long create(long socialUserId, String groupName)
			throws CommunityManagerException {
		UserGroup group = new UserGroup();
		group.setName(groupName);
		group.setOwnerId(socialUserId);
		group.setUserIds(new HashSet<Long>());
		long groupId = -1;
		try {
			groupId = socialEngineClient.create(group);
			logger.debug("Group created, id " + groupId);
		} catch (Exception e) {
			logger.error("Exception during creation of social engine group "
					+ groupName, e);
			throw new CommunityManagerException();
		}

		return groupId;
	}

	/**
	 * updates of a group
	 * 
	 * @param group
	 *            group of update
	 * @return true if operation gone fine, false otherwise
	 * @throws CommunityManagerException
	 */
	public boolean update(Group group) throws CommunityManagerException {
		try {
			UserGroup usergroup = socialEngineClient.readUserGroup(group
					.getSocialId());
			usergroup.setName(group.getName());
			socialEngineClient.update(usergroup);
			return true;
		} catch (Exception e) {
			logger.error("exception updating group", e);
			throw new CommunityManagerException();
		}

	}

	/**
	 * adds a user to a group
	 * 
	 * @param ownerId
	 *            social id of the owner of group
	 * @param groupId
	 *            id of the group
	 * @param userIds
	 *            list of social user ids to add to group
	 * @return true if operation gone fine, false otherwise
	 * @throws CommunityManagerException
	 */
	public boolean addUser(long ownerId, long groupId, List<Long> userIds)
			throws CommunityManagerException {
		try {
			UserGroup group = socialEngineClient.readUserGroup(groupId);
			User owner = socialEngineClient.readUser(ownerId);
			for (Long uid : userIds) {
				group.getUserIds().add(uid);
				if (!owner.getKnownUserIds().contains(uid)) {
					owner.getKnownUserIds().add(uid);
				}
			}

			socialEngineClient.update(group);
			socialEngineClient.update(owner);
			return true;
		} catch (Exception e) {
			logger.error("exception add users to group " + groupId, e);
			throw new CommunityManagerException();
		}
	}

	/**
	 * adds a user to a list of groups
	 * 
	 * @param ownerId
	 *            social id of the owner of groups
	 * @param groupIds
	 *            list of groups in which add the user
	 * @param userIdToAdd
	 *            social id of the user to add to groups
	 * @return true if operation gone fine, false otherwise
	 * @throws CommunityManagerException
	 */
	public boolean addUser(long ownerId, List<Long> groupIds, long userIdToAdd)
			throws CommunityManagerException {
		try {
			User owner = socialEngineClient.readUser(ownerId);
			List<Long> groupsToRemove = groupsToRemove(
					memberOf(new ArrayList<Long>(owner.getUserGroupIds()),
							userIdToAdd), groupIds);
			removeUser(userIdToAdd, groupsToRemove);
		} catch (Exception e) {
			logger.error("Exception removing user from old groups");
		}

		try {
			User user = socialEngineClient.readUser(ownerId);
			if (!user.getKnownUserIds().contains(userIdToAdd)) {
				user.getKnownUserIds().add(userIdToAdd);
				socialEngineClient.update(user);
			}
		} catch (Exception e) {
			logger.error("Exception adding user " + userIdToAdd
					+ " to default group");
			throw new CommunityManagerException();
		}

		boolean success = true;
		for (Long groupId : groupIds) {
			if (!(success = addUser(ownerId, groupId, userIdToAdd))) {
				break;
			}
		}
		return success;
	}

	/**
	 * adds a user to a group
	 * 
	 * @param ownerId
	 *            social id of the owner of the group
	 * @param groupId
	 *            id of the group
	 * @param userIdToAdd
	 *            social id of the user to add to the group
	 * @return true if operation gone fine, false otherwise
	 * @throws CommunityManagerException
	 */
	private boolean addUser(long ownerId, long groupId, long userIdToAdd)
			throws CommunityManagerException {
		try {
			UserGroup group = socialEngineClient.readUserGroup(groupId);
			if (group.getUserIds() == null) {
				group.setUserIds(new HashSet<Long>());
			}
			if (!group.getUserIds().contains(userIdToAdd)) {
				group.getUserIds().add(userIdToAdd);
				socialEngineClient.update(group);
			}
		} catch (Exception e) {
			logger.error("Exception adding user " + userIdToAdd + " to group "
					+ groupId);
			throw new CommunityManagerException();
		}
		return true;
	}

	/**
	 * remove a user from a list of groups
	 * 
	 * @param userId
	 *            social id of user to remove
	 * @param groupIds
	 *            list of the groups from which remove the user
	 * @return true if operation gone fine, false otherwise
	 * @throws CommunityManagerException
	 */
	public boolean removeUser(long userId, List<Long> groupIds)
			throws CommunityManagerException {
		boolean success = false;
		for (Long groupId : groupIds) {
			if (!(success = removeUser(userId, groupId))) {
				break;
			}
		}
		return success;
	}

	/**
	 * removes a list of users from a group
	 * 
	 * @param userIds
	 *            list of social user ids to remove from the group
	 * @param groupId
	 *            id of the group
	 * @return true if operation gone fine, false otherwise
	 * @throws CommunityManagerException
	 */
	public boolean removeUser(List<Long> userIds, long groupId)
			throws CommunityManagerException {
		boolean success = false;
		try {
			UserGroup group = socialEngineClient.readUserGroup(groupId);
			if ((success = group.getUserIds().removeAll(userIds))) {
				socialEngineClient.update(group);
			}
		} catch (Exception e) {
			logger.error("Exception removing users from group " + groupId, e);
			throw new CommunityManagerException();
		}
		return success;
	}

	/**
	 * removes a user from a group
	 * 
	 * @param userId
	 *            social id of the user of remove
	 * @param groupId
	 *            id of the group
	 * @return true if operation gone fine, false otherwise
	 * @throws CommunityManagerException
	 */
	public boolean removeUser(long userId, long groupId)
			throws CommunityManagerException {
		boolean success = false;
		try {
			UserGroup group = socialEngineClient.readUserGroup(groupId);
			if (group.getUserIds() != null
					&& group.getUserIds().contains(userId)) {
				if ((success = group.getUserIds().remove(userId))) {
					socialEngineClient.update(group);
				}
			}
		} catch (Exception e) {
			logger.error("Exception removing user " + userId + " from group "
					+ groupId);
			throw new CommunityManagerException();
		}
		return success;
	}

	/**
	 * removes a user to remove from default group
	 * 
	 * @param ownerId
	 *            social id of the owner of the group
	 * @param userId
	 *            social id of the user to remove
	 * @return true if operation gone fine, false otherwise
	 * @throws Exception
	 */
	public boolean removeFromDefault(long ownerId, long userId)
			throws Exception {
		try {
			User user = socialEngineClient.readUser(ownerId);
			if (user.getKnownUserIds() != null
					&& user.getKnownUserIds().remove(userId)) {
				socialEngineClient.update(user);
				logger.info("Removed user " + userId
						+ " from default group of user " + ownerId);
				for (Long groupId : user.getUserGroupIds()) {
					if (!removeUser(userId, groupId)) {
						logger.error("Exception removing user " + userId
								+ " from group " + groupId);
					}
				}
			}
			return true;
		} catch (Exception e) {
			logger.error("Exception removing user " + userId
					+ " from default group of user " + ownerId, e);
			throw new CommunityManagerException();
		}
	}

	/**
	 * filters list of groups passed as parameter and returns only the groups
	 * which user is member of
	 * 
	 * @param groupIds
	 *            list of group ids
	 * @param userId
	 *            social id of the user
	 * @return a subset of list of group ids which user is member of, an empty
	 *         list otherwise
	 * @throws CommunityManagerException
	 */
	private List<Long> memberOf(List<Long> groupIds, long userId)
			throws CommunityManagerException {
		List<Long> memberOf = new ArrayList<Long>();
		for (Long groupId : groupIds) {
			UserGroup group;
			try {
				group = socialEngineClient.readUserGroup(groupId);
				if (group.getUserIds() != null
						&& group.getUserIds().contains(userId)) {
					memberOf.add(group.getId());
				}
			} catch (Exception e) {
				logger.error("Exception in method memberOf group " + groupId);
				throw new CommunityManagerException();
			}
		}
		return memberOf;
	}

	private List<Long> groupsToRemove(List<Long> oldSituation,
			List<Long> newSituation) {
		List<Long> groupsToRemove = new ArrayList<Long>();
		try {
			for (Long old : oldSituation) {
				if (!newSituation.contains(old)) {
					groupsToRemove.add(old);
				}
			}
		} catch (Exception e) {
		}
		return groupsToRemove;
	}

	/**
	 * checks permission constraints
	 * 
	 * @param userId
	 *            social id of user to checks
	 * @param groupId
	 *            id of group that user wants to manage
	 * @return true if user can manage the group, false otherwise
	 * @throws CommunityManagerException
	 */

	public boolean checkPermission(long userId, long groupId)
			throws CommunityManagerException {
		User user;
		try {
			user = socialEngineClient.readUser(userId);
			return user.getUserGroupIds().contains(groupId);
		} catch (Exception e) {
			throw new CommunityManagerException();
		}

	}
}
