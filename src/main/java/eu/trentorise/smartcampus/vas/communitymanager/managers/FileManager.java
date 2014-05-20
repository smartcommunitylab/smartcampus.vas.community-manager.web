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

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;

import eu.trentorise.smartcampus.vas.communitymanager.model.MinimalProfile;
import eu.trentorise.smartcampus.vas.communitymanager.model.Picture;

/**
 * <i>FileManager</i> manages functionalities of resources uploaded into user
 * space
 * 
 * @author mirko perillo
 * 
 */
@Component
public class FileManager {

	private static final Logger logger = Logger.getLogger(FileManager.class);

	@Autowired
	@Value("${picture.folder}")
	private String pictureFolderPath;

	@Autowired
	UserManager userManager;

	@PostConstruct
	@SuppressWarnings("unused")
	private void init() {
		if (!StringUtils.hasText(pictureFolderPath)) {
			String msg = "Properties picture.folder unsetted";
			logger.error(msg);
			throw new IllegalArgumentException(msg);
		} else {
			if (!pictureFolderPath.endsWith("/")
					&& !pictureFolderPath.endsWith("\\")) {
				pictureFolderPath += "/";
			}
			File pictureFolder = new File(pictureFolderPath);
			if (!pictureFolder.exists()) {
				if (pictureFolder.mkdirs()) {
					logger.info("Created picture folder: " + pictureFolderPath);
				} else {
					logger.error("Error creating picture folder: "
							+ pictureFolderPath);
				}
			}
		}
	}

	/**
	 * uploads resources into user space
	 * 
	 * @param ownerId
	 *            social id of user space
	 * @param file
	 *            resource to upload
	 * @return Picture containing file details
	 * @throws CommunityManagerException
	 */
	public Picture upload(long ownerId, File file)
			throws CommunityManagerException {
		File picture = new File(pictureFolderPath, "" + ownerId + "."
				+ getExtension(file));
		// if (picture.exists()) {
		// throw new CommunityManagerException(
		// "profile picture already exists");
		// } else {
		try {
			FileUtils.copyFile(file, picture);
		} catch (IOException e) {
			String msg = "Exception storing picture: "
					+ picture.getAbsolutePath();
			logger.error(msg);
			throw new CommunityManagerException(msg);
		}
		// }

		return new Picture(picture.getName(), "" + ownerId);
	}

	/**
	 * uploads resources into user space
	 * 
	 * @param ownerId
	 *            social id of user space
	 * @param file
	 *            resource byte array
	 * @return Picture containing file details
	 * @throws CommunityManagerException
	 */
	public Picture updload(long ownerId, byte[] file)
			throws CommunityManagerException {
		try {
			File temp = File.createTempFile("sc_file", null);
			temp.deleteOnExit();
			FileCopyUtils.copy(file, temp);
			Picture picture = upload(ownerId, temp);
			try {
				temp.delete();
			} catch (SecurityException e) {
				logger.warn("Exception delete temporary file", e);
			}
			return picture;
		} catch (Exception e) {
			String msg = "Exception uploading file for user " + ownerId;
			logger.error(msg, e);
			throw new CommunityManagerException(msg);
		}
	}

	/**
	 * downloads a resource
	 * 
	 * @param socialId
	 *            id of resource to download
	 * @return byte array of resource
	 * @throws CommunityManagerException
	 */
	public byte[] download(long socialId) throws CommunityManagerException {
		try {
			File picture = getProfilePicture(socialId);
			return FileCopyUtils.copyToByteArray(picture);
		} catch (Exception e) {
			logger.error("Exception downloading file", e);
			throw new CommunityManagerException();
		}

	}

	/**
	 * deletes resource
	 * 
	 * @param socialId
	 *            id of resource to delete
	 * @return true if operation gone fine, false otherwise
	 */
	public boolean delete(long socialId) throws CommunityManagerException {
		try {
			return getProfilePicture(socialId).delete();
		} catch (CommunityManagerException e) {
			return false;
		}
	}

	/**
	 * updates resource with other resource
	 * 
	 * @param socialId
	 *            id of resource to update
	 * @param file
	 *            new content for the resource
	 * @return true if operation gone fine, false otherwise
	 * @throws CommunityManagerException
	 */
	public boolean replace(long socialId, File file)
			throws CommunityManagerException {

		File picture = getProfilePicture(socialId);
		try {
			FileUtils.copyFile(file, picture);
		} catch (IOException e) {
			String msg = "Error replacing profile picture for user " + socialId;
			logger.error(msg);
			throw new CommunityManagerException(msg);
		}
		return true;
	}

	/**
	 * updates resource with other resource
	 * 
	 * @param socialId
	 *            id of resource to update
	 * @param file
	 *            new content for the resource
	 * @return true if operation gone fine, false otherwise
	 */
	public boolean replace(long socialId, byte[] file) {
		try {
			File temp = File.createTempFile("sc_file", null);
			temp.deleteOnExit();
			FileCopyUtils.copy(file, temp);
			replace(socialId, temp);
			try {
				temp.delete();
			} catch (SecurityException e) {
				logger.warn("Exception delete temporary file", e);
			}
			return true;
		} catch (Exception e) {
			logger.error("Exception replacing file " + socialId, e);
			return false;
		}
	}

	private String getExtension(File file) {
		String name = file.getName();
		return name.substring(name.lastIndexOf(".") + 1);
	}

	private File getProfilePicture(long userId)
			throws CommunityManagerException {
		MinimalProfile profile = userManager.getUserById(userId);
		String msg = null;
		if (profile == null) {
			msg = "No profile for user " + userId;
			logger.error(msg);
			throw new CommunityManagerException(msg);
		}
		boolean pictureExist = StringUtils.hasText(profile.getPictureUrl());

		File picture = null;
		if (pictureExist) {
			picture = new File(pictureFolderPath + profile.getPicturePath());
			pictureExist = picture.exists();
		}

		if (!pictureExist) {
			msg = "No profile picture for user " + userId;
			logger.error(msg);
			throw new CommunityManagerException(msg);

		}
		return picture;

	}
}
