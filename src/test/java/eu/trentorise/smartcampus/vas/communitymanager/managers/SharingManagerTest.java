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
package eu.trentorise.smartcampus.vas.communitymanager.managers;

import it.unitn.disi.sweb.webapi.client.WebApiException;
import it.unitn.disi.sweb.webapi.model.entity.Entity;
import it.unitn.disi.sweb.webapi.model.entity.EntityBase;
import it.unitn.disi.sweb.webapi.model.smartcampus.social.User;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import eu.trentorise.smartcampus.common.Concept;
import eu.trentorise.smartcampus.common.Constants;
import eu.trentorise.smartcampus.common.ShareVisibility;
import eu.trentorise.smartcampus.vas.communitymanager.model.Community;
import eu.trentorise.smartcampus.vas.communitymanager.model.EntityType;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/spring/applicationContext.xml")
public class SharingManagerTest {

	@Autowired
	private SharingManager sharingManager;
	@Autowired
	private GroupManager groupManager;
	@Autowired
	private CommunityManager communityManager;
	@Autowired
	private SocialEngineOperation socialOperation;

	private static final long CREATOR_ID = -1l;

	/**
	 * Profiling get shared object by Smartcampus community
	 * 
	 * @throws CommunityManagerException
	 * @throws WebApiException
	 */
	// @Test
	public void profilingCommunity() throws CommunityManagerException,
			WebApiException {
		User user1 = socialOperation.createUser();
		Community comm = communityManager
				.getCommunity(Constants.SMARTCAMPUS_COMMUNITY);
		sharingManager.getShared(user1.getId(), null, -1L,
				Arrays.asList(comm.getSocialId()), 0, 50, null, false).size();

		socialOperation.deleteUser(user1.getId());
	}

	/**
	 * Profiling time of sharing N events
	 * 
	 * @throws WebApiException
	 * @throws CommunityManagerException
	 */

	// @Test
	public void profiling() throws WebApiException, CommunityManagerException {
		User user1 = socialOperation.createUser();
		User user2 = socialOperation.createUser();
		int eventN = 50;

		// event of user1
		Entity[] e = new Entity[eventN];
		for (int i = 0; i < eventN; i++) {
			e[i] = socialOperation.createEntity(user2.getEntityBaseId(),
					EntityTypes.event);
		}
		List<Long> ids = Arrays.asList(new Long[] { user1.getId() });

		ShareVisibility visibility = new ShareVisibility();
		visibility.setUserIds(ids);

		for (Entity event : e) {
			sharingManager.share(event.getId(), user2.getId(), visibility);
		}

		sharingManager.getShared(user1.getId(), Arrays.asList(user2.getId()),
				-1L, null, 0, 100, null, false).size();

		for (Entity event : e) {
			socialOperation.deleteEntity(event);
		}

		socialOperation.deleteUser(user1.getId());
		socialOperation.deleteUser(user2.getId());

	}

	/**
	 * Failure test. If a entityType binded to a concept already exists, method
	 * threw exception
	 * 
	 * @throws CommunityManagerException
	 * @throws AlreadyExistException
	 */
	@Test(expected = CommunityManagerException.class)
	public void entityTypeAlreadyExists() throws CommunityManagerException,
			AlreadyExistException {
		Concept c = sharingManager.getConceptByGlobalId(4080L);
		Assert.assertNotNull(sharingManager.createEntityType(c.getId()));
		sharingManager.createEntityType(c.getId());

	}

	/**
	 * Entity type crud
	 * 
	 * @throws CommunityManagerException
	 * @throws AlreadyExistException
	 */
	@Test
	public void entityTypeCrud() throws CommunityManagerException,
			AlreadyExistException {

		Concept c = sharingManager.getConceptByGlobalId(4080L);
		long conceptId = c.getId();

		if (sharingManager.getEntityTypeByConcept(c) != null) {
			sharingManager.deleteEntityType(sharingManager
					.getEntityTypeByConcept(c).getId());
		}

		EntityType newType = sharingManager.createEntityType(c.getId());
		Assert.assertNotNull(sharingManager.getEntityType(newType.getId()));
		Assert.assertEquals(c.getName(), newType.getName());
		Assert.assertEquals(new Long(conceptId), newType.getConcept().getId());
		Assert.assertTrue(sharingManager.deleteEntityType(newType.getId()));
		Assert.assertNull(sharingManager.getEntityType(newType.getId()));

	}

