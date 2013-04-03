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
import it.unitn.disi.sweb.webapi.model.entity.AttributeDef;
import it.unitn.disi.sweb.webapi.model.entity.DataType;
import it.unitn.disi.sweb.webapi.model.entity.Entity;
import it.unitn.disi.sweb.webapi.model.entity.Filter;
import it.unitn.disi.sweb.webapi.model.smartcampus.ac.Operation;
import it.unitn.disi.sweb.webapi.model.smartcampus.livetopics.LiveTopic;
import it.unitn.disi.sweb.webapi.model.smartcampus.livetopics.LiveTopicContentType;
import it.unitn.disi.sweb.webapi.model.smartcampus.livetopics.LiveTopicSource;
import it.unitn.disi.sweb.webapi.model.smartcampus.livetopics.LiveTopicStatus;
import it.unitn.disi.sweb.webapi.model.smartcampus.livetopics.LiveTopicSubject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.trentorise.smartcampus.ac.provider.model.User;
import eu.trentorise.smartcampus.common.Concept;
import eu.trentorise.smartcampus.common.Constants;
import eu.trentorise.smartcampus.common.SemanticHelper;
import eu.trentorise.smartcampus.common.ShareVisibility;
import eu.trentorise.smartcampus.vas.communitymanager.converters.SocialEngineConverter;
import eu.trentorise.smartcampus.vas.communitymanager.model.EntityType;
import eu.trentorise.smartcampus.vas.communitymanager.model.SharedContent;

/**
 * <i>SharingManager</i> manages all aspects about sharing resources in the
 * social network
 * 
 * @author mirko perillo
 * 
 */
@Component
public class SharingManager extends SocialEngineConnector {

	private static final Logger logger = Logger.getLogger(SharingManager.class);
	@Autowired
	private SocialEngineConverter socialConverter;

	/*
	 * type in social engine
	 * 
	 * 
	 * social community event experience computer file journey person location
	 * portfolio narrative
	 */

	private static final String[] supportedTypes = new String[] {
			Constants.ENTTIY_TYPE_EVENT, Constants.ENTTIY_TYPE_EXPERIENCE,
			Constants.ENTTIY_TYPE_FILE, Constants.ENTTIY_TYPE_JOURNEY,
			Constants.ENTTIY_TYPE_POI, Constants.ENTTIY_TYPE_STORY,
			Constants.ENTTIY_TYPE_PORTFOLIO };
	private static Map<String, Long> typeIds = null;

	private Map<String, Long> getTypesIds() throws WebApiException {
		if (typeIds == null) {
			typeIds = new HashMap<String, Long>(supportedTypes.length);
			for (int i = 0; i < supportedTypes.length; i++) {
				it.unitn.disi.sweb.webapi.model.entity.EntityType eType = socialEngineClient
						.readEntityType(supportedTypes[i], SemanticHelper
								.getSCCommunityEntityBase(socialEngineClient)
								.getKbLabel());
				if (eType != null) {
					typeIds.put(supportedTypes[i], eType.getId());
				} else {
					logger.error("Type " + supportedTypes[i]
							+ " is not instantiated");
				}
			}
		}
		return typeIds;
	}

	/**
	 * shares an entity with some share options
	 * 
	 * @param entityId
	 *            id of entity to share
	 * @param ownerId
	 *            social id of owner of entity
	 * @param visibility
	 *            sharing options, see ShareVisibility class to details
	 * @return true if operation gone fine
	 * @throws CommunityManagerException
	 */
	public boolean share(long entityId, long ownerId, ShareVisibility visibility)
			throws CommunityManagerException {
		try {
			SemanticHelper.shareEntity(socialEngineClient, entityId, ownerId,
					visibility);
			return true;
		} catch (WebApiException e) {
			logger.error("Exception sharing entity " + entityId + " of user "
					+ ownerId, e);
			throw new CommunityManagerException();

		}
	}

	/**
	 * unshares an entity
	 * 
	 * @param entityId
	 *            id of the entity to unshare
	 * @param ownerId
	 *            social id of the owner of the entity
	 * @return true if operation gone fine
	 * @throws CommunityManagerException
	 */
	public boolean unshare(long entityId, long ownerId)
			throws CommunityManagerException {
		try {
			SemanticHelper.unshareEntity(socialEngineClient, entityId, ownerId);
			return true;
		} catch (WebApiException e) {
			logger.error("Exception unsharing entity " + entityId);
			throw new CommunityManagerException();
		}
	}

