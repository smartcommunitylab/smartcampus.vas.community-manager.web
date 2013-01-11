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
import eu.trentorise.smartcampus.vas.communitymanager.managers.CommunityManager;
import eu.trentorise.smartcampus.vas.communitymanager.managers.CommunityManagerException;
import eu.trentorise.smartcampus.vas.communitymanager.model.Community;

@Controller("communityController")
public class CommunityController extends RestController {

	private static final Logger logger = Logger
			.getLogger(CommunityController.class);

	@Autowired
	private CommunityManager communityManager;

	@RequestMapping(method = RequestMethod.POST, value = "/eu.trentorise.smartcampus.cm.model.Community")
	public @ResponseBody
	long create(HttpServletRequest request, HttpServletResponse response,
			HttpSession session, @RequestBody Community communityInRequest)
			throws CommunityManagerException {
		return communityManager.create(communityInRequest);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/eu.trentorise.smartcampus.cm.model.Community")
	public @ResponseBody
	List<Community> getCommunities(HttpServletRequest request,
			HttpServletResponse response, HttpSession session)
			throws CommunityManagerException {
		User user = retrieveUser(request, response);

		return communityManager.getCommunities(user.getSocialId(), false);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/eu.trentorise.smartcampus.cm.model.Community/{cid}")
	public @ResponseBody
	Community getCommunities(HttpServletRequest request,
			HttpServletResponse response, HttpSession session,
			@PathVariable("cid") long cid) throws CommunityManagerException {
		return communityManager.getCommunity(cid);
	}

	@RequestMapping(method = RequestMethod.PUT, value = "/addtocommunity/{cid}")
	public @ResponseBody
	boolean addUser(HttpServletRequest request, HttpServletResponse response,
			HttpSession session, @PathVariable("cid") long cid)
			throws CommunityManagerException {
		User user = retrieveUser(request, response);
		return communityManager.addUser(user.getSocialId(), cid);

	}

	@RequestMapping(method = RequestMethod.PUT, value = "/removefromcommunity/{cid}")
	public @ResponseBody
	boolean removeUser(HttpServletRequest request,
			HttpServletResponse response, HttpSession session,
			@PathVariable("cid") long cid) throws CommunityManagerException {
		User user = retrieveUser(request, response);
		return communityManager.removeUser(user.getSocialId(), cid);

	}

}
