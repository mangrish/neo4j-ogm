package org.neo4j.ogm.drivers.embedded;

import java.io.File;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.ogm.drivers.embedded.driver.EmbeddedDriver;
import org.neo4j.ogm.multidrivertest.*;
import org.neo4j.ogm.service.Components;
import org.neo4j.ogm.testutil.FileUtils;
import org.neo4j.ogm.testutil.TestServer;
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

	public static class HttpAAATest extends AAATest {

	}

	public static class HttpAABBTest extends AABBTest {

	}

	public static class HttpAATest extends AATest {

	}

	public static class HttpABBTest extends ABBTest {

	}

	public static class HttpAbstractWithGenericPropertyRelationshipTest extends AbstractWithGenericPropertyRelationshipTest {

	}

	public static class HttpABTest extends ABTest {

	}

	public static class HttpAssociatedObjectsTest extends AssociatedObjectsTest {

	}

	public static class HttpBidirectionalMappingTest extends BidirectionalMappingTest {

	}

	public static class HttpBikeTest extends BikeTest {

	}

	public static class HttpBlogTest extends BlogTest {

	}

	public static class HttpCanonicalTest extends CanonicalTest {

	}

	public static class HttpCineastsIntegrationTest extends CineastsIntegrationTest {

	}

	public static class HttpCineastsRelationshipEntityTest extends CineastsRelationshipEntityTest {

	}

	public static class HttpClassHierarchiesIntegrationTest extends ClassHierarchiesIntegrationTest {

		@Override
		protected GraphDatabaseService getGraphDatabaseService() {
			return impermanentDb;
		}
	}

	public static class HttpClosedTransactionTest extends ClosedTransactionTest {

	}

	public static class HttpCollectionsTest extends CollectionsTest {

	}

	public static class HttpCompaniesIntegrationTest extends CompaniesIntegrationTest {

	}

	public static class HttpConvertibleIntegrationTest extends ConvertibleIntegrationTest {

	}

	public static class HttpDegenerateEntityModelTests extends DegenerateEntityModelTests {

		@Override
		protected GraphDatabaseService getGraphDatabaseService() {
			return impermanentDb;
		}
	}

	public static class HttpDeleteCapabilityTest extends DeleteCapabilityTest {

	}

	public static class HttpDirtyObjectsTest extends DirtyObjectsTest {

	}

	public static class HttpDualRelationshipTest extends DualRelationshipTest {

	}

	public static class HttpDualTargetEntityRelationshipTest extends DualTargetEntityRelationshipTest {

	}

	public static class HttpDualTargetRelationshipsTest extends DualTargetRelationshipsTest {

	}

	public static class HttpEducationIntegrationTest extends EducationIntegrationTest {

	}

	public static class HttpEducationTest extends EducationTest {

	}

	public static class HttpElectionTest extends ElectionTest {

	}

	public static class HttpEndToEndTest extends EndToEndTest {

	}

	public static class HttpEntityGraphMapperTest extends EntityGraphMapperTest {

		@Override
		protected GraphDatabaseService getGraphDatabaseService() {
			return impermanentDb;
		}
	}

	public static class HttpEnumsNotScannedTest extends EnumsNotScannedTest {

	}

	public static class HttpEnumsScannedTest extends EnumsScannedTest {

	}

	public static class HttpEventTestBaseClass extends EventTestBaseClass {

	}

	public static class HttpExtraAABBTest extends ExtraAABBTest {

	}

	public static class HttpExtraABBTest extends ExtraABBTest {

	}

	public static class HttpExtraABTest extends ExtraABTest {

	}

	public static class HttpExtraRelationshipEntityPartialMappingTest extends ExtraRelationshipEntityPartialMappingTest {

		@Override
		protected GraphDatabaseService getGraphDatabaseService() {
			return impermanentDb;
		}
	}

	public static class HttpExtraRelationshipEntityTest extends ExtraRelationshipEntityTest {

	}

	public static class HttpFriendshipsRelationshipEntityTest extends FriendshipsRelationshipEntityTest {

	}

	public static class HttpFriendsInLongTransactionTest extends FriendsInLongTransactionTest {

	}

	public static class HttpGraphIdCapabilityTest extends GraphIdCapabilityTest {

	}

	public static class HttpHeirarchyRelsTest extends HeirarchyRelsTest {

	}

	public static class HttpIdentityTest extends IdentityTest {

	}

	public static class HttpIngredientsIntegrationTest extends IngredientsIntegrationTest {

	}

	public static class HttpLifecycleTest extends LifecycleTest {

	}

	public static class HttpLoadCapabilityTest extends LoadCapabilityTest {

	}

	public static class HttpLookupByPrimaryIndexTests extends LookupByPrimaryIndexTests {

	}

	public static class HttpMovieTest extends MovieTest {

	}

	public static class HttpMultipleRelationshipsTest extends MultipleRelationshipsTest {

	}

	public static class HttpMusicIntegrationTest extends MusicIntegrationTest {

	}

	public static class HttpNodeEntityTest extends NodeEntityTest {

	}

	public static class HttpNumericConversionTest extends NumericConversionTest {

	}

	public static class HttpParameterizedConversionTest extends ParameterizedConversionTest {

	}

	public static class HttpPetIntegrationTest extends PetIntegrationTest {

	}

	public static class HttpPizzaIntegrationTest extends PizzaIntegrationTest {

		@Override
		protected GraphDatabaseService getGraphDatabaseService() {
			return impermanentDb;
		}
	}

	public static class HttpQueryCapabilityTest extends QueryCapabilityTest {

	}

	public static class HttpRelationshipEntityMappingTest extends RelationshipEntityMappingTest {

		@Override
		protected GraphDatabaseService getGraphDatabaseService() {
			return impermanentDb;
		}
	}

	public static class HttpRelationshipEntityPartialMappingTest extends RelationshipEntityPartialMappingTest {

	}

	public static class HttpRelationshipEntityTest extends RelationshipEntityTest {

	}

	public static class HttpRelationshipMappingTest extends RelationshipMappingTest {

		@Override
		protected GraphDatabaseService getGraphDatabaseService() {
			return impermanentDb;
		}
	}

	public static class HttpRelationshipTrait extends RelationshipTrait {

	}

	public static class HttpRestaurantIntegrationTest extends RestaurantIntegrationTest {

		@Override
		protected GraphDatabaseService getGraphDatabaseService() {
			return impermanentDb;
		}
	}

	public static class HttpRichRelationTest extends RichRelationTest {

	}

	public static class HttpSatelliteIntegrationTest extends SatelliteIntegrationTest {

	}

	public static class HttpSaveCapabilityTest extends SaveCapabilityTest {

	}

	public static class HttpSessionAndMappingContextTest extends SessionAndMappingContextTest {

	}

	public static class HttpSimpleNetworkIntegrationTest extends SimpleNetworkIntegrationTest {

	}

	public static class HttpSocialIntegrationTest extends SocialIntegrationTest {

	}

	public static class HttpSocialRelationshipsIntegrationTest extends SocialRelationshipsIntegrationTest {

		@Override
		protected GraphDatabaseService getGraphDatabaseService() {
			return impermanentDb;
		}
	}

	public static class HttpSpyIntegrationTest extends SpyIntegrationTest {

	}

	public static class HttpStaleObjectTest extends StaleObjectTest {

	}

	public static class HttpTransactionManagerTest extends TransactionManagerTest {

	}

	public static class HttpTransactionSerialisationTest extends TransactionSerialisationTest {

	}

	public static class HttpTransactionTest extends TransactionTest {

	}

	public static class HttpTreeIntegrationTest extends TreeIntegrationTest {

	}

	public static class HttpUserTest extends UserTest {

	}
}
