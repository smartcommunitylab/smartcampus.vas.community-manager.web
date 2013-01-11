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
import it.unitn.disi.sweb.webapi.model.smartcampus.livetopics.LiveTopicSource;
import it.unitn.disi.sweb.webapi.model.smartcampus.social.User;
import it.unitn.disi.sweb.webapi.model.smartcampus.social.UserGroup;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;

import org.json.JSONException;

public class TestAssignment1 {

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

		// group of user1, members: uid2
		UserGroup group = new UserGroup();
		group.setName("TEST_GROUP_" + System.currentTimeMillis());
		group.setOwnerId(uid1);
		group.setUserIds(new HashSet<Long>(Arrays.asList(new Long[] { uid2 })));
		long gId = client.create(group);
		System.out.println("Group created, id " + gId);
		User user = client.readUser(uid1);
		user.getUserGroupIds().add(gId);
		user.getKnownUserIds().add(uid2);

		client.update(user);
		System.out.println("Updated group of user " + user.getId());

		LiveTopicSource assignments = client.readAssignments(entityId,
				Operation.READ, uid1);
		if (!assignments.getUserIds().contains(uid2)) {
			assignments.getUserIds().add(uid2);
		}

		assignments.getGroupIds().add(gId);

		// 282 -> id of smartcampus community on dev environment
		assignments.getCommunityIds().add(282l);
		assignments.setAllCommunities(true);
		assignments.setAllKnownCommunities(true);
		assignments.setAllKnownUsers(true);
		assignments.setAllUsers(true);

		System.out.println("=== VALUE ASSIGNMENT  ===");
		Iterator<Long> iter = assignments.getCommunityIds().iterator();
		while (iter.hasNext()) {
			System.out.println("Community: " + iter.next());
		}
		iter = assignments.getGroupIds().iterator();
		while (iter.hasNext()) {
			System.out.println("Group: " + iter.next());
		}
		iter = assignments.getUserIds().iterator();
		while (iter.hasNext()) {
			System.out.println("User: " + iter.next());
		}
		System.out.println(assignments.isAllCommunities());
		System.out.println(assignments.isAllKnownCommunities());
		System.out.println(assignments.isAllKnownUsers());
		System.out.println(assignments.isAllUsers());

		client.updateAssignments(entityId, Operation.READ, uid1, assignments);
		System.out.println(" ASSIGNMENT UPDATE");
		System.out.println("===VALUE ASSIGNMENT RELOAD ===");
		LiveTopicSource rereadAssignment = client.readAssignments(entityId,
				Operation.READ, uid1);

		iter = rereadAssignment.getCommunityIds().iterator();
		while (iter.hasNext()) {
			System.out.println("Community: " + iter.next());
		}
		iter = rereadAssignment.getGroupIds().iterator();
		while (iter.hasNext()) {
			System.out.println("Group: " + iter.next());
		}
		iter = rereadAssignment.getUserIds().iterator();
		while (iter.hasNext()) {
			System.out.println("User: " + iter.next());
		}
		System.out.println(rereadAssignment.isAllCommunities());
		System.out.println(rereadAssignment.isAllKnownCommunities());
		System.out.println(rereadAssignment.isAllKnownUsers());
		System.out.println(rereadAssignment.isAllUsers());

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
