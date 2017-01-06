package org.neo4j.ogm.drivers.bolt;

import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.ogm.driver.Driver;
import org.neo4j.ogm.multidrivertest.*;
import org.neo4j.ogm.service.Components;
import org.neo4j.ogm.testutil.TestServer;

/**
 * Created by markangrish on 05/01/2017.
 */
@RunWith(Enclosed.class)
public class EnclosedBoltTests {

	Driver driver = Components.driver();

	private static TestServer testServer = new TestServer.Builder()
			.enableBolt(true)
			.transactionTimeoutSeconds(30)
			.build();

	public static class BoltAutoIndexManagerTest extends AutoIndexManagerTest {

		@Override
		protected GraphDatabaseService getGraphDatabaseService() {
			return testServer.getGraphDatabaseService();
		}
	}

	public static class BoltAAATest extends AAATest {

	}

	public static class BoltAABBTest extends AABBTest {

	}

	public static class BoltAATest extends AATest {

	}

	public static class BoltABBTest extends ABBTest {

	}

	public static class BoltAbstractWithGenericPropertyRelationshipTest extends AbstractWithGenericPropertyRelationshipTest {

	}

	public static class BoltABTest extends ABTest {

	}

	public static class BoltAssociatedObjectsTest extends AssociatedObjectsTest {

	}

	public static class BoltBidirectionalMappingTest extends BidirectionalMappingTest {

	}

	public static class BoltBikeTest extends BikeTest {

	}

	public static class BoltBlogTest extends BlogTest {

	}

	public static class BoltCanonicalTest extends CanonicalTest {

	}

	public static class BoltCineastsIntegrationTest extends CineastsIntegrationTest {

	}

	public static class BoltCineastsRelationshipEntityTest extends CineastsRelationshipEntityTest {

	}

	public static class BoltClassHierarchiesIntegrationTest extends ClassHierarchiesIntegrationTest {

		@Override
		protected GraphDatabaseService getGraphDatabaseService() {
			return testServer.getGraphDatabaseService();
		}
	}

	public static class BoltClosedTransactionTest extends ClosedTransactionTest {

	}

	public static class BoltCollectionsTest extends CollectionsTest {

	}

	public static class BoltCompaniesIntegrationTest extends CompaniesIntegrationTest {

	}

	public static class BoltConvertibleIntegrationTest extends ConvertibleIntegrationTest {

	}

	public static class BoltDegenerateEntityModelTests extends DegenerateEntityModelTests {

		@Override
		protected GraphDatabaseService getGraphDatabaseService() {
			return testServer.getGraphDatabaseService();
		}
	}

	public static class BoltDeleteCapabilityTest extends DeleteCapabilityTest {

	}

	public static class BoltDirtyObjectsTest extends DirtyObjectsTest {

	}

	public static class BoltDualRelationshipTest extends DualRelationshipTest {

	}

	public static class BoltDualTargetEntityRelationshipTest extends DualTargetEntityRelationshipTest {

	}

	public static class BoltDualTargetRelationshipsTest extends DualTargetRelationshipsTest {

	}

	public static class BoltEducationIntegrationTest extends EducationIntegrationTest {

	}

	public static class BoltEducationTest extends EducationTest {

	}

	public static class BoltElectionTest extends ElectionTest {

	}

	public static class BoltEndToEndTest extends EndToEndTest {

	}

	public static class BoltEntityGraphMapperTest extends EntityGraphMapperTest {

		@Override
		protected GraphDatabaseService getGraphDatabaseService() {
			return testServer.getGraphDatabaseService();
		}
	}

	public static class BoltEnumsNotScannedTest extends EnumsNotScannedTest {

	}

	public static class BoltEnumsScannedTest extends EnumsScannedTest {

	}

	public static class BoltEventTestBaseClass extends EventTestBaseClass {

	}

	public static class BoltExtraAABBTest extends ExtraAABBTest {

	}

	public static class BoltExtraABBTest extends ExtraABBTest {

	}

	public static class BoltExtraABTest extends ExtraABTest {

	}

	public static class BoltExtraRelationshipEntityPartialMappingTest extends ExtraRelationshipEntityPartialMappingTest {

