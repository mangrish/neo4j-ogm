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

package org.neo4j.ogm.http;

import static org.junit.Assert.assertEquals;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.config.DriverConfiguration;
import org.neo4j.ogm.drivers.AbstractDriverTestSuite;
import org.neo4j.ogm.exception.ConnectionException;
import org.neo4j.ogm.service.Components;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.TestServer;

/**
 * @author vince
 */
public class HttpDriverTest extends AbstractDriverTestSuite {

    private static TestServer testServer;

    @BeforeClass
    public static void configure() {
        Components.configure("ogm-http.properties");
        System.out.println("Http: " + Components.neo4jVersion());
        testServer = new TestServer.Builder().build();
    }

    @AfterClass
    public static void reset() {
        testServer.shutdown();
        Components.destroy();
    }

    @Before
    public void setUpTest() {
    }

    @Override
    public void tearDownTest() {
    }

    @Test(expected = ConnectionException.class)
    public void shouldThrowExceptionWhenHttpDriverCannotConnect() {
        Components.configure("src/test/resources/ogm-http-invalid.properties");
        SessionFactory sessionFactory = new SessionFactory("org.neo4j.ogm.domain.social");
        Session session = sessionFactory.openSession();
        session.purgeDatabase();
    }

    @Test
    public void shouldLoadHttpDriverConfigFromPropertiesFile() {
        DriverConfiguration driverConfig = new DriverConfiguration(new Configuration("src/test/resources/http.driver.properties"));
        assertEquals("http://neo4j:password@localhost:7474", driverConfig.getURI());
    }
}
