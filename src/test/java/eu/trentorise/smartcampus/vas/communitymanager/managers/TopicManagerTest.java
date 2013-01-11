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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import eu.trentorise.smartcampus.common.Concept;
import eu.trentorise.smartcampus.vas.communitymanager.model.Group;
import eu.trentorise.smartcampus.vas.communitymanager.model.Topic;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/spring/applicationContext.xml")
public class TopicManagerTest {

	private static final Logger logger = Logger
			.getLogger(TopicManagerTest.class);
	@Autowired
	private TopicManager topicManager;
	@Autowired
	private GroupManager groupManager;

	@Autowired
	private SocialEngineOperation socialOperation;

	/**
	 * Crud operation on topic
	 * 
	 * @throws CommunityManagerException
	 * @throws WebApiException
	 */
	@Test
	public void crud() throws CommunityManagerException, WebApiException {
		User user1 = socialOperation.createUser();
		Topic topic = new Topic();
		topic.setName("SC_TEST_TOPIC_" + System.currentTimeMillis());
		topic.setKeywords(Arrays.asList(new String[] { "city", "happy hour" }));
		long tid = topicManager.create(user1.getId(), topic);
		Assert.assertTrue(tid > 0);
		Assert.assertEquals(2, topic.getKeywords().size());
		topic = topicManager.getTopic(tid);
		topic.setKeywords(Arrays.asList(new String[] { "mtb" }));
		Assert.assertTrue(topicManager.update(user1.getId(), topic));
		topic = topicManager.getTopic(tid);
		Assert.assertEquals(1, topic.getKeywords().size());
		Assert.assertTrue(topicManager.delete(tid));

		socialOperation.deleteUser(user1.getId());
	}

	/**
	 * Test a complex topic
	 * 
	 * @throws CommunityManagerException
	 * @throws WebApiException
	 */
	@Test
	public void complexTopic() throws CommunityManagerException,
			WebApiException {
		User user1 = socialOperation.createUser();
		Topic topic = new Topic();
		topic.setName("SC_TEST_TOPIC_" + System.currentTimeMillis());

		// add keywords
		topic.setKeywords(Arrays.asList(new String[] { "city", "happy hour" }));

		// add concepts
		List<Concept> concepts = new ArrayList<Concept>();
		concepts.add(socialOperation.getTestConcept());
		topic.setConcepts(concepts);

		// add source group
		long gid = groupManager.create(user1.getId(),
				"SC_TEST_GROUP_" + System.currentTimeMillis());
		Assert.assertTrue(gid > 0);
		topic.setGroups(Arrays.asList(new Group[] { groupManager.getGroup(gid) }));

		long tid = topicManager.create(user1.getId(), topic);
		Assert.assertTrue(tid > 0);

		Assert.assertTrue(topicManager.delete(tid));

		socialOperation.deleteUser(user1.getId());
	}
}
