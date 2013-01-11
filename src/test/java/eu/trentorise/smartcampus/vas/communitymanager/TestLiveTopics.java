/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.trentorise.smartcampus.vas.communitymanager;

import it.unitn.disi.sweb.webapi.client.WebApiException;
import it.unitn.disi.sweb.webapi.client.smartcampus.SCWebApiClient;
import it.unitn.disi.sweb.webapi.model.entity.Entity;
import it.unitn.disi.sweb.webapi.model.entity.EntityBase;
import it.unitn.disi.sweb.webapi.model.entity.EntityType;
import it.unitn.disi.sweb.webapi.model.smartcampus.livetopics.LiveTopic;
import it.unitn.disi.sweb.webapi.model.smartcampus.livetopics.LiveTopicContentType;
import it.unitn.disi.sweb.webapi.model.smartcampus.livetopics.LiveTopicNews;
import it.unitn.disi.sweb.webapi.model.smartcampus.livetopics.LiveTopicSource;
import it.unitn.disi.sweb.webapi.model.smartcampus.livetopics.LiveTopicStatus;
import it.unitn.disi.sweb.webapi.model.smartcampus.livetopics.LiveTopicSubject;
import it.unitn.disi.sweb.webapi.model.smartcampus.social.User;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.json.JSONException;

/**
 * 
 * @author Viktor Pravdin
 */
public class TestLiveTopics {

	public static void main(String[] args) throws WebApiException,
			JSONException {
		SCWebApiClient client = SCWebApiClient.getInstance(Locale.ENGLISH,
				"sweb.smartcampuslab.it", 8080);
		if (!client.ping()) {
			throw new RuntimeException("Ping failed");
		}
		System.out.println("Creating an entity base...");
		EntityBase eb = new EntityBase();
		eb.setLabel("TEST_SC_EB_" + System.currentTimeMillis());
		Long ebId = client.create(eb);
		// Re-read to get the ID of the default KB
		eb = client.readEntityBase(ebId);
		System.out.println("Created an entity base " + eb.getLabel()
				+ " with ID " + ebId);
		long uid = createUser(client, eb);

		// another entity base
		eb = new EntityBase();
		eb.setLabel("TEST_SC_EB_" + System.currentTimeMillis());
		ebId = client.create(eb);
		// Re-read to get the ID of the default KB
		eb = client.readEntityBase(ebId);
		System.out.println("Created an entity base " + eb.getLabel()
				+ " with ID " + ebId);
		long uid1 = createUser(client, eb);

		LiveTopic lt = new LiveTopic();
		lt.setActorId(uid);
		lt.setName("TEST_TOPIC_" + System.currentTimeMillis());
		LiveTopicSource src = new LiveTopicSource();
		src.setUserIds(new HashSet<Long>(Arrays.asList(new Long[] { uid1 })));
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
		for (LiveTopic l : client.readLiveTopics(uid, null, false)) {
			System.out.println("Topic: \n" + l.toJson().toString(2));
		}
		lt = client.readLiveTopic(tid);
		lt.getSource().setAllKnownCommunities(true);
		lt.getSource().setAllKnownUsers(false);

		LiveTopicSubject newSbj = new LiveTopicSubject();

		newSbj.setKeyword("groovy");
		lt.getSubjects().add(newSbj);

		client.update(lt);
		System.out.println("Updated topic:");
		System.out.println(client.readLiveTopic(tid).toJson().toString(2));
		List<LiveTopicNews> news = client.readLiveTopicNews(tid, null);
		System.out.println("NEWS:");
		for (LiveTopicNews ln : news) {
			System.out.println(ln.toJson().toString(2));
		}
	}

	// public static void main(String[] args) throws WebApiException,
	// JSONException {
	// // SCWebApiClient client = SCWebApiClient.getInstance(Locale.ENGLISH,
	// // "localhost", 8080);
	// SCWebApiClient client = SCWebApiClient.getInstance(Locale.ENGLISH,
	// "213.21.154.85", 8080);
	// if (!client.ping()) {
	// throw new RuntimeException("Ping failed");
	// }
	// System.out.println("Creating an entity base...");
	// EntityBase eb = new EntityBase();
	// eb.setLabel("TEST_SC_EB_" + System.currentTimeMillis());
	// Long ebId = client.create(eb);
	// // Re-read to get the ID of the default KB
	// eb = client.readEntityBase(ebId);
	// System.out.println("Created an entity base " + eb.getLabel()
	// + " with ID " + ebId);
	// long uid = createUser(client, eb);
	// LiveTopic lt = new LiveTopic();
	// lt.setActorId(uid);
	// lt.setName("TEST_TOPIC_" + System.currentTimeMillis());
	// LiveTopicSource src = new LiveTopicSource();
	// src.setAllKnownUsers(true);
	// lt.setSource(src);
	// lt.setStatus(LiveTopicStatus.ACTIVE);
	// Set<LiveTopicSubject> subjs = new HashSet<LiveTopicSubject>();
	// LiveTopicSubject subj = new LiveTopicSubject();
	// subj.setKeyword("java");
	// subjs.add(subj);
	// lt.setSubjects(subjs);
	// LiveTopicContentType type = new LiveTopicContentType();
	// type.setAllTypes(true);
	// lt.setType(type);
	// long tid = client.create(lt);
	// for (LiveTopic l : client.readLiveTopics(uid, null, false)) {
	// System.out.println("Topic: \n" + l.toJson().toString(2));
	// }
	// lt = client.readLiveTopic(tid);
	// lt.getSource().setAllKnownCommunities(true);
	// lt.getSource().setAllKnownUsers(false);
	//
	// LiveTopicSubject newSbj = new LiveTopicSubject();
	//
	// newSbj.setKeyword("groovy");
	// lt.getSubjects().add(newSbj);
	//
	// client.update(lt);
	// System.out.println("Updated topic:");
	// System.out.println(client.readLiveTopic(tid).toJson().toString(2));
	// List<LiveTopicNews> news = client.readLiveTopicNews(tid, null);
	// System.out.println("NEWS:");
	// for (LiveTopicNews ln : news) {
	// System.out.println(ln.toJson().toString(2));
	// }
	// }

	private static long createUser(SCWebApiClient client, EntityBase eb)
			throws WebApiException {
		System.out.println("Creating an entity...");
		EntityType person = client.readEntityType("person", eb.getKbLabel());
		Entity entity = new Entity();
		entity.setEntityBase(eb);
		entity.setEtype(person);
		Long eid = client.create(entity);
		System.out.println("Created entity with ID " + eid);
		System.out.println("Creating a user...");
		User user = new User();
		user.setName("Test user " + System.currentTimeMillis());
		user.setEntityBaseId(eb.getId());
		user.setPersonEntityId(eid);
		long id = client.create(user);
		System.out.println("New user's ID: " + id);
		return id;
	}
}
