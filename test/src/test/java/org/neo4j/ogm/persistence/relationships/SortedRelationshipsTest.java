/*
 * Copyright (c) 2002-2018 "Neo Technology,"
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

package org.neo4j.ogm.persistence.relationships;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.ogm.domain.entityMapping.Movie;
import org.neo4j.ogm.domain.entityMapping.Person;
import org.neo4j.ogm.domain.entityMapping.Rating;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.MultiDriverTestClass;

/**
 * @author Gerrit Meier
 */
public class SortedRelationshipsTest extends MultiDriverTestClass {

    private Session session;

    @Before
    public void init() throws IOException {
        session = new SessionFactory(driver, "org.neo4j.ogm.domain.entityMapping").openSession();
        session.purgeDatabase();
    }

    @After
    public void cleanup() {
        session.purgeDatabase();
        session.clear();
    }

    /**
     * @see issue 386
     */
    @Test
    public void shouldCreateGraphProperly() {
        Person person = new Person();
        person.name = "OrderLovingPerson";
        session.save(person);

        Movie matrix = new Movie();
        matrix.name = "The Matrix";

        Movie dieHard = new Movie();
        dieHard.name = "Die Hard";

        Rating ratingOne = Rating.create(person, matrix, 0);
        Rating ratingTwo = Rating.create(person, dieHard, 1);

        person.movieRatings.add(ratingOne);
        person.movieRatings.add(ratingTwo);

        session.save(person);

        Long dieHardId = dieHard.id;
        Long matrixId = matrix.id;
        Long personId = person.id;

        session.query("MATCH (:Movie)<-[r:RATED]-(:PersonX) return r.something, r.value", Collections.emptyMap()).queryResults().forEach(System.out::println);
        session.clear();

        person = session.load(Person.class, personId);
        List<Rating> movieRatings = person.movieRatings;
        Rating rating1 = movieRatings.get(0);
        Rating rating2 = movieRatings.get(1);
        System.out.println(rating1.movie.name);
        System.out.println(rating2.movie.name);
        assertThat(rating1.movie.id, equalTo(matrixId));
        assertThat(rating2.movie.id, equalTo(dieHardId));


        session.clear();


    }
}
