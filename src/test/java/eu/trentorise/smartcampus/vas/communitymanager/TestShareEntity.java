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
import it.unitn.disi.sweb.webapi.model.smartcampus.ac.Operation;
import it.unitn.disi.sweb.webapi.model.smartcampus.livetopics.LiveTopic;
import it.unitn.disi.sweb.webapi.model.smartcampus.livetopics.LiveTopicContentType;
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

public class TestShareEntity {

	public static void main(String[] args) throws WebApiException,
			JSONException {
		SCWebApiClient client = SCWebApiClient.getInstance(Locale.ENGLISH,
				"sweb.smartcampuslab.it", 8080);
		if (!client.ping()) {
			throw new RuntimeException("Ping failed");
		}
		// user 1 and its EB
		System.out.println("Creating an entity base 1...");
		EntityBase eb1 = new EntityBase();
		eb1.setLabel("TEST_SC_EB_1_" + System.currentTimeMillis());
		Long ebId1 = client.create(eb1);
		// Re-read to get the ID of the default KB
		eb1 = client.readEntityBase(ebId1);
		System.out.println("Created an entity base 1 " + eb1.getLabel()
				+ " with ID " + ebId1);
		long uid1 = createUser(client, eb1);

		// user 2 and its EB
		System.out.println("Creating an entity base 2...");
		EntityBase eb2 = new EntityBase();
		eb2.setLabel("TEST_SC_EB_2_" + System.currentTimeMillis());
		Long ebId2 = client.create(eb2);
		// Re-read to get the ID of the default KB
		eb2 = client.readEntityBase(ebId2);
		System.out.println("Created an entity base 1 " + eb2.getLabel()
				+ " with ID " + ebId2);
		long uid2 = createUser(client, eb2);

		// compute shared entities with user 2
		LiveTopic filter = new LiveTopic();
		LiveTopicSource filterSource = new LiveTopicSource();
		filter.setActorId(uid2); // <-- mandatory
		filterSource.setUserIds(new HashSet<Long>(Arrays
				.asList(new Long[] { uid1 })));
		filter.setSource(filterSource);
		LiveTopicContentType type = new LiveTopicContentType();
		type.setAllTypes(true);
		filter.setType(type); // <-- mandatory
		filter.setStatus(LiveTopicStatus.ACTIVE); // <-- mandatory
		LiveTopicSubject lts = new LiveTopicSubject();
		lts.setAllSubjects(true);
		Set<LiveTopicSubject> subjects = new HashSet<LiveTopicSubject>();
		subjects.add(lts);
		filter.setSubjects(subjects);

		List<Long> sharedId = client.computeEntitiesForLiveTopic(filter, null,
				null);

		System.out.println("Shared entities IDs: " + sharedId);

		// entity of user 1
		EntityType et = client.readEntityType("social", eb1.getKbLabel());
		Entity entity = new Entity();
		entity.setEntityBase(eb1);
		entity.setEtype(et);

		long entityId = client.create(entity);

		// share entity with user 2
		LiveTopicSource assignments = client.readAssignments(entityId,
				Operation.READ, uid1);
		if (!assignments.getUserIds().contains(uid2)) {
			assignments.getUserIds().add(uid2);
		}
		client.updateAssignments(entityId, Operation.READ, uid1, assignments);

		// compute shared entities
		sharedId = client.computeEntitiesForLiveTopic(filter, null, null);

		System.out.println("Shared entities IDs: " + sharedId);

	}

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
