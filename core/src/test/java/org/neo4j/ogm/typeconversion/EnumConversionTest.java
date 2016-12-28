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
import org.neo4j.ogm.domain.convertible.enums.*;
import org.neo4j.ogm.metadata.ClassInfo;
import org.neo4j.ogm.metadata.FieldInfo;
import org.neo4j.ogm.metadata.MethodInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 */
public class EnumConversionTest {

    private static final MetaData metaData = new MetaData("org.neo4j.ogm.domain.convertible.enums");

    private static final ClassInfo algebraInfo = metaData.classInfo("Algebra");
    private static final ClassInfo personInfo = metaData.classInfo("Person");
    private static final ClassInfo tagEntityInfo = metaData.classInfo("TagEntity");

    @Test
    public void testSaveFieldWithAnnotatedConverter() {
        FieldInfo fieldInfo = algebraInfo.propertyField("numberSystem");
        assertTrue(fieldInfo.hasPropertyConverter());

        Algebra algebra = new Algebra();
        algebra.setNumberSystem(NumberSystem.NATURAL);
        Assert.assertEquals("N", algebra.getNumberSystem().getDomain());
        String value = (String) fieldInfo.getPropertyConverter().toGraphProperty(algebra.getNumberSystem());
        // the converted enum value that will be stored as a neo4j node / rel property
        assertEquals("NATURAL", value);
    }

    @Test
    public void testLoadFieldWithAnnotatedConverter() {
        FieldInfo fieldInfo = algebraInfo.propertyField("numberSystem");
        assertTrue(fieldInfo.hasPropertyConverter());
        // a node / rel property value loaded from neo4j, to be stored in on an enum
        String value = "INTEGER";
        Algebra algebra = new Algebra();
        algebra.setNumberSystem((NumberSystem) fieldInfo.getPropertyConverter().toEntityAttribute(value));

        Assert.assertEquals(NumberSystem.INTEGER, algebra.getNumberSystem());
        Assert.assertEquals("Z", algebra.getNumberSystem().getDomain());
    }

    @Test
    public void testGenderFieldWithAutoDetectedConverter() {

        Person bob = new Person();
        bob.setGender(Gender.MALE);

        FieldInfo fieldInfo = personInfo.propertyField("gender");

        assertTrue(fieldInfo.hasPropertyConverter());
        Assert.assertEquals("MALE", fieldInfo.getPropertyConverter().toGraphProperty(bob.getGender()));

    }

    /**
     * @see DATAGRAPH-550
     */
    @Test
    public void testEducationArrayFieldWithAutoDetectedConverter() {

        Person bob = new Person();
        bob.setGender(Gender.MALE);
        Education[] inProgress = new Education[]{Education.MASTERS, Education.PHD};
        bob.setInProgressEducation(inProgress);

        FieldInfo fieldInfo = personInfo.propertyField("inProgressEducation");

        assertTrue(fieldInfo.hasPropertyConverter());
        String[] converted = (String[]) fieldInfo.getPropertyConverter().toGraphProperty(bob.getInProgressEducation());
        assertTrue("MASTERS".equals(converted[0]) || "MASTERS".equals(converted[1]));
        assertTrue("PHD".equals(converted[0]) || "PHD".equals(converted[1]));

    }

    /**
     * @see DATAGRAPH-550
     */
    @Test
    public void testEducationCollectionFieldWithAutoDetectedConverter() {
        List<Education> completedEducation = new ArrayList<>();
        completedEducation.add(Education.HIGHSCHOOL);
        completedEducation.add(Education.BACHELORS);

        Person bob = new Person();
        bob.setCompletedEducation(completedEducation);

        FieldInfo fieldInfo = personInfo.propertyField("completedEducation");

        assertTrue(fieldInfo.hasPropertyConverter());
        String[] converted = (String[]) fieldInfo.getPropertyConverter().toGraphProperty(bob.getCompletedEducation());
        assertTrue("HIGHSCHOOL".equals(converted[0]) || "HIGHSCHOOL".equals(converted[1]));
        assertTrue("BACHELORS".equals(converted[0]) || "BACHELORS".equals(converted[1]));
    }

    /**
     * @see DATAGRAPH-720
     */
    @Test
    public void shouldNotRegisterEnumWhenTypeContainsEnumType() {
        FieldInfo fieldInfo = tagEntityInfo.relationshipFieldByName("tags");
        assertFalse(fieldInfo.hasPropertyConverter());
    }
}
