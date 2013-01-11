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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/spring/applicationContext.xml")
public class FileManagerTest {

	@Autowired
	private FileManager fileManager;

	@Autowired
	private SocialEngineOperation socialOperation;

	private static final String FILE_NAME = "profile_icon.jpeg";

	/**
	 * Test on upload and delete of a file in user space
	 * 
	 * @throws CommunityManagerException
	 * @throws WebApiException
	 */
	@Test
	public void crud() throws CommunityManagerException, WebApiException {
		File file = null;
		try {
			file = File.createTempFile("sc_test_file", "");
			file.deleteOnExit();

			InputStream in = getClass().getResourceAsStream(FILE_NAME);
			BufferedInputStream bin = new BufferedInputStream(in);
			FileOutputStream fos = new FileOutputStream(file);
			byte[] buffer = new byte[200];
			while (bin.read(buffer, 0, buffer.length) != -1) {
				fos.write(buffer);
			}
			fos.close();
			bin.close();
			in.close();
		} catch (IOException e) {
			Assert.fail("Exception creating temp file");
		}
		User user1 = socialOperation.createUser();
		long fid = fileManager.upload(user1.getId(), file);
		Assert.assertTrue(fid > 0);

		Assert.assertTrue(fileManager.delete(fid));

		socialOperation.deleteUser(user1.getId());

	}

}
