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

package org.neo4j.ogm.annotations;

import static org.junit.Assert.*;

import java.util.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.domain.forum.ForumTopicLink;
import org.neo4j.ogm.domain.forum.Member;
import org.neo4j.ogm.domain.forum.Topic;
import org.neo4j.ogm.domain.forum.activity.Activity;
import org.neo4j.ogm.domain.forum.activity.Comment;
import org.neo4j.ogm.domain.forum.activity.Post;
import org.neo4j.ogm.domain.satellites.Location;
import org.neo4j.ogm.domain.satellites.Program;
import org.neo4j.ogm.domain.satellites.Satellite;
import org.neo4j.ogm.entity.io.*;
import org.neo4j.ogm.metadata.ClassInfo;
import org.neo4j.ogm.metadata.DomainInfo;
import org.neo4j.ogm.metadata.impl.legacy.LegacyDomainInfo;

/**
 * @author Adam George
 * @author Luanne Misquitta
 */
public class EntityAccessManagerTest {

	private DomainInfo domainInfo;

	@Before
	public void setup() {
		domainInfo = new LegacyDomainInfo(
				"org.neo4j.ogm.domain.forum",
				"org.neo4j.ogm.domain.satellites",
				"org.neo4j.ogm.annotations"
		);
	}


	@Test
	public void shouldPreferAnnotatedFieldToPlainMethodWhenFindingPropertyToSet() {
		ClassInfo classInfo = this.domainInfo.getClass(DummyDomainObject.class.getName());

		// testProp matches the setter/getter name but because the field is annotated then it should be used instead
		EntityAccess objectAccess = EntityAccessManager.getPropertyWriter(classInfo, "testProp");
		assertNotNull("The resultant object accessor shouldn't be null", objectAccess);

		DummyDomainObject domainObject = new DummyDomainObject();
		objectAccess.write(domainObject, "TEST");
		assertEquals("TEST", domainObject.annotatedTestProperty);
	}

	/**
	 * @see DATAGRAPH-674
	 */
	@Test
	public void shouldPreferAnnotatedFieldToMethodNotAnnotatedWithPropertyWhenFindingPropertyToSet() {
		ClassInfo classInfo = this.domainInfo.getClass(DummyDomainObject.class.getName());
		DummyDomainObject domainObject = new DummyDomainObject();

		EntityAccess objectAccess = EntityAccessManager.getPropertyWriter(classInfo, "testIgnored");
		assertNotNull("The resultant object accessor shouldn't be null", objectAccess);
		assertTrue(objectAccess instanceof FieldWriter);
		assertEquals(String.class, objectAccess.type());
		objectAccess.write(domainObject, "TEST");
		assertEquals("TEST", domainObject.propertyMethodsIgnored);
	}

	@Test
	public void shouldAccessViaFieldCorrespondingToPropertyIfNoAnnotationsOrAccessorMethodsArePresent() {
		ClassInfo classInfo = this.domainInfo.getClass(DummyDomainObject.class.getName());

		DummyDomainObject domainObject = new DummyDomainObject();
		domainObject.propertyWithoutAccessorMethods = 9;

		// test writing via field
		EntityAccess writer = EntityAccessManager.getPropertyWriter(classInfo, "propertyWithoutAccessorMethods");
		assertNotNull("The resultant writer shouldn't be null", writer);
		writer.write(domainObject, 27);
		assertEquals(27, domainObject.propertyWithoutAccessorMethods);

		// test reading via field
		PropertyReader reader = EntityAccessManager.getPropertyReader(classInfo, "propertyWithoutAccessorMethods");
		assertNotNull("The resultant reader shouldn't be null", reader);
		assertEquals(domainObject.propertyWithoutAccessorMethods, reader.readProperty(domainObject));
	}

