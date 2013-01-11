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
import it.unitn.disi.sweb.webapi.model.entity.AttributeDef;
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
import it.unitn.disi.sweb.webapi.model.smartcampus.social.UserGroup;
import it.unitn.disi.sweb.webapi.model.ss.Concept;
import it.unitn.disi.sweb.webapi.model.ss.SemanticString;
import it.unitn.disi.sweb.webapi.model.ss.Token;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

public class SocialEngineClientTest {

	private static final Logger logger = Logger
			.getLogger(SocialEngineClientTest.class);
	private static final String SE_HOST = "sweb.smartcampuslab.it";
	private static final int SE_PORT = 8080;

	private static SCWebApiClient client;

	public SocialEngineClientTest() {
	}

	@BeforeClass
	public static void setupClient() {
		client = SCWebApiClient.getInstance(Locale.ENGLISH, SE_HOST, SE_PORT);

	}

	@Test
	public void createGroup() throws WebApiException {
		UserGroup group = new UserGroup();
		group.setName("SC_TEST_GROUP");
		logger.info("Creating an entity base...");
		EntityBase eb = new EntityBase();
		eb.setLabel("TEST_SC_EB_" + System.currentTimeMillis());
		Long ebId = client.create(eb);
		// Re-read to get the ID of the default KB
		eb = client.readEntityBase(ebId);
		logger.info("Created an entity base " + eb.getLabel() + " with ID "
				+ ebId);
		User user = createUser();
		group.setOwnerId(user.getId());
		long groupId = client.create(group);
		logger.info("group id: " + groupId);
		client.deleteUserGroup(groupId);
		logger.info("group deleted");
	}

	/*
	 * 
	 * USER
	 */

	@Test
	public void createDeleteUser() throws WebApiException {
		logger.info("Creating an entity base...");
		EntityBase eb = new EntityBase();
		eb.setLabel("TEST_SC_EB_" + System.currentTimeMillis());
		Long ebId = client.create(eb);
		// Re-read to get the ID of the default KB
		eb = client.readEntityBase(ebId);
		logger.info("Created an entity base " + eb.getLabel() + " with ID "
				+ ebId);
		logger.info("Creating an entity...");
		EntityType person = client.readEntityType("person", eb.getKbLabel());
		Entity entity = new Entity();
		entity.setEntityBase(eb);
		entity.setEtype(person);
		Long eid = client.create(entity);
		logger.info("Created entity with ID " + eid);
		logger.info("Creating a user...");
		User user = new User();
		user.setName("Test user");
		user.setEntityBaseId(ebId);
		user.setPersonEntityId(eid);
		long id = client.create(user);
		logger.info("New user's ID: " + id);
		logger.info("Reading user " + id + ":");
		logger.info("User deleted: " + client.deleteUser(id));
		logger.info("Entity base deleted: " + client.deleteEntityBase(ebId));
	}

	/*
	 * 
	 * TOPIC
	 */

	@Test
	public void createTopic() throws Exception {
		if (!client.ping()) {
			throw new RuntimeException("Ping failed");
		}
		logger.info("Creating an entity base...");
		EntityBase eb = new EntityBase();
		eb.setLabel("TEST_SC_EB_" + System.currentTimeMillis());
		Long ebId = client.create(eb);
		// Re-read to get the ID of the default KB
		eb = client.readEntityBase(ebId);
		logger.info("Created an entity base " + eb.getLabel() + " with ID "
				+ ebId);
		long uid = createUser().getId();
		LiveTopic lt = new LiveTopic();
		lt.setActorId(uid);
		lt.setName("TEST_TOPIC_" + System.currentTimeMillis());
		LiveTopicSource src = new LiveTopicSource();
		src.setAllKnownUsers(true);
		lt.setSource(src);
		lt.setStatus(LiveTopicStatus.ACTIVE);
		Set<LiveTopicSubject> subjs = new HashSet<LiveTopicSubject>();
		LiveTopicSubject subj = new LiveTopicSubject();
		subj.setKeyword("java");
		subjs.add(subj);

		// add concept
		subj = new LiveTopicSubject();
		subj.setConceptId(36982L);
		subj.setKeyword("test string");
		subjs.add(subj);

		lt.setSubjects(subjs);
		LiveTopicContentType type = new LiveTopicContentType();
		type.setAllTypes(true);
		lt.setType(type);
		long tid = client.create(lt);
		for (LiveTopic l : client.readLiveTopics(uid, null, false)) {
			logger.info("Topic: \n" + l.toJson().toString(2));
		}
		lt = client.readLiveTopic(tid);
		lt.getSource().setAllKnownCommunities(true);
		lt.getSource().setAllKnownUsers(false);

		LiveTopicSubject newSbj = new LiveTopicSubject();
		newSbj.setKeyword("groovy");

		lt.getSubjects().clear();

		lt.getSubjects().add(newSbj);

		client.update(lt);
		logger.info("Updated topic:");

		logger.info("nr. subjects "
				+ client.readLiveTopic(tid).getSubjects().size());

		logger.info(client.readLiveTopic(tid).toJson().toString(2));
		List<LiveTopicNews> news = client.readLiveTopicNews(tid, null);

		logger.info("NEWS:");
		for (LiveTopicNews ln : news) {
			logger.info(ln.toJson().toString(2));
		}
	}

