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
import it.unitn.disi.sweb.webapi.model.smartcampus.social.User;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import eu.trentorise.smartcampus.vas.communitymanager.model.Community;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/spring/applicationContext.xml")
public class CommunityManagerTest {

	@Autowired
	private CommunityManager communityManager;

	@Autowired
	private SocialEngineOperation socialOperation;

	@Test
	public void create() throws CommunityManagerException {
		Community community = new Community();
		community.setName("SC_TEST_COMMUNITY_" + System.currentTimeMillis());
		long id = communityManager.create(community);
		Assert.assertTrue(id > 0);
		Assert.assertTrue(communityManager.delete(id));
	}

	/**
	 * Test on community user subscription
	 * 
	 * @throws CommunityManagerException
	 * @throws WebApiException
	 */
	@Test
	public void getUserCommunities() throws CommunityManagerException,
			WebApiException {
		User user1 = socialOperation.createUser();

		// creation of a community
		Community community = new Community();
		community.setName("SC_TEST_COMMUNITY_" + System.currentTimeMillis());
		long id = communityManager.create(community);
		Assert.assertTrue(id > 0);

		Assert.assertEquals(0,
				communityManager.getCommunities(user1.getId(), true).size());
		// user1 is added to community
		Assert.assertTrue(communityManager.addUser(user1.getId(), id));
		Assert.assertEquals(1,
				communityManager.getCommunities(user1.getId(), true).size());

		communityManager.delete(id);
		socialOperation.deleteUser(user1.getId());
	}

	/**
	 * Test communities which user not belongs to
	 * 
	 * @throws CommunityManagerException
	 * @throws WebApiException
	 */
	@Test
	public void unsubscibedCommunities() throws CommunityManagerException,
			WebApiException {
		User user1 = socialOperation.createUser();
		Community community = new Community();
		community.setName("SC_TEST_COMMUNITY_" + System.currentTimeMillis());
		long id = communityManager.create(community);
		Assert.assertTrue(id > 0);

		// number of community which user1 not belongs to
		int unsubscribedCommunities = communityManager.getCommunities(
				user1.getId(), false).size();

		Assert.assertTrue(unsubscribedCommunities > 0);
		Assert.assertEquals(0,
				communityManager.getCommunities(user1.getId(), true).size());
		// user1 is added to community
		Assert.assertTrue(communityManager.addUser(user1.getId(), id));

		Assert.assertEquals(1,
				communityManager.getCommunities(user1.getId(), true).size());

		Assert.assertEquals(unsubscribedCommunities - 1, communityManager
				.getCommunities(user1.getId(), false).size());

		communityManager.delete(id);
		socialOperation.deleteUser(user1.getId());
	}

}