	@Test
	public void shouldRetrieveObjectAccessForWritingIterableObject() {
		ClassInfo classInfo = this.domainInfo.getClass(Program.class.getName());

		RelationalWriter iterableAccess = EntityAccessManager.getIterableWriter(classInfo, Satellite.class, "satellites", Relationship.OUTGOING);
		assertNotNull("The resultant object accessor shouldn't be null", iterableAccess);
		Program spaceProgramme = new Program();
		iterableAccess.write(spaceProgramme, Arrays.asList(new Satellite()));
		assertNotNull("The satellites list wasn't set correctly", spaceProgramme.getSatellites());
		assertFalse("The satellites list wasn't set correctly", spaceProgramme.getSatellites().isEmpty());
	}

	@Test
	@Ignore // we do expect this to work now.
	public void shouldNotRetrieveSetterMethodObjectAccessIfTypesAreIncompatible() {
		ClassInfo classInfo = this.domainInfo.getClass(Program.class.getName());

		Satellite singleSatellite = new Satellite();

		// the SATELLITES type matches the setter that takes an Iterable argument
		RelationalWriter objectAccess = EntityAccessManager.getRelationalWriter(classInfo, "SATELLITES", Relationship.OUTGOING, singleSatellite);
		assertNull("A compatible object accessor shouldn't have been found", objectAccess);
	}

	@Test
	public void shouldPreferAnnotatedMethodToAnnotatedFieldWhenSettingRelationshipObject() {
		// 1st, try to find a method annotated with the relationship type.
		ClassInfo classInfo = this.domainInfo.getClass(Member.class.getName());
		List<? extends Activity> parameter = Arrays.asList(new Comment());

		RelationalWriter objectAccess = EntityAccessManager.getRelationalWriter(classInfo, "HAS_ACTIVITY", Relationship.OUTGOING, new Comment());
		assertNotNull("The resultant object accessor shouldn't be null", objectAccess);
		Member member = new Member();
		objectAccess.write(member, parameter);
		assertEquals(member.getActivityList(), parameter);
	}

	@Test
	public void shouldPreferAnnotatedFieldToPlainSetterMatchingRelationshipTypeWhenSettingRelationshipObject() {
		// 2nd, try to find a field annotated with with relationship type
		ClassInfo classInfo = this.domainInfo.getClass(DummyDomainObject.class.getName());

		Member parameter = new Member();

		RelationalWriter objectAccess = EntityAccessManager.getRelationalWriter(classInfo, "CONTAINS", Relationship.OUTGOING, parameter);
		assertNotNull("The resultant object accessor shouldn't be null", objectAccess);
		DummyDomainObject domainObject = new DummyDomainObject();
		objectAccess.write(domainObject, parameter);
		assertEquals(domainObject.member, parameter);

		Member otherMember = new Member();
		objectAccess = EntityAccessManager.getRelationalWriter(classInfo, "REGISTERED", Relationship.OUTGOING, otherMember);
		assertNotNull("The resultant object accessor shouldn't be null", objectAccess);
		domainObject = new DummyDomainObject();
		objectAccess.write(domainObject, otherMember);
		assertEquals(domainObject.registeredMember, otherMember);
	}

	@Test
	public void shouldPreferSetterBasedOnRelationshipTypeToFieldInObjectWithoutAnnotations() {
		// 3rd, try to find a "setXYZ" method where XYZ is derived from the relationship type
		ClassInfo classInfo = this.domainInfo.getClass(Satellite.class.getName());

		Location satelliteLocation = new Location();
		satelliteLocation.setName("Outer Space");

		RelationalWriter objectAccess = EntityAccessManager.getRelationalWriter(classInfo, "LOCATION", Relationship.OUTGOING, satelliteLocation);
		assertNotNull("The resultant object accessor shouldn't be null", objectAccess);
		Satellite satellite = new Satellite();
		objectAccess.write(satellite, satelliteLocation);
		assertEquals(satellite.getLocation(), satelliteLocation);
	}