	private User createUser() throws WebApiException {
		logger.info("Creating an entitybase...");
		EntityBase eb = new EntityBase();
		eb.setLabel("TEST_SC_EB_" + System.currentTimeMillis());
		Long ebId = client.create(eb);
		// Re-read to get the ID of the default KB
		eb = client.readEntityBase(ebId);
		logger.info("Creating an entity...");
		EntityType person = client.readEntityType("person", eb.getKbLabel());
		Entity entity = new Entity();
		entity.setEntityBase(eb);
		entity.setEtype(person);
		Long eid = client.create(entity);
		logger.info("Created entity with ID " + eid);
		logger.info("Creating a user...");
		User user = new User();
		user.setName("Test user " + System.currentTimeMillis());
		user.setEntityBaseId(eb.getId());
		user.setPersonEntityId(eid);
		long id = client.create(user);
		logger.info("New user's ID: " + id);
		user = client.readUser(id);
		return user;
	}

	private void deleteUser(User u) throws WebApiException {
		long eid = u.getPersonEntityId();
		long ebid = u.getEntityBaseId();

		client.deleteUser(u.getId());
		client.deleteEntity(eid);
		client.deleteEntityBase(ebid);
	}

	private void deleteEntity(Entity e) throws WebApiException {
		EntityBase eb = e.getEntityBase();
		client.deleteEntity(e.getId());
		client.deleteEntityBase(eb.getId());
	}

	void readShareSample() throws WebApiException {
		LiveTopicSource assignments = client.readAssignments(12345L,
				Operation.READ, 1L);
		if (!assignments.getUserIds().contains(2L)) {
			assignments.getUserIds().add(2L);
			client.updateAssignments(12345L, Operation.READ, 1L, assignments);
		}
	}

