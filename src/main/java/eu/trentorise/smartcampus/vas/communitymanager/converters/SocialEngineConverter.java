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
package eu.trentorise.smartcampus.vas.communitymanager.converters;

import it.unitn.disi.sweb.webapi.model.entity.Entity;
import it.unitn.disi.sweb.webapi.model.smartcampus.livetopics.LiveTopic;
import it.unitn.disi.sweb.webapi.model.smartcampus.livetopics.LiveTopicContentType;
import it.unitn.disi.sweb.webapi.model.smartcampus.livetopics.LiveTopicSource;
import it.unitn.disi.sweb.webapi.model.smartcampus.livetopics.LiveTopicStatus;
import it.unitn.disi.sweb.webapi.model.smartcampus.livetopics.LiveTopicSubject;
import it.unitn.disi.sweb.webapi.model.smartcampus.social.UserGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.trentorise.smartcampus.common.Concept;
import eu.trentorise.smartcampus.common.ShareVisibility;
import eu.trentorise.smartcampus.vas.communitymanager.Constants;
import eu.trentorise.smartcampus.vas.communitymanager.managers.CommunityManager;
import eu.trentorise.smartcampus.vas.communitymanager.managers.CommunityManagerException;
import eu.trentorise.smartcampus.vas.communitymanager.managers.GroupManager;
import eu.trentorise.smartcampus.vas.communitymanager.managers.TopicManager;
import eu.trentorise.smartcampus.vas.communitymanager.managers.UserManager;
import eu.trentorise.smartcampus.vas.communitymanager.model.Community;
import eu.trentorise.smartcampus.vas.communitymanager.model.Group;
import eu.trentorise.smartcampus.vas.communitymanager.model.MinimalProfile;
import eu.trentorise.smartcampus.vas.communitymanager.model.SharedContent;
import eu.trentorise.smartcampus.vas.communitymanager.model.Topic;

/**
 * <i>SocialEngineConverter</i> converts all model classes from and to social
 * engine model to smartcampus model
 * 
 * @author mirko perillo
 * 
 */
@Component
public class SocialEngineConverter {

	@Autowired
	private UserManager userManager;

	@Autowired
	private CommunityManager communityManager;

	@Autowired
	private GroupManager groupManager;

	@Autowired
	private TopicManager topicManager;

	public List<Group> toGroup(List<UserGroup> userGroups)
			throws CommunityManagerException {
		List<Group> groups = new ArrayList<Group>();
		try {
			for (UserGroup temp : userGroups) {
				groups.add(toGroup(temp));
			}
		} catch (NullPointerException e) {
			groups = null;
		}
		return groups;
	}

	public Group toGroup(UserGroup userGroup) throws CommunityManagerException {
		Group group = new Group();
		try {
			group.setSocialId(userGroup.getId());
			group.setName(userGroup.getName());
			if (userGroup.getUserIds() != null) {
				group.setUsers(userManager.getUsers(new ArrayList<Long>(
						userGroup.getUserIds())));
			}
		} catch (NullPointerException e) {
			group = null;
		}

		return group;
	}

	public Community toCommunity(
			it.unitn.disi.sweb.webapi.model.smartcampus.social.Community community) {
		Community com = new Community();
		try {
			com.setSocialId(community.getId());
			com.setName(community.getName());
			com.setId("" + community.getId());
		} catch (NullPointerException e) {
			com = null;
		}
		return com;
	}

	public List<Community> toCommunity(
			List<it.unitn.disi.sweb.webapi.model.smartcampus.social.Community> communities) {
		List<Community> comList = new ArrayList<Community>();
		try {
			for (it.unitn.disi.sweb.webapi.model.smartcampus.social.Community temp : communities) {
				comList.add(toCommunity(temp));
			}
		} catch (NullPointerException e) {
			comList = null;
		}
		return comList;
	}

