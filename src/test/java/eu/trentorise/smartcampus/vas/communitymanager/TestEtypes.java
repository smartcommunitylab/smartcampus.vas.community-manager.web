/*
 * Copyright (c) 2010-2013, TrentoRise
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * 
 * Unless required by applicable law or agreed to in writing, software
 *
 * distributed under the License is distributed on an "AS IS" BASIS,
 *
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 *
 * limitations under the License.
 */
package eu.trentorise.smartcampus.vas.communitymanager;

import it.unitn.disi.sweb.webapi.client.WebApiException;
import it.unitn.disi.sweb.webapi.client.smartcampus.SCWebApiClient;
import it.unitn.disi.sweb.webapi.model.entity.AttributeDef;
import it.unitn.disi.sweb.webapi.model.entity.DataType;
import it.unitn.disi.sweb.webapi.model.entity.EntityType;

import java.util.Locale;

import org.json.JSONException;

/**
 * @author Viktor Pravdin <pravdin@disi.unitn.it>
 */
public class TestEtypes {

	public static void main(String[] args) throws WebApiException,
			JSONException {
		// SCWebApiClient client = SCWebApiClient.getInstance(Locale.ENGLISH,
		// "213.21.154.85", 9090);
		SCWebApiClient client = SCWebApiClient.getInstance(Locale.ENGLISH,
				"sweb.smartcampuslab.it", 8080);
		if (!client.ping()) {
			throw new RuntimeException("Ping failed");
		}
		// Concept for "test"
		Long ctest = 4080L;
		// Concept for "height"
		Long cheight = 28302L;
		// Concept for "label"
		Long clabel = 39096L;
		EntityType et = client.readEntityType("test", "uk");
		if (et != null) {
			client.deleteEntityType(et.getId());
		}
		et = new EntityType(null, null, null, ctest);
		AttributeDef adTag = new AttributeDef(null, null, null, null, clabel,
				DataType.STRING, false, null, null);
		AttributeDef adHeight = new AttributeDef(null, null, null, null,
				cheight, DataType.FLOAT, false, null, null);
		et.getAttrDefs().add(adTag);
		et.getAttrDefs().add(adHeight);
		long id = client.create(et);
		et = client.readEntityType(id);
		System.out.println("Etype: " + et.toJsonObject(true, true));
		// Now let's change the height data type to integer, remove the label
		// and add the "test" attribute
		et.getAttrDefs().clear();
		adHeight = new AttributeDef(null, null, null, null, cheight,
				DataType.INTEGER, false, null, null);
		AttributeDef adTest = new AttributeDef(null, null, null, null, ctest,
				DataType.RELATION, false, null, null);
		et.getAttrDefs().add(adHeight);
		et.getAttrDefs().add(adTest);
		et.setGlobalId(123456L);
		client.update(et);
		et = client.readEntityType(id);
		System.out.println("Updated etype: " + et.toJsonObject(true, true));
		client.deleteEntityType(id);
		if ((et = client.readEntityType(id)) != null) {
			System.out.println("Etype after delete: "
					+ et.toJsonObject(true, true));
			throw new RuntimeException("The etype wasn't deleted");
		}
	}
}