	@Test
	public void sharing() {
		Entity event = null;

		EntityBase eb = new EntityBase();
		eb.setLabel("SC_TEST_EB_" + System.currentTimeMillis());
		long ebid = -1;
		try {
			ebid = client.create(eb);
		} catch (WebApiException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return;
		}
		try {
			eb = client.readEntityBase(ebid);
		} catch (WebApiException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return;
		}

		long userId;
		try {
			userId = createUser().getId();
		} catch (WebApiException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return;
		}
		try {
			event = createEvent();
		} catch (WebApiException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		LiveTopicSource assignments = new LiveTopicSource();
		Set<Long> users = new HashSet<Long>(
				Arrays.asList(new Long[] { userId }));
		assignments.setUserIds(users);

		LiveTopicSource source = null;
		try {
			source = client.readAssignments(event.getId(), Operation.READ, 66);
		} catch (WebApiException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		logger.info("source " + source);
		try {
			client.updateAssignments(event.getId(), Operation.READ, 66,
					assignments);
		} catch (WebApiException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			source = client.readAssignments(event.getId(), Operation.READ, 66);
		} catch (WebApiException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		logger.info("source " + source);

		try {
			client.deleteUser(userId);
		} catch (WebApiException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			client.deleteEntityBase(ebid);
		} catch (WebApiException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private Entity createEvent() throws WebApiException {
		String type = "event";
		EntityBase eb = new EntityBase();
		eb.setLabel("SC_TEST_ENTITY_" + System.currentTimeMillis());

		Long ebid = client.create(eb);
		eb = client.readEntityBase(ebid);

		EntityType etype = client.readEntityType(type, "uk");

		Entity e = new Entity();
		e.setEntityBase(eb);
		e.setEtype(etype);

		Long eid = client.create(e);
		e = client.readEntity(eid, null);
		return e;

	}

	private Entity createEntity() throws WebApiException {
		logger.info("Creating an entity base...");
		EntityBase eb1 = new EntityBase();
		eb1.setLabel("TEST_SC_EB1_" + System.currentTimeMillis());
		Long eb1Id = client.create(eb1);
		// Re-read to get the ID of the default KB
		eb1 = client.readEntityBase(eb1Id);
		EntityType et = client.readEntityType("social", eb1.getKbLabel());

		Entity related = new Entity();
		related.setEntityBase(eb1);
		related.setEtype(et);
		long relatedId = client.create(related);
		Entity social = new Entity();
		social.setEntityBase(eb1);
		social.setEtype(et);
		List<Attribute> attrs = new ArrayList<Attribute>();
		List<Value> values = new ArrayList<Value>();
		Value v = new Value();
		// String tag attribute
		v.setType(DataType.STRING);
		v.setStringValue("This is a text tag");
		values.add(v);
		Attribute a = new Attribute();
		a.setAttributeDefinition(et.getAttributeDefByName("text"));
		a.setValues(values);
		attrs.add(a);
		// Entity tag attribute
		values = new ArrayList<Value>();
		v = new Value();
		v.setType(DataType.RELATION);
		v.setRelationEntity(client.readEntity(relatedId, null));
		values.add(v);
		a = new Attribute();
		a.setAttributeDefinition(et.getAttributeDefByName("entity"));
		a.setValues(values);
		attrs.add(a);
		// Semantic tag attribute
		values = new ArrayList<Value>();
		v = new Value();
		v.setType(DataType.SEMANTIC_STRING);
		// The semantic string itself
		SemanticString ss = new SemanticString();
		ss.setString("java");
		List<Token> tokens = new ArrayList<Token>();
		Concept c = client.readConceptByGlobalId(36982L, eb1.getKbLabel());
		List<Concept> concepts = new ArrayList<Concept>();
		concepts.add(c);
		Token t = new Token("java", c.getLabel(), c.getId(), concepts);
		tokens.add(t);
		ss.setTokens(tokens);
		v.setSemanticStringValue(ss);
		values.add(v);
		a = new Attribute();
		a.setAttributeDefinition(et.getAttributeDefByName("semantic"));
		a.setValues(values);
		attrs.add(a);

		social.setAttributes(attrs);
		long eid = client.create(social);
		logger.info("Created entity ID:" + eid);
		social = client.readEntity(eid, null);
		return social;
	}

	private void readETypes() throws WebApiException {
		List<EntityType> etypes = client.readEntityTypes("uk");
		for (EntityType et : etypes) {
			logger.info("\nEntity type " + et.getName() + " ("
					+ et.getGlobalId() + ")");
			for (AttributeDef ad : et.getAttrDefs()) {
				logger.info("\t" + ad.getName() + "\t\t" + ad.getType());
			}
		}
	}

	@Test
	public void assignment() throws WebApiException {
		Entity e = createEntity();

		User owner = createUser();
		User user1 = createUser();
		User user2 = createUser();

		LiveTopicSource assignments = new LiveTopicSource();

		assignments.setUserIds(new HashSet<Long>(Arrays
				.asList(new Long[] { user1.getId() })));

		LiveTopicSource source = null;
		source = client.readAssignments(e.getId(), Operation.READ,
				owner.getId());
		client.updateAssignments(e.getId(), Operation.READ, owner.getId(),
				assignments);
		source = client.readAssignments(e.getId(), Operation.READ,
				owner.getId());

		assignments = new LiveTopicSource();

		// assignments.setUserIds(new HashSet<Long>(Arrays
		// .asList(new Long[] { user2.getId() })));
		source.setUserIds(new HashSet<Long>(Arrays.asList(new Long[] { user2
				.getId() })));
		client.updateAssignments(e.getId(), Operation.READ, owner.getId(),
				source);
		source = client.readAssignments(e.getId(), Operation.READ,
				owner.getId());

		deleteUser(owner);
		deleteUser(user1);
		deleteUser(user2);
		deleteEntity(e);
	}

	public void fileUpload() throws WebApiException, IOException {

		// user 1 and its EB
		logger.info("Creating an entity base 1...");
		EntityBase eb1 = new EntityBase();
		eb1.setLabel("TEST_SC_EB_1_" + System.currentTimeMillis());
		Long ebId1 = client.create(eb1);
		// Re-read to get the ID of the default KB
		eb1 = client.readEntityBase(ebId1);
		logger.info("Created an entity base 1 " + eb1.getLabel() + " with ID "
				+ ebId1);

		// CRUD on file
		// create a file
		java.io.File file = new java.io.File("test_file.txt");
		FileWriter fw = new FileWriter(file);
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write("hello world text file example");
		bw.close();
		Long id = client.uploadFile(file, eb1.getLabel());
		logger.info("Created File ID: " + id);
		logger.info("Created File Name: " + file.getName());

		// read the file
		InputStream is = client.openStream(id);

		InputStreamReader isr = new InputStreamReader(is);
		StringBuilder sb = new StringBuilder();
		BufferedReader br = new BufferedReader(isr);
		String read = br.readLine();

		while (read != null) {
			sb.append(read);
			read = br.readLine();
		}
		is.close();
		logger.info("Contents of the file: " + sb.toString());

		// delete the file
		// client.deleteFile(id);
		// is = client.openStream(id);
		// System.out.printf("input stream should be null: " + is);
	}

}