	public Topic toTopic(LiveTopic lt) throws CommunityManagerException {
		Topic topic = new Topic();
		topic.setSocialId(lt.getId());
		topic.setName(lt.getName());
		topic.setKeywords(getKeywords(lt));
		topic.setConcepts(getConcepts(lt));
		topic.setCommunities(getCommunities(lt));
		topic.setEntities(getEntities(lt));
		topic.setGroups(getGroups(lt));
		topic.setUsers(getUsers(lt));
		topic.setContentTypes(getContentTypes(lt));
		topic.setStatus(convertStatus(lt.getStatus()));
		topic.setAllUsers(lt.getSource().isAllUsers());
		topic.setAllKnownUsers(lt.getSource().isAllKnownUsers());
		topic.setAllCommunities(lt.getSource().isAllCommunities());
		topic.setAllKnownCommunities(lt.getSource().isAllKnownCommunities());
		return topic;
	}

	public List<Topic> toTopic(List<LiveTopic> liveTopicList)
			throws CommunityManagerException {
		List<Topic> topics = new ArrayList<Topic>();
		try {
			for (LiveTopic lt : liveTopicList) {
				topics.add(toTopic(lt));
			}
		} catch (NullPointerException e) {
			topics = null;
		}
		return topics;
	}

	public LiveTopic fromTopic(LiveTopic liveTopic, Topic topic)
			throws CommunityManagerException {
		if (liveTopic == null) {
			liveTopic = new LiveTopic();
		}
		if (topic.getSocialId() > 0) {
			liveTopic.setId(topic.getSocialId());
		}
		liveTopic.setName(topic.getName());
		LiveTopicStatus status = convertStatus(topic.getStatus());
		liveTopic.setStatus((status == null) ? LiveTopicStatus.ACTIVE : status);
		liveTopic.setSource(createSource(liveTopic.getSource(), topic));
		liveTopic.setSubjects(createSubjects(liveTopic.getSubjects(),
				topic.getConcepts(), topic.getKeywords(), topic.getEntities()));
		liveTopic.setType(createType(liveTopic.getType(),
				topic.getContentTypes()));
		return liveTopic;
	}

	public int convertStatus(LiveTopicStatus ltStatus) {
		if (ltStatus == LiveTopicStatus.ACTIVE) {
			return Constants.TOPIC_ACTIVE;
		}
		if (ltStatus == LiveTopicStatus.SUSPENDED) {
			return Constants.TOPIC_SUSPEND;
		}

		return -1;
	}

	public LiveTopicStatus convertStatus(int topicStatus) {
		if (topicStatus == Constants.TOPIC_ACTIVE) {
			return LiveTopicStatus.ACTIVE;
		}
		if (topicStatus == Constants.TOPIC_SUSPEND) {
			return LiveTopicStatus.SUSPENDED;
		}

		return null;
	}

	private List<String> getContentTypes(LiveTopic lt)
			throws CommunityManagerException {
		List<String> contentTypes = new ArrayList<String>();
		if (lt.getType() != null && lt.getType().getEntityTypeIds() != null) {
			for (Long id : lt.getType().getEntityTypeIds()) {
				String typeName = topicManager.getEntityTypeName(id);
				if (typeName != null) {
					contentTypes.add(typeName);
				}
			}
			return contentTypes;
		} else {
			return null;
		}
	}

	private List<Community> getCommunities(LiveTopic lt)
			throws CommunityManagerException {
		if (lt.getSource() != null && lt.getSource().getCommunityIds() != null) {
			List<Community> communities = new ArrayList<Community>();
			for (Long communityId : lt.getSource().getCommunityIds()) {
				Community community = communityManager
						.getCommunity(communityId);
				if (community != null) {
					communities.add(community);
				}
			}
			return communities;
		} else {
			return null;
		}
	}

	private List<Concept> getEntities(LiveTopic lt) {
		List<Concept> entities = new ArrayList<Concept>();
		try {
			for (LiveTopicSubject subject : lt.getSubjects()) {
				if (subject.getEntityId() != null) {
					Concept nc = new Concept(subject.getEntityId(),
							subject.getKeyword());
					entities.add(nc);
				}
			}
		} catch (NullPointerException e) {
			return null;
		}
		return entities;
	}

