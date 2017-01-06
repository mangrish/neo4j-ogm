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

import java.util.Collection;

import org.junit.Test;
import org.neo4j.ogm.domain.bike.Bike;
import org.neo4j.ogm.domain.bike.Wheel;
import org.neo4j.ogm.session.Neo4jSession;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.StubDriver;

/**
 * @author Vince Bickers
 */
public class BikeTest {


	@Test
	public void testDeserialiseBikeModel() throws Exception {

		BikeRequest bikeRequest = new BikeRequest();

		SessionFactory sessionFactory = new SessionFactory("org.neo4j.ogm.domain.bike");
		Neo4jSession session = ((Neo4jSession) sessionFactory.openSession());
		session.setDriver(bikeRequest);

		long now = -System.currentTimeMillis();
		Collection<Bike> bikes = session.loadAll(Bike.class);
		System.out.println("deserialised in " + (now + System.currentTimeMillis()) + " milliseconds");

		assertFalse(bikes.isEmpty());
		Bike bike = bikes.iterator().next();

		assertNotNull(bike);
		assertEquals(15, (long) bike.getId());
		assertEquals(2, bike.getColours().length);

		// check the frame
		assertEquals(18, (long) bike.getFrame().getId());
		assertEquals(27, (int) bike.getFrame().getSize());

		// check the saddle
		assertEquals(19, (long) bike.getSaddle().getId());
		assertEquals(42.99, bike.getSaddle().getPrice(), 0.00);
		assertEquals("plastic", bike.getSaddle().getMaterial());

		// check the wheels
		assertEquals(2, bike.getWheels().size());
		for (Wheel wheel : bike.getWheels()) {
			if (wheel.getId().equals(16L)) {
				assertEquals(3, (int) wheel.getSpokes());
			}
			if (wheel.getId().equals(17L)) {
				assertEquals(5, (int) wheel.getSpokes());
			}
		}
	}

	@Test
	public void testReloadExistingDomain() {

		BikeRequest bikeRequest = new BikeRequest();

		SessionFactory sessionFactory = new SessionFactory("org.neo4j.ogm.domain.bike");
		Neo4jSession session = ((Neo4jSession) sessionFactory.openSession());
		session.setDriver(bikeRequest);

		Collection<Bike> bikes = session.loadAll(Bike.class);
		Collection<Bike> theSameBikes = session.loadAll(Bike.class);

		assertEquals(bikes.size(), theSameBikes.size());
	}

	static public class BikeRequest extends StubDriver {


		// each element in the array is a row in the response
		private static String[] jsonModel = {
				"{\"graph\": { " +
						"\"nodes\" :[ " +
						"{\"id\" : \"15\",\"labels\" : [ \"Bike\"], \"properties\" : { \"colours\" :[\"red\", \"black\"] } }, " +
						"{\"id\" : \"16\",\"labels\" : [ \"Wheel\", \"FrontWheel\" ],\"properties\" : {\"spokes\" : 3 } }, " +
						"{\"id\" : \"17\",\"labels\" : [ \"Wheel\", \"BackWheel\" ],\"properties\" : {\"spokes\" : 5 } }, " +
						"{\"id\" : \"18\",\"labels\" : [ \"Frame\" ],\"properties\" : {\"size\" : 27 } }, " +
						"{\"id\" : \"19\",\"labels\" : [ \"Saddle\" ],\"properties\" : {\"price\" : 42.99, \"material\" : \"plastic\" } } " +
						"], " +
						"\"relationships\": [" +
						"{\"id\":\"141\",\"type\":\"HAS_WHEEL\",\"startNode\":\"15\",\"endNode\":\"16\",\"properties\":{ \"purchased\" : 20130917 }}, " +
						"{\"id\":\"142\",\"type\":\"HAS_WHEEL\",\"startNode\":\"15\",\"endNode\":\"17\",\"properties\":{ \"purchased\" : 20130917 }}," +
						"{\"id\":\"143\",\"type\":\"HAS_FRAME\",\"startNode\":\"15\",\"endNode\":\"18\",\"properties\":{ \"purchased\" : 20130917 }}," +
						"{\"id\":\"144\",\"type\":\"HAS_SADDLE\",\"startNode\":\"15\",\"endNode\":\"19\",\"properties\":{\"purchased\" : 20130922 }} " +
						"] " +
						"} }"
		};

		public String[] getResponse() {
			return jsonModel;
		}
	}
}
