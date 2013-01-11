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

import it.unitn.disi.sweb.webapi.client.WebApiException;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import eu.trentorise.smartcampus.ac.provider.model.User;
import eu.trentorise.smartcampus.common.Concept;
import eu.trentorise.smartcampus.vas.communitymanager.managers.CommonsManager;
import eu.trentorise.smartcampus.vas.communitymanager.managers.CommunityManagerException;
import eu.trentorise.smartcampus.vas.communitymanager.managers.FileManager;

@Controller
public class CommonController extends RestController {

	@Autowired
	CommonsManager commonsManager = null;

	@Autowired
	FileManager fileManager;

	@RequestMapping(value = "/search/conceptSuggest", method = RequestMethod.GET)
	public @ResponseBody
	ResponseEntity<List<Concept>> suggest(HttpServletRequest request,
			HttpServletResponse response, HttpSession session,
			@RequestParam String q) throws WebApiException {

		try {
			List<Concept> responseList = commonsManager.getSuggestions(q, 10);
			return new ResponseEntity<List<Concept>>(responseList,
					HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<List<Concept>>(HttpStatus.METHOD_FAILURE);
		}
	}

	@RequestMapping(value = "/file", method = RequestMethod.POST)
	public @ResponseBody
	long uploadFile(HttpServletRequest request, HttpServletResponse response,
			HttpSession session, @RequestParam("file") MultipartFile file)
			throws IOException, CommunityManagerException {
		User user = retrieveUser(request, response);

		long fid = fileManager.updload(user.getSocialId(), file.getBytes());
		return fid;
	}

	@RequestMapping(value = "/file/{fileId}", method = RequestMethod.GET)
	public @ResponseBody
	byte[] downloadFile(HttpServletRequest request,
			HttpServletResponse response, HttpSession session,
			@PathVariable("fileId") long fileId) throws IOException,
			CommunityManagerException {
		User user = retrieveUser(request, response);
		return fileManager.download(fileId);
	}

	@RequestMapping(value = "/file/{fileId}", method = RequestMethod.DELETE)
	public @ResponseBody
	boolean removeFile(HttpServletRequest request,
			HttpServletResponse response, HttpSession session,
			@PathVariable("fileId") long fileId) throws IOException,
			CommunityManagerException {
		User user = retrieveUser(request, response);
		return fileManager.delete(fileId);
	}

	@RequestMapping(value = "/file/{fileId}", method = RequestMethod.POST)
	public @ResponseBody
	boolean replaceFile(HttpServletRequest request,
			HttpServletResponse response, HttpSession session,
			@PathVariable("fileId") long fileId,
			@RequestParam("file") MultipartFile file) throws IOException,
			CommunityManagerException {
		User user = retrieveUser(request, response);
		return fileManager.replace(fileId, file.getBytes());
	}

	private String getUrlFromRequest(HttpServletRequest request) {
		return ((request.getProtocol().contains("HTTP/1.1")) ? "http" : "https")
				+ "://"
				+ request.getServerName()
				+ ":"
				+ request.getServerPort() + request.getContextPath() + "/file";
	}
}
