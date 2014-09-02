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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.trentorise.smartcampus.presentation.common.exception.DataException;
import eu.trentorise.smartcampus.vas.communitymanager.converters.ProfileConverter;
import eu.trentorise.smartcampus.vas.communitymanager.model.MinimalProfile;
import eu.trentorise.smartcampus.vas.communitymanager.model.StoreProfile;
import eu.trentorise.smartcampus.vas.communitymanager.storage.ProfileStorage;

/**
 * <i>UserManager</i> manages the profile informations of all the user in the
 * system
 * 
 * @author mirko perillo
 * 
 */
@Component
public class UserManager {

	private static final Logger logger = Logger.getLogger(UserManager.class);

	@Autowired
	private ProfileStorage storage;

	/**
	 * returns all minimal profile of users who match part of name
	 * 
	 * @param name
	 *            the string to match with name of user
	 * @return the list of minimal profile of users which name contains
	 *         parameter or an empty list
	 * @throws CommunityManagerException
	 */
	public List<MinimalProfile> getUsers(String name)
			throws CommunityManagerException {
		try {
			return ProfileConverter.toMinimalProfile(storage.findByName(name));
		} catch (Exception e) {
			throw new CommunityManagerException();
		}
	}

	/**
	 * returns all users profile in the system
	 * 
	 * @return the list of all minimal profiles of users
	 * @throws CommunityManagerException
	 */
	public List<MinimalProfile> getUsers() throws CommunityManagerException {
		try {
			return ProfileConverter
					.toMinimalProfile((List<StoreProfile>) storage
							.getObjectsByType(StoreProfile.class));
		} catch (Exception e) {
			logger.error("Exception getting system users");
			throw new CommunityManagerException();
		}
	}

	/**
	 * returns the minimal profiles of given users
	 * 
	 * @param socialUserIds
	 *            list of social user ids
	 * @return the list of minimal profiles
	 * @throws CommunityManagerException
	 */
	public List<MinimalProfile> getUsers(List<Long> socialUserIds)
			throws CommunityManagerException {
		List<MinimalProfile> profiles = new ArrayList<MinimalProfile>();
		try {
			for (Long temp : socialUserIds) {
				MinimalProfile profile = getUserBySocialId(temp);
				if (profile != null) {
					profiles.add(profile);
				}
			}
		} catch (Exception e) {
			logger.error("Exception getting user profiles " + socialUserIds);
			throw new CommunityManagerException();
		}
		return profiles;
	}

	/**
	 * persist a profile
	 * 
	 * @param storeProfile
	 *            the profile to persist
	 * @return the stored profile
	 * @throws CommunityManagerException
	 */
	public StoreProfile create(StoreProfile storeProfile)
			throws CommunityManagerException {
		try {
			storeProfile.setFullname(createFullName(storeProfile));
			StoreProfile present = getStoreProfileByUserId(storeProfile
					.getUserId());
			if (present != null) {
				ProfileConverter.copyDifferences(storeProfile, present);
				storage.updateObject(present);
				storeProfile = present;
			} else {
				storage.storeObject(storeProfile);
				storeProfile = getStoreProfileByUserId(storeProfile.getUserId());
			}
			return storeProfile;
		} catch (Exception e) {
			throw new CommunityManagerException();
		}
	}

	/**
	 * Deletes a profile
	 * 
	 * @param storeProfile
	 *            profile to delete
	 * @return true if operation gone fine. false otherwise
	 * @throws CommunityManagerException
	 */
	public boolean delete(StoreProfile storeProfile)
			throws CommunityManagerException {
		try {
			storage.deleteObject(storeProfile);
			return true;
		} catch (DataException e) {
			throw new CommunityManagerException();
		}
	}

	/**
	 * updates a profile
	 * 
	 * @param storeProfile
	 *            profile to update
	 * @return true if operation gone fine, false otherwise
	 * @throws CommunityManagerException
	 */
	public boolean update(StoreProfile storeProfile)
			throws CommunityManagerException {
		try {
			storeProfile.setFullname(createFullName(storeProfile));
			storage.updateObject(storeProfile);
			return true;
		} catch (Exception e) {
			throw new CommunityManagerException();
		}
	}

