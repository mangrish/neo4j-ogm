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

package org.neo4j.ogm.domain.education;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Vince Bickers
 */
public class School extends DomainObject {

    private Set<Teacher> teachers = new HashSet<>();
    private String name;

    public School() {
    }

    public School(String name) {
        setName(name);
    }

    public Set<Teacher> getTeachers() {
        return teachers;
    }

    public void setTeachers(Iterable<Teacher> teachers) {
        for (Teacher teacher : teachers) {
            if (!this.teachers.contains(teacher)) {
                teacher.setSchool(this);
                this.teachers.add(teacher);
            }
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