	private List<Group> getGroups(LiveTopic lt)
			throws CommunityManagerException {
		if (lt.getSource() != null && lt.getSource().getGroupIds() != null) {
			List<Group> groups = new ArrayList<Group>();
			for (Long id : lt.getSource().getGroupIds()) {
				Group group = groupManager.getGroup(id);
				if (group != null) {
					groups.add(group);
				}
			}
			if (lt.getSource().isAllKnownUsers()) {
				groups.add(groupManager.getDefaultGroup(lt.getActorId()));
			}
			return groups;
		} else {
			return null;
		}
	}

	private List<MinimalProfile> getUsers(LiveTopic lt)
			throws CommunityManagerException {
		if (lt.getSource() != null && lt.getSource().getUserIds() != null) {
			List<MinimalProfile> mpList = new ArrayList<MinimalProfile>();
			for (Long id : lt.getSource().getUserIds()) {
				MinimalProfile mp = userManager.getUserBySocialId(id);
				if (mp != null) {
					mpList.add(mp);
				}
			}
			return mpList;
		} else {
			return null;
		}
	}

	private List<Concept> getConcepts(LiveTopic lt)
			throws CommunityManagerException {
		List<Concept> concepts = new ArrayList<Concept>();
		try {
			for (LiveTopicSubject subject : lt.getSubjects()) {
				if (subject.getEntityId() == null
						&& subject.getConceptId() != null) {
					it.unitn.disi.sweb.webapi.model.ss.Concept c = topicManager
							.getConcept(subject.getConceptId());
					if (c != null) {
						Concept nc = new Concept(c.getId(), c.getLabel());
						nc.setDescription(c.getDescription());
						nc.setSummary(c.getSummary());
						concepts.add(nc);
					}
				}
			}
		} catch (NullPointerException e) {
			return null;
		}
		return concepts;
	}

	private List<String> getKeywords(LiveTopic lt) {
		List<String> keywords = new ArrayList<String>();
		try {
			for (LiveTopicSubject subject : lt.getSubjects()) {
				if (subject.getKeyword() != null
						&& subject.getConceptId() == null
						&& subject.getEntityId() == null
						&& subject.getKeyword().length() > 0) {
					keywords.add(subject.getKeyword());
				}
			}
		} catch (NullPointerException e) {
			return null;
		}
		return keywords;
	}

	public LiveTopicSource createSource(LiveTopicSource source, Topic topic) {
		if (source == null) {
			source = new LiveTopicSource();
		}

		boolean emptyUsers = topic.getUsers() == null
				|| topic.getUsers().size() == 0;
		boolean emptyGroups = topic.getGroups() == null
				|| topic.getGroups().size() == 0;
		boolean emptyCommunities = topic.getCommunities() == null
				|| topic.getCommunities().size() == 0;

		source.setAllKnownCommunities(topic.isAllKnownCommunities());
		source.setAllKnownUsers(topic.isAllKnownUsers());
		source.setAllCommunities(topic.isAllCommunities());
		source.setAllUsers(topic.isAllUsers());

		if (!emptyCommunities) {
			Set<Long> ids = new HashSet<Long>();
			for (Community c : topic.getCommunities()) {
				ids.add(c.getSocialId());
			}
			source.setCommunityIds(ids);
		}
		if (!emptyGroups) {
			Set<Long> ids = new HashSet<Long>();
			for (Group g : topic.getGroups()) {
				if (!g.getName()
						.equals(eu.trentorise.smartcampus.common.Constants.MY_PEOPLE_GROUP_NAME))
					ids.add(g.getSocialId());
				else
					source.setAllKnownUsers(true);
			}
			source.setGroupIds(ids);
		}
		if (!emptyUsers) {
			Set<Long> ids = new HashSet<Long>();
			for (MinimalProfile mp : topic.getUsers()) {
				ids.add(mp.getSocialId());
			}
			source.setUserIds(ids);
		}

		return source;
	}

