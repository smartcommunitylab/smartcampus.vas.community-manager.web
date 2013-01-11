/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.trentorise.smartcampus.vas.communitymanager;

import it.unitn.disi.sweb.webapi.client.WebApiException;
import it.unitn.disi.sweb.webapi.client.smartcampus.SCWebApiClient;
import it.unitn.disi.sweb.webapi.client.smartcampus.SharedUtils;
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
import it.unitn.disi.sweb.webapi.model.smartcampus.social.UserGroup;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.json.JSONException;

public class TestShareEntity2 {

	public static void main(String[] args) throws WebApiException,
			JSONException {
		// SCWebApiClient client = SCWebApiClient.getInstance(Locale.ENGLISH,
		// "213.21.154.85", 9090);
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
		long uid1 = SharedUtils.createUser(client, eb1);

		// user 2 and its EB
		System.out.println("Creating an entity base 2...");
		EntityBase eb2 = new EntityBase();
		eb2.setLabel("TEST_SC_EB_2_" + System.currentTimeMillis());
		Long ebId2 = client.create(eb2);
		// Re-read to get the ID of the default KB
		eb2 = client.readEntityBase(ebId2);
		System.out.println("Created an entity base 1 " + eb2.getLabel()
				+ " with ID " + ebId2);
		long uid2 = SharedUtils.createUser(client, eb2);

		// entity of user 1
		EntityType et = client.readEntityType("entity", eb1.getKbLabel());
		Entity entity = new Entity();
		entity.setEntityBase(eb1);
		entity.setEtype(et);

		long entityId = client.create(entity);
		System.out.println("Entity of user 1 created with ID: " + entityId);

		// group of user2, members: uid1
		UserGroup group = new UserGroup();
		group.setName("TEST_GROUP_" + System.currentTimeMillis());
		group.setOwnerId(uid2);
		group.setUserIds(new HashSet<Long>(Arrays.asList(new Long[] { uid1 })));
		long gId = client.create(group);
		System.out.println("Group created, id " + gId);
		User user = client.readUser(uid2);
		user.getUserGroupIds().add(gId);
		user.getKnownUserIds().add(uid1);

		client.update(user);
		System.out.println("Updated group of user " + user.getId());

		// compute shared entities with user 2
		LiveTopic filter = new LiveTopic();
		LiveTopicSource filterSource = new LiveTopicSource();
		filter.setActorId(uid2); // <-- mandatory
		filterSource.setGroupIds(new HashSet<Long>(Arrays
				.asList(new Long[] { gId })));
		// filterSource.setUserIds(new HashSet<Long>(Arrays.asList(new
		// Long[]{uid1})));
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
		System.out
				.println("Shared entities IDs (should be empty even if user 1 has an entity): "
						+ sharedId);

		boolean isPermission = client.readPermission(uid2, entityId,
				Operation.READ);
		System.out
				.println("Checking permission for user 2 to read the created entity of user 1 (should be FALSE): "
						+ isPermission);

		// share entity with user 2
		LiveTopicSource assignments = client.readAssignments(entityId,
				Operation.READ, uid1);
		if (!assignments.getUserIds().contains(uid2)) {
			assignments.getUserIds().add(uid2);
		}
		client.updateAssignments(entityId, Operation.READ, uid1, assignments);

		isPermission = client.readPermission(uid2, entityId, Operation.READ);
		System.out
				.println("Checking permission for user 2 to read the created entity of user 1 (should be TRUE): "
						+ isPermission);

		// compute shared entities
		sharedId = client.computeEntitiesForLiveTopic(filter, null, null);
		System.out
				.println("Shared entities IDs (should be one shared entity): "
						+ sharedId);

		// check for public entities
		filter.setActorId(null);
		sharedId = client.computeEntitiesForLiveTopic(filter, null, null);
		System.out
				.println("Shared entities IDs (should be empty as there are no public entities): "
						+ sharedId);

		// make the entity public
		// share entity with user 2
		assignments = client.readAssignments(entityId, Operation.READ, uid1);
		assignments.setAllUsers(true);
		client.updateAssignments(entityId, Operation.READ, uid1, assignments);

		// check for public entities
		filter.setActorId(null);
		sharedId = client.computeEntitiesForLiveTopic(filter, null, null);
		System.out
				.println("Shared entities IDs (should be one shared entity): "
						+ sharedId);

		// clean up data
		client.deleteEntity(entityId);
		client.deleteUserGroup(gId);
		client.deleteUser(uid1);
		client.deleteUser(uid2);
		client.deleteEntityBase(ebId1);
		client.deleteEntityBase(ebId2);

		System.out.println("data cleaned successfully");
	}
}
