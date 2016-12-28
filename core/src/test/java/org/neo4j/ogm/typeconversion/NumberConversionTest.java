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

package org.neo4j.ogm.typeconversion;

import org.junit.Assert;
import org.junit.Test;
import org.neo4j.ogm.MetaData;
import org.neo4j.ogm.domain.convertible.numbers.Account;
import org.neo4j.ogm.metadata.ClassInfo;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 */
public class NumberConversionTest {

    private static final MetaData metaData = new MetaData("org.neo4j.ogm.domain.convertible.numbers");
    private static final ClassInfo accountInfo = metaData.classInfo("Account");

    @Test
    public void assertAccountFieldsHaveDefaultConverters() {
        assertTrue(accountInfo.propertyField("balance").hasPropertyConverter());
        assertTrue(accountInfo.propertyField("facility").hasPropertyConverter());
        assertTrue(accountInfo.propertyField("deposits").hasPropertyConverter());
        assertTrue(accountInfo.propertyField("loans").hasPropertyConverter());

    }


    @Test
    public void assertHasCompositeConverter() {
        MetaData metaData = new MetaData("org.neo4j.ogm.domain.restaurant");
        ClassInfo restaurantInfo = metaData.classInfo("Restaurant");
        assertTrue(restaurantInfo.propertyField("location").hasCompositeConverter());
    }
}