	@Test
	public void clean() throws CommunityManagerException {
		sharingManager.deleteEntityType(sharingManager
				.getEntityTypeByConceptId(5081l).getId());
	}

	/**
	 * Unsharing using unsharing method and reset manually SharedVisibility
	 * options
	 * 
	 * @throws CommunityManagerException
	 * @throws WebApiException
	 */
	@Test
	public void unsharing() throws CommunityManagerException, WebApiException {
		User user1 = socialOperation.createUser();
		User user2 = socialOperation.createUser();
		// user1 creates event1
		Entity event = socialOperation.createEntity(user1.getEntityBaseId(),
				EntityTypes.event);

		Assert.assertEquals(
				0,
				sharingManager.getShared(user2.getId(),
						Arrays.asList(user1.getId()), -1L, null, 0, 5, null,
						false).size());

		// user1 shares an entity with user2
		ShareVisibility visibility = new ShareVisibility();
		visibility.setUserIds(Arrays.asList(user2.getId()));

		Assert.assertTrue(sharingManager.share(event.getId(), user1.getId(),
				visibility));

		Assert.assertEquals(
				1,
				sharingManager.getShared(user2.getId(),
						Arrays.asList(new Long[] { user1.getId() }), -1L, null,
						0, 5, null, false).size());

		// unshare
		Assert.assertTrue(sharingManager.unshare(event.getId(), user1.getId()));

		Assert.assertEquals(
				0,
				sharingManager.getShared(user2.getId(),
						Arrays.asList(user1.getId()), -1L, null, 0, 5, null,
						false).size());

		// user1 shares an entity with user2
		Assert.assertTrue(sharingManager.share(event.getId(), user1.getId(),
				visibility));

		Assert.assertEquals(
				1,
				sharingManager.getShared(user2.getId(),
						Arrays.asList(user1.getId()), -1L, null, 0, 5, null,
						false).size());

		// reset sharing options
		visibility.setUserIds(null);

		Assert.assertTrue(sharingManager.share(event.getId(), user1.getId(),
				visibility));

		Assert.assertEquals(
				0,
				sharingManager.getShared(user2.getId(),
						Arrays.asList(new Long[] { user1.getId() }), -1L, null,
						0, 5, null, false).size());

		socialOperation.deleteEntity(event);
		socialOperation.deleteUser(user1.getId());
		socialOperation.deleteUser(user2.getId());

	}

	/**
	 * Test sharing
	 * 
	 * @throws CommunityManagerException
	 * @throws WebApiException
	 */
	@Test
	public void sharing() throws CommunityManagerException, WebApiException {
		User user1 = socialOperation.createUser();
		User user2 = socialOperation.createUser();

		// user1 creates event1
		Entity event = socialOperation.createEntity(user1.getEntityBaseId(),
				EntityTypes.event);

		Assert.assertEquals(
				0,
				sharingManager.getShared(user2.getId(),
						Arrays.asList(user1.getId()), -1L, null, 0, 5, null,
						false).size());

		// user1 shares an entity with user2
		ShareVisibility visibility = new ShareVisibility();
		visibility.setUserIds(Arrays.asList(user2.getId()));

		Assert.assertTrue(sharingManager.share(event.getId(), user1.getId(),
				visibility));

		Assert.assertEquals(
				1,
				sharingManager.getShared(user2.getId(),
						Arrays.asList(user1.getId()), -1L, null, 0, 5, null,
						false).size());

		// get assignments
		Assert.assertEquals(
				1,
				sharingManager
						.getAssignments(user1.getId(),
								Arrays.asList(event.getId())).get(0)
						.getUserIds().size());

		// public share

		Assert.assertEquals(
				0,
				sharingManager.getShared(-1L, Arrays.asList(user1.getId()),
						-1L, null, 0, 5, null, false).size());

		visibility = new ShareVisibility();
		visibility.setAllUsers(true);
		visibility.setAllCommunities(true);
		// user1 shares a public event
		Assert.assertTrue(sharingManager.share(event.getId(), user1.getId(),
				visibility));

		Assert.assertEquals(
				1,
				sharingManager.getShared(-1L, Arrays.asList(user1.getId()),
						-1L, null, 0, 5, null, false).size());

		socialOperation.deleteEntity(event);
		socialOperation.deleteUser(user1.getId());
		socialOperation.deleteUser(user2.getId());

	}

