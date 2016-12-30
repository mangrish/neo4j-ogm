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

package org.neo4j.ogm.metadata;


import org.neo4j.ogm.typeconversion.AttributeConverter;
import org.neo4j.ogm.typeconversion.CompositeAttributeConverter;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 * @author Mark Angrish
 */
public interface FieldInfo {

	boolean hasCompositeConverter();

	String getName();

	AttributeConverter getPropertyConverter();

	CompositeAttributeConverter getCompositeConverter();

	ObjectAnnotations getAnnotations();

	boolean isTypeOf(Class<?> type);

	boolean hasAnnotation(String annotationName);

	String getTypeDescriptor();

	boolean persistableAsProperty();

	boolean hasPropertyConverter();

	void setPropertyConverter(AttributeConverter<?, ?> attributeConverter);

	String getCollectionClassname();

	boolean isIterable();

	boolean isArray();

	String property();

	boolean isConstraint();

	String relationship();

	boolean isParameterisedTypeOf(Class<?> type);

	boolean isArrayOf(Class<?> type);

	String relationshipDirection(String defaultDirection);

	boolean isScalar();

	Class<?> convertedType();

	boolean isLabelField();

	String relationshipTypeAnnotation();

	boolean isDate();

	boolean isBigInteger();

	boolean isBigDecimal();

	boolean isByteArray();

	boolean isByteArrayWrapper();
}