	public LiveTopicContentType createType(LiveTopicContentType type,
			List<String> typeNames) throws CommunityManagerException {
		if (type == null) {
			type = new LiveTopicContentType();
		}
		boolean emptyTypes = typeNames == null || typeNames.size() == 0;
		type.setAllTypes(emptyTypes);
		if (!emptyTypes) {
			List<Long> typeIds = topicManager.getEntityTypeId(typeNames);
			type.setEntityTypeIds((typeIds != null) ? new HashSet<Long>(typeIds)
					: Collections.<Long> emptySet());
		}
		return type;
	}

	public Set<LiveTopicSubject> createSubjects(Set<LiveTopicSubject> subjects,
			List<Concept> concepts, List<String> keywords,
			List<Concept> entities) {
		if (subjects == null) {
			subjects = new HashSet<LiveTopicSubject>();
		} else {
			subjects.clear();
		}

		if (concepts != null) {
			for (Concept concept : concepts) {
				LiveTopicSubject subject = new LiveTopicSubject();
				subject.setConceptId(concept.getId());
				subject.setKeyword(concept.getName());
				subjects.add(subject);
			}
		}
		if (keywords != null) {
			for (String keyword : keywords) {
				LiveTopicSubject subject = new LiveTopicSubject();
				subject.setKeyword(keyword);
				subjects.add(subject);
			}
		}

		if (entities != null) {
			for (Concept entityConcept : entities) {
				LiveTopicSubject subject = new LiveTopicSubject();
				subject.setEntityId(entityConcept.getId());
				subject.setKeyword(entityConcept.getName());
				subjects.add(subject);
			}
		}
		if (subjects.isEmpty()) {
			LiveTopicSubject subject = new LiveTopicSubject();
			subject.setAllSubjects(true);
			subject.setKeyword("any");// TODO remove later
			subjects.add(subject);
		}

		return subjects;
	}

	public LiveTopicSource fromShareVisibility(ShareVisibility visibility) {
		LiveTopicSource source = new LiveTopicSource();
		source.setAllCommunities(visibility.isAllCommunities());
		source.setAllKnownCommunities(visibility.isAllKnownCommunities());
		source.setAllKnownUsers(visibility.isAllKnownUsers());
		source.setAllUsers(visibility.isAllUsers());

		source.setCommunityIds(new HashSet<Long>(visibility.getCommunityIds()));
		source.setGroupIds(new HashSet<Long>(visibility.getGroupIds()));
		source.setUserIds(new HashSet<Long>(visibility.getUserIds()));

		return source;

	}

	public ShareVisibility toShareVisibility(LiveTopicSource src) {
		ShareVisibility sv = new ShareVisibility();
		sv.setUserIds(new ArrayList<Long>(src.getUserIds()));
		sv.setGroupIds(new ArrayList<Long>(src.getGroupIds()));
		sv.setCommunityIds(new ArrayList<Long>(src.getCommunityIds()));
		sv.setAllCommunities(src.isAllCommunities());
		sv.setAllKnownCommunities(src.isAllKnownCommunities());
		sv.setAllUsers(src.isAllUsers());
		sv.setAllKnownUsers(src.isAllKnownUsers());
		return sv;

	}

	public List<SharedContent> toSharedContent(List<Entity> entities,
			boolean addActor) {
		List<SharedContent> scList = new ArrayList<SharedContent>();
		try {
			for (Entity e : entities) {
				SharedContent sc = toSharedContent(e, addActor);
				if (sc != null) {
					scList.add(sc);
				}
			}
		} catch (NullPointerException e) {
			return null;
		}
		return scList;
	}

	public SharedContent toSharedContent(Entity e, boolean addActor) {
		SharedContent sc = null;
		try {
			sc = new SharedContent();
			sc.setEntityId(e.getId());
			sc.setOwnerId(e.getOwnerId());
			if (e.getCreationTime() != null) {
				sc.setCreationDate(new Date(e.getCreationTime()));
			}
			sc.setEntityType(e.getEtype().getName());
			sc.setTitle(e.getAttributeByName("name").getFirstValue()
					.getString());
			if (addActor) {
				try {
					sc.setUser(userManager.getUserByEntityBase(e
							.getEntityBase()));
				} catch (CommunityManagerException e1) {
				}
			}
		} catch (NullPointerException npe) {
			return null;
		}
		return sc;
	}
}
