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
package eu.trentorise.smartcampus.vas.communitymanager.managers;

import it.unitn.disi.sweb.webapi.client.WebApiException;
import it.unitn.disi.sweb.webapi.model.smartcampus.social.User;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import eu.trentorise.smartcampus.ac.provider.model.Attribute;
import eu.trentorise.smartcampus.ac.provider.model.Authority;
import eu.trentorise.smartcampus.presentation.common.exception.NotFoundException;
import eu.trentorise.smartcampus.vas.communitymanager.model.MinimalProfile;
import eu.trentorise.smartcampus.vas.communitymanager.model.Profile;
import eu.trentorise.smartcampus.vas.communitymanager.model.StoreProfile;
import eu.trentorise.smartcampus.vas.communitymanager.model.UserInformation;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/spring/applicationContext.xml")
public class UserManagerTest {

	@Autowired
	private UserManager userManager;

	@Autowired
	private SocialEngineOperation socialOperation;

	private static User socialUser;
	private static StoreProfile testProfile;

	@Before
	public void setupDb() throws CommunityManagerException, WebApiException,
			NotFoundException {
		cleanDb();
		socialUser = socialOperation.createUser();
		testProfile = new StoreProfile();
		testProfile.setName("Mirko");
		testProfile.setSurname("Perillo");
		testProfile.setFullname("mirko perillo");
		testProfile.setAge(30);
		UserInformation info = new UserInformation();
		info.setFaculty("trentorise");
		info.setPosition("developer");
		testProfile.setUserInformation(info);
		testProfile.setUserId(1);
		testProfile.setSocialId(socialUser.getId());
		userManager.create(testProfile);
	}

	@After
	public void cleanDb() throws CommunityManagerException, WebApiException {

		for (MinimalProfile p : userManager.getUsers()) {
			userManager.delete(userManager.getStoreProfileByUserId(p
					.getUserId()));
			socialOperation.deleteUser(p.getSocialId());
		}
	}

	@Test
	public void create() throws CommunityManagerException, WebApiException {
		StoreProfile profile = new StoreProfile();
		profile.setFullname("sc profile");
		profile.setName("sc");
		profile.setSurname("profile");
		profile.setAge(30);
		profile.setUserId(2);
		profile.setSocialId(socialOperation.createUser().getId());

		UserInformation info = new UserInformation();
		info.setFaculty("trentorise");
		info.setPosition("developer");
		profile.setUserInformation(info);
		Assert.assertNotNull(userManager.create(profile));
		Assert.assertTrue(userManager.getUsers("perillo").size() == 1);
		Assert.assertTrue(userManager.getUsers().size() == 2);
		Assert.assertTrue(userManager.delete(profile));
		Assert.assertTrue(userManager.getUsers().size() == 1);

	}

	@Test
	public void searchByFullname() throws CommunityManagerException {
		String searchString = "peri";
		Assert.assertNotNull(userManager.getUsers(searchString));
		Assert.assertTrue(userManager.getUsers(searchString).size() == 1);
	}

	@Test
	public void searchBySocialId() throws CommunityManagerException {
		Assert.assertNotNull(userManager.getProfileBySocialId(socialUser
				.getId()));
		Assert.assertNull(userManager.getProfileBySocialId(1000));
	}

	@Test
	public void searchById() throws CommunityManagerException {
		Assert.assertNotNull(userManager.getProfileById(testProfile.getId()));
		Assert.assertNull(userManager.getProfileById("1000"));
	}

	@Test
	public void searchByUserId() throws CommunityManagerException {
		Assert.assertNotNull(userManager.getProfileByUserId(testProfile
				.getUserId()));
		Assert.assertNull(userManager.getProfileByUserId(100000));
	}

	@Test
	public void storePresentUser() throws CommunityManagerException {
		StoreProfile testProfile = new StoreProfile();
		testProfile.setFullname("Smartcampus user");
		testProfile.setName("Smartcampus");
		testProfile.setSurname("Smartcampus");
		testProfile.setAge(30);
		UserInformation info = new UserInformation();
		info.setFaculty("trentorise");
		info.setPosition("tester");
		testProfile.setUserInformation(info);
		testProfile.setUserId(1);
		testProfile.setSocialId(socialUser.getId());

		Assert.assertEquals("mirko perillo", userManager
				.getStoreProfileByUserId(1).getFullname());
		userManager.create(testProfile);
		Assert.assertEquals(1, userManager.getUsers().size());

		Assert.assertEquals("Smartcampus user", userManager
				.getStoreProfileByUserId(1).getFullname());
		Assert.assertEquals("tester", userManager.getStoreProfileByUserId(1)
				.getUserInformation().getPosition());

	}

	@Test
	public void getOrCreateProfile() throws CommunityManagerException,
			WebApiException {
		User socialUser = socialOperation.createUser();
		eu.trentorise.smartcampus.ac.provider.model.User user = new eu.trentorise.smartcampus.ac.provider.model.User();
		user.setAuthToken("dummieToken");
		user.setExpTime(1000L);
		user.setId(1000L);
		user.setSocialId(socialUser.getId());

		user.setAttributes(getTestKOAttrs());

		Assert.assertNull(userManager.getOrCreateProfile(user));

		user.setAttributes(getTestOKAttrs());

		Profile profile = userManager.getOrCreateProfile(user);
		Assert.assertNotNull(profile);

		Assert.assertEquals("sc", profile.getName());
		Assert.assertEquals("trentorise", profile.getSurname());

		socialOperation.deleteUser(socialUser.getId());
	}

	private List<Attribute> getTestKOAttrs() {
		List<Attribute> attrs = new ArrayList<Attribute>();

		Authority auth = new Authority();
		auth.setId(1L);
		auth.setName("fbk");
		auth.setRedirectUrl("fbk");

		Attribute attr = new Attribute();
		attr.setAuthority(auth);
		attr.setKey("Shib-Session-ID");
		attr.setValue("dummievalue");
		attrs.add(attr);

		attr = new Attribute();
		attr.setAuthority(auth);
		attr.setKey("Shib-Application-ID");
		attr.setValue("dummievalue");
		attrs.add(attr);

		attr = new Attribute();
		attr.setAuthority(auth);
		attr.setKey("eu.trentorise.smartcampus.givenname");
		attr.setValue("sc");
		attrs.add(attr);
		return attrs;

	}

	private List<Attribute> getTestOKAttrs() {
		List<Attribute> attrs = new ArrayList<Attribute>();

		Authority auth = new Authority();
		auth.setId(1L);
		auth.setName("fbk");
		auth.setRedirectUrl("fbk");

		Attribute attr = new Attribute();
		attr.setAuthority(auth);
		attr.setKey("Shib-Session-ID");
		attr.setValue("dummievalue");
		attrs.add(attr);

		attr = new Attribute();
		attr.setAuthority(auth);
		attr.setKey("eu.trentorise.smartcampus.givenname");
		attr.setValue("sc");
		attrs.add(attr);

		attr = new Attribute();
		attr.setAuthority(auth);
		attr.setKey("Shib-Application-ID");
		attr.setValue("dummievalue");
		attrs.add(attr);

		attr = new Attribute();
		attr.setAuthority(auth);
		attr.setKey("eu.trentorise.smartcampus.surname");
		attr.setValue("trentorise");
		attrs.add(attr);

		return attrs;

	}

}
