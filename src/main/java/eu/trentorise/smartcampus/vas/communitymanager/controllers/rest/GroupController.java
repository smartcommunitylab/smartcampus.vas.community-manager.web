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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import eu.trentorise.smartcampus.ac.provider.model.User;
import eu.trentorise.smartcampus.vas.communitymanager.managers.CommunityManagerException;
import eu.trentorise.smartcampus.vas.communitymanager.managers.GroupManager;
import eu.trentorise.smartcampus.vas.communitymanager.model.Group;
import eu.trentorise.smartcampus.vas.communitymanager.model.GroupAssignment;

@Controller("groupController")
public class GroupController extends RestController {

	private static final Logger logger = Logger
			.getLogger(GroupController.class);

	@Autowired
	private GroupManager groupManager;

	@RequestMapping(method = RequestMethod.GET, value = "/eu.trentorise.smartcampus.cm.model.Group")
	public @ResponseBody
	List<Group> getUserGroups(HttpServletRequest request,
			HttpServletResponse response, HttpSession session)
			throws CommunityManagerException, IOException {

		User user = retrieveUser(request, response);
		return groupManager.getGroups(user.getSocialId());
	}

	@RequestMapping(method = RequestMethod.GET, value = "/eu.trentorise.smartcampus.cm.model.Group/{groupId}")
	public @ResponseBody
	Group getUserGroup(HttpServletRequest request,
			HttpServletResponse response, HttpSession session,
			@PathVariable("groupId") long groupId)
			throws CommunityManagerException {

		User user = retrieveUser(request, response);
		if (!groupManager.checkPermission(user.getSocialId(), groupId)) {
			throw new SecurityException();
		}

		return groupManager.getGroup(groupId);
	}

	@RequestMapping(method = RequestMethod.POST, value = "/eu.trentorise.smartcampus.cm.model.Group")
	public @ResponseBody
	Group createGroup(HttpServletRequest request, HttpServletResponse response,
			HttpSession session, @RequestBody Group groupInRequest)
			throws CommunityManagerException, IOException {

		User user = retrieveUser(request, response);

		long id = groupManager.create(user.getSocialId(),
				groupInRequest.getName());
		if (id != -1) {
			groupInRequest.setSocialId(id);
		} else {
			groupInRequest = null;
		}
		return groupInRequest;
	}

	@RequestMapping(method = RequestMethod.DELETE, value = "/eu.trentorise.smartcampus.cm.model.Group/{groupId}")
	public @ResponseBody
	boolean deleteGroup(HttpServletRequest request,
			HttpServletResponse response, HttpSession session,
			@PathVariable("groupId") long groupId)
			throws CommunityManagerException, IOException {

		User user = retrieveUser(request, response);

		if (!groupManager.checkPermission(user.getSocialId(), groupId)) {
			throw new SecurityException();
		}

		return groupManager.delete(groupId);
	}

	@RequestMapping(method = RequestMethod.PUT, value = "/eu.trentorise.smartcampus.cm.model.Group/{groupId}")
	public @ResponseBody
	boolean updateGroup(HttpServletRequest request,
			HttpServletResponse response, HttpSession session,
			@PathVariable("groupId") long groupId,
			@RequestBody Group groupInRequest)
			throws CommunityManagerException, IOException {

		User user = retrieveUser(request, response);

		if (!groupManager.checkPermission(user.getSocialId(), groupId)) {
			throw new SecurityException();
		}
		// socialId is mandatory
		groupInRequest.setSocialId(groupId);

		return groupManager.update(groupInRequest);
	}

	@RequestMapping(method = RequestMethod.PUT, value = "/assigntogroup")
	public @ResponseBody
	boolean assignToGroup(HttpServletRequest request,
			HttpServletResponse response, HttpSession session,
			@RequestBody GroupAssignment requestBody)
			throws CommunityManagerException, IOException {

		long userIdToAssign = requestBody.getUserId();
		List<Long> groupIds = requestBody.getGroupIds();
		if (groupIds == null)
			groupIds = Collections.emptyList();

		User user = retrieveUser(request, response);

		return groupManager.addUser(user.getSocialId(), groupIds,
				userIdToAssign);
	}

	@RequestMapping(method = RequestMethod.PUT, value = "/addtogroup")
	public @ResponseBody
	boolean addUser(HttpServletRequest request, HttpServletResponse response,
			HttpSession session, @RequestBody String requestBody)
			throws CommunityManagerException, IOException {
		long groupId = -1;
		List<Long> userIds = new ArrayList<Long>();
		try {
			JSONObject jsonRequest = new JSONObject(requestBody);
			groupId = jsonRequest.getLong("groupId");
			JSONArray jsonUsers = jsonRequest.getJSONArray("userIds");
			for (int i = 0; i < jsonUsers.length(); i++) {
				userIds.add(jsonUsers.getLong(i));
			}

		} catch (JSONException e) {
			logger.error("Exception parsing JSON request", e);
			try {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST,
						"Invalid body request");
				return false;
			} catch (IOException e1) {
				logger.error("Exception sending HTTP error");
				return false;
			}
		}

		User user = retrieveUser(request, response);

		return groupManager.addUser(user.getSocialId(), userIds, groupId);

	}

	@RequestMapping(method = RequestMethod.PUT, value = "/removefromgroup")
	public @ResponseBody
	boolean removeUser(HttpServletRequest request,
			HttpServletResponse response, HttpSession session,
			@RequestBody String requestBody) throws CommunityManagerException,
			IOException {
		long groupId = -1;
		List<Long> userIds = new ArrayList<Long>();
		try {
			JSONObject jsonRequest = new JSONObject(requestBody);
			groupId = jsonRequest.getLong("groupId");
			JSONArray jsonUsers = jsonRequest.getJSONArray("userIds");
			for (int i = 0; i < jsonUsers.length(); i++) {
				userIds.add(jsonUsers.getLong(i));
			}

		} catch (JSONException e) {
			logger.error("Exception parsing JSON request", e);
			try {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST,
						"Invalid body request");
				return false;
			} catch (IOException e1) {
				logger.error("Exception sending HTTP error");
				return false;
			}
		}

		User user = retrieveUser(request, response);

		if (!groupManager.checkPermission(user.getSocialId(), groupId)) {
			throw new SecurityException();
		}

		return groupManager.removeUser(userIds, groupId);
	}

	@RequestMapping(method = RequestMethod.DELETE, value = "/defaultgroup/{userId}")
	public @ResponseBody
	boolean removeFromDefault(HttpServletRequest request,
			HttpServletResponse response, HttpSession session,
			@PathVariable("userId") long userId) throws Exception {

		User user = retrieveUser(request, response);
		return groupManager.removeFromDefault(user.getSocialId(), userId);
	}

}
