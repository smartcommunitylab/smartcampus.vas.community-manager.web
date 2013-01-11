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
package eu.trentorise.smartcampus.vas.communitymanager;

import it.unitn.disi.sweb.webapi.client.WebApiException;
import it.unitn.disi.sweb.webapi.client.smartcampus.SCWebApiClient;
import it.unitn.disi.sweb.webapi.model.entity.Attribute;
import it.unitn.disi.sweb.webapi.model.entity.DataType;
import it.unitn.disi.sweb.webapi.model.entity.Entity;
import it.unitn.disi.sweb.webapi.model.entity.EntityBase;
import it.unitn.disi.sweb.webapi.model.entity.EntityType;
import it.unitn.disi.sweb.webapi.model.entity.Value;
import it.unitn.disi.sweb.webapi.model.smartcampus.ac.Operation;
import it.unitn.disi.sweb.webapi.model.smartcampus.livetopics.LiveTopic;
import it.unitn.disi.sweb.webapi.model.smartcampus.livetopics.LiveTopicContentType;
import it.unitn.disi.sweb.webapi.model.smartcampus.livetopics.LiveTopicNews;
import it.unitn.disi.sweb.webapi.model.smartcampus.livetopics.LiveTopicSource;
import it.unitn.disi.sweb.webapi.model.smartcampus.livetopics.LiveTopicStatus;
import it.unitn.disi.sweb.webapi.model.smartcampus.livetopics.LiveTopicSubject;
import it.unitn.disi.sweb.webapi.model.smartcampus.social.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.log4j.Logger;

public class TestLiveTopicNews {

	/**
	 * @param args
	 * @throws WebApiException
	 */

	private static final Logger logger = Logger.getLogger(Logger.class);

