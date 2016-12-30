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


import java.util.List;
import java.util.Map;

import org.neo4j.ogm.typeconversion.ConversionCallback;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 * @author Mark Angrish
 */
public interface DomainInfo extends ClassFileProcessor {

	ClassInfo getClassSimpleName(String name);

	List<ClassInfo> getClassInfosWithAnnotation(String annotationName);

	ClassInfo getClassInfoForInterface(String interfaceName);

	Map<String, ClassInfo> getClassInfoMap();

	List<ClassInfo> getClassInfos(String interfaceName);

	void registerConversionCallback(ConversionCallback conversionCallback);

	ClassInfo getClass(String name);
}
