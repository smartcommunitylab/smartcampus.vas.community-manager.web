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
import it.unitn.disi.sweb.webapi.model.entity.Attribute;
import it.unitn.disi.sweb.webapi.model.entity.DataType;
import it.unitn.disi.sweb.webapi.model.entity.Entity;
import it.unitn.disi.sweb.webapi.model.entity.EntityBase;
import it.unitn.disi.sweb.webapi.model.entity.EntityType;
import it.unitn.disi.sweb.webapi.model.entity.Value;
import it.unitn.disi.sweb.webapi.model.ss.Concept;
import it.unitn.disi.sweb.webapi.model.ss.SemanticString;
import it.unitn.disi.sweb.webapi.model.ss.Token;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.json.JSONException;

/**
 * 
 * @author Viktor Pravdin <pravdin@disi.unitn.it>
 * @date Jul 11, 2012 4:25:42 PM
 */
public class TestEntity {

	public static void main(String[] args) throws WebApiException,
			JSONException {
		SCWebApiClient client = SCWebApiClient.getInstance(Locale.ENGLISH,
				"sweb.smartcampuslab.it", 8080);
		if (!client.ping()) {
			throw new RuntimeException("Ping failed");
		}
		System.out.println("Creating an entity base...");
		EntityBase eb1 = new EntityBase();
		eb1.setLabel("TEST_SC_EB1_" + System.currentTimeMillis());
		Long eb1Id = client.create(eb1);
		// Re-read to get the ID of the default KB
		eb1 = client.readEntityBase(eb1Id);
		EntityType et = client.readEntityType("social", eb1.getKbLabel());

		Entity related = new Entity();
		related.setEntityBase(eb1);
		related.setEtype(et);
		long relatedId = client.create(related);
		Entity social = new Entity();
		social.setEntityBase(eb1);
		social.setEtype(et);
		List<Attribute> attrs = new ArrayList<Attribute>();
		List<Value> values = new ArrayList<Value>();
		Value v = new Value();
		// String tag attribute
		v.setType(DataType.STRING);
		v.setStringValue("This is a text tag");
		values.add(v);
		Attribute a = new Attribute();
		a.setAttributeDefinition(et.getAttributeDefByName("text"));
		a.setValues(values);
		attrs.add(a);
		// Entity tag attribute
		values = new ArrayList<Value>();
		v = new Value();
		v.setType(DataType.RELATION);
		v.setRelationEntity(client.readEntity(relatedId, null));
		values.add(v);
		a = new Attribute();
		a.setAttributeDefinition(et.getAttributeDefByName("entity"));
		a.setValues(values);
		attrs.add(a);
		// Semantic tag attribute
		values = new ArrayList<Value>();
		v = new Value();
		v.setType(DataType.SEMANTIC_STRING);
		// The semantic string itself
		SemanticString ss = new SemanticString();
		ss.setString("java");
		List<Token> tokens = new ArrayList<Token>();
		Concept c = client.readConceptByGlobalId(36982L, eb1.getKbLabel());
		List<Concept> concepts = new ArrayList<Concept>();
		concepts.add(c);
		Token t = new Token("java", c.getLabel(), c.getId(), concepts);
		tokens.add(t);
		ss.setTokens(tokens);
		v.setSemanticStringValue(ss);
		values.add(v);
		a = new Attribute();
		a.setAttributeDefinition(et.getAttributeDefByName("semantic"));
		a.setValues(values);
		attrs.add(a);

		social.setAttributes(attrs);
		System.out.println("Created entity ID:" + client.create(social));
		// client.deleteEntityBase(eb1Id);
	}
}
