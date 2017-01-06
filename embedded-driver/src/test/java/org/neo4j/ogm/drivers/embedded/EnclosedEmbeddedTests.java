package org.neo4j.ogm.drivers.embedded;

import java.io.File;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.ogm.drivers.embedded.driver.EmbeddedDriver;
import org.neo4j.ogm.multidrivertest.*;
import org.neo4j.ogm.multidrivertest.identity.IdentityTest;
import org.neo4j.ogm.multidrivertest.propertyrelationships.*;
import org.neo4j.ogm.multidrivertest.relationships.ExtraRelationshipEntityTest;
import org.neo4j.ogm.multidrivertest.relationships.aa.AATest;
import org.neo4j.ogm.multidrivertest.relationships.aaa.AAATest;
import org.neo4j.ogm.multidrivertest.relationships.aabb.AABBTest;
import org.neo4j.ogm.multidrivertest.relationships.aabb2.AABB2Test;
import org.neo4j.ogm.multidrivertest.relationships.ab.ABTest;
import org.neo4j.ogm.multidrivertest.relationships.ab2.AB2Test;
import org.neo4j.ogm.multidrivertest.relationships.abb.ABBTest;
import org.neo4j.ogm.multidrivertest.relationships.abb2.ABB2Test;
import org.neo4j.ogm.service.Components;
import org.neo4j.ogm.testutil.FileUtils;
import org.neo4j.test.TestGraphDatabaseFactory;

/**
 * Created by markangrish on 06/01/2017.
 */
@RunWith(Enclosed.class)
public class EnclosedEmbeddedTests {

	private static GraphDatabaseService impermanentDb;
	private static File graphStore;

	@BeforeClass
	public static void oneTimeSetup() {
		Components.configure("ogm-embedded.properties");
		graphStore = FileUtils.createTemporaryGraphStore();
		impermanentDb = new TestGraphDatabaseFactory().newImpermanentDatabase(graphStore);
		Components.setDriver(new EmbeddedDriver(impermanentDb));
	}

	@AfterClass
	public static synchronized void tearDownMultiDriverTestEnvironment() {
		if (impermanentDb != null) {
			if (impermanentDb.isAvailable(1000)) {
				impermanentDb.shutdown();
			}
			impermanentDb = null;
			graphStore = null;
		}
	}

	public static class HttpAutoIndexManagerTest extends AutoIndexManagerTest {

		@Override
		protected GraphDatabaseService getGraphDatabaseService() {
			return impermanentDb;
		}
	}

	public static class HttpClassHierarchiesIntegrationTest extends ClassHierarchiesIntegrationTest {

		@Override
		protected GraphDatabaseService getGraphDatabaseService() {
			return impermanentDb;
		}
	}

	public static class HttpDegenerateEntityModelTests extends DegenerateEntityModelTests {

		@Override
		protected GraphDatabaseService getGraphDatabaseService() {
			return impermanentDb;
		}
	}

	public static class HttpEntityGraphMapperTest extends EntityGraphMapperTest {

		@Override
		protected GraphDatabaseService getGraphDatabaseService() {
			return impermanentDb;
		}
	}

	public static class HttpExtraRelationshipEntityPartialMappingTest extends ExtraRelationshipEntityPartialMappingTest {

		@Override
		protected GraphDatabaseService getGraphDatabaseService() {
			return impermanentDb;
		}
	}

	public static class HttpPizzaIntegrationTest extends PizzaIntegrationTest {

		@Override
		protected GraphDatabaseService getGraphDatabaseService() {
			return impermanentDb;
		}
	}

	public static class HttpRelationshipEntityMappingTest extends RelationshipEntityMappingTest {

		@Override
		protected GraphDatabaseService getGraphDatabaseService() {
			return impermanentDb;
		}
	}

	public static class HttpRelationshipMappingTest extends RelationshipMappingTest {

		@Override
		protected GraphDatabaseService getGraphDatabaseService() {
			return impermanentDb;
		}
	}

	public static class HttpRestaurantIntegrationTest extends RestaurantIntegrationTest {

		@Override
		protected GraphDatabaseService getGraphDatabaseService() {
			return impermanentDb;
		}
	}

	public static class HttpSocialRelationshipsIntegrationTest extends SocialRelationshipsIntegrationTest {

		@Override
		protected GraphDatabaseService getGraphDatabaseService() {
			return impermanentDb;
		}
	}
}
