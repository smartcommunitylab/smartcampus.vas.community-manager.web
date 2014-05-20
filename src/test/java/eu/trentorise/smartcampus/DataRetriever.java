package eu.trentorise.smartcampus;

import it.unitn.disi.sweb.webapi.client.WebApiException;
import it.unitn.disi.sweb.webapi.client.smartcampus.SCWebApiClient;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.httpclient.URIException;
import org.apache.commons.io.FileUtils;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import eu.trentorise.smartcampus.vas.communitymanager.managers.CommunityManagerException;
import eu.trentorise.smartcampus.vas.communitymanager.managers.UserManager;
import eu.trentorise.smartcampus.vas.communitymanager.model.MinimalProfile;

public class DataRetriever {

	private static final String HOST = "sweb.smartcampuslab.it";
	private static final int PORT = 8080;
	private static final String DESTINATION = "/home/mirko/data/temp/mypeople";

	private static final String DB_URL = "jdbc:mysql://localhost/acprovider-test";
	private static final String DB_USER = "ac";
	private static final String DB_PWD = "ac";

	public static final void main(String[] a) {
		System.out.println(String.format("SWEB HOST: %s", HOST));
		System.out.println(String.format("SWEB PORT: %s", PORT));
		System.out
				.println(String.format("DESTINATION FOLDER: %s", DESTINATION));
		System.out.println(String.format("DB URL: %s", DB_URL));

		// init
		SCWebApiClient client = SCWebApiClient.getInstance(Locale.ENGLISH,
				HOST, PORT);
		System.out.println(String.format("sweb client initialized"));

		File destFolder = new File(DESTINATION);
		if (!destFolder.exists()) {
			if (destFolder.mkdirs()) {
				System.out.println(String.format("%s created", destFolder));
			}
		}
		List<Long> userIds = new ArrayList<Long>();

		UserManager userManager = new ClassPathXmlApplicationContext(
				"classpath:/spring/applicationContext.xml")
				.getBean(UserManager.class);

		// collect all userId
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			Connection conn = DriverManager.getConnection(DB_URL, DB_USER,
					DB_PWD);
			System.out.println("Connected to db " + DB_URL);
			java.sql.Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT id FROM user");
			while (rs.next()) {
				userIds.add(rs.getLong("id"));
			}
			System.out
					.println(String.format("Founded %s users", userIds.size()));
			if (userIds.size() == 0) {
				System.exit(0);
			} else {
				// retrieve fileId from mypeopledb
				for (Long uid : userIds) {
					MinimalProfile p = null;
					try {
						p = userManager.getUserById(uid);
						if (p != null && p.getPictureUrl() != null
								&& !p.getPictureUrl().trim().isEmpty()) {
							InputStream in = client.openStream(Long.valueOf(p
									.getPictureUrl()));
							FileUtils.copyInputStreamToFile(in, new File(
									DESTINATION, uid + ".jpg"));
						}
					} catch (CommunityManagerException e) {
						System.err.println(String.format(
								"Exception getting picture id of user %s: %s",
								uid, e.getMessage()));
					} catch (URIException e) {
						System.err.println(String.format(
								"Exception open sweb stream fileId %s, %s",
								p.getPictureUrl(), e.getMessage()));
					} catch (WebApiException e) {
						System.err.println(String.format(
								"Exception open sweb stream fileId %s, %s",
								p.getPictureUrl(), e.getMessage()));
					} catch (IOException e) {
						System.err
								.println(String
										.format("Exception writing on destination fileId %s, %s",
												p.getPictureUrl(),
												e.getMessage()));
					}
				}
			}
		} catch (InstantiationException e) {
			exitMsg("Exception connecting db " + e.getMessage());
		} catch (IllegalAccessException e) {
			exitMsg("Exception connecting db " + e.getMessage());
		} catch (ClassNotFoundException e) {
			exitMsg("Exception connecting db " + e.getMessage());
		} catch (SQLException e) {
			exitMsg("Exception connecting db " + e.getMessage());
		}
		System.out.println("DONE!");
	}

	private static void exitMsg(String msg) {
		System.err.println(msg);
		System.exit(-1);
	}
}