	/**
	 * returns the shared contents for a user from particular sources
	 * 
	 * @param ownerId
	 *            id of the user destination of the shared contents
	 * @param userIds
	 *            list of social ids of user owners that shares the contents
	 * @param groupId
	 *            list of ids of groups that shares the contents
	 * @param communityIds
	 *            list of ids of communities that shares the contents
	 * @param position
	 *            counter for buffering the results
	 * @param size
	 *            number of results to retrieve
	 * @param filterType
	 *            type of entities to retrieve
	 * @param addUser
	 *            if true add minimal profile details about user who own the
	 *            entities, none details otherwise
	 * @return the list of contents shared with ownerId from the list of
	 *         userIds, groupId and communityIds, empty list if no shared
	 *         contents are present for the user
	 * @throws CommunityManagerException
	 */
	public List<SharedContent> getShared(long ownerId, List<Long> userIds,
			Long groupId, List<Long> communityIds, int position, int size,
			Long filterType, boolean addUser) throws CommunityManagerException {
		try {
			LiveTopic filter = new LiveTopic();
			LiveTopicSource filterSource = new LiveTopicSource();
			if (ownerId > 0) {
				filter.setActorId(ownerId); // <-- mandatory
			}
			if (userIds != null && !userIds.isEmpty()) {
				filterSource.setUserIds(new HashSet<Long>(userIds));
			}
			if (groupId != null) {

				// check if default group
				if (groupId == Constants.MY_PEOPLE_GROUP_ID) {
					if (ownerId != -1) {
						filterSource.setAllKnownUsers(true);
					} else {
						throw new CommunityManagerException(
								"Cannot user both null ownerId and default group");
					}
				} else if (groupId > 0) {
					filterSource.setGroupIds(Collections.singleton(groupId));
				}
			}

			if (communityIds != null && !communityIds.isEmpty()) {
				filterSource.setCommunityIds(new HashSet<Long>(communityIds));
			}
			filter.setSource(filterSource);

			LiveTopicSubject subject = new LiveTopicSubject();
			subject.setAllSubjects(true); // <-- important
			filter.setSubjects(Collections.singleton(subject));

			LiveTopicContentType type = new LiveTopicContentType();
			if (filterType != null) {
				type.setEntityTypeIds(Collections.singleton(filterType));
			} else {
				type.setEntityTypeIds(new HashSet<Long>(getTypesIds().values()));
			}
			filter.setType(type); // <-- mandatory
			filter.setStatus(LiveTopicStatus.ACTIVE); // <-- mandatory

			// long start = System.currentTimeMillis();
			// logger.info("START compute");
			List<Long> sharedIds = socialEngineClient
					.computeEntitiesForLiveTopic(filter, null, null);
			// logger.info("STOP compute " + (System.currentTimeMillis() -
			// start));
			Collections.sort(sharedIds, new Comparator<Long>() {
				public int compare(Long o1, Long o2) {
					return o2 > o1 ? 1 : o2 == o1 ? 0 : -1;
				}
			});
			if (position < sharedIds.size()) {
				sharedIds = sharedIds.subList(position,
						Math.min(position + size, sharedIds.size()));
			} else {
				return Collections.emptyList();
			}
			if (!sharedIds.isEmpty()) {
				// filter to retrieve only name attribute of entity
				Filter f = new Filter(null, new HashSet<String>(
						Arrays.asList("name")), false, false, 0, null, null,
						null, null);
				List<SharedContent> shared = new ArrayList<SharedContent>(
						sharedIds.size());
				// logger.info(String.format("START read items %s",
				// sharedIds.size()));
				List<Entity> results = socialEngineClient.readEntities(
						sharedIds, f);
				// logger.info("STOP read " + (System.currentTimeMillis() -
				// start));
				for (Entity e : results) {
					shared.add(socialConverter.toSharedContent(e, addUser));
				}
				return shared;
			} else {
				return Collections.emptyList();
			}
		} catch (WebApiException e) {
			logger.error("Exception getting user shared content", e);
			return Collections.emptyList();
		}
	}

