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
package eu.trentorise.smartcampus.vas.communitymanager.controllers.rest;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import eu.trentorise.smartcampus.ac.provider.model.User;
import eu.trentorise.smartcampus.vas.communitymanager.managers.CommunityManagerException;
import eu.trentorise.smartcampus.vas.communitymanager.managers.TopicManager;
import eu.trentorise.smartcampus.vas.communitymanager.model.Topic;

@Controller
public class TopicController extends RestController {

	private static final Logger logger = Logger
			.getLogger(TopicController.class);

	@Autowired
	private TopicManager topicManager;

	@RequestMapping(method = RequestMethod.GET, value = "/eu.trentorise.smartcampus.cm.model.Topic")
	public @ResponseBody
	List<Topic> getUserTopics(HttpServletRequest request,
			HttpServletResponse response, HttpSession session)
			throws CommunityManagerException, IOException {

		User user = retrieveUser(request, response);

		return topicManager.getTopics(user.getSocialId());
	}

	@RequestMapping(method = RequestMethod.GET, value = "/eu.trentorise.smartcampus.cm.model.Topic/{tid}")
	public @ResponseBody
	Topic getTopic(HttpServletRequest request, HttpServletResponse response,
			HttpSession session, @PathVariable("tid") long tid)
			throws CommunityManagerException, IOException {

		User user = retrieveUser(request, response);
		if (!topicManager.checkPermission(user.getSocialId(), tid)) {
			throw new SecurityException();
		}
		return topicManager.getTopic(tid);
	}

	@RequestMapping(method = RequestMethod.POST, value = "/eu.trentorise.smartcampus.cm.model.Topic")
	public @ResponseBody
	Topic createTopic(HttpServletRequest request, HttpServletResponse response,
			HttpSession session, @RequestBody Topic topicInRequest)
			throws CommunityManagerException, IOException {

		User user = retrieveUser(request, response);

		long id = topicManager.create(user.getSocialId(), topicInRequest);
		if (id < 0)
			throw new IllegalArgumentException();
		return topicManager.getTopic(id);
	}

	@RequestMapping(method = RequestMethod.DELETE, value = "/eu.trentorise.smartcampus.cm.model.Topic/{tid}")
	public @ResponseBody
	boolean deleteTopic(HttpServletRequest request,
			HttpServletResponse response, HttpSession session,
			@PathVariable("tid") long tid) throws IOException,
			CommunityManagerException {

		User user = retrieveUser(request, response);
		if (!topicManager.checkPermission(user.getSocialId(), tid)) {
			throw new SecurityException();
		}
		return topicManager.delete(tid);
	}

	@RequestMapping(method = RequestMethod.PUT, value = "/eu.trentorise.smartcampus.cm.model.Topic/{tid}")
	public @ResponseBody
	boolean updateTopic(HttpServletRequest request,
			HttpServletResponse response, HttpSession session,
			@PathVariable("tid") long tid, @RequestBody Topic topicInRequest)
			throws CommunityManagerException, IOException {

		User user = retrieveUser(request, response);
		if (!topicManager.checkPermission(user.getSocialId(), tid)) {
			throw new SecurityException();
		}

		// socialId is mandatory
		topicInRequest.setSocialId(tid);

		return topicManager.update(user.getSocialId(), topicInRequest);
	}

	@RequestMapping(method = RequestMethod.PUT, value = "/changestatus/{tid}/{status}")
	public @ResponseBody
	boolean changeStatus(HttpServletRequest request,
			HttpServletResponse response, HttpSession session,
			@PathVariable("tid") long tid, @PathVariable("status") int status)
			throws IOException, CommunityManagerException {

		User user = retrieveUser(request, response);

		if (!topicManager.checkPermission(user.getSocialId(), tid)) {
			throw new SecurityException();
		}

		return topicManager.changeStatus(tid, status);
	}
}
