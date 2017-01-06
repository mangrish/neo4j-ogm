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

package org.neo4j.ogm.domain.cineasts.minimum;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

/**
 * @author Vince Bickers
 */
@RelationshipEntity(type = "ACTS_IN")
public class Role {

    Long id;
    String played;

    @StartNode
	Actor actor;

    @EndNode
	Movie movie;

    public Role() {
    }

    public Role(String character, Actor actor, Movie movie) {
        played = character;
        this.actor = actor;
        this.movie = movie;
    }


}
