package org.neo4j.ogm.drivers.bolt;

import static org.junit.Assert.*;

import org.junit.Test;
import org.neo4j.ogm.authentication.Credentials;
import org.neo4j.ogm.authentication.UsernamePasswordCredentials;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.config.DriverConfiguration;

/**
 * Created by markangrish on 05/01/2017.
 */
public class BoltDriverConfigurationTest {


	@Test
	public void shouldLoadBoltDriverConfigFromPropertiesFile() {
		DriverConfiguration driverConfig = new DriverConfiguration(new Configuration("src/test/resources/bolt.driver.properties"));
		assertEquals("bolt://neo4j:password@localhost", driverConfig.getURI());
		assertEquals(Integer.valueOf(150), driverConfig.getConnectionPoolSize());
		assertEquals("NONE", driverConfig.getEncryptionLevel());
		assertEquals("TRUST_ON_FIRST_USE", driverConfig.getTrustStrategy());
		assertEquals("/tmp/cert", driverConfig.getTrustCertFile());
	}

	@Test
	public void shouldSetUsernameAndPasswordCredentialsForBoltProtocol() {
		String username = "neo4j";
		String password = "password";
		Configuration dbConfig = new Configuration();
		dbConfig.driverConfiguration().setURI("bolt://" + username + ":" + password + "@localhost");
		Credentials credentials = dbConfig.driverConfiguration().getCredentials();
		UsernamePasswordCredentials basic = (UsernamePasswordCredentials) credentials;
		assertNotNull(basic);
		assertEquals(username, basic.getUsername());
		assertEquals(password, basic.getPassword());
	}
}