	/**
	 * returns sharing options of a list of entities
	 * 
	 * @param ownerId
	 *            the social id of the owner of entities
	 * @param entityIds
	 *            list of id of entities
	 * @return the list of sharing options relative to the list of entities
	 * @throws CommunityManagerException
	 */
	public List<ShareVisibility> getAssignments(long ownerId,
			List<Long> entityIds) throws CommunityManagerException {
		List<ShareVisibility> visibilities = new ArrayList<ShareVisibility>();
		for (Long eid : entityIds) {
			visibilities.add(getAssignments(ownerId, eid));
		}
		return visibilities;
	}

	/**
	 * returns the sharing options of an entity
	 * 
	 * @param ownerId
	 *            the social id of the owner of the entity
	 * @param entityId
	 *            the id of the entity
	 * @return sharing options for given entity
	 * @throws CommunityManagerException
	 */
	public ShareVisibility getAssignments(long ownerId, long entityId)
			throws CommunityManagerException {
		try {
			return socialConverter.toShareVisibility(socialEngineClient
					.readAssignments(entityId, Operation.READ, ownerId));
		} catch (WebApiException e) {
			logger.error("Exception getting assignment of entity", e);
			throw new CommunityManagerException();
		}
	}

	public EntityType getEntityTypeByConcept(Concept concept)
			throws CommunityManagerException {
		long conceptGlobalId = getConceptGlobalId(concept.getId());
		try {
			return socialConverter.toEntityType(socialEngineClient
					.readEntityTypeByConceptGlobalId(
							conceptGlobalId,
							SemanticHelper.getSCCommunityEntityBase(
									socialEngineClient).getKbLabel()));
		} catch (WebApiException e) {
			logger.error("Exception getting entitytype by concept global id "
					+ conceptGlobalId, e);
			throw new CommunityManagerException();
		}
	}

	public EntityType getEntityTypeByName(String name)
			throws CommunityManagerException {
		try {
			return socialConverter.toEntityType(socialEngineClient
					.readEntityType(name, SemanticHelper
							.getSCCommunityEntityBase(socialEngineClient)
							.getKbLabel()));
		} catch (WebApiException e) {
			throw new CommunityManagerException(e);
		}
	}

	public EntityType getEntityTypeByConceptId(long conceptId)
			throws CommunityManagerException {
		long conceptGlobalId = getConceptGlobalId(conceptId);
		try {
			return socialConverter.toEntityType(socialEngineClient
					.readEntityTypeByConceptGlobalId(
							conceptGlobalId,
							SemanticHelper.getSCCommunityEntityBase(
									socialEngineClient).getKbLabel()));
		} catch (WebApiException e) {
			logger.error("Exception getting entitytype by concept global id "
					+ conceptGlobalId, e);
			throw new CommunityManagerException();
		}
	}

	public EntityType getEntityType(long entityTypeId)
			throws CommunityManagerException {
		try {
			return socialConverter.toEntityType(socialEngineClient
					.readEntityType(entityTypeId));
		} catch (Exception e) {
			logger.error("Exception getting entity type", e);
			throw new CommunityManagerException();
		}

	}

	public EntityType createEntityType(long conceptId)
			throws CommunityManagerException, AlreadyExistException {
		try {
			// search if entitytype already exists
			long conceptGlobalId = getConceptGlobalId(conceptId);
			if (socialEngineClient.readEntityTypeByConceptGlobalId(
					conceptGlobalId, "uk") != null) {
				throw new AlreadyExistException("entityType binded to concept "
						+ conceptId + " already exists");
			}
			it.unitn.disi.sweb.webapi.model.entity.EntityType et = new it.unitn.disi.sweb.webapi.model.entity.EntityType(
					null, null, null, conceptGlobalId);
			AttributeDef textTag = new AttributeDef(null, null, null, null,
					34287L, DataType.STRING, true, null, null);
			AttributeDef semTag = new AttributeDef(null, null, null, null,
					98309L, DataType.SEMANTIC_STRING, true, null, null);
			AttributeDef name = new AttributeDef(null, null, null, null, 2L,
					DataType.STRING, true, null, null);
			AttributeDef entityTag = new AttributeDef(null, null, null, null,
					1L, DataType.RELATION, true, null, null);
			AttributeDef description = new AttributeDef(null, null, null, null,
					3L, DataType.STRING, true, null, null);

			et.getAttrDefs().add(name);
			et.getAttrDefs().add(semTag);
			et.getAttrDefs().add(textTag);
			et.getAttrDefs().add(entityTag);
			et.getAttrDefs().add(description);

			long id = socialEngineClient.create(et);
			return socialConverter.toEntityType(socialEngineClient
					.readEntityType(id));
		} catch (WebApiException e) {
			logger.error("Exception creating entity type", e);
			throw new CommunityManagerException();
		}

	}

