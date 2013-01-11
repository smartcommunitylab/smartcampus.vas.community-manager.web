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
package eu.trentorise.smartcampus.vas.communitymanager;

import it.unitn.disi.sweb.webapi.client.smartcampus.SCWebApiClient;
import it.unitn.disi.sweb.webapi.model.entity.EntityBase;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;

/**
 * Created with IntelliJ IDEA. User: Ilya Date: 16/08/12 Time: 17.10
 */
public class TestFileCRUD {

	public static void main(String[] args) throws Exception {
		SCWebApiClient client = SCWebApiClient.getInstance(Locale.ENGLISH,
				"sweb.smartcampuslab.it", 8080);

		// user 1 and its EB
		System.out.println("Creating an entity base 1...");
		EntityBase eb1 = new EntityBase();
		eb1.setLabel("TEST_SC_EB_1_" + System.currentTimeMillis());
		Long ebId1 = client.create(eb1);
		// Re-read to get the ID of the default KB
		eb1 = client.readEntityBase(ebId1);
		System.out.println("Created an entity base 1 " + eb1.getLabel()
				+ " with ID " + ebId1);

		// CRUD on file
		// create a file
		java.io.File file = new java.io.File("test_file.txt");
		FileWriter fw = new FileWriter(file);
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write("hello world text file example");
		bw.close();
		Long id = client.uploadFile(file, eb1.getLabel());
		System.out.println("Created File ID: " + id);
		System.out.println("Created File Name: " + file.getName());

		// read the file
		InputStream is = client.openStream(id);
		InputStreamReader isr = new InputStreamReader(is);
		StringBuilder sb = new StringBuilder();
		BufferedReader br = new BufferedReader(isr);
		String read = br.readLine();

		while (read != null) {
			sb.append(read);
			read = br.readLine();
		}
		is.close();
		System.out.println("Contents of the file: " + sb.toString());

		// delete the file
		// client.deleteFile(id);
		// is = client.openStream(id);
		// System.out.printf("input stream should be null: " + is);
	}
}
