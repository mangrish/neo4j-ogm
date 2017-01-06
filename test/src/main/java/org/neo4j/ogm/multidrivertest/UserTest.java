/*
 * Copyright (c) 2002-2016 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 *  conditions of the subcomponent's license, as noted in the LICENSE file.
 */

package org.neo4j.ogm.multidrivertest;

import static org.junit.Assert.*;

import org.junit.Test;
import org.neo4j.ogm.domain.cineasts.annotated.User;
import org.neo4j.ogm.session.Neo4jSession;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.StubDriver;


/**
 * @author Luanne Misquitta
 * @author Mark Angrish
 */
public abstract class UserTest {

	@Test
	public void testDeserialiseUserWithArrayOfEnums() {

		UsersRequest userRequest = new UsersRequest();

		SessionFactory sessionFactory = new SessionFactory("org.neo4j.ogm.domain.cineasts.annotated");
		Neo4jSession session = ((Neo4jSession) sessionFactory.openSession());
		session.setDriver(userRequest);

		User user = session.load(User.class, "luanne", 1);

		assertEquals("luanne", user.getLogin());
		assertNotNull(user.getSecurityRoles());
		assertEquals(2, user.getSecurityRoles().length);
	}

	static class UsersRequest extends StubDriver {

		private static String[] jsonModel = {
				"{\"graph\": { " +
						"\"nodes\" :[ " +
						"{\"id\" : \"15\",\"labels\" : [ \"User\"],    \"properties\" : {\"uuid\" : \"38ebe777-bc85-4810-8217-096f29a361f1\", \"login\" : \"luanne\", \"securityRoles\" : [\"USER\",\"ADMIN\"]}}" +
						"]} }"
		};

		public String[] getResponse() {
			return jsonModel;
		}
	}
}