	public boolean deleteEntityType(long id) throws CommunityManagerException {
		try {
			return socialEngineClient.deleteEntityType(id);
		} catch (WebApiException e) {
			logger.error("Exception deleting entity type " + id, e);
			throw new CommunityManagerException();
		}
	}

	public Concept getConceptById(long id) throws CommunityManagerException {
		try {
			return socialConverter
					.toConcept(socialEngineClient.readConcept(id));
		} catch (WebApiException e) {
			logger.error("Exception getting concept", e);
			throw new CommunityManagerException();
		}
	}

	public Concept getConceptByGlobalId(long id)
			throws CommunityManagerException {
		try {
			return socialConverter.toConcept(socialEngineClient
					.readConceptByGlobalId(id, SemanticHelper
							.getSCCommunityEntityBase(socialEngineClient)
							.getKbLabel()));
		} catch (WebApiException e) {
			logger.error("Exception getting concept", e);
			throw new CommunityManagerException();
		}
	}

	public long getConceptGlobalId(long conceptId)
			throws CommunityManagerException {
		try {
			return socialEngineClient.readConcept(conceptId).getGlobalId();
		} catch (WebApiException e) {
			logger.error("Exception getting concept gloabal id", e);
			throw new CommunityManagerException();
		}
	}

	public List<Concept> getConceptSuggestions(String prefix, int maxResults)
			throws CommunityManagerException {
		try {
			return SemanticHelper.getSuggestions(socialEngineClient, prefix,
					maxResults);
		} catch (WebApiException e) {
			logger.error("Exception getting concept suggestions", e);
			throw new CommunityManagerException();
		}
	}

	public eu.trentorise.smartcampus.vas.communitymanager.model.Entity createEntity(
			eu.trentorise.smartcampus.vas.communitymanager.model.Entity entity)
			throws CommunityManagerException {
		try {
			long id = SemanticHelper.createEntity(socialEngineClient,
					entity.getCreatorId(), entity.getType(), entity.getName(),
					entity.getDescription(), entity.getTags(),
					entity.getRelations()).getId();
			entity.setId(id);
			return entity;
		} catch (WebApiException e) {
			logger.error("Exception creating entity", e);
			throw new CommunityManagerException();
		}
	}

	public void updateEntity(
			eu.trentorise.smartcampus.vas.communitymanager.model.Entity entity)
			throws CommunityManagerException {
		try {
			SemanticHelper.updateEntity(socialEngineClient, entity.getId(),
					entity.getName(), entity.getDescription(),
					entity.getTags(), entity.getRelations()).getId();
		} catch (WebApiException e) {
			logger.error("Exception creating entity", e);
			throw new CommunityManagerException();
		}
	}

	public boolean deleteEntity(long entityId) throws CommunityManagerException {
		try {
			return SemanticHelper.deleteEntity(socialEngineClient, entityId);
		} catch (WebApiException e) {
			logger.error("Exception deleting entity " + entityId, e);
			throw new CommunityManagerException();
		}
	}

	public boolean checkPermission(User user, long entityId)
			throws CommunityManagerException {
		Filter f = new Filter(null, new HashSet<String>(Arrays.asList("name")),
				false, false, 0, null, null, null, null);
		try {
			Entity e = socialEngineClient.readEntity(entityId, f);
			long ownerId = socialEngineClient.readActorByEntityBase(
					e.getEntityBase().getId()).getId();
			return user.getSocialId() == ownerId;
		} catch (WebApiException e1) {
			logger.error("Exception reading entity " + entityId, e1);
			throw new CommunityManagerException();
		}
	}
}
