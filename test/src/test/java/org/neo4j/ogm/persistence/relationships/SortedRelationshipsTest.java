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
import java.util.ArrayList;
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
    private Long personId;
    private Long matrixId;
    private Long dieHardId;

    @Before
    public void init() throws IOException {
        session = new SessionFactory(driver, "org.neo4j.ogm.domain.entityMapping").openSession();
        session.purgeDatabase();

        createDataForTests();
    }

    private void createDataForTests() {
        Person person = new Person();
        person.name = "OrderLovingPerson";
        session.save(person);

        Movie matrixMovie = new Movie();
        matrixMovie.name = "The Matrix";

        Movie dieHardMovie = new Movie();
        dieHardMovie.name = "Die Hard";

        Rating ratingOne = Rating.create(person, matrixMovie, 0);
        Rating ratingTwo = Rating.create(person, dieHardMovie, 1);

        person.movieRatings.add(ratingOne);
        person.movieRatings.add(ratingTwo);

        session.save(person);

        matrixId = matrixMovie.id;
        dieHardId = dieHardMovie.id;
        personId = person.id;

        session.clear();
    }

    @After
    public void cleanup() {
        session.purgeDatabase();
        session.clear();
    }

    /**
     * @see issue #386
     */
    @Test
    public void newSortedRelationshipCollectionSave() {
        Person person = session.load(Person.class, personId);
        List<Rating> movieRatings = person.movieRatings;
        Rating rating1 = movieRatings.get(0);
        Rating rating2 = movieRatings.get(1);
        assertThat(rating1.movie.id, equalTo(matrixId));
        assertThat(rating2.movie.id, equalTo(dieHardId));
    }

    /**
     * @see issue #386
     */
    @Test
    public void reorderSortedRelationship() {
        Person person = session.load(Person.class, personId);
        List<Rating> movieRatings = new ArrayList<>(person.movieRatings);

        person.movieRatings.clear();
        person.movieRatings.add(movieRatings.get(1));
        person.movieRatings.add(movieRatings.get(0));

        session.save(person);
        session.clear();

        person = session.load(Person.class, personId);
        movieRatings = person.movieRatings;
        Rating rating1 = movieRatings.get(0);
        Rating rating2 = movieRatings.get(1);
        assertThat(rating1.movie.id, equalTo(dieHardId));
        assertThat(rating2.movie.id, equalTo(matrixId));
    }

    /**
     * @see issue #386
     */
    @Test
    public void addRelationshipInTheMiddle() {
        Person person = session.load(Person.class, personId);

        Movie starTrekMovie = new Movie();
        starTrekMovie.name = "Star Trek I";
        Rating starTrekRating = Rating.create(person, starTrekMovie, 10);

        person.movieRatings.add(1, starTrekRating);

        session.save(person);

        long starTrekId = starTrekMovie.id;

        session.clear();

        person = session.load(Person.class, personId);
        List<Rating> movieRatings = person.movieRatings;
        Rating rating1 = movieRatings.get(0);
        Rating rating2 = movieRatings.get(1);
        Rating rating3 = movieRatings.get(2);
        assertThat(rating1.movie.id, equalTo(matrixId));
        assertThat(rating2.movie.id, equalTo(starTrekId));
        assertThat(rating3.movie.id, equalTo(dieHardId));
    }

    /**
     * @see issue #386
     */
    @Test
    public void addRelationshipAtTheEnd() {
        Person person = session.load(Person.class, personId);

        Movie starTrekMovie = new Movie();
        starTrekMovie.name = "Star Trek I";
        Rating starTrekRating = Rating.create(person, starTrekMovie, 10);

        person.movieRatings.add(starTrekRating);

        session.save(person);

        long starTrekId = starTrekMovie.id;

        session.clear();

        person = session.load(Person.class, personId);
        List<Rating> movieRatings = person.movieRatings;
        Rating rating1 = movieRatings.get(0);
        Rating rating2 = movieRatings.get(1);
        Rating rating3 = movieRatings.get(2);
        assertThat(rating1.movie.id, equalTo(matrixId));
        assertThat(rating2.movie.id, equalTo(dieHardId));
        assertThat(rating3.movie.id, equalTo(starTrekId));
    }

}
