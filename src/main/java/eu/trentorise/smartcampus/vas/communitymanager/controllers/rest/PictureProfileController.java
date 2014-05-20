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
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import eu.trentorise.smartcampus.profileservice.BasicProfileService;
import eu.trentorise.smartcampus.profileservice.ProfileServiceException;
import eu.trentorise.smartcampus.profileservice.model.BasicProfile;
import eu.trentorise.smartcampus.vas.communitymanager.managers.CommunityManagerException;
import eu.trentorise.smartcampus.vas.communitymanager.managers.FileManager;
import eu.trentorise.smartcampus.vas.communitymanager.managers.UserManager;
import eu.trentorise.smartcampus.vas.communitymanager.model.MinimalProfile;
import eu.trentorise.smartcampus.vas.communitymanager.model.Picture;
import eu.trentorise.smartcampus.vas.communitymanager.model.PictureProfile;
import eu.trentorise.smartcampus.vas.communitymanager.model.StoreProfile;

/**
 * 
 * @author raman
 * 
 */
@Controller
public class PictureProfileController {

	@Autowired
	@Value("${aacURL}")
	private String url;

	@Autowired
	private UserManager userManager;

	@Autowired
	FileManager fileManager;

	private BasicProfileService basicProfileService = null;

	private BasicProfileService getBasicProfileService() {
		if (basicProfileService == null)
			basicProfileService = new BasicProfileService(url);
		return basicProfileService;
	}

	@RequestMapping(method = RequestMethod.GET, value = "pictureprofile/current")
	public @ResponseBody
	PictureProfile getProfile(HttpServletRequest request)
			throws SecurityException, ProfileServiceException {
		String token = getToken(request);
		BasicProfile bp = getBasicProfileService().getBasicProfile(token);
		PictureProfile profile = toPictureProfile(bp);
		return profile;
	}

	@RequestMapping(method = RequestMethod.GET, value = "pictureprofile")
	public @ResponseBody
	List<PictureProfile> getProfile(HttpServletRequest request,
			@RequestParam(required = false) String filter,
			@RequestParam(required = false) List<String> ids)
			throws SecurityException, ProfileServiceException {
		String token = getToken(request);
		List<BasicProfile> bpList = null;
		if (ids != null) {
			bpList = getBasicProfileService().getBasicProfilesByUserId(ids,
					token);
		} else {
			bpList = getBasicProfileService().getBasicProfiles(filter, token);
		}

		List<PictureProfile> list = new ArrayList<PictureProfile>();
		if (bpList != null)
			for (BasicProfile bp : bpList)
				list.add(toPictureProfile(bp));
		return list;
	}

	private PictureProfile toPictureProfile(BasicProfile bp)
			throws ProfileServiceException {
		PictureProfile profile = new PictureProfile(bp);
		try {
			MinimalProfile mp = userManager.getUserById(Long.parseLong(bp
					.getUserId()));
			if (mp == null) {
				StoreProfile sp = new StoreProfile();
				sp.setUser(bp.getUserId());
				sp.setUserId(Long.parseLong(bp.getUserId()));
				sp.setSocialId(Long.parseLong(bp.getSocialId()));
				sp.setName(bp.getName());
				sp.setSurname(bp.getSurname());
				userManager.create(sp);
			} else {
				profile.setPictureUrl(mp.getPictureUrl());
			}
		} catch (Exception e) {
			throw new ProfileServiceException(e);
		}
		return profile;
	}

	private String getToken(HttpServletRequest request) {
		return (String) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal();
	}

	@RequestMapping(value = "/pictureprofile/file", method = RequestMethod.POST)
	public @ResponseBody
	PictureProfile uploadFile(HttpServletRequest request,
			HttpServletResponse response, HttpSession session,
			@RequestParam("file") MultipartFile file) throws IOException,
			CommunityManagerException, SecurityException,
			ProfileServiceException {
		String token = getToken(request);
		BasicProfile bp = getBasicProfileService().getBasicProfile(token);
		PictureProfile pp = toPictureProfile(bp);
		long userId = Long.parseLong(bp.getUserId());
		StoreProfile sp = userManager.getStoreProfileByUserId(userId);
		Picture picture = fileManager.updload(userId, file.getBytes());
		sp.setPictureUrl(picture.getId());
		sp.setPicturePath(picture.getPath());
		userManager.update(sp);
		pp.setPictureUrl(sp.getPictureUrl());
		return pp;
	}

	@RequestMapping(value = "/pictureprofile/file/{fileId}", method = RequestMethod.GET)
	public @ResponseBody
	byte[] downloadFile(HttpServletRequest request,
			HttpServletResponse response, HttpSession session,
			@PathVariable("fileId") long fileId) throws IOException,
			CommunityManagerException {
		return fileManager.download(fileId);
	}

}
