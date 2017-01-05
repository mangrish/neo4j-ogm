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

package org.neo4j.ogm.testutil;

import java.io.File;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.ogm.driver.Driver;
import org.neo4j.ogm.service.Components;
import org.neo4j.test.TestGraphDatabaseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author vince
 */
public class MultiDriverTestClass {

	private static final Logger logger = LoggerFactory.getLogger(MultiDriverTestClass.class);
    private static TestServer testServer;
    private static GraphDatabaseService impermanentDb;
    private static File graphStore;

    @BeforeClass
    public static synchronized void setupMultiDriverTestEnvironment() {

//        Driver driver = Components.driver(); // this will load the driver
//
//        if (driver instanceof HttpDriver ) {
//            if (Components.neo4jVersion() < 2.2) {
//                testServer = new TestServer.Builder()
//                        .enableAuthentication(false)
//                        .enableBolt(false)
//                        .transactionTimeoutSeconds(30)
//                        .build();
//            } else {
//                testServer = new TestServer.Builder()
//                        .enableAuthentication(true)
//                        .enableBolt(false)
//                        .transactionTimeoutSeconds(30)
//                        .build();
//            }
//        }
//        else if (driver instanceof BoltDriver) {
//            testServer = new TestServer.Builder()
//                    .enableBolt(true)
//                    .transactionTimeoutSeconds(30)
//                    .build();
//        }
//        else {
//            graphStore = FileUtils.createTemporaryGraphStore();
//            impermanentDb = new TestGraphDatabaseFactory().newImpermanentDatabase(graphStore);
//			logger.info("Creating new impermanent database {}", impermanentDb);
//            Components.setDriver(new EmbeddedDriver(impermanentDb));
//        }

    }

    @AfterClass
    public static synchronized void tearDownMultiDriverTestEnvironment() {
        close();
    }

    private static void close() {

        if (testServer != null) {
            if (testServer.isRunning(1000)) {
                testServer.shutdown();
            }
            testServer = null;
        }
        if (impermanentDb != null) {
            if (impermanentDb.isAvailable(1000)) {
                impermanentDb.shutdown();
            }
            impermanentDb = null;
			graphStore = null;
        }
    }

    public static synchronized GraphDatabaseService getGraphDatabaseService() {
        if (testServer != null) {
            return testServer.getGraphDatabaseService();
        }
        return impermanentDb;
    }
}
