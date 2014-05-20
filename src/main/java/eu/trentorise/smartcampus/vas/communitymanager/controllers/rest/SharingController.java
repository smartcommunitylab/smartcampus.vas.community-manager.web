package eu.trentorise.smartcampus.vas.communitymanager.controllers.rest;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import eu.trentorise.smartcampus.aac.AACException;
import eu.trentorise.smartcampus.aac.AACService;
import eu.trentorise.smartcampus.aac.model.TokenData;
import eu.trentorise.smartcampus.profileservice.BasicProfileService;
import eu.trentorise.smartcampus.profileservice.ProfileServiceException;
import eu.trentorise.smartcampus.profileservice.model.BasicProfile;
import eu.trentorise.smartcampus.socialservice.SocialService;
import eu.trentorise.smartcampus.socialservice.SocialServiceException;
import eu.trentorise.smartcampus.socialservice.beans.Entity;

@Controller
public class SharingController {

	private static final Logger logger = Logger
			.getLogger(SharingController.class);

	@Autowired
	@Value("${aacURL}")
	private String aacUrl;

	@Autowired
	@Value("${smartcampus.clientId}")
	private String scClientId;

	@Autowired
	@Value("${smartcampus.clientSecret}")
	private String scClientSecret;

	@Autowired
	private SocialService socialService;

	AACService aacService;

	private BasicProfileService profileService;

	private TokenData token;

	private String getClientToken() {
		try {
			if (token == null
					|| token.getExpires_on() <= System.currentTimeMillis()) {
				logger.info("Client token null or expired, getting new....");
				token = getAACService().generateClientToken();

			}
		} catch (AACException e) {
			logger.error("Exception getting client token " + e.getMessage());
		}
		return token.getAccess_token();
	}

	@RequestMapping(method = RequestMethod.GET, value = "/share/{appId}")
	public @ResponseBody
	Entity share(HttpServletRequest request, @RequestBody Entity entity,
			@PathVariable String appId) throws SecurityException,
			ProfileServiceException, SocialServiceException {
		String authToken = getRequestToken(request);
		BasicProfile profile = getBasicProfileService().getBasicProfile(
				authToken);
		return socialService.createOrUpdateUserEntityByApp(getClientToken(),
				appId, profile.getUserId(), entity);
	}

	private String getRequestToken(HttpServletRequest request) {
		return (String) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal();
	}

	private BasicProfileService getBasicProfileService() {
		if (profileService == null) {
			profileService = new BasicProfileService(aacUrl);
		}
		return profileService;
	}

	private AACService getAACService() {
		if (aacService == null) {
			aacService = new AACService(aacUrl, scClientId, scClientSecret);
		}

		return aacService;
	}
}