	@Test
	public void shouldPreferFieldBasedOnRelationshipTypeToPlainSetterWithMatchingParameterType() {
		// 4th, try to find a "XYZ" field name where XYZ is derived from the relationship type
		ClassInfo classInfo = this.domainInfo.getClass(DummyDomainObject.class.getName());
		Topic favouriteTopic = new Topic();

		// NB: the setter is called setTopic here, so a relationship type of just "TOPIC" would choose the setter
		RelationalWriter objectAccess = EntityAccessManager.getRelationalWriter(classInfo, "FAVOURITE_TOPIC", Relationship.OUTGOING, favouriteTopic);
		assertNotNull("The resultant object accessor shouldn't be null", objectAccess);
		DummyDomainObject domainObject = new DummyDomainObject();
		objectAccess.write(domainObject, favouriteTopic);
		assertEquals(domainObject.favouriteTopic, favouriteTopic);
		assertFalse("The access should be via the field", domainObject.topicAccessorWasCalled);
	}

	@Test
	public void shouldDefaultToFieldThatMatchesTheParameterTypeIfRelationshipTypeCannotBeMatchedAndNoSetterExists() {
		// 6th, try to find a field that shares the same type as the parameter
		ClassInfo classInfo = this.domainInfo.getClass(DummyDomainObject.class.getName());
		Post forumPost = new Post();

		RelationalWriter objectAccess = EntityAccessManager.getRelationalWriter(classInfo, "UTTER_RUBBISH", Relationship.OUTGOING, forumPost);
		assertNotNull("The resultant object accessor shouldn't be null", objectAccess);
		DummyDomainObject domainObject = new DummyDomainObject();
		objectAccess.write(domainObject, forumPost);
		assertEquals(domainObject.postWithoutAccessorMethods, forumPost);
	}

	@Test
	public void shouldPreferAnnotatedFieldToPlainGetterWhenReadingFromAnObject() {
		ClassInfo classInfo = this.domainInfo.getClass(DummyDomainObject.class.getName());

		DummyDomainObject domainObject = new DummyDomainObject();
		domainObject.annotatedTestProperty = "more arbitrary text";

		PropertyReader objectAccess = EntityAccessManager.getPropertyReader(classInfo, "testProp");
		assertNotNull("The resultant object accessor shouldn't be null", objectAccess);
		assertEquals(domainObject.annotatedTestProperty, objectAccess.readProperty(domainObject));
	}

	@Test
	public void shouldPreferAnnotatedFieldToGetterWhenReadingFromAnObject() {
		ClassInfo classInfo = this.domainInfo.getClass(DummyDomainObject.class.getName());

		DummyDomainObject domainObject = new DummyDomainObject();
		domainObject.propertyWithDifferentAnnotatedGetter = "more arbitrary text";
		Collection<PropertyReader> readers = EntityAccessManager.getPropertyReaders(classInfo);

		PropertyReader objectAccess = EntityAccessManager.getPropertyReader(classInfo, "differentAnnotationOnGetter");
		assertNotNull("The resultant object accessor shouldn't be null", objectAccess);
		assertEquals(domainObject.propertyWithDifferentAnnotatedGetter, objectAccess.readProperty(domainObject));

		for (PropertyReader reader : readers) {
			if (reader.propertyName().equals("differentAnnotationOnGetter")) {
				assertTrue(reader instanceof FieldReader);
			}
		}
	}

	@Test
	public void shouldPreferMethodBasedAccessToFieldAccessWhenReadingFromObjectsWithoutAnnotations() {
		ClassInfo classInfo = this.domainInfo.getClass(DummyDomainObject.class.getName());

		DummyDomainObject domainObject = new DummyDomainObject();
		domainObject.nonAnnotatedTestProperty = new Double(30.16);

		PropertyReader objectAccess = EntityAccessManager.getPropertyReader(classInfo, "nonAnnotatedTestProperty");
		assertNotNull("The resultant object accessor shouldn't be null", objectAccess);
		assertEquals(domainObject.nonAnnotatedTestProperty, objectAccess.readProperty(domainObject));
	}

	@Test
	public void shouldPreferAnnotatedMethodToAnnotatedFieldMatchingRelationshipTypeWhenReadingRelationshipObject() {
		ClassInfo classInfo = this.domainInfo.getClass(Member.class.getName());
		Member member = new Member();
		member.setActivityList(Arrays.<Activity>asList(new Comment()));

		RelationalReader reader = EntityAccessManager.getRelationalReader(classInfo, "HAS_ACTIVITY", Relationship.OUTGOING);
		assertNotNull("The resultant object reader shouldn't be null", reader);
		assertSame(member.getActivityList(), reader.read(member));
		assertEquals("HAS_ACTIVITY", reader.relationshipType());
	}

