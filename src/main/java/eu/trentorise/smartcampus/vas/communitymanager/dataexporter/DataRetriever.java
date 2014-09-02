package eu.trentorise.smartcampus.vas.communitymanager.dataexporter;

import it.unitn.disi.sweb.webapi.client.WebApiException;
import it.unitn.disi.sweb.webapi.client.smartcampus.SCWebApiClient;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.httpclient.URIException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.mongodb.Mongo;
import com.mongodb.MongoException;

import eu.trentorise.smartcampus.presentation.storage.sync.mongo.SyncObjectBean;

/**
 * Utility class to port picture profile files from sweb social engine to
 * mypeople
 * 
 * @author mirko perillo
 * 
 */
public class DataRetriever {

	private static final String HOST = "sweb.smartcampuslab.it";
	private static final int PORT = 8080;
	private static final String DESTINATION = "/home/dev/mypeople-picture";

	private static final String DB_URL = "jdbc:mysql://localhost/acprovider";
	private static final String DB_USER = "ac";
	private static final String DB_PWD = "ac";

	private static final String PROFILE_DB_HOST = "localhost";
	private static final int PROFILE_DB_PORT = 27017;
	private static final String PROFILE_DB_NAME = "profiledb";

	public static final void main(String[] a) {
		System.out.println(String.format("SWEB HOST: %s", HOST));
		System.out.println(String.format("SWEB PORT: %s", PORT));
		System.out
				.println(String.format("DESTINATION FOLDER: %s", DESTINATION));
		System.out.println(String.format("DB URL: %s", DB_URL));
		System.out.println(String
				.format("PROFILE DB HOST: %s", PROFILE_DB_HOST));
		System.out.println(String
				.format("PROFILE DB PORT: %s", PROFILE_DB_PORT));
		System.out.println(String
				.format("PROFILE DB NAME: %s", PROFILE_DB_NAME));

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
				MongoOperations mongo = new MongoTemplate(
						new SimpleMongoDbFactory(new Mongo(PROFILE_DB_HOST,
								PROFILE_DB_PORT), PROFILE_DB_NAME));
				System.out
						.println("Connected to profile db " + PROFILE_DB_NAME);
				ObjectMapper mapper = new ObjectMapper();
				mapper.configure(
						DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES,
						false);
				mapper.configure(
						DeserializationConfig.Feature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT,
						true);
				// retrieve fileId from mypeopledb
				for (Long uid : userIds) {
					SyncObjectBean profileContainer = null;
					String pictureUrl = null;
					try {
						Criteria criteria = new Criteria("content.userId")
								.is(uid)
								.and("type")
								.is("eu.trentorise.smartcampus.vas.communitymanager.model.StoreProfile");
						profileContainer = mongo.findOne(Query.query(criteria),
								SyncObjectBean.class);
						if (profileContainer == null) {
							System.out.println(String.format(
									"User %s not found in profile db", uid));
						} else {
							pictureUrl = (String) profileContainer.getContent()
									.get("pictureUrl");
							if (StringUtils.isBlank(pictureUrl)) {
								System.out.println(String.format(
										"PictureUrl not setted for user %s ",
										uid));
							} else {
								InputStream in = client.openStream(Long
										.valueOf(pictureUrl));
								if (in == null) {
									System.out.println(String.format(
											"Picture %s not present in sweb ",
											pictureUrl));
								}
								FileUtils.copyInputStreamToFile(in, new File(
										DESTINATION, uid + ".png"));
							}
						}
					} catch (URIException e) {
						System.err.println(String.format(
								"Exception open sweb stream fileId %s, %s",
								pictureUrl, e.getMessage()));
					} catch (WebApiException e) {
						System.err.println(String.format(
								"Exception open sweb stream fileId %s, %s",
								pictureUrl, e.getMessage()));
					} catch (IOException e) {
						System.err
								.println(String
										.format("Exception writing on destination fileId %s, %s",
												pictureUrl, e.getMessage()));
					}
				}
			}
		} catch (InstantiationException e) {
			exitMsg("Exception connecting ac db " + e.getMessage());
		} catch (IllegalAccessException e) {
			exitMsg("Exception connecting ac db " + e.getMessage());
		} catch (ClassNotFoundException e) {
			exitMsg("Exception connecting ac db " + e.getMessage());
		} catch (SQLException e) {
			exitMsg("Exception connecting ac db " + e.getMessage());
		} catch (UnknownHostException e) {
			exitMsg("Exception connecting profile db " + e.getMessage());
		} catch (MongoException e) {
			exitMsg("Exception connecting profile db " + e.getMessage());
		}
		System.out.println("DONE!");
	}

	private static void exitMsg(String msg) {
		System.err.println(msg);
		System.exit(-1);
	}
}
