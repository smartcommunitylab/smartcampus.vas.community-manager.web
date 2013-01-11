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
import it.unitn.disi.sweb.webapi.model.smartcampus.ac.Operation;
import it.unitn.disi.sweb.webapi.model.smartcampus.livetopics.LiveTopicSource;

import java.util.Locale;

public class TestAssignment {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		SCWebApiClient client = SCWebApiClient.getInstance(Locale.ENGLISH,
				"sweb.smartcampuslab.it", 8080);
		if (!client.ping()) {
			throw new RuntimeException("Ping failed");
		}
		LiveTopicSource assignments = client.readAssignments(12345L,
				Operation.READ, 1L);

		if (!assignments.getUserIds().contains(2L)) {
			assignments.getUserIds().add(2L);
			client.updateAssignments(12345L, Operation.READ, 1L, assignments);
		}
	}

}