	/**
	 * Test to check sharing operation of My People group
	 * 
	 * @throws CommunityManagerException
	 * @throws WebApiException
	 */
	@Test
	public void myPeopleShare() throws CommunityManagerException,
			WebApiException {
		User user1 = socialOperation.createUser();
		User user2 = socialOperation.createUser();

		// user2 creates entity1
		Entity entity1 = socialOperation.createEntity(user2.getEntityBaseId(),
				EntityTypes.event);

		// user1 create group1
		long gid = groupManager.create(user1.getId(),
				"SC_TEST_GROUP_" + System.currentTimeMillis());
		// user1 adds user2 to group1
		groupManager.addUser(user1.getId(), Collections.singletonList(gid),
				user2.getId());

		Assert.assertEquals(
				0,
				sharingManager.getShared(user1.getId(), null,
						Constants.MY_PEOPLE_GROUP_ID, null, 0, 5, null, false)
						.size());

		// user2 shares entity1 public
		ShareVisibility visibility = new ShareVisibility();
		visibility.setAllUsers(true);
		Assert.assertTrue(sharingManager.share(entity1.getId(), user2.getId(),
				visibility));

		Assert.assertEquals(
				1,
				sharingManager.getShared(user1.getId(), null,
						Constants.MY_PEOPLE_GROUP_ID, null, 0, 5, null, false)
						.size());

		socialOperation.deleteUser(user1.getId());
		socialOperation.deleteUser(user2.getId());
		socialOperation.deleteEntity(entity1);

	}

	/**
	 * Getting entities created by a user
	 * 
	 * @throws CommunityManagerException
	 * @throws WebApiException
	 */
	@Test
	public void myContents() throws CommunityManagerException, WebApiException {

		User user1 = socialOperation.createUser();
		// event of user1
		Entity e1 = socialOperation.createEntity(user1.getEntityBaseId(),
				EntityTypes.event);
		Entity e2 = socialOperation.createEntity(user1.getEntityBaseId(),
				EntityTypes.location);
		Entity e3 = socialOperation.createEntity(user1.getEntityBaseId(),
				EntityTypes.experience);

		Assert.assertEquals(
				3,
				sharingManager.getShared(user1.getId(),
						Collections.singletonList(user1.getId()), -1L, null, 0,
						10, null, false).size());

		Assert.assertEquals(
				1,
				sharingManager.getShared(user1.getId(),
						Collections.singletonList(user1.getId()), -1L, null, 0,
						1, null, false).size());

		Assert.assertEquals(
				2,
				sharingManager.getShared(user1.getId(),
						Collections.singletonList(user1.getId()), -1L, null, 1,
						10, null, false).size());

		Assert.assertEquals(
				1,
				sharingManager.getShared(user1.getId(),
						Collections.singletonList(user1.getId()), -1L, null, 0,
						10, EntityTypes.event.toString(), false).size());

		socialOperation.deleteUser(user1.getId());
		socialOperation.deleteEntity(e1);
		socialOperation.deleteEntity(e2);
		socialOperation.deleteEntity(e3);

	}

	/**
	 * Test sharing options read
	 * 
	 * @throws WebApiException
	 * @throws CommunityManagerException
	 */

