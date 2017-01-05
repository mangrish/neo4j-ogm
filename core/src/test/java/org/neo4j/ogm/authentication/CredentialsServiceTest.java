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

package org.neo4j.ogm.authentication;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;


/**
 * @author Vince Bickers
 */
public class CredentialsServiceTest {

    @Before
    public void setUp() {
        System.setProperty("username", "neo4j");
        System.setProperty("password", "password");
    }

    @After
    public void tearDown() {
        System.getProperties().remove("username");
        System.getProperties().remove("password");
    }

    @Test
    public void testUserNameAndPassword() {
        assertEquals("bmVvNGo6cGFzc3dvcmQ=", CredentialsService.userNameAndPassword().credentials());

    }


    @Test
    public void testUserNameNoPassword() {

        System.getProperties().remove("password");
        assertNull(CredentialsService.userNameAndPassword());

    }


    @Test
    public void testNoUserNamePassword() {

        System.getProperties().remove("username");
        assertNull(CredentialsService.userNameAndPassword());

    }

}