	@Test
	public void shouldPreferAnnotatedFieldToPlainGetterMethodMatchingRelationshipType() {
		ClassInfo classInfo = this.domainInfo.getClass(DummyDomainObject.class.getName());

		DummyDomainObject domainObject = new DummyDomainObject();
		domainObject.member = new Member();
		domainObject.registeredMember = new Member();

		RelationalReader reader = EntityAccessManager.getRelationalReader(classInfo, "CONTAINS", Relationship.OUTGOING);
		assertNotNull("The resultant object reader shouldn't be null", reader);
		assertSame(domainObject.member, reader.read(domainObject));
		assertEquals("CONTAINS", reader.relationshipType());

		reader = EntityAccessManager.getRelationalReader(classInfo, "REGISTERED", Relationship.OUTGOING);
		assertNotNull("The resultant object reader shouldn't be null", reader);
		assertSame(domainObject.registeredMember, reader.read(domainObject));
		assertEquals("REGISTERED", reader.relationshipType());
	}

	@Test
	public void shouldPreferGetterBasedOnRelationshipTypeToFieldInObjectWithoutAnnotations() {
		ClassInfo classInfo = this.domainInfo.getClass(Satellite.class.getName());

		Satellite satellite = new Satellite();
		Location satelliteLocation = new Location();
		satelliteLocation.setName("Outer Space");
		satellite.setLocation(satelliteLocation);

		RelationalReader reader = EntityAccessManager.getRelationalReader(classInfo, "LOCATION", Relationship.OUTGOING);
		assertNotNull("The resultant object accessor shouldn't be null", reader);
		assertSame(satellite.getLocation(), reader.read(satellite));
		assertEquals("LOCATION", reader.relationshipType());
	}

	@Test
	public void shouldReadFromFieldMatchingRelationshipTypeInObjectWithoutAnnotationsOrAccessorMethods() {
		ClassInfo classInfo = this.domainInfo.getClass(DummyDomainObject.class.getName());

		DummyDomainObject domainObject = new DummyDomainObject();
		domainObject.postWithoutAccessorMethods = new Post();

		RelationalReader reader = EntityAccessManager.getRelationalReader(classInfo, "POST_WITHOUT_ACCESSOR_METHODS", Relationship.OUTGOING);
		assertNotNull("The resultant object accessor shouldn't be null", reader);
		assertSame(domainObject.postWithoutAccessorMethods, reader.read(domainObject));
		assertEquals("POST_WITHOUT_ACCESSOR_METHODS", reader.relationshipType());
	}

	@Test
	public void shouldUseFieldAccessUnconditionallyForReadingIdentityProperty() {
		ClassInfo classInfo = this.domainInfo.getClass(DummyDomainObject.class.getName());

		final long id = 593L;
		DummyDomainObject domainObject = new DummyDomainObject();
		domainObject.setId(id);

		PropertyReader idReader = EntityAccessManager.getIdentityPropertyReader(classInfo);
		assertNotNull("The resultant ID reader shouldn't be null", idReader);
		assertEquals(id, idReader.readProperty(domainObject));
	}

