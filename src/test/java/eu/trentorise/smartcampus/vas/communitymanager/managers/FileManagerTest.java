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

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import eu.trentorise.smartcampus.vas.communitymanager.model.Picture;
import eu.trentorise.smartcampus.vas.communitymanager.model.StoreProfile;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/spring/applicationContext.xml")
public class FileManagerTest {

	@Autowired
	private FileManager fileManager;

	@Autowired
	private UserManager userManager;

	@Autowired
	@Value("${picture.folder}")
	private String folder;

	@Before
	public void init() throws CommunityManagerException, IOException {
		if (userManager.getStoreProfileByUserId(23l) == null) {
			StoreProfile profile = new StoreProfile();
			profile.setName("m");
			profile.setSurname("p");
			profile.setUserId(23l);

			userManager.create(profile);
		}

		FileUtils.cleanDirectory(new File(folder));

	}

	@Test
	public void crud() {

		try {
			Picture picture = fileManager.upload(
					23l,
					FileUtils.toFile(Thread.currentThread()
							.getContextClassLoader()
							.getResource("profile_icon.jpeg")));
			StoreProfile profile = userManager.getStoreProfileByUserId(23l);
			profile.setPictureUrl(picture.getId());
			profile.setPicturePath(picture.getPath());
			userManager.update(profile);
			fileManager.download(Long.valueOf(picture.getId()));
		} catch (CommunityManagerException e) {
			Assert.fail("Exception: " + e.getMessage());
		}
	}
}