	@Test
	public void checkAssignements() throws WebApiException,
			CommunityManagerException {
		User user1 = socialOperation.createUser();
		User user2 = socialOperation.createUser();

		// user1 creates event1
		Entity e1 = socialOperation.createEntity(user1.getEntityBaseId(),
				EntityTypes.event);

		// user1 create group1
		long gid = groupManager.create(user1.getId(),
				"SC_TEST_GROUP_" + System.currentTimeMillis());

		// share visibility has no setted value
		ShareVisibility visibility = sharingManager.getAssignments(
				user1.getId(), e1.getId());
		Assert.assertTrue(visibility.getGroupIds().size() == 0);
		Assert.assertTrue(visibility.getUserIds().size() == 0);
		Assert.assertTrue(visibility.getCommunityIds().size() == 0);
		Assert.assertTrue(!visibility.isAllCommunities());
		Assert.assertTrue(!visibility.isAllKnownCommunities());
		Assert.assertTrue(!visibility.isAllKnownUsers());
		Assert.assertTrue(!visibility.isAllUsers());

		// user1 shares e1 with default commmunity
		long communityId = socialOperation.getDefaultCommunity();
		visibility.setCommunityIds(Arrays.asList(communityId));

		// user1 shares e1 with user2
		visibility.setUserIds(Arrays.asList(user2.getId()));

		// user1 shares e1 with group1
		visibility.setGroupIds(Arrays.asList(gid));

		visibility.setAllCommunities(true);
		visibility.setAllKnownCommunities(true);

		visibility.setAllKnownUsers(true);
		visibility.setAllUsers(true);

		Assert.assertTrue(sharingManager.share(e1.getId(), user1.getId(),
				visibility));

		// share visibility has one community setted
		Assert.assertEquals(1,
				sharingManager.getAssignments(user1.getId(), e1.getId())
						.getGroupIds().size());
		Assert.assertEquals(1,
				sharingManager.getAssignments(user1.getId(), e1.getId())
						.getGroupIds().size());
		Assert.assertEquals(1,
				sharingManager.getAssignments(user1.getId(), e1.getId())
						.getGroupIds().size());

		socialOperation.deleteUser(user1.getId());
		socialOperation.deleteEntity(e1);
	}

	@Test
	public void readEntities() throws WebApiException {
		EntityBase eb1 = socialOperation.createEntityBase();
		Entity e1 = socialOperation
				.createEntity(eb1.getId(), EntityTypes.event);
		Entity e2 = socialOperation
				.createEntity(eb1.getId(), EntityTypes.event);

		socialOperation.getEntity(e1.getId());
		socialOperation.getEntity(e2.getId());

		List<Long> eids = Arrays.asList(e1.getId(), e2.getId());

		socialOperation.getEntities(eids);

		socialOperation.deleteEntity(e1);
		socialOperation.deleteEntity(e2);

	}

	@Test
	public void entityCrud() throws CommunityManagerException,
			AlreadyExistException {
		List<Concept> concepts = sharingManager.getConceptSuggestions(
				"concert", 1);
		Concept test = sharingManager.getConceptByGlobalId(4080l);
		EntityType entityType = sharingManager.getEntityTypeByConcept(test);
		if (entityType != null) {
			sharingManager.deleteEntityType(entityType.getId());
		}
		entityType = sharingManager.createEntityType(test.getId());
		eu.trentorise.smartcampus.vas.communitymanager.model.Entity e = new eu.trentorise.smartcampus.vas.communitymanager.model.Entity();
		e.setCreatorId(CREATOR_ID);
		e.setDescription("description");
		e.setName("entitySample");
		e.setTags(Arrays.asList(concepts.get(0)));
		e.setType(entityType.getName());
		Assert.assertTrue(e.getId() <= 0);
		e = sharingManager.createEntity(e);
		Assert.assertTrue(e.getId() > 0);
		Assert.assertTrue(sharingManager.deleteEntity(e.getId()));
		Assert.assertTrue(sharingManager.deleteEntityType(entityType.getId()));
	}

}