	@Test
	public void shouldRetrieveAppropriateObjectAccessToAllRelationalAttributesForParticularClass() {
		ClassInfo classInfo = this.domainInfo.getClass(DummyDomainObject.class.getName());

		DummyDomainObject domainObject = new DummyDomainObject();
		domainObject.postWithoutAccessorMethods = new Post();
		domainObject.favouriteTopic = new Topic();
		domainObject.member = new Member();
		domainObject.readOnlyComment = new Comment();
		domainObject.registeredMember = new Member();
		domainObject.naturalSatellites = new ArrayList<>();
		domainObject.artificialSatellites = Collections.singletonList(new Satellite());

		Collection<RelationalReader> relationalAccessors = EntityAccessManager.getRelationalReaders(classInfo);
		assertNotNull("The resultant list of object accessors shouldn't be null", relationalAccessors);
		assertEquals("An unexpected number of accessors was returned", 7, relationalAccessors.size());

		Map<String, Class<? extends RelationalReader>> expectedRelationalReaders = new HashMap<>();
		expectedRelationalReaders.put("FAVOURITE_TOPIC", FieldReader.class);
		expectedRelationalReaders.put("CONTAINS", FieldReader.class);
		expectedRelationalReaders.put("POST_WITHOUT_ACCESSOR_METHODS", FieldReader.class);
		expectedRelationalReaders.put("NATURAL", FieldReader.class);
		expectedRelationalReaders.put("ARTIFICIAL", FieldReader.class);
		expectedRelationalReaders.put("REGISTERED", FieldReader.class);

		for (RelationalReader objectAccess : relationalAccessors) {
			String relType = objectAccess.relationshipType();
			assertTrue("Relationship type " + relType + " wasn't expected", expectedRelationalReaders.containsKey(relType));
			assertEquals(expectedRelationalReaders.get(relType), objectAccess.getClass());
			assertNotNull(objectAccess.read(domainObject));
		}
	}

	@Test
	public void shouldRetrieveAppropriateObjectAccessToEndNodeAttributeOnRelationshipEntity() {
		ClassInfo relationshipEntityClassInfo = domainInfo.getClass(ForumTopicLink.class.getName());

		RelationalReader endNodeReader = EntityAccessManager.getEndNodeReader(relationshipEntityClassInfo);
		assertNotNull("The resultant end node reader shouldn't be null", endNodeReader);

		ForumTopicLink forumTopicLink = new ForumTopicLink();
		Topic topic = new Topic();
		forumTopicLink.setTopic(topic);
		assertSame("The value wasn't read correctly", topic, endNodeReader.read(forumTopicLink));
	}

	@Test
	public void shouldReturnNullOnAttemptToAccessNonExistentEndNodeAttributeOnRelationshipEntity() {
		ClassInfo classInfoOfNonRelationshipEntity = domainInfo.getClass(Member.class.getName());
		assertNull(EntityAccessManager.getEndNodeReader(classInfoOfNonRelationshipEntity));
	}

	/**
	 * @see DATAGRAPH-637
	 */
	@Test
	public void shouldPreferAnnotatedFieldWithMatchingRelationshipTypeWhenGettingIterableWriter() {
		// 2nd, try to find a field annotated with with relationship type
		ClassInfo classInfo = this.domainInfo.getClass(DummyDomainObject.class.getName());

		List<Satellite> natural = new ArrayList<>();
		natural.add(new Satellite());

		RelationalWriter objectAccess = EntityAccessManager.getIterableWriter(classInfo, Satellite.class, "NATURAL", Relationship.OUTGOING);
		assertNotNull("The resultant object accessor shouldn't be null", objectAccess);
		DummyDomainObject domainObject = new DummyDomainObject();
		objectAccess.write(domainObject, natural);
		assertEquals(natural, domainObject.naturalSatellites);
	}


	/**
	 * @see DATAGRAPH-637
	 */
	@Test
	public void shouldPreferAnnotatedFieldWithMatchingRelationshipTypeWhenGettingIterableReader() {
		// 2nd, try to find a field annotated with with relationship type
		ClassInfo classInfo = this.domainInfo.getClass(DummyDomainObject.class.getName());

		List<Satellite> natural = new ArrayList<>();
		natural.add(new Satellite());

		RelationalReader relationalReader = EntityAccessManager.getIterableReader(classInfo, Satellite.class, "NATURAL", Relationship.OUTGOING);
		assertNotNull("The resultant object accessor shouldn't be null", relationalReader);
		DummyDomainObject domainObject = new DummyDomainObject();
		domainObject.naturalSatellites = natural;
		Object o = relationalReader.read(domainObject);
		assertEquals(natural, o);
	}

	/**
	 * Domain object exhibiting various annotation configurations on its properties for test purposes.
	 */
	public static class DummyDomainObject {

