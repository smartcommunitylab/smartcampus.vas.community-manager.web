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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import eu.trentorise.smartcampus.ac.provider.AcService;
import eu.trentorise.smartcampus.ac.provider.filters.AcProviderFilter;
import eu.trentorise.smartcampus.ac.provider.model.User;
import eu.trentorise.smartcampus.vas.communitymanager.managers.CommunityManagerException;

@Controller
public class RestController {

	private static final Logger logger = Logger.getLogger(RestController.class);
	@Autowired
	private AcService acService;

	protected User retrieveUser(HttpServletRequest request,
			HttpServletResponse response) throws CommunityManagerException {
		try {
			String token = request.getHeader(AcProviderFilter.TOKEN_HEADER);
			return acService.getUserByToken(token);
		} catch (Exception e) {
			logger.error("Exception checking token");
			throw new CommunityManagerException();
		}
	}

}
