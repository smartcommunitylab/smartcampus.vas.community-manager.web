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
import it.unitn.disi.sweb.webapi.model.entity.Entity;
import it.unitn.disi.sweb.webapi.model.entity.EntityBase;
import it.unitn.disi.sweb.webapi.model.entity.EntityType;
import it.unitn.disi.sweb.webapi.model.smartcampus.social.User;
import it.unitn.disi.sweb.webapi.model.smartcampus.social.UserGroup;

import java.util.List;
import java.util.Locale;

import org.json.JSONException;

/**
 * 
 * @author Viktor Pravdin <pravdin@disi.unitn.it>
 * @date Jul 11, 2012 4:25:42 PM
 */
public class TestCRUD {
	public static void main(String[] args) throws WebApiException,
			JSONException {
		SCWebApiClient client = SCWebApiClient.getInstance(Locale.ENGLISH,
				"sweb.smartcampuslab.it", 8080);
		if (!client.ping()) {
			throw new RuntimeException("Ping failed");
		}

		UserGroup group = new UserGroup();
		group.setName("SC_TEST_GROUP");
		long groupId = client.create(group);
		System.out.println("group id: " + groupId);
		client.deleteUserGroup(groupId);
		System.out.println("group deleted");
	}

	// public static void main(String[] args) throws WebApiException,
	// JSONException {
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
	// long u1id = createUser(client, eb);
	// long u2id = createUser(client, eb);
	// User u1 = client.readUser(u1id);
	// u1.getKnownUserIds().add(u2id);
	// client.update(u1);
	// u1 = client.readUser(u1id);
	// printUser(u1);
	// client.deleteUser(u1id);
	// client.deleteUser(u2id);
	// client.deleteEntityBase(ebId);
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

	private static void printUsers(List<User> users) throws JSONException {
		for (User u : users) {
			printUser(u);
		}
	}

	private static void printUser(User u) throws JSONException {
		if (u != null) {
			System.out.println(u.toJson().toString(2));
		} else {
			System.out.println("NULL");
		}
	}
}
