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
import it.unitn.disi.sweb.webapi.model.entity.EntityType;
import it.unitn.disi.sweb.webapi.model.smartcampus.livetopics.LiveTopic;
import it.unitn.disi.sweb.webapi.model.smartcampus.livetopics.LiveTopicStatus;
import it.unitn.disi.sweb.webapi.model.ss.Concept;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.trentorise.smartcampus.vas.communitymanager.converters.SocialEngineConverter;
import eu.trentorise.smartcampus.vas.communitymanager.model.Topic;

/**
 * <i>TopicManager</i> manages all aspect about the topics
 * 
 * @author mirko perillo
 * 
 */
@Component
public class TopicManager extends SocialEngineConnector {

	public TopicManager() throws IOException {
		super();
	}

	private static final Logger logger = Logger.getLogger(TopicManager.class);

	@Autowired
	private SocialEngineConverter socialConverter;

	/**
	 * saves a topic for a user
	 * 
	 * @param socialUserId
	 *            social id of owner of the topic
	 * @param topic
	 *            topic data
	 * @return the id of saved topic
	 * @throws CommunityManagerException
	 */
	public long create(long socialUserId, Topic topic)
			throws CommunityManagerException {
		LiveTopic liveTopic = socialConverter.fromTopic(null, topic);
		liveTopic.setActorId(socialUserId);
		try {

			return socialEngineClient.create(liveTopic);
		} catch (WebApiException e) {
			logger.error("Exception creating topic for user " + socialUserId);
			throw new CommunityManagerException();
		}
	}

	/**
	 * returns a concept given its id
	 * 
	 * @param socialId
	 *            the id of concept
	 * @return the concept or null if it doesn't exist
	 * @throws CommunityManagerException
	 */
	public Concept getConcept(long socialId) throws CommunityManagerException {
		try {
			return socialEngineClient.readConcept(socialId);
		} catch (WebApiException e) {
			logger.error("Exception getting concept " + socialId);
			throw new CommunityManagerException();
		}
	}

	private EntityType getEntityType(long socialId)
			throws CommunityManagerException {

		try {
			return socialEngineClient.readEntityType(socialId);
		} catch (WebApiException e) {
			logger.error("Exception getting entitytype " + socialId);
			throw new CommunityManagerException();
		}
	}

	private EntityType getEntityType(String name)
			throws CommunityManagerException {

		try {
			return socialEngineClient.readEntityType(name, "uk");
		} catch (WebApiException e) {
			logger.error("Exception getting entitytype " + name);
			throw new CommunityManagerException();
		}
	}

	/**
	 * returns entity type name given entity type id
	 * 
	 * @param socialId
	 *            the id of entity type
	 * @return the type name of entity
	 * @throws CommunityManagerException
	 */
	public String getEntityTypeName(long socialId)
			throws CommunityManagerException {
		EntityType et = getEntityType(socialId);
		return (et != null) ? et.getName() : null;

	}

	/**
	 * returns a list of entity type id given its name
	 * 
	 * @param names
	 *            list of names of entity types
	 * @return the list of ids of entity types
	 * @throws CommunityManagerException
	 */
	public List<Long> getEntityTypeId(List<String> names)
			throws CommunityManagerException {
		List<Long> ids = new ArrayList<Long>();
		try {
			for (String name : names) {
				ids.add(getEntityType(name).getId());
			}
		} catch (NullPointerException e) {
			logger.error("Exception getting entity type ids", e);
			throw new CommunityManagerException();
		}
		return ids;
	}

	/**
	 * returns id of entity type given its name
	 * 
	 * @param name
	 *            the name of entity type
	 * @return the id of entity type
	 * @throws CommunityManagerException
	 */
	public Long getEntityTypeId(String name) throws CommunityManagerException {
		try {
			return getEntityType(name).getId();
		} catch (NullPointerException e) {
			throw new CommunityManagerException();
		}
	}

	/**
	 * change the status of a topic
	 * 
	 * @param topicId
	 *            the id of topic
	 * @param status
	 *            new status
	 * @return true if operation gone fine, false otherwise
	 * @throws CommunityManagerException
	 */
	public boolean changeStatus(long topicId, int status)
			throws CommunityManagerException {
		try {
			LiveTopic lt = socialEngineClient.readLiveTopic(topicId);
			LiveTopicStatus ltStatus = socialConverter.convertStatus(status);
			// if convert in a null status input status isn't valid
			if (ltStatus == null) {
				return false;
			}
			lt.setStatus(ltStatus);
			socialEngineClient.update(lt);
			return true;
		} catch (Exception e) {
			logger.error("Exception changing status of topic", e);
			throw new CommunityManagerException();
		}

	}

	/**
	 * deletes a topic
	 * 
	 * @param topicId
	 *            id of the topic
	 * @return tur if operation gone fine, false otherwise
	 * @throws CommunityManagerException
	 */
	public boolean delete(long topicId) throws CommunityManagerException {
		try {
			return socialEngineClient.deleteTopic(topicId);
		} catch (WebApiException e) {
			throw new CommunityManagerException();
		}
	}

	/**
	 * returns a topic
	 * 
	 * @param topicId
	 *            the id of the topic
	 * @return the topic or threw a CommunityManagerException if it doesn't
	 *         exist
	 * @throws CommunityManagerException
	 */
	public Topic getTopic(long topicId) throws CommunityManagerException {

		try {
			return socialConverter.toTopic(socialEngineClient
					.readLiveTopic(topicId));
		} catch (WebApiException e) {
			throw new CommunityManagerException();
		}

	}

	/**
	 * returns all the topics of a user
	 * 
	 * @param socialUserId
	 *            the social id of the user
	 * @return the list of the topic of the user or null if user doesn't have
	 *         topics
	 * @throws CommunityManagerException
	 */
	public List<Topic> getTopics(long socialUserId)
			throws CommunityManagerException {
		try {
			return socialConverter.toTopic(socialEngineClient.readLiveTopics(
					socialUserId, null, false));
		} catch (WebApiException e) {
			throw new CommunityManagerException();
		}
	}

	/**
	 * updates a topic
	 * 
	 * @param socialUserId
	 *            the social id of owner of the topic
	 * @param topic
	 *            topic data
	 * @return true if operation gone fine, false otherwise
	 * @throws CommunityManagerException
	 */
	public boolean update(long socialUserId, Topic topic)
			throws CommunityManagerException {
		try {
			LiveTopic lt = socialEngineClient
					.readLiveTopic(topic.getSocialId());

			lt = socialConverter.fromTopic(lt, topic);
			lt.setActorId(socialUserId);
			socialEngineClient.update(lt);
		} catch (WebApiException e) {
			logger.error("Exception updating topic", e);
			throw new CommunityManagerException();
		}
		return true;
	}

	/**
	 * returns all entity types
	 * 
	 * @throws WebApiException
	 */
	public void getTypes() throws WebApiException {
		for (EntityType t : socialEngineClient.readEntityTypes("uk")) {
			logger.info(t.getName());
		}
	}

	/**
	 * checks permission to manage a topic
	 * 
	 * @param socialUserId
	 *            social id of user who want to manage topic
	 * @param topicId
	 *            the id of topic to manage
	 * @return true if user can manage topic, false otherwise
	 * @throws CommunityManagerException
	 */
	public boolean checkPermission(long socialUserId, long topicId)
			throws CommunityManagerException {
		try {
			return socialEngineClient.readLiveTopic(topicId).getActorId()
					.equals(socialUserId);
		} catch (Exception e) {
			throw new CommunityManagerException();
		}
	}

}
