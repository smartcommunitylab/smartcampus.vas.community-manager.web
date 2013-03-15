package eu.trentorise.smartcampus.vas.communitymanager;

import it.unitn.disi.sweb.webapi.client.WebApiException;
import it.unitn.disi.sweb.webapi.client.smartcampus.SCWebApiClient;
import it.unitn.disi.sweb.webapi.client.smartcampus.SharedUtils;
import it.unitn.disi.sweb.webapi.model.entity.Entity;
import it.unitn.disi.sweb.webapi.model.entity.EntityBase;
import it.unitn.disi.sweb.webapi.model.entity.EntityType;
import it.unitn.disi.sweb.webapi.model.smartcampus.ac.Operation;
import it.unitn.disi.sweb.webapi.model.smartcampus.livetopics.LiveTopicSource;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class TestPermission {

	public static void main(String[] a) throws WebApiException {
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

		EntityBase eb2 = new EntityBase();
		eb2.setLabel("TEST_SC_EB_2_" + System.currentTimeMillis());
		Long ebId2 = client.create(eb2);
		// Re-read to get the ID of the default KB
		eb2 = client.readEntityBase(ebId2);
		System.out.println("Created an entity base 2 " + eb2.getLabel()
				+ " with ID " + ebId2);
		long uid2 = SharedUtils.createUser(client, eb2);

		EntityType et = client.readEntityType("entity", eb1.getKbLabel());
		Entity entity = new Entity();
		entity.setEntityBase(eb1);
		entity.setEtype(et);
		client.create(entity);

		System.out.println("u1: "
				+ client.readPermission(uid1, entity.getId(), Operation.READ));

		System.out.println("u2: "
				+ client.readPermission(uid2, entity.getId(), Operation.READ));

		LiveTopicSource ass = client.readAssignments(entity.getId(),
				Operation.READ, uid1);

		Set<Long> ids = new HashSet<Long>();
		ids.add(uid2);
		ass.setUserIds(ids);

		client.updateAssignments(entity.getId(), Operation.READ, uid1, ass);
		System.out.println("assignement");
		System.out.println("u1: "
				+ client.readPermission(uid1, entity.getId(), Operation.READ));

		System.out.println("u2: "
				+ client.readPermission(uid2, entity.getId(), Operation.READ));

	}
}
