package org.neo4j.ogm.drivers.embedded;

import java.io.File;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.ogm.drivers.embedded.driver.EmbeddedDriver;
import org.neo4j.ogm.multidrivertest.*;
import org.neo4j.ogm.multidrivertest.identity.IdentityTest;
import org.neo4j.ogm.multidrivertest.propertyrelationships.AbstractWithGenericPropertyRelationshipTest;
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
import org.neo4j.ogm.testutil.TestServer;
import org.neo4j.test.TestGraphDatabaseFactory;

/**
 * Created by markangrish on 06/01/2017.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({AAATest.class,
		AABBTest.class,
		AABB2Test.class,
		AATest.class,
		ABBTest.class,
		ABB2Test.class,
		ABTest.class,
		AB2Test.class,
		AbstractWithGenericPropertyRelationshipTest.class,
		AssociatedObjectsTest.class,
		BidirectionalMappingTest.class,
		BikeTest.class,
		BlogTest.class,
		CanonicalTest.class,
		CineastsIntegrationTest.class,
		CineastsRelationshipEntityTest.class,
		ClosedTransactionTest.class,
		CollectionsTest.class,
		CompaniesIntegrationTest.class,
		ConvertibleIntegrationTest.class,
		DeleteCapabilityTest.class,
		DirtyObjectsTest.class,
		DualRelationshipTest.class,
		DualTargetEntityRelationshipTest.class,
		DualTargetRelationshipsTest.class,
		EducationIntegrationTest.class,
		EducationTest.class,
		ElectionTest.class,
		EndToEndTest.class,
		EnumsNotScannedTest.class,
		EnumsScannedTest.class,
		ExtraRelationshipEntityTest.class,
		FriendshipsRelationshipEntityTest.class,
		FriendsInLongTransactionTest.class,
		GraphIdCapabilityTest.class,
		HeirarchyRelsTest.class,
		IdentityTest.class,
		IngredientsIntegrationTest.class,
		LifecycleTest.class,
		LoadCapabilityTest.class,
		LookupByPrimaryIndexTests.class,
		MovieTest.class,
		MultipleRelationshipsTest.class,
		MusicIntegrationTest.class,
		NodeEntityTest.class,
		NumericConversionTest.class,
		ParameterizedConversionTest.class,
		PetIntegrationTest.class,
		QueryCapabilityTest.class,
		RelationshipEntityPartialMappingTest.class,
		RelationshipEntityTest.class,
		RichRelationTest.class,
		SatelliteIntegrationTest.class,
		SaveCapabilityTest.class,
		SessionAndMappingContextTest.class,
		SimpleNetworkIntegrationTest.class,
		SocialIntegrationTest.class,
		SpyIntegrationTest.class,
		StaleObjectTest.class,
		TransactionManagerTest.class,
		TransactionSerialisationTest.class,
		TransactionTest.class,
		TreeIntegrationTest.class,
		UserTest.class
})
public class EmbeddedTestSuite {

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
}
