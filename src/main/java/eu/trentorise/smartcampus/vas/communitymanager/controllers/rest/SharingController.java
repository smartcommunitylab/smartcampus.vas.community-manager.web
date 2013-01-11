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
import java.util.Collections;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import eu.trentorise.smartcampus.ac.provider.model.User;
import eu.trentorise.smartcampus.common.ShareVisibility;
import eu.trentorise.smartcampus.vas.communitymanager.managers.CommunityManagerException;
import eu.trentorise.smartcampus.vas.communitymanager.managers.SharingManager;
import eu.trentorise.smartcampus.vas.communitymanager.model.ShareOperation;
import eu.trentorise.smartcampus.vas.communitymanager.model.SharedContent;

@Controller("sharingController")
public class SharingController extends RestController {

	private static final Logger logger = Logger
			.getLogger(SharingController.class);

	@Autowired
	private SharingManager sharingManager;

	@RequestMapping(method = RequestMethod.POST, value = "/share")
	public @ResponseBody
	boolean share(HttpServletRequest request, HttpServletResponse response,
			HttpSession session, @RequestBody ShareOperation shareOperation)
			throws IOException, CommunityManagerException {

		User user = retrieveUser(request, response);

		return sharingManager.share(shareOperation.getEntityId(),
				user.getSocialId(), shareOperation.getVisibility());
	}

	@RequestMapping(method = RequestMethod.PUT, value = "/unshare/{entityId}")
	public @ResponseBody
	boolean unshare(HttpServletRequest request, HttpServletResponse response,
			HttpSession session, @PathVariable("entityId") long entityId)
			throws IOException, CommunityManagerException {

		User user = retrieveUser(request, response);

		return sharingManager.unshare(entityId, user.getSocialId());
	}

	@RequestMapping(method = RequestMethod.POST, value = "/sharedcontent")
	public @ResponseBody
	List<SharedContent> get(HttpServletRequest request,
			HttpServletResponse response, HttpSession session,
			@RequestBody ShareVisibility visibility,
			@RequestParam int position, @RequestParam int size,
			@RequestParam String type) throws IOException,
			CommunityManagerException {

		User user = retrieveUser(request, response);

		long groupId = (visibility.getGroupIds() != null && !visibility
				.getGroupIds().isEmpty()) ? visibility.getGroupIds().get(0)
				: -1;
		return sharingManager.getShared(user.getSocialId(),
				visibility.getUserIds(), groupId, visibility.getCommunityIds(),
				position, size, type, visibility.getCommunityIds() == null || visibility.getCommunityIds().isEmpty());
	}

	@RequestMapping(method = RequestMethod.GET, value = "/content")
	public @ResponseBody
	List<SharedContent> getMyContents(HttpServletRequest request,
			HttpServletResponse response, HttpSession session,
			@RequestParam int position, @RequestParam int size,
			@RequestParam String type) throws IOException,
			CommunityManagerException {

		User user = retrieveUser(request, response);

		return sharingManager.getShared(user.getSocialId(), Collections.singletonList(user.getSocialId()), null, null, position, size, type, false);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/assignments/{eid}")
	public @ResponseBody
	ShareVisibility getAssignments(HttpServletRequest request,
			HttpServletResponse response, HttpSession session,
			@PathVariable("eid") long eid) throws IOException,
			CommunityManagerException {

		User user = retrieveUser(request, response);

		return sharingManager.getAssignments(user.getSocialId(), eid);
	}
}