		@Override
		protected GraphDatabaseService getGraphDatabaseService() {
			return testServer.getGraphDatabaseService();
		}
	}

	public static class BoltExtraRelationshipEntityTest extends ExtraRelationshipEntityTest {

	}

	public static class BoltFriendshipsRelationshipEntityTest extends FriendshipsRelationshipEntityTest {

	}

	public static class BoltFriendsInLongTransactionTest extends FriendsInLongTransactionTest {

	}

	public static class BoltGraphIdCapabilityTest extends GraphIdCapabilityTest {

	}

	public static class BoltHeirarchyRelsTest extends HeirarchyRelsTest {

	}

	public static class BoltIdentityTest extends IdentityTest {

	}

	public static class BoltIngredientsIntegrationTest extends IngredientsIntegrationTest {

	}

	public static class BoltLifecycleTest extends LifecycleTest {

	}

	public static class BoltLoadCapabilityTest extends LoadCapabilityTest {

	}

	public static class BoltLookupByPrimaryIndexTests extends LookupByPrimaryIndexTests {

	}

	public static class BoltMovieTest extends MovieTest {

	}

	public static class BoltMultipleRelationshipsTest extends MultipleRelationshipsTest {

	}

	public static class BoltMusicIntegrationTest extends MusicIntegrationTest {

	}

	public static class BoltNodeEntityTest extends NodeEntityTest {

	}

	public static class BoltNumericConversionTest extends NumericConversionTest {

	}

	public static class BoltParameterizedConversionTest extends ParameterizedConversionTest {

	}

	public static class BoltPetIntegrationTest extends PetIntegrationTest {

	}

	public static class BoltPizzaIntegrationTest extends PizzaIntegrationTest {

		@Override
		protected GraphDatabaseService getGraphDatabaseService() {
			return testServer.getGraphDatabaseService();
		}
	}

	public static class BoltQueryCapabilityTest extends QueryCapabilityTest {

	}

	public static class BoltRelationshipEntityMappingTest extends RelationshipEntityMappingTest {

		@Override
		protected GraphDatabaseService getGraphDatabaseService() {
			return testServer.getGraphDatabaseService();
		}
	}

	public static class BoltRelationshipEntityPartialMappingTest extends RelationshipEntityPartialMappingTest {

	}

	public static class BoltRelationshipEntityTest extends RelationshipEntityTest {

	}

	public static class BoltRelationshipMappingTest extends RelationshipMappingTest {

		@Override
		protected GraphDatabaseService getGraphDatabaseService() {
			return testServer.getGraphDatabaseService();
		}
	}

	public static class BoltRelationshipTrait extends RelationshipTrait {

	}

	public static class BoltRestaurantIntegrationTest extends RestaurantIntegrationTest {

		@Override
		protected GraphDatabaseService getGraphDatabaseService() {
			return testServer.getGraphDatabaseService();
		}
	}

	public static class BoltRichRelationTest extends RichRelationTest {

	}

	public static class BoltSatelliteIntegrationTest extends SatelliteIntegrationTest {

	}

	public static class BoltSaveCapabilityTest extends SaveCapabilityTest {

	}

	public static class BoltSessionAndMappingContextTest extends SessionAndMappingContextTest {

	}

	public static class BoltSimpleNetworkIntegrationTest extends SimpleNetworkIntegrationTest {

	}

	public static class BoltSocialIntegrationTest extends SocialIntegrationTest {

	}

	public static class BoltSocialRelationshipsIntegrationTest extends SocialRelationshipsIntegrationTest {

		@Override
		protected GraphDatabaseService getGraphDatabaseService() {
			return testServer.getGraphDatabaseService();
		}
	}

	public static class BoltSpyIntegrationTest extends SpyIntegrationTest {

	}

	public static class BoltStaleObjectTest extends StaleObjectTest {

	}

	public static class BoltTransactionManagerTest extends TransactionManagerTest {

	}

	public static class BoltTransactionSerialisationTest extends TransactionSerialisationTest {

	}

	public static class BoltTransactionTest extends TransactionTest {

	}

	public static class BoltTreeIntegrationTest extends TreeIntegrationTest {

	}

	public static class BoltUserTest extends UserTest {

	}
}
