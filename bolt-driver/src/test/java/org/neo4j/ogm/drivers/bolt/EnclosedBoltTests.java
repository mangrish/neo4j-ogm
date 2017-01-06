package org.neo4j.ogm.drivers.bolt;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.ogm.multidrivertest.*;
import org.neo4j.ogm.multidrivertest.DegenerateEntityModelTests;
import org.neo4j.ogm.multidrivertest.EntityGraphMapperTest;
import org.neo4j.ogm.multidrivertest.RelationshipEntityMappingTest;
import org.neo4j.ogm.multidrivertest.RelationshipMappingTest;
import org.neo4j.ogm.service.Components;
import org.neo4j.ogm.testutil.TestServer;

/**
 * Created by markangrish on 05/01/2017.
 */
@RunWith(Enclosed.class)
public class EnclosedBoltTests {

	private static TestServer testServer;

	@BeforeClass
	public static void oneTimeSetup() {
		Components.configure("ogm-bolt.properties");
		testServer = new TestServer.Builder()
				.enableBolt(true)
				.transactionTimeoutSeconds(10)
				.build();
	}

	@AfterClass
	public static synchronized void tearDownMultiDriverTestEnvironment() {
		if (testServer != null) {
			if (testServer.isRunning(1000)) {
				testServer.shutdown();
			}
			testServer = null;
		}
	}

	public static class BoltAutoIndexManagerTest extends AutoIndexManagerTest {

		@Override
		protected GraphDatabaseService getGraphDatabaseService() {
			return testServer.getGraphDatabaseService();
		}
	}

	public static class BoltClassHierarchiesIntegrationTest extends ClassHierarchiesIntegrationTest {

		@Override
		protected GraphDatabaseService getGraphDatabaseService() {
			return testServer.getGraphDatabaseService();
		}
	}

	public static class BoltDegenerateEntityModelTests extends DegenerateEntityModelTests {

		@Override
		protected GraphDatabaseService getGraphDatabaseService() {
			return testServer.getGraphDatabaseService();
		}
	}

	public static class BoltEntityGraphMapperTest extends EntityGraphMapperTest {

		@Override
		protected GraphDatabaseService getGraphDatabaseService() {
			return testServer.getGraphDatabaseService();
		}
	}

	public static class BoltExtraRelationshipEntityPartialMappingTest extends ExtraRelationshipEntityPartialMappingTest {

		@Override
		protected GraphDatabaseService getGraphDatabaseService() {
			return testServer.getGraphDatabaseService();
		}
	}

	public static class BoltPizzaIntegrationTest extends PizzaIntegrationTest {

		@Override
		protected GraphDatabaseService getGraphDatabaseService() {
			return testServer.getGraphDatabaseService();
		}
	}

	public static class BoltRelationshipEntityMappingTest extends RelationshipEntityMappingTest {

		@Override
		protected GraphDatabaseService getGraphDatabaseService() {
			return testServer.getGraphDatabaseService();
		}
	}

	public static class BoltRelationshipMappingTest extends RelationshipMappingTest {

		@Override
		protected GraphDatabaseService getGraphDatabaseService() {
			return testServer.getGraphDatabaseService();
		}
	}

	public static class BoltRestaurantIntegrationTest extends RestaurantIntegrationTest {

		@Override
		protected GraphDatabaseService getGraphDatabaseService() {
			return testServer.getGraphDatabaseService();
		}
	}

	public static class BoltSocialRelationshipsIntegrationTest extends SocialRelationshipsIntegrationTest {

		@Override
		protected GraphDatabaseService getGraphDatabaseService() {
			return testServer.getGraphDatabaseService();
		}
	}
}
