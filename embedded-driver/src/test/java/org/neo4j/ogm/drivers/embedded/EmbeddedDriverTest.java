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

package org.neo4j.ogm.drivers.embedded;

import static org.junit.Assert.assertEquals;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.kernel.impl.factory.GraphDatabaseFacade;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.config.DriverConfiguration;
import org.neo4j.ogm.drivers.AbstractDriverTestSuite;
import org.neo4j.ogm.drivers.embedded.driver.EmbeddedDriver;
import org.neo4j.ogm.exception.ConnectionException;
import org.neo4j.ogm.service.Components;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;

/**
 * @author vince
 */
public class EmbeddedDriverTest extends AbstractDriverTestSuite {

    private GraphDatabaseService graphDatabaseService;

    @BeforeClass
    public static void configure() throws Exception {
		Components.configure("embedded.driver.properties");
		AbstractDriverTestSuite.deleteExistingEmbeddedDatabase();
		System.out.println("Embedded: " + Components.neo4jVersion());
    }


	@AfterClass
    public static void reset() {
        Components.destroy();
    }

    @Override
    public void setUpTest() {
		graphDatabaseService = ((EmbeddedDriver) Components.driver()).getGraphDatabaseService();
		Assert.assertTrue(graphDatabaseService instanceof GraphDatabaseFacade);
    }

    @Override
    public void tearDownTest() {
    }

	@Test(expected = ConnectionException.class)
	public void shouldThrowExceptionWhenEmbeddedDriverCannotConnect() {
		Components.configure("src/test/resources/ogm-embedded-invalid.properties");
		SessionFactory sessionFactory = new SessionFactory("org.neo4j.ogm.domain.social");
		Session session = sessionFactory.openSession();
		session.purgeDatabase();
	}

	@Test
	public void shouldLoadEmbeddedDriverConfigFromPropertiesFile() {
		DriverConfiguration driverConfig = new DriverConfiguration(new Configuration("src/test/resources/embedded.driver.properties"));
		assertEquals("file:///var/tmp/neo4j.db", driverConfig.getURI());
	}


	@Test
	public void shouldLoadEmbeddedHaPropertiesFromRawConfiguration() {
		Configuration config = new Configuration("neo4j-ha.properties");
		assertEquals("1", config.get("ha.server_id"));
		assertEquals("localhost:5001", config.get("ha.initial_hosts"));
		assertEquals("true", config.get("ha.allow_init_cluster"));
	}

	@Test
	public void shouldGetNeo4jHaPropertiesFileFromDriverConfiguration() {
		DriverConfiguration config = new DriverConfiguration(new Configuration("src/test/resources/embedded.ha.driver.properties"));
		assertEquals("neo4j-ha.properties", config.getNeo4jHaPropertiesFile());
	}
}
