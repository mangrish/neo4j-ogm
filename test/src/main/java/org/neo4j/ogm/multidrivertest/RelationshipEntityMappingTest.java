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

import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.ogm.domain.canonical.hierarchies.A;
import org.neo4j.ogm.domain.canonical.hierarchies.B;
import org.neo4j.ogm.domain.canonical.hierarchies.CR;
import org.neo4j.ogm.domain.cineasts.annotated.Actor;
import org.neo4j.ogm.domain.cineasts.annotated.Movie;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.GraphTestUtils;

import java.io.IOException;


/**
 * @author Vince Bickers
 * @author Mark Angrish
 */
public abstract class RelationshipEntityMappingTest  {

	private static final SessionFactory sessionFactory = new SessionFactory("org.neo4j.ogm.domain.cineasts.annotated", "org.neo4j.ogm.domain.canonical.hierarchies");

	private Session session;

	@Before
	public void init() throws IOException {
		session = sessionFactory.openSession();
		session.purgeDatabase();
	}

	private GraphDatabaseService getDatabase() {
		return getGraphDatabaseService();
	}

	protected abstract GraphDatabaseService getGraphDatabaseService();

	@Test
	public void testThatAnnotatedRelationshipOnRelationshipEntityCreatesTheCorrectRelationshipTypeInTheGraph() {
		Movie hp = new Movie("Goblet of Fire", 2005);

		Actor daniel = new Actor("Daniel Radcliffe");
		daniel.playedIn(hp, "Harry Potter");
		session.save(daniel);
		GraphTestUtils.assertSameGraph(getDatabase(), "MERGE (m:Movie {uuid:\"" + hp.getUuid().toString() + "\"}) SET m.title = 'Goblet of Fire', m.year = 2005 MERGE (a:Actor {uuid:\"" + daniel.getUuid().toString() + "\"}) SET a.name='Daniel Radcliffe' create (a)-[:ACTS_IN {role:'Harry Potter'}]->(m)");
	}

	@Test
	public void testThatRelationshipEntityNameIsUsedAsRelationshipTypeWhenTypeIsNotDefined() {
		Movie hp = new Movie("Goblet of Fire", 2005);

		Actor daniel = new Actor("Daniel Radcliffe");
		daniel.nominatedFor(hp, "Saturn Award", 2005);
		session.save(daniel);
		GraphTestUtils.assertSameGraph(getDatabase(), "MERGE (m:Movie {uuid:\"" + hp.getUuid().toString() + "\"}) SET m.title = 'Goblet of Fire', m.year = 2005 MERGE (a:Actor {uuid:\"" + daniel.getUuid().toString() + "\"}) SET a.name='Daniel Radcliffe' create (a)-[:NOMINATIONS {name:'Saturn Award', year:2005}]->(m)");
	}

	@Test
	public void shouldUseCorrectTypeFromHierarchyOfRelationshipEntities() {

		A a = new A();
		B b = new B();

		CR r = new CR();
		r.setA(a);
		r.setB(b);

		a.setR(r);

		session.save(a);
		GraphTestUtils.assertSameGraph(getDatabase(),
				"CREATE (a:A) " +
						"CREATE (b:B) " +
						"CREATE (a)-[:CR]->(b)");
	}
}
