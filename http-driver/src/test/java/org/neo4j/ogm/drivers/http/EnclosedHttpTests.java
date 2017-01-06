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

	public static class EmbeddedAAATest extends AAATest {

	}

	public static class EmbeddedAABBTest extends AABBTest {

	}

	public static class EmbeddedAATest extends AATest {

	}

	public static class EmbeddedABBTest extends ABBTest {

	}

	public static class EmbeddedAbstractWithGenericPropertyRelationshipTest extends AbstractWithGenericPropertyRelationshipTest {

	}

	public static class EmbeddedABTest extends ABTest {

	}

	public static class EmbeddedAssociatedObjectsTest extends AssociatedObjectsTest {

	}

	public static class EmbeddedBidirectionalMappingTest extends BidirectionalMappingTest {

	}

	public static class EmbeddedBikeTest extends BikeTest {

	}

	public static class EmbeddedBlogTest extends BlogTest {

	}

	public static class EmbeddedCanonicalTest extends CanonicalTest {

	}

	public static class EmbeddedCineastsIntegrationTest extends CineastsIntegrationTest {

	}

	public static class EmbeddedCineastsRelationshipEntityTest extends CineastsRelationshipEntityTest {

	}

	public static class EmbeddedClassHierarchiesIntegrationTest extends ClassHierarchiesIntegrationTest {

		@Override
		protected GraphDatabaseService getGraphDatabaseService() {
			return testServer.getGraphDatabaseService();
		}
	}

	public static class EmbeddedClosedTransactionTest extends ClosedTransactionTest {

	}

	public static class EmbeddedCollectionsTest extends CollectionsTest {

	}

	public static class EmbeddedCompaniesIntegrationTest extends CompaniesIntegrationTest {

	}

	public static class EmbeddedConvertibleIntegrationTest extends ConvertibleIntegrationTest {

	}

	public static class EmbeddedDegenerateEntityModelTests extends DegenerateEntityModelTests {

		@Override
		protected GraphDatabaseService getGraphDatabaseService() {
			return testServer.getGraphDatabaseService();
		}
	}

	public static class EmbeddedDeleteCapabilityTest extends DeleteCapabilityTest {

	}

	public static class EmbeddedDirtyObjectsTest extends DirtyObjectsTest {

	}

	public static class EmbeddedDualRelationshipTest extends DualRelationshipTest {

	}

	public static class EmbeddedDualTargetEntityRelationshipTest extends DualTargetEntityRelationshipTest {

	}

	public static class EmbeddedDualTargetRelationshipsTest extends DualTargetRelationshipsTest {

	}

	public static class EmbeddedEducationIntegrationTest extends EducationIntegrationTest {

	}

	public static class EmbeddedEducationTest extends EducationTest {

	}

	public static class EmbeddedElectionTest extends ElectionTest {

	}

	public static class EmbeddedEndToEndTest extends EndToEndTest {

	}

	public static class EmbeddedEntityGraphMapperTest extends EntityGraphMapperTest {

		@Override
		protected GraphDatabaseService getGraphDatabaseService() {
			return testServer.getGraphDatabaseService();
		}
	}

	public static class EmbeddedEnumsNotScannedTest extends EnumsNotScannedTest {

	}

	public static class EmbeddedEnumsScannedTest extends EnumsScannedTest {

	}

	public static class EmbeddedEventTestBaseClass extends EventTestBaseClass {

	}

	public static class EmbeddedExtraAABBTest extends ExtraAABBTest {

	}

	public static class EmbeddedExtraABBTest extends ExtraABBTest {

	}

	public static class EmbeddedExtraABTest extends ExtraABTest {

	}

	public static class EmbeddedExtraRelationshipEntityPartialMappingTest extends ExtraRelationshipEntityPartialMappingTest {

		@Override
		protected GraphDatabaseService getGraphDatabaseService() {
			return testServer.getGraphDatabaseService();
		}
	}

	public static class EmbeddedExtraRelationshipEntityTest extends ExtraRelationshipEntityTest {

	}

	public static class EmbeddedFriendshipsRelationshipEntityTest extends FriendshipsRelationshipEntityTest {

	}

	public static class EmbeddedFriendsInLongTransactionTest extends FriendsInLongTransactionTest {

	}

	public static class EmbeddedGraphIdCapabilityTest extends GraphIdCapabilityTest {

	}

	public static class EmbeddedHeirarchyRelsTest extends HeirarchyRelsTest {

	}

	public static class EmbeddedIdentityTest extends IdentityTest {

	}

	public static class EmbeddedIngredientsIntegrationTest extends IngredientsIntegrationTest {

	}

	public static class EmbeddedLifecycleTest extends LifecycleTest {

	}

	public static class EmbeddedLoadCapabilityTest extends LoadCapabilityTest {

	}

	public static class EmbeddedLookupByPrimaryIndexTests extends LookupByPrimaryIndexTests {

	}

	public static class EmbeddedMovieTest extends MovieTest {

	}

	public static class EmbeddedMultipleRelationshipsTest extends MultipleRelationshipsTest {

	}

	public static class EmbeddedMusicIntegrationTest extends MusicIntegrationTest {

	}

	public static class EmbeddedNodeEntityTest extends NodeEntityTest {

	}

	public static class EmbeddedNumericConversionTest extends NumericConversionTest {

	}

	public static class EmbeddedParameterizedConversionTest extends ParameterizedConversionTest {

	}

	public static class EmbeddedPetIntegrationTest extends PetIntegrationTest {

	}

	public static class EmbeddedPizzaIntegrationTest extends PizzaIntegrationTest {

		@Override
		protected GraphDatabaseService getGraphDatabaseService() {
			return testServer.getGraphDatabaseService();
		}
	}

	public static class EmbeddedQueryCapabilityTest extends QueryCapabilityTest {

	}

	public static class EmbeddedRelationshipEntityMappingTest extends RelationshipEntityMappingTest {

		@Override
		protected GraphDatabaseService getGraphDatabaseService() {
			return testServer.getGraphDatabaseService();
		}
	}

	public static class EmbeddedRelationshipEntityPartialMappingTest extends RelationshipEntityPartialMappingTest {

	}

	public static class EmbeddedRelationshipEntityTest extends RelationshipEntityTest {

	}

	public static class EmbeddedRelationshipMappingTest extends RelationshipMappingTest {

		@Override
		protected GraphDatabaseService getGraphDatabaseService() {
			return testServer.getGraphDatabaseService();
		}
	}

	public static class EmbeddedRelationshipTrait extends RelationshipTrait {

	}

	public static class EmbeddedRestaurantIntegrationTest extends RestaurantIntegrationTest {

		@Override
		protected GraphDatabaseService getGraphDatabaseService() {
			return testServer.getGraphDatabaseService();
		}
	}

	public static class EmbeddedRichRelationTest extends RichRelationTest {

	}

	public static class EmbeddedSatelliteIntegrationTest extends SatelliteIntegrationTest {

	}

	public static class EmbeddedSaveCapabilityTest extends SaveCapabilityTest {

	}

	public static class EmbeddedSessionAndMappingContextTest extends SessionAndMappingContextTest {

	}

	public static class EmbeddedSimpleNetworkIntegrationTest extends SimpleNetworkIntegrationTest {

	}

	public static class EmbeddedSocialIntegrationTest extends SocialIntegrationTest {

	}

	public static class EmbeddedSocialRelationshipsIntegrationTest extends SocialRelationshipsIntegrationTest {

		@Override
		protected GraphDatabaseService getGraphDatabaseService() {
			return testServer.getGraphDatabaseService();
		}
	}

	public static class EmbeddedSpyIntegrationTest extends SpyIntegrationTest {

	}

	public static class EmbeddedStaleObjectTest extends StaleObjectTest {

	}

	public static class EmbeddedTransactionManagerTest extends TransactionManagerTest {

	}

	public static class EmbeddedTransactionSerialisationTest extends TransactionSerialisationTest {

	}

	public static class EmbeddedTransactionTest extends TransactionTest {

	}

	public static class EmbeddedTreeIntegrationTest extends TreeIntegrationTest {

	}

	public static class EmbeddedUserTest extends UserTest {

	}
}
