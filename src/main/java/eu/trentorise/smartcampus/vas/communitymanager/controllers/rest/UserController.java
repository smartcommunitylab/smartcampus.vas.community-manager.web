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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Validator;
import org.owasp.esapi.errors.IntrusionException;
import org.owasp.esapi.errors.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import eu.trentorise.smartcampus.ac.provider.model.User;
import eu.trentorise.smartcampus.vas.communitymanager.managers.CommunityManagerException;
import eu.trentorise.smartcampus.vas.communitymanager.managers.GroupManager;
import eu.trentorise.smartcampus.vas.communitymanager.managers.UserManager;
import eu.trentorise.smartcampus.vas.communitymanager.model.MinimalProfile;
import eu.trentorise.smartcampus.vas.communitymanager.model.Profile;
import eu.trentorise.smartcampus.vas.communitymanager.model.StoreProfile;
import eu.trentorise.smartcampus.vas.communitymanager.model.filters.ProfileFilter;

@Controller("userController")
public class UserController extends RestController {

	private static final Logger logger = Logger.getLogger(UserController.class);

	private static final int INPUT_MAX_LENGTH = 99;

	@Autowired
	private UserManager userManager;
	@Autowired
	private GroupManager groupManager;

	private Validator validator = ESAPI.validator();
	
	@RequestMapping(method = RequestMethod.GET, value = "/eu.trentorise.smartcampus.cm.model.MinimalProfile")
	public @ResponseBody
	List<MinimalProfile> getUsers(HttpServletRequest request,
			HttpServletResponse response, HttpSession session)
			throws CommunityManagerException, IOException {

		User user = retrieveUser(request, response);

		List<MinimalProfile> list = userManager.getUsers();
		return assignKnown(user, list);
	}

	private List<MinimalProfile> assignKnown(User user,
			List<MinimalProfile> list) throws CommunityManagerException {
		Set<Long> known = groupManager.getKnownUsers(user.getSocialId());
		if (known == null)
			known = Collections.emptySet();
		if (list != null) {
			for (Iterator<MinimalProfile> iterator = list.iterator(); iterator
					.hasNext();) {
				MinimalProfile mp = iterator.next();
				if (mp.getSocialId() == user.getSocialId())
					iterator.remove();
				if (known.contains(mp.getSocialId())) {
					mp.setKnown(true);
				} else {
					mp.setKnown(false);
				}
			}
		}
		return list;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/eu.trentorise.smartcampus.cm.model.MinimalProfile/{userId}")
	public @ResponseBody
	MinimalProfile getUser(HttpServletRequest request,
			HttpServletResponse response, HttpSession session,
			@PathVariable("userId") long userId) throws IOException,
			CommunityManagerException {

		User user = retrieveUser(request, response);

		return userManager.getUserById(userId);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/objects")
	public @ResponseBody
	Map<String, List<MinimalProfile>> searchUsers(HttpServletRequest request,
			HttpServletResponse response, HttpSession session,
			@RequestParam("filter") String jsonFilter)
			throws CommunityManagerException, IOException {

		ProfileFilter filter = null;
		try {
			ObjectMapper mapper = new ObjectMapper();
			filter = mapper.readValue(jsonFilter, ProfileFilter.class);
		} catch (Exception e) {
			try {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST,
						"Invalid parameter format");
			} catch (IOException e1) {
				logger.error("Exception sending HTTP error");
			}
			return null;
		}

		User user = retrieveUser(request, response);
		Map<String, List<MinimalProfile>> result = new HashMap<String, List<MinimalProfile>>();
		result.put("eu.trentorise.smartcampus.cm.model.MinimalProfile",
				assignKnown(user, userManager.getUsers(filter.getFullname())));
		return result;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/eu.trentorise.smartcampus.cm.model.Profile/{userId}")
	public @ResponseBody
	Profile getProfile(HttpServletRequest request,
			HttpServletResponse response, HttpSession session,
			@PathVariable("userId") long userId) throws IOException,
			CommunityManagerException {

		User user = retrieveUser(request, response);

		if (userId != user.getId()) {
			throw new SecurityException();
		}

		return userManager.getOrCreateProfile(user);
	}

	@RequestMapping(method = RequestMethod.PUT, value = "/eu.trentorise.smartcampus.cm.model.StoreProfile/{profileId}")
	public @ResponseBody
	boolean updateProfile(HttpServletRequest request,
			HttpServletResponse response, HttpSession session,
			@PathVariable("profileId") String profileId,
			@RequestBody StoreProfile profileInRequest) throws IOException,
			CommunityManagerException, ValidationException, IntrusionException {

		User user = retrieveUser(request, response);

		if (!userManager.checkPermission(user.getSocialId(), profileId)) {
			throw new SecurityException();
		}

		cleanUpProfile(profileInRequest);
		// use parameter profileId
		profileInRequest.setId(profileId);

		return userManager.update(profileInRequest);
	}

	@RequestMapping(method = RequestMethod.POST, value = "/eu.trentorise.smartcampus.cm.model.StoreProfile")
	public @ResponseBody
	StoreProfile createProfile(HttpServletRequest request,
			HttpServletResponse response, HttpSession session,
			@RequestBody StoreProfile profileInRequest) throws IOException,
			CommunityManagerException, ValidationException, IntrusionException {

		cleanUpProfile(profileInRequest);

		User user = retrieveUser(request, response);
		profileInRequest.setUser("" + user.getId());
		profileInRequest.setUserId(user.getId());
		profileInRequest.setSocialId(user.getSocialId());
		return userManager.create(profileInRequest);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/eu.trentorise.smartcampus.cm.model.Profile/current")
	public @ResponseBody
	Profile findProfile(HttpServletRequest request,
			HttpServletResponse response, HttpSession session)
			throws IOException, CommunityManagerException {

		User user = retrieveUser(request, response);
		return userManager.getOrCreateProfile(user);
	}
	
	private void cleanUpProfile(StoreProfile profile) throws ValidationException, IntrusionException {
		if (profile.getName() != null) {
			String s = validator.getValidSafeHTML("name", profile.getName(), INPUT_MAX_LENGTH, true);
			profile.setName(StringEscapeUtils.unescapeHtml(s));
		}
		if (profile.getSurname() != null) {
			String s = validator.getValidSafeHTML("surname", profile.getSurname(), INPUT_MAX_LENGTH, true);
			profile.setSurname(StringEscapeUtils.unescapeHtml(s));
		}
	}
}
