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

import it.unitn.disi.sweb.webapi.client.WebApiException;
import it.unitn.disi.sweb.webapi.client.smartcampus.SCWebApiClient;
import it.unitn.disi.sweb.webapi.model.entity.AttributeDef;
import it.unitn.disi.sweb.webapi.model.entity.EntityType;

import java.util.List;
import java.util.Locale;

import org.json.JSONException;

/**
 * 
 * @author Viktor Pravdin <pravdin@disi.unitn.it>
 * @date Jul 11, 2012 4:25:42 PM
 */
public class DumpEtypes {

	public static void main(String[] args) throws WebApiException,
			JSONException {
		SCWebApiClient client = SCWebApiClient.getInstance(Locale.ENGLISH,
				"sweb.smartcampuslab.it", 8080);
		if (!client.ping()) {
			throw new RuntimeException("Ping failed");
		}
		List<EntityType> etypes = client.readEntityTypes("uk");
		for (EntityType et : etypes) {
			System.out.println("\nEntity type " + et.getName() + " ("
					+ et.getGlobalId() + ")");
			for (AttributeDef ad : et.getAttrDefs()) {
				System.out.println("\t" + ad.getName() + "\t\t" + ad.getType());
			}
		}
	}
}
