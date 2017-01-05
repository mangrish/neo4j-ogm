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

package org.neo4j.ogm.persistence.relationships.transitive.aabb;

import org.junit.*;
import org.neo4j.ogm.annotation.*;
import org.neo4j.ogm.persistence.relationships.direct.RelationshipTrait;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;

import java.io.IOException;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 */
public abstract class AABBTest extends RelationshipTrait {

    private static SessionFactory sessionFactory = new SessionFactory("org.neo4j.ogm.persistence.relationships.transitive.aabb");
    private Session session;
    private A a1, a2, a3;
    private B b1, b2, b3;
    private R r1, r2, r3, r4, r5, r6;

    @Before
    public void init() throws IOException {
        session = sessionFactory.openSession();
        setUpEntityModel();
    }

    @After
    public void cleanup() {
        session.purgeDatabase();
    }

    private void setUpEntityModel() {
        // three source nodes
        a1 = new A();
        a2 = new A();
        a3 = new A();

        // three target nodes
        b1 = new B();
        b2 = new B();
        b3 = new B();

        // six relationships
        r1 = new R(a1, b1);
        r2 = new R(a1, b2); //problem
        r3 = new R(a2, b1);
        r4 = new R(a2, b3);
        r5 = new R(a3, b2);
        r6 = new R(a3, b3);

        // assign relationships to both sides to ensure entity graph is fully connected
        a1.r = new R[]{r1, r2};
        a2.r = new R[]{r3, r4};
        a3.r = new R[]{r5, r6};

        b1.r = new R[]{r1, r3};
        b2.r = new R[]{r2, r5};
        b3.r = new R[]{r4, r6};
    }

    @Test
    public void shouldFindBFromA() {

        // because the graph is fully connected, we should be able to save any object to fully populate the graph
        session.save(b1);

        a1 = session.load(A.class, a1.id);
        a2 = session.load(A.class, a2.id);
        a3 = session.load(A.class, a3.id);

        assertSameArray(new B[]{a1.r[0].b, a1.r[1].b}, new B[]{b1, b2});
        assertSameArray(new B[]{a2.r[0].b, a2.r[1].b}, new B[]{b1, b3});
        assertSameArray(new B[]{a3.r[0].b, a3.r[1].b}, new B[]{b2, b3});

    }

    @Test
    public void shouldFindAFromB() {

        // because the graph is fully connected, we should be able to save any object to fully populate the graph
        session.save(a1);

        b1 = session.load(B.class, b1.id);
        b2 = session.load(B.class, b2.id);
        b3 = session.load(B.class, b3.id);

        assertEquals(2, b1.r.length);
        assertEquals(2, b2.r.length);
        assertEquals(2, b3.r.length);
        assertSameArray(new A[]{b1.r[0].a, b1.r[1].a}, new A[]{a1, a2});
        assertSameArray(new A[]{b2.r[0].a, b2.r[1].a}, new A[]{a1, a3});
        assertSameArray(new A[]{b3.r[0].a, b3.r[1].a}, new A[]{a2, a3});

    }

    @Test
    public void shouldReflectRemovalA() {

        // because the graph is fully connected, we should be able to save any object to fully populate the graph
        session.save(a1);

        // it is programmer's responsibility to keep the domain entities synchronized
        b2.r = null;
        a1.r = new R[]{r1};
        a3.r = new R[]{r6};

        session.save(b2);

        // when we reload a1
        a1 = session.load(A.class, a1.id);
        // expect the b2 relationship to have gone.
        assertEquals(1, a1.r.length);
        assertSameArray(new B[]{b1}, new B[]{a1.r[0].b});


        // when we reload a3
        a3 = session.load(A.class, a3.id);
        // expect the b2 relationship to have gone.
        assertSameArray(new B[]{b3}, new B[]{a3.r[0].b});


        // and when we reload a2
        a2 = session.load(A.class, a2.id);
        // expect its relationships to be intact.
        assertSameArray(new B[]{b1, b3}, new B[]{a2.r[0].b, a2.r[1].b});

    }


    @Test
    @Ignore
    public void shouldHandleAddNewRelationshipBetweenASingleABPair() {
        // fully connected, will persist everything
        session.save(a1);

        R r7 = new R(a1, b1);

        a1.r = new R[]{r2, r7};
        b1.r = new R[]{r3, r7};

        session.save(a1);

        b1 = session.load(B.class, b1.id);

        assertSameArray(new R[]{r1, r3, r7}, b1.r);
        assertSameArray(new R[]{r1, r2, r7}, a1.r);

    }

    /**
     * @see DATAGRAPH-611
     */
    @Test
    public void shouldSaveRelationsForA1InTheCorrectDirection() {
        session.save(a1);

        session.clear();

        a1 = session.load(A.class, a1.id);
        assertSameArray(new R[]{r1, r2}, a1.r);
    }