	public static void main(String[] args) throws WebApiException {
		SCWebApiClient client = SCWebApiClient.getInstance(Locale.ENGLISH,
				"sweb.smartcampuslab.it", 8080);
		User user1 = createUser(client);
		User user2 = createUser(client);

		// User user1 = client.readUser(264);
		// User user2 = client.readUser(265);

		// LiveTopic topic1 = createTopic(client, user1, user2, "java things");

		// Entity e1 = createJavaEntity(client, user2, "event", "event1");
		Entity e1 = client.readEntity(2880L, null);
		System.out.println("entity ID: " + e1.getId());

		publicEntity(client, e1, user2);

		// printSharedObjects(client, user2);

		try {
			System.out.println("Sleep..");
			Thread.sleep(1000 * 30);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		getTopicNews(user1.getId(), client);

	}

	public static void getTopicNews(long actorId, SCWebApiClient client)
			throws WebApiException {
		List<LiveTopic> topics = client.readLiveTopics(actorId,
				LiveTopicStatus.ACTIVE, false);
		for (LiveTopic lt : topics) {
			logger.info("NEWS topic " + lt.getName() + " "
					+ client.readLiveTopicNews(lt.getId(), null));
			List<LiveTopicNews> news = client.readLiveTopicNews(lt.getId(),
					null);
			for (LiveTopicNews n : news) {
				System.out.println("topic news ID: " + n.getId());
			}

		}
	}

	private static User createUser(SCWebApiClient client)
			throws WebApiException {
		EntityBase eb = new EntityBase();
		eb.setLabel("TEST_SC_EB_" + System.currentTimeMillis());
		Long ebId = client.create(eb);
		// Re-read to get the ID of the default KB
		eb = client.readEntityBase(ebId);
		EntityType person = client.readEntityType("person", eb.getKbLabel());
		Entity entity = new Entity();
		entity.setEntityBase(eb);
		entity.setEtype(person);
		Long eid = client.create(entity);
		User user = new User();
		user.setName("Test user " + System.currentTimeMillis());
		user.setEntityBaseId(eb.getId());
		user.setPersonEntityId(eid);
		long id = client.create(user);
		logger.info("Create user ID " + id);
		return client.readUser(id);
	}

	protected static Entity createJavaEntity(SCWebApiClient client, User user,
			String type, String name) throws WebApiException {

		EntityBase eb1 = client.readEntityBase(user.getEntityBaseId());
		EntityType et = client.readEntityType(type, eb1.getKbLabel());

		Entity social = new Entity();
		social.setEntityBase(eb1);
		social.setEtype(et);

		List<Attribute> attrs = new ArrayList<Attribute>();
		List<Value> values = new ArrayList<Value>();
		Value v = new Value();
		// String tag attribute
		v.setType(DataType.STRING);
		v.setStringValue(name);
		values.add(v);
		Attribute a = new Attribute();
		a.setAttributeDefinition(et.getAttributeDefByName("text"));
		a.setValues(values);
		attrs.add(a);

		// // Entity name
		values = new ArrayList<Value>();
		v = new Value();
		v.setType(DataType.STRING);
		v.setStringValue(type.toString());
		values.add(v);
		a = new Attribute();
		a.setAttributeDefinition(et.getAttributeDefByName("name"));
		a.setValues(values);
		attrs.add(a);

		// Entity description
		// values = new ArrayList<Value>();
		// v = new Value();
		// v.setType(DataType.STRING);
		// v.setStringValue("Event description");
		// values.add(v);
		// a = new Attribute();
		// a.setAttributeDefinition(et.getAttributeDefByName("description"));
		// a.setValues(values);
		// attrs.add(a);

		// Semantic tag attribute
		// values = new ArrayList<Value>();
		// v = new Value();
		// v.setType(DataType.SEMANTIC_STRING);
		// // // The semantic string itself
		// SemanticString ss = new SemanticString();
		// ss.setString("java");
		// List<Token> tokens = new ArrayList<Token>();
		// Concept c = client.readConceptByGlobalId(36982L, eb1.getKbLabel());
		// List<Concept> concepts = new ArrayList<Concept>();
		// concepts.add(c);
		// Token t = new Token("java", c.getLabel(), c.getId(), concepts);
		// tokens.add(t);
		// ss.setTokens(tokens);
		// v.setSemanticStringValue(ss);
		// values.add(v);
		// a = new Attribute();
		// // a.setAttributeDefinition(et.getAttributeDefByName("semantic"));
		// a.setAttributeDefinition(et.getAttributeDefByGlobalId(50005l));
		// a.setValues(values);
		// attrs.add(a);
		values = new ArrayList<Value>();
		v = new Value();
		v.setType(DataType.STRING);
		// // The semantic string itself

		v.setStringValue("java");
		values.add(v);
		a = new Attribute();
		// a.setAttributeDefinition(et.getAttributeDefByName("semantic"));
		a.setAttributeDefinition(et.getAttributeDefByGlobalId(50005l));
		a.setValues(values);
		attrs.add(a);

		social.setAttributes(attrs);
		long eid = client.create(social);
		logger.info("Created entity ID:" + eid);
		social = client.readEntity(eid, null);
		return social;
	}

	private static void publicEntity(SCWebApiClient client, Entity e, User user)
			throws WebApiException {
		LiveTopicSource a = client.readAssignments(e.getId(), Operation.READ,
				user.getId());
		a.setAllUsers(true);
		e.setCreationTime(System.currentTimeMillis());// change the timestamp to
														// find the entity as a
														// live topic news
		client.updateEntity(e);// update timestamp
		// a.setAllCommunities(true);
		client.updateAssignments(e.getId(), Operation.READ, user.getId(), a);
		logger.info("Entity publicated " + e.getId());
	}

	private static void printSharedObjects(SCWebApiClient client, User owner)
			throws WebApiException {
		LiveTopic filter = new LiveTopic();
		LiveTopicSource filterSource = new LiveTopicSource();
		filter.setActorId(null); // <-- mandatory
		filterSource.setAllUsers(true);
		filter.setSource(filterSource);

		LiveTopicSource s = new LiveTopicSource();
		s.setUserIds(new HashSet<Long>(
				Arrays.asList(new Long[] { owner.getId() })));

		LiveTopicSubject subject = new LiveTopicSubject();
		subject.setAllSubjects(true);
		Set<LiveTopicSubject> subjects = new HashSet<LiveTopicSubject>();
		subjects.add(subject);
		filter.setSubjects(subjects);

		LiveTopicContentType type = new LiveTopicContentType();
		type.setAllTypes(true);
		filter.setType(type); // <-- mandatory
		filter.setStatus(LiveTopicStatus.ACTIVE); // <-- mandatory

		List<Long> ids = new ArrayList<Long>();
		for (Long i : client.computeEntitiesForLiveTopic(filter, null, null)) {
			if (supportedTypes.contains(client.readEntity(i, null).getEtype()
					.getName())) {
				ids.add(i);
			}
		}

		logger.info("Public objects of user " + owner.getId() + ": " + ids);
		System.out.println("Public objects of user " + owner.getId() + ": "
				+ ids);
	}

	private static LiveTopic createTopic(SCWebApiClient client, User creator,
			User source, String name) throws WebApiException {
		LiveTopic lt = new LiveTopic();
		lt.setActorId(creator.getId());
		lt.setName(name);
		LiveTopicSource src = new LiveTopicSource();
		src.setUserIds(new HashSet<Long>(Arrays.asList(new Long[] { source
				.getId() })));
		lt.setSource(src);
		lt.setStatus(LiveTopicStatus.ACTIVE);
		Set<LiveTopicSubject> subjs = new HashSet<LiveTopicSubject>();
		LiveTopicSubject subj = new LiveTopicSubject();
		subj.setKeyword("java");
		subjs.add(subj);
		lt.setSubjects(subjs);
		LiveTopicContentType type = new LiveTopicContentType();
		type.setAllTypes(true);
		lt.setType(type);
		long tid = client.create(lt);
		logger.info("Created topic " + name + " (" + tid + ")");
		return client.readLiveTopic(tid);

	}

	private static final List<String> supportedTypes = Arrays
			.asList(new String[] { "event", "experience", "computer file",
					"journey", "location", "portfolio", "narrative" });
}