		@Property(name = "testProp")
		String annotatedTestProperty;
		Double nonAnnotatedTestProperty;
		boolean nonAnnotatedTestPropertyAccessorWasCalled;
		@Property(name = "testAnnoProp")
		String fullyAnnotatedProperty;
		@Property(name = "testIgnored")
		String propertyMethodsIgnored;
		@Property(name = "differentAnnotationOnGetter")
		String propertyWithDifferentAnnotatedGetter;
		@Property(name = "testAnnoProp")
		boolean fullyAnnotatedPropertyAccessorWasCalled;
		int propertyWithoutAccessorMethods;
		@Relationship(type = "CONTAINS")
		Member member;
		@Relationship(type = "REGISTERED")
		Member registeredMember;
		@Relationship(type = "NATURAL")
		List<Satellite> naturalSatellites;
		@Relationship(type = "ARTIFICIAL")
		List<Satellite> artificialSatellites;
		Topic favouriteTopic;
		boolean topicAccessorWasCalled;
		Post postWithoutAccessorMethods;
		@Relationship(type = "COMMENT")
		Comment readOnlyComment;
		// interestingly, if I extend DomainObject then the inherited ID field isn't found within a nested class
		@SuppressWarnings("unused")
		private Long id;

		public Long getId() {
			throw new UnsupportedOperationException("Shouldn't be calling the ID getter");
		}

		public void setId(Long id) {
			this.id = id;
		}

		public String getTestProp() {
			throw new UnsupportedOperationException("Shouldn't be calling the getter");
		}

		public void setTestProp(String value) {
			throw new UnsupportedOperationException("Shouldn't be calling the setter with: " + value);
		}

		public Double getNonAnnotatedTestProperty() {
			this.nonAnnotatedTestPropertyAccessorWasCalled = true;
			return this.nonAnnotatedTestProperty;
		}

		public void setNonAnnotatedTestProperty(Double value) {
			this.nonAnnotatedTestPropertyAccessorWasCalled = true;
			this.nonAnnotatedTestProperty = value;
		}

		public String getFullyAnnotatedProperty() {
			this.fullyAnnotatedPropertyAccessorWasCalled = true;
			return fullyAnnotatedProperty;
		}

		public void setFullyAnnotatedProperty(String fullyAnnotatedProperty) {
			this.fullyAnnotatedPropertyAccessorWasCalled = true;
			this.fullyAnnotatedProperty = fullyAnnotatedProperty;
		}

		public Member getContains() {
			throw new UnsupportedOperationException("Shouldn't be calling the getter");
		}

		public void setContains(Member nestedObject) {
			throw new UnsupportedOperationException("Shouldn't be calling the setter with: " + nestedObject);
		}

		public Member getRegisteredMember() {
			throw new UnsupportedOperationException("Shouldn't be calling the getter");
		}

		public void setRegisteredMember(Member registeredMember) {
			throw new UnsupportedOperationException("Shouldn't be calling the setter with: " + registeredMember);
		}

		public Topic getTopic() {
			this.topicAccessorWasCalled = true;
			return favouriteTopic;
		}

		public void setTopic(Topic favouriteTopic) {
			this.topicAccessorWasCalled = true;
			this.favouriteTopic = favouriteTopic;
		}

		public Comment getReadOnlyComment() {
			return this.readOnlyComment;
		}

		public String getPropertyMethodsIgnored() {
			return propertyMethodsIgnored;
		}

		public void setPropertyMethodsIgnored(String propertyMethodsIgnored) {
			this.propertyMethodsIgnored = propertyMethodsIgnored;
		}

		@JsonIgnore //we've used @JsonIgnore but it could be any other annotation
		public String getDifferentAnnotationOnGetter() {
			return propertyWithDifferentAnnotatedGetter;
		}

		@JsonIgnore
		public void setDifferentAnnotationOnGetter(String propertyWithDifferentAnnotatedGetter) {
			this.propertyWithDifferentAnnotatedGetter = propertyWithDifferentAnnotatedGetter;
		}
	}
}
