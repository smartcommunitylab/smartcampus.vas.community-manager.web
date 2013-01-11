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

import java.util.List;
import java.util.Locale;

import org.json.JSONException;

/**
 * 
 * @author Viktor Pravdin <pravdin@disi.unitn.it>
 * @date Jul 10, 2012 11:49:50 AM
 */
public class TestClient {
	public static void main(String[] args) throws WebApiException,
			JSONException {
		SCWebApiClient client = SCWebApiClient.getInstance(Locale.ENGLISH,
				"sweb.smartcampuslab.it", 8080);
		if (!client.ping())
			throw new RuntimeException("Ping failed");
		System.out.println("Users: ");
		printUsers(client.readUsers());
		System.out.println("Creating an entity base...");
		EntityBase eb = new EntityBase();
		eb.setLabel("" + 4);
		Long ebId = client.create(eb);
		// Re-read to get the ID of the default KB
		eb = client.readEntityBase(ebId);
		System.out.println("Created an entity base " + eb.getLabel()
				+ " with ID " + ebId);
		System.out.println("Creating an entity...");
		EntityType person = client.readEntityType("person", eb.getKbLabel());
		Entity entity = new Entity();
		entity.setEntityBase(eb);
		entity.setEtype(person);
		Long eid = client.create(entity);
		System.out.println("Created entity with ID " + eid);
		System.out.println("Creating a user...");
		User user = new User();
		user.setName("Test user");
		user.setEntityBaseId(ebId);
		user.setPersonEntityId(eid);
		long id = client.create(user);
		System.out.println("New user's ID: " + id);
		System.out.println("Reading user " + id + ":");
		printUser(client.readUser(id));
		System.out.println("User deleted: " + client.deleteUser(id));
		System.out.println("Entity base deleted: "
				+ client.deleteEntityBase(ebId));
	}

	private static void printUsers(List<User> users) throws JSONException {
		for (User u : users) {
			printUser(u);
		}
	}

	private static void printUser(User u) throws JSONException {
		if (u != null)
			System.out.println(u.toJson().toString(2));
		else
			System.out.println("NULL");
	}
}
