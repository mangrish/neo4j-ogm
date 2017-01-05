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

package org.neo4j.ogm.persistence.session.capability;

import org.junit.Before;
import org.junit.Test;
import org.neo4j.ogm.domain.music.Album;
import org.neo4j.ogm.domain.music.Recording;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;

/**
 * @author vince
 */
public abstract class DeleteCapabilityTest {

	private Session session = new SessionFactory("org.neo4j.ogm.domain.music").openSession();

	@Before
	public void init() {
		session.clear();
	}

	@Test
	public void shouldNotFailIfDeleteNodeEntityAgainstEmptyDatabase() {
		session.deleteAll(Album.class);
	}

	@Test
	public void shouldNotFailIfDeleteRelationshipEntityAgainstEmptyDatabase() {
		session.deleteAll(Recording.class);
	}
}