    /**
     * @see DATAGRAPH-611
     */
    @Test
    public void shouldSaveRelationsForA2TheCorrectDirection() {
        session.save(a2);

        session.clear();

        a2 = session.load(A.class, a2.id);
        assertSameArray(new R[]{r3, r4}, a2.r);
    }

    /**
     * @see DATAGRAPH-611
     */
    @Test
    public void shouldSaveRelationsForA3TheCorrectDirection() {
        session.save(a3);

        session.clear();

        a3 = session.load(A.class, a3.id);
        assertSameArray(new R[]{r5, r6}, a3.r);
    }

    /**
     * @see DATAGRAPH-611
     */
    @Test
    public void shouldSaveRelationsForB1TheCorrectDirection() {
        session.save(b1);

        session.clear();

        b1 = session.load(B.class, b1.id);
        assertSameArray(new R[]{r1, r3}, b1.r);
    }

    /**
     * @see DATAGRAPH-611
     */
    @Test
    public void shouldSaveRelationsForB2TheCorrectDirection() {
        session.save(b2);

        session.clear();

        b2 = session.load(B.class, b2.id);
        assertSameArray(new R[]{r2, r5}, b2.r);
    }

    /**
     * @see DATAGRAPH-611
     */
    @Test
    public void shouldSaveRelationsForB3TheCorrectDirection() {
        session.save(b3);

        session.clear();

        b3 = session.load(B.class, b3.id);
        assertSameArray(new R[]{r4, r6}, b3.r);
    }

    /**
     * @see DATAGRAPH-714
     */
    @Test
    public void shouldBeAbleToUpdateRBySavingA() {
        A a1 = new A();
        B b3 = new B();
        R r3 = new R();
        r3.a = a1;
        r3.b = b3;
        r3.number = 1;
        a1.r = new R[]{r3};
        b3.r = new R[]{r3};

        session.save(a1);
        r3.number = 2;
        session.save(a1);

        session.clear();
        b3 = session.load(B.class, b3.id);
        assertEquals(2, b3.r[0].number);
    }

    /**
     * @see DATAGRAPH-714
     */
    @Test
    public void shouldBeAbleToUpdateRBySavingB() {
        A a1 = new A();
        B b3 = new B();
        R r3 = new R();
        r3.a = a1;
        r3.b = b3;
        r3.number = 1;
        a1.r = new R[]{r3};
        b3.r = new R[]{r3};

        session.save(a1);
        r3.number = 2;
        session.save(b3);

        session.clear();
        b3 = session.load(B.class, b3.id);
        assertEquals(2, b3.r[0].number);
    }

    /**
     * @see DATAGRAPH-714
     */
    @Test
    public void shouldBeAbleToUpdateRBySavingR() {
        A a1 = new A();
        B b3 = new B();
        R r3 = new R();
        r3.a = a1;
        r3.b = b3;
        r3.number = 1;
        a1.r = new R[]{r3};
        b3.r = new R[]{r3};

        session.save(a1);
        r3.number = 2;
        session.save(r3);

        session.clear();
        b3 = session.load(B.class, b3.id);
        assertEquals(2, b3.r[0].number);
    }


    @NodeEntity(label = "A")
    public static class A extends E {

        @Relationship(type = "EDGE", direction = Relationship.OUTGOING)
        R[] r;

    }

    @NodeEntity(label = "B")
    public static class B extends E {

        @Relationship(type = "EDGE", direction = Relationship.INCOMING)
        R[] r;

    }

    @RelationshipEntity(type = "EDGE")
    public static class R {

        Long id;

        @StartNode
        A a;
        @EndNode
        B b;

        int number;

        public R(A a, B b) {
            this.a = a;
            this.b = b;
        }

        public R() {
        }

        @Override
        public String toString() {
            return this.getClass().getSimpleName() + ":" + a.id + "->" + b.id;
        }

    }

    /**
     * Can be used as the basic class at the root of any entity for these tests,
     * provides the mandatory id field, a unique ref, a simple to-string method
     * and equals/hashcode implementation.
     * <p/>
     * Note that without an equals/hashcode implementation, reloading
     * an object which already has a collection of items in it
     * will result in the collection items being added again, because
     * of the behaviour of the ogm merge function when handling
     * arrays and iterables.
     */
    public abstract static class E {

        public Long id;
        public String key;

        public E() {
            this.key = UUID.randomUUID().toString();
        }

        @Override
        public String toString() {
            return this.getClass().getSimpleName() + ":" + id + ":" + key;
        }

        @Override
        public boolean equals(Object o) {

            if (this == o) return true;

            if (o == null || getClass() != o.getClass()) return false;

            return (key.equals(((E) o).key));
        }

        @Override
        public int hashCode() {
            return key.hashCode();
        }
    }
}
