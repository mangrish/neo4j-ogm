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

import org.junit.Test;
import org.neo4j.ogm.MetaData;
import org.neo4j.ogm.domain.convertible.bytes.Photo;
import org.neo4j.ogm.metadata.ClassInfo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Vince Bickers
 */
public class ByteArrayConversionTest {

    private static final MetaData metaData = new MetaData("org.neo4j.ogm.domain.convertible.bytes");
    private static final ClassInfo photoInfo = metaData.classInfo("Photo");

    @Test
    public void testConvertersLoaded() {

        assertTrue(photoInfo.propertyField("image").hasPropertyConverter());

    }

}
