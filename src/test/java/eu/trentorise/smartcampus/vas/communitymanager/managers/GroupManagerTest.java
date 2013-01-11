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

import java.util.Collections;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/spring/applicationContext.xml")
public class GroupManagerTest {

	@Autowired
	private GroupManager groupManager;

	@Autowired
	private SocialEngineOperation socialOperation;

	// sample code of retrieve spring bean in a non spring context

	// public static void init() throws WebApiException {
	// ClassPathXmlApplicationContext ctx = new
	// ClassPathXmlApplicationContext(
	// "spring/applicationContext.xml");
	// groupManager = ctx.getBean(GroupManager.class);
	// }

	@Test
	public void create() throws CommunityManagerException, WebApiException {
		User user1 = socialOperation.createUser();
		String groupName = "SC_TEST_GROUP_" + System.currentTimeMillis();
		long groupId = groupManager.create(user1.getId(), groupName);
		Assert.assertTrue(groupId > 0);
		Assert.assertTrue(groupManager.delete(groupId));

		socialOperation.deleteUser(user1.getId());
	}

	/**
	 * Test on delete an user from the default system group
	 * 
	 * @throws Exception
	 */
	@Test
	public void removeFromDefaultGroup() throws Exception {
		User user1 = socialOperation.createUser();
		User user2 = socialOperation.createUser();

		Assert.assertEquals(0, groupManager.getKnownUsers(user1.getId()).size());

		// user1 creates group1 adding user2 in
		long groupId = groupManager.create(user1.getId(), "SC_TEST_GROUP_"
				+ System.currentTimeMillis());
		Assert.assertTrue(groupId > -1);

		Assert.assertTrue(groupManager.addUser(user1.getId(),
				Collections.singletonList(groupId), user2.getId()));

		Assert.assertEquals(1, groupManager.getKnownUsers(user1.getId()).size());

		// user1 removes user2 from his knownUsers
		Assert.assertTrue(groupManager.removeFromDefault(user1.getId(),
				user2.getId()));

		Assert.assertEquals(0, groupManager.getKnownUsers(user1.getId()).size());

		groupManager.delete(groupId);
		socialOperation.deleteUser(user1.getId());
		socialOperation.deleteUser(user2.getId());

	}

}