	private String createFullName(StoreProfile storeProfile) {
		if (storeProfile != null) {
			return ((storeProfile.getName() == null ? "" : storeProfile
					.getName()) + " " + (storeProfile.getSurname() == null ? ""
					: storeProfile.getSurname())).trim();
		}
		return "";
	}

	/**
	 * returns the MinimalProfile of a given user
	 * 
	 * @param userId
	 *            id of user
	 * @return MinimalProfile of user or null if it doesn't exist
	 * @throws CommunityManagerException
	 */
	public MinimalProfile getUserById(long userId)
			throws CommunityManagerException {
		Map<String, Object> filter = new HashMap<String, Object>();
		filter.put("userId", userId);
		MinimalProfile profile = null;
		try {
			profile = ProfileConverter.toMinimalProfile(storage.searchObjects(
					StoreProfile.class, filter).get(0));
		} catch (IndexOutOfBoundsException e) {
			return null;
		} catch (Exception e) {
			throw new CommunityManagerException();
		}

		return profile;
	}

	/**
	 * returns the MinimalProfile of a given pictureUrl
	 * 
	 * @param userId
	 *            id of user
	 * @return MinimalProfile of user or null if it doesn't exist
	 * @throws CommunityManagerException
	 */
	public MinimalProfile getUserByPictureUrl(long picture)
			throws CommunityManagerException {
		Map<String, Object> filter = new HashMap<String, Object>();
		filter.put("pictureUrl", String.valueOf(picture));
		MinimalProfile profile = null;
		try {
			profile = ProfileConverter.toMinimalProfile(storage.searchObjects(
					StoreProfile.class, filter).get(0));
		} catch (IndexOutOfBoundsException e) {
			return null;
		} catch (Exception e) {
			throw new CommunityManagerException();
		}

		return profile;
	}

	/**
	 * returns the MinimalProfile of a user given its social id
	 * 
	 * @param socialId
	 *            social id of the user
	 * @return MinimalProfile of user or null if it doesn't exist
	 * @throws CommunityManagerException
	 */
	public MinimalProfile getUserBySocialId(long socialId)
			throws CommunityManagerException {
		Map<String, Object> filter = new HashMap<String, Object>();
		filter.put("socialId", socialId);
		MinimalProfile profile = null;
		try {
			profile = ProfileConverter.toMinimalProfile(storage.searchObjects(
					StoreProfile.class, filter).get(0));
		} catch (IndexOutOfBoundsException e) {
			return null;
		} catch (Exception e) {
			throw new CommunityManagerException();
		}

		return profile;
	}

	/**
	 * returns a StoreProfile given user id
	 * 
	 * @param userId
	 *            user id
	 * @return the StoreProfile or null if doesn't exist
	 * @throws CommunityManagerException
	 */
	public StoreProfile getStoreProfileByUserId(long userId)
			throws CommunityManagerException {
		Map<String, Object> filter = new HashMap<String, Object>();
		filter.put("userId", userId);
		try {
			return storage.searchObjects(StoreProfile.class, filter).get(0);
		} catch (IndexOutOfBoundsException e) {
			return null;
		} catch (Exception e) {
			throw new CommunityManagerException();
		}

	}

	/**
	 * returns StoreProfile of user given its social id
	 * 
	 * @param socialId
	 *            social id of the user
	 * @return StoreProfile or null if it doesn't exist
	 * @throws CommunityManagerException
	 */
	public StoreProfile getStoreProfileBySocialId(long socialId)
			throws CommunityManagerException {
		Map<String, Object> filter = new HashMap<String, Object>();
		filter.put("socialId", socialId);
		try {
			return storage.searchObjects(StoreProfile.class, filter).get(0);
		} catch (IndexOutOfBoundsException e) {
			return null;
		} catch (Exception e) {
			throw new CommunityManagerException();
		}

	}

}