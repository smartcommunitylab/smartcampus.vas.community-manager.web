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
import eu.trentorise.smartcampus.common.Concept;
import eu.trentorise.smartcampus.common.ShareVisibility;
import eu.trentorise.smartcampus.vas.communitymanager.managers.AlreadyExistException;
import eu.trentorise.smartcampus.vas.communitymanager.managers.CommunityManagerException;
import eu.trentorise.smartcampus.vas.communitymanager.managers.SharingManager;
import eu.trentorise.smartcampus.vas.communitymanager.model.Entity;
import eu.trentorise.smartcampus.vas.communitymanager.model.EntityType;
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
			@RequestParam Long type) throws IOException,
			CommunityManagerException {

		User user = retrieveUser(request, response);

		long groupId = (visibility.getGroupIds() != null && !visibility
				.getGroupIds().isEmpty()) ? visibility.getGroupIds().get(0)
				: -1;
		return sharingManager.getShared(user.getSocialId(),
				visibility.getUserIds(), groupId, visibility.getCommunityIds(),
				position, size, type, visibility.getCommunityIds() == null
						|| visibility.getCommunityIds().isEmpty());
	}

	@RequestMapping(method = RequestMethod.GET, value = "/content")
	public @ResponseBody
	List<SharedContent> getMyContents(HttpServletRequest request,
			HttpServletResponse response, HttpSession session,
			@RequestParam int position, @RequestParam int size,
			@RequestParam Long type) throws IOException,
			CommunityManagerException {

		User user = retrieveUser(request, response);

		return sharingManager.getShared(user.getSocialId(),
				Collections.singletonList(user.getSocialId()), null, null,
				position, size, type, false);
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

	@RequestMapping(method = RequestMethod.POST, value = "/entitytype/{conceptId}")
	public @ResponseBody
	EntityType createEntityType(HttpServletRequest request,
			HttpServletResponse response, HttpSession session,
			@PathVariable long conceptId) throws IOException,
			CommunityManagerException {

		User user = retrieveUser(request, response);

		try {
			return sharingManager.createEntityType(conceptId);
		} catch (AlreadyExistException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST,
					e.getMessage());
			return null;
		}
	}

	@RequestMapping(method = RequestMethod.GET, value = "/entitytype-by-id/{etId}")
	public @ResponseBody
	EntityType getEntityTypeById(HttpServletRequest request,
			HttpServletResponse response, HttpSession session,
			@PathVariable long etId) throws IOException,
			CommunityManagerException {

		User user = retrieveUser(request, response);

		return sharingManager.getEntityType(etId);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/entitytype-by-conceptid/{cId}")
	public @ResponseBody
	EntityType getEntityTypeByConcept(HttpServletRequest request,
			HttpServletResponse response, HttpSession session,
			@PathVariable long cId) throws IOException,
			CommunityManagerException {

		User user = retrieveUser(request, response);

		return sharingManager.getEntityTypeByConceptId(cId);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/entitytype-by-prefix/{prefix}")
	public @ResponseBody
	List<EntityType> getEntityTypeBySuggestions(HttpServletRequest request,
			HttpServletResponse response, HttpSession session,
			@PathVariable String prefix,
			@RequestParam(required = false) Integer maxResults)
			throws IOException, CommunityManagerException {

		User user = retrieveUser(request, response);

		return sharingManager.getEntityTypeByName(prefix, maxResults);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/suggestion/{prefix}")
	public @ResponseBody
	List<Concept> getConceptSuggestions(HttpServletRequest request,
			HttpServletResponse response, HttpSession session,
			@PathVariable String prefix,
			@RequestParam(required = false) Integer maxResults)
			throws IOException, CommunityManagerException {

		User user = retrieveUser(request, response);

		return sharingManager.getConceptSuggestions(prefix, maxResults);
	}

	@RequestMapping(method = RequestMethod.POST, value = "/entity")
	public @ResponseBody
	Entity createEntity(HttpServletRequest request,
			HttpServletResponse response, @RequestBody Entity entity)
			throws CommunityManagerException {
		User user = retrieveUser(request, response);
		entity.setCreatorId(user.getSocialId());
		return sharingManager.createEntity(entity);
	}

	@RequestMapping(method = RequestMethod.PUT, value = "/entity")
	public @ResponseBody
	boolean updateEntity(HttpServletRequest request,
			HttpServletResponse response, @RequestBody Entity entity)
			throws CommunityManagerException {
		User user = retrieveUser(request, response);
		if (!sharingManager.checkPermission(user, entity.getId())) {
			throw new SecurityException();
		}
		sharingManager.updateEntity(entity);
		return true;
	}

	@RequestMapping(method = RequestMethod.DELETE, value = "/entity/{eid}")
	public @ResponseBody
	boolean deleteEntity(HttpServletRequest request,
			HttpServletResponse response, @PathVariable long eid)
			throws CommunityManagerException {
		User user = retrieveUser(request, response);
		if (!sharingManager.checkPermission(user, eid)) {
			throw new SecurityException();
		}
		return sharingManager.deleteEntity(eid);
	}
}