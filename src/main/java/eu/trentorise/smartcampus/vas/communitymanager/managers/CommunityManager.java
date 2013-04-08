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
import it.unitn.disi.sweb.webapi.model.entity.Entity;
import it.unitn.disi.sweb.webapi.model.entity.EntityBase;
import it.unitn.disi.sweb.webapi.model.entity.EntityType;
import it.unitn.disi.sweb.webapi.model.smartcampus.social.User;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.trentorise.smartcampus.common.Constants;
import eu.trentorise.smartcampus.vas.communitymanager.converters.SocialEngineConverter;
import eu.trentorise.smartcampus.vas.communitymanager.model.Community;

/**
 * <i>CommunityManager</i> manages all aspects about communities
 * 
 * @author mirko
 * 
 */
@Component
public class CommunityManager extends SocialEngineConnector {

	public CommunityManager() throws IOException {
		super();
	}

	private static final Logger logger = Logger
			.getLogger(CommunityManager.class);

	private static Community defaultCommunity;

	@Autowired
	private SocialEngineConverter socialConverter;

	/**
	 * methods checks at initialization class if default community exists. If it
	 * doesn't exist then it's created
	 */
	@PostConstruct
	protected void init() throws CommunityManagerException {
		super.init();
		if ((defaultCommunity = getCommunity(Constants.SMARTCAMPUS_COMMUNITY)) == null) {
			logger.info("No default community is present in the system");
			defaultCommunity = new Community();
			defaultCommunity.setName(Constants.SMARTCAMPUS_COMMUNITY);
			if (create(defaultCommunity) > 0) {
				logger.info("Created default community: "
						+ Constants.SMARTCAMPUS_COMMUNITY);
				defaultCommunity = getCommunity(Constants.SMARTCAMPUS_COMMUNITY);
			} else {
				logger.warn("Exception creating default community");
			}
		} else {

			logger.info("Default community found in system, "
					+ Constants.SMARTCAMPUS_COMMUNITY);
		}
	}

	/**
	 * returns all communities present in the system
	 * 
	 * @return list of communities
	 * @throws CommunityManagerException
	 */
	public List<Community> getCommunities() throws CommunityManagerException {
		try {
			return socialConverter.toCommunity(socialEngineClient
					.readCommunities());
		} catch (WebApiException e) {
			throw new CommunityManagerException();
		}
	}

	/**
	 * returns communities of a given user. If belongsTo is true returns
	 * communities which user belongsTo, other communities otherwise
	 * 
	 * @param socialUserId
	 *            social id of user
	 * @param belongsTo
	 *            flag : if true returns communities which user belongs to,
	 *            other otherwise
	 * 
	 * @return the list of communities
	 * @throws CommunityManagerException
	 */
	public List<Community> getCommunities(long socialUserId, boolean belongsTo)
			throws CommunityManagerException {
		try {
			User user = socialEngineClient.readUser(socialUserId);
			if (belongsTo) {
				if (user.getKnownCommunityIds() == null || user.getKnownCommunityIds().isEmpty()) {
					return new ArrayList<Community>();
				} else {
					return socialConverter.toCommunity(socialEngineClient
							.readCommunities(new ArrayList<Long>(user
									.getKnownCommunityIds())));
				}
			} else {
				return new ArrayList<Community>(CollectionUtils.subtract(
						getCommunities(), getCommunities(socialUserId, true)));
			}
		} catch (Exception e) {
			logger.error("Exception getting communities of user "
					+ socialUserId, e);
			throw new CommunityManagerException();
		}
	}

	/**
	 * returns community for given name
	 * 
	 * @param name
	 *            name of community
	 * @return return the community or null if it doesn't exist
	 * @throws CommunityManagerException
	 */
	public Community getCommunity(String name) throws CommunityManagerException {
		try {
			return socialConverter.toCommunity(socialEngineClient
					.readCommunity(name));

		} catch (Exception e) {
			logger.error("Exception getting community " + name);
			throw new CommunityManagerException();
		}
	}

	/**
	 * returns community for given id
	 * 
	 * @param communityId
	 *            id of community
	 * @return the community or null if it doesn't exist
	 * @throws CommunityManagerException
	 */
	public Community getCommunity(long communityId)
			throws CommunityManagerException {
		try {
			return socialConverter.toCommunity(socialEngineClient
					.readCommunity(communityId));
		} catch (WebApiException e) {
			logger.error("Exception getting community " + communityId);
			throw new CommunityManagerException();
		}
	}

	/**
	 * creates a new community
	 * 
	 * @param community
	 *            data to persist
	 * @return the id of new community
	 * @throws CommunityManagerException
	 */

	public long create(Community community) throws CommunityManagerException {
		long id = -1;
		it.unitn.disi.sweb.webapi.model.smartcampus.social.Community toSave = new it.unitn.disi.sweb.webapi.model.smartcampus.social.Community();
		toSave.setName(community.getName());
		try {
			EntityBase eb = new EntityBase();
			eb.setLabel("SC_TEST_COMMUNITY_" + System.currentTimeMillis());
			// Re-read to get the ID of the default KB
			eb = socialEngineClient.readEntityBase(socialEngineClient
					.create(eb));

			EntityType communityType = socialEngineClient.readEntityType(
					"community", eb.getKbLabel());
			Entity entity = new Entity();
			entity.setEntityBase(eb);
			entity.setEtype(communityType);
			Long entityId = socialEngineClient.create(entity);
			toSave.setEntityId(entityId);
			toSave.setEntityBaseId(eb.getId());
			id = socialEngineClient.create(toSave);
		} catch (WebApiException e) {
			logger.error("Exception creating community", e);
			throw new CommunityManagerException();
		}
		return id;
	}

	/**
	 * deletes a community
	 * 
	 * @param communityId
	 *            id of community to delete
	 * @return true if delete gone fine, false otherwise
	 * @throws CommunityManagerException
	 */
	public boolean delete(long communityId) throws CommunityManagerException {
		try {
			return socialEngineClient.deleteCommunity(communityId);
		} catch (WebApiException e) {
			logger.error("Exception deleting community " + communityId);
			throw new CommunityManagerException();
		}
	}

	/**
	 * adds a user to a community
	 * 
	 * @param socialId
	 *            social id of the user to add
	 * @param communityId
	 *            id of community
	 * @return true if operation gone fine, false otherwise
	 * @throws CommunityManagerException
	 */
	public boolean addUser(long socialId, long communityId)
			throws CommunityManagerException {
		try {
			User user = socialEngineClient.readUser(socialId);
			if (user.getKnownCommunityIds() == null) {
				user.setKnownCommunityIds(new HashSet<Long>(1));
			}
			user.getKnownCommunityIds().add(communityId);
			socialEngineClient.update(user);
			return true;
		} catch (Exception e) {
			logger.error("Exception adding user " + socialId + " to community "
					+ communityId);
			throw new CommunityManagerException();
		}
	}

	/**
	 * removes a user from a community
	 * 
	 * @param socialId
	 *            social id of the user to remove
	 * @param communityId
	 *            id of community
	 * @return true if operation gone fine, false otherwise
	 */
	public boolean removeUser(long socialId, long communityId) {
		boolean success = false;
		try {
			User user = socialEngineClient.readUser(socialId);
			if (user.getKnownCommunityIds() != null &&  user.getKnownCommunityIds().contains(communityId)) {
				if (user.getKnownCommunityIds().remove(communityId)) {
					socialEngineClient.update(user);
					success = true;
				}
			}

		} catch (Exception e) {
			success = false;
		}
		return success;
	}

}
