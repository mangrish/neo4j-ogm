package org.neo4j.ogm.drivers.http;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.ogm.multidrivertest.*;
import org.neo4j.ogm.service.Components;
import org.neo4j.ogm.testutil.TestServer;

/**
 * Created by markangrish on 06/01/2017.
 */
@RunWith(Enclosed.class)
public class EnclosedHttpTests {

	private static TestServer testServer;

	@BeforeClass
	public static void oneTimeSetup() {
		Components.configure("ogm-http.properties");
		if (Components.neo4jVersion() < 2.2) {
			testServer = new TestServer.Builder()
					.enableAuthentication(false)
					.enableBolt(false)
					.transactionTimeoutSeconds(30)
					.build();
		} else {
			testServer = new TestServer.Builder()
					.enableAuthentication(true)
					.enableBolt(false)
					.transactionTimeoutSeconds(30)
					.build();
		}
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

	public static class EmbeddedAutoIndexManagerTest extends AutoIndexManagerTest {

		@Override
		protected GraphDatabaseService getGraphDatabaseService() {
			return testServer.getGraphDatabaseService();
		}
	}

	public static class EmbeddedClassHierarchiesIntegrationTest extends ClassHierarchiesIntegrationTest {

		@Override
		protected GraphDatabaseService getGraphDatabaseService() {
			return testServer.getGraphDatabaseService();
		}
	}

	public static class EmbeddedDegenerateEntityModelTests extends DegenerateEntityModelTests {

		@Override
		protected GraphDatabaseService getGraphDatabaseService() {
			return testServer.getGraphDatabaseService();
		}
	}

	public static class EmbeddedEntityGraphMapperTest extends EntityGraphMapperTest {

		@Override
		protected GraphDatabaseService getGraphDatabaseService() {
			return testServer.getGraphDatabaseService();
		}
	}

	public static class EmbeddedExtraRelationshipEntityPartialMappingTest extends ExtraRelationshipEntityPartialMappingTest {

		@Override
		protected GraphDatabaseService getGraphDatabaseService() {
			return testServer.getGraphDatabaseService();
		}
	}

	public static class EmbeddedPizzaIntegrationTest extends PizzaIntegrationTest {

		@Override
		protected GraphDatabaseService getGraphDatabaseService() {
			return testServer.getGraphDatabaseService();
		}
	}

	public static class EmbeddedRelationshipEntityMappingTest extends RelationshipEntityMappingTest {

		@Override
		protected GraphDatabaseService getGraphDatabaseService() {
			return testServer.getGraphDatabaseService();
		}
	}

	public static class EmbeddedRelationshipMappingTest extends RelationshipMappingTest {

		@Override
		protected GraphDatabaseService getGraphDatabaseService() {
			return testServer.getGraphDatabaseService();
		}
	}


	public static class EmbeddedRestaurantIntegrationTest extends RestaurantIntegrationTest {

		@Override
		protected GraphDatabaseService getGraphDatabaseService() {
			return testServer.getGraphDatabaseService();
		}
	}

	public static class EmbeddedSocialRelationshipsIntegrationTest extends SocialRelationshipsIntegrationTest {

		@Override
		protected GraphDatabaseService getGraphDatabaseService() {
			return testServer.getGraphDatabaseService();
		}
	}
}
