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

import java.util.UUID;

import org.junit.Test;
import org.neo4j.ogm.domain.cineasts.annotated.Movie;
import org.neo4j.ogm.domain.cineasts.annotated.Rating;
import org.neo4j.ogm.session.Neo4jSession;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.StubDriver;

/**
 * @author Michal Bachman
 * @author Mark Angrish
 */
public class MovieTest {

	@Test
	public void testDeserialiseMovie() {

		MoviesRequest movieRequest = new MoviesRequest();

		SessionFactory sessionFactory = new SessionFactory("org.neo4j.ogm.domain.cineasts.annotated");
		Neo4jSession session = ((Neo4jSession) sessionFactory.openSession());
		session.setDriver(movieRequest);

		Movie movie = session.load(Movie.class, UUID.fromString("38ebe777-bc85-4810-8217-096f29a361f1"), 1);

		assertEquals("Pulp Fiction", movie.getTitle());
		assertNotNull(movie.getRatings());
		assertEquals(1, movie.getRatings().size());

		Rating rating = movie.getRatings().iterator().next();

		assertEquals("Michal", rating.getUser().getName());
		assertEquals("Pulp Fiction", rating.getMovie().getTitle());
	}

	static class MoviesRequest extends StubDriver {

		private static String[] jsonModel = {
				"{\"graph\": { " +
						"\"nodes\" :[ " +
						"{\"id\" : \"15\",\"labels\" : [ \"Movie\"],    \"properties\" : {\"uuid\" : \"38ebe777-bc85-4810-8217-096f29a361f1\", \"title\" : \"Pulp Fiction\"}}, " +
						"{\"id\" : \"16\",\"labels\" : [ \"Movie\"],    \"properties\" : {\"uuid\" : \"38ebe777-bc85-4810-8217-096f29a361f2\", \"title\" : \"Top Gun\"}}, " +
						"{\"id\" : \"17\",\"labels\" : [ \"Movie\"],    \"properties\" : {\"uuid\" : \"38ebe777-bc85-4810-8217-096f29a361f3\", \"title\" : \"Django Unchained\"}}, " +
						"{\"id\" : \"18\",\"labels\" : [ \"User\"],     \"properties\" : {\"uuid\" : \"38ebe777-bc85-4810-8217-096f29a361f4\", \"name\" : \"Michal\"}}, " +
						"{\"id\" : \"19\",\"labels\" : [ \"User\"],     \"properties\" : {\"uuid\" : \"38ebe777-bc85-4810-8217-096f29a361f5\", \"name\" : \"Vince\"}}, " +
						"{\"id\" : \"20\",\"labels\" : [ \"User\"],     \"properties\" : {\"uuid\" : \"38ebe777-bc85-4810-8217-096f29a361f6\", \"name\" : \"Daniela\"}} " +
						"], " +
						"\"relationships\": [" +
						"{\"id\":\"141\",\"type\":\"RATED\",\"startNode\":\"18\",\"endNode\":\"15\",\"properties\":{ \"stars\" : 5, \"comment\" : \"Best Film Ever!\" }}, " +
						"{\"id\":\"142\",\"type\":\"RATED\",\"startNode\":\"18\",\"endNode\":\"16\",\"properties\":{ \"stars\" : 3, \"comment\" : \"Overrated\" }}, " +
						"{\"id\":\"143\",\"type\":\"RATED\",\"startNode\":\"19\",\"endNode\":\"16\",\"properties\":{ \"stars\" : 4 }} " +
						"] " +
						"} }"
		};

		public String[] getResponse() {
			return jsonModel;
		}
	}
}
