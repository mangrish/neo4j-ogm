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

package org.neo4j.ogm.domain.hierarchy.domain.trans;

import org.neo4j.ogm.annotation.Transient;
import org.neo4j.ogm.domain.hierarchy.domain.plain.PlainSingleClass;

/**
 * @author Michal Bachman
 */
public class PlainClassWithTransientFields {

    private Long id;

    private TransientSingleClass transientField;

    @Transient
    private PlainSingleClass anotherTransientField;

    private transient PlainSingleClass yetAnotherTransientField;

    public TransientSingleClass getTransientField() {
        return transientField;
    }

    public void setTransientField(TransientSingleClass transientField) {
        this.transientField = transientField;
    }

    public PlainSingleClass getAnotherTransientField() {
        return anotherTransientField;
    }

    public void setAnotherTransientField(PlainSingleClass anotherTransientField) {
        this.anotherTransientField = anotherTransientField;
    }

    public PlainSingleClass getYetAnotherTransientField() {
        return yetAnotherTransientField;
    }

    public void setYetAnotherTransientField(PlainSingleClass yetAnotherTransientField) {
        this.yetAnotherTransientField = yetAnotherTransientField;
    }
}
