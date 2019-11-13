/*
 * Copyright (c) 2002-2019 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neo4j.ogm.metadata;

import static org.neo4j.ogm.metadata.DomainInfoUtils.*;

import io.github.classgraph.AnnotationParameterValueList;
import io.github.classgraph.ClassInfo;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.neo4j.ogm.annotation.Labels;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.RelationshipEntity;

/**
 * Provides a view on ClassGraphs {@link ClassInfo} and access to all informations necessary for OGM.
 *
 * @author Michael J. Simons
 * @soundtrack Tom Holkenborg - Terminator Dark: Fate
 */
public final class ClassInfoAccessor {

    private final ClassInfo classInfo;

    private final String neo4jName;

    private final Set<String> staticLabels;

    public ClassInfoAccessor(ClassInfo classInfo) {

        if (classInfo == null) {
            throw new IllegalArgumentException("A valid ClassInfo instance is required (instance was null).");
        }
        this.classInfo = classInfo;

        this.neo4jName = generateNeo4jName(classInfo);
        this.staticLabels = computeStaticLabels(classInfo);
    }

    /**
     * @return The name used for the entity inside the database (either as label on nodes or type on relationships)
     */
    public String getNeo4jName() {
        return neo4jName;
    }

    /**
     * @return The simple name. In the case of inner classes,
     * this will contain the outer class name as well (<code>A$B</code>)
     */
    public String getSimpleName() {
        return classInfo.getSimpleName();
    }

    /**
     * <p>
     * Retrieves the static labels that are applied to nodes in the database. If the class' instances are persisted by
     * a relationship instead of a node then this method returns an empty collection.
     * </p>
     * <p>
     * Note that this method returns only the static labels. A node entity instance may declare additional labels
     * managed at runtime by using the @Labels annotation on a collection field, therefore the full set of labels to be
     * mapped to a node will be the static labels, in addition to any labels declared by the backing field of an
     * {@link Labels} annotation.
     * </p>
     *
     * @return A {@link Collection} of all the static labels that apply to the node or an empty list if there aren't
     * any, never <code>null</code>
     */
    public Collection<String> getStaticLabels() {
        return staticLabels;
    }

    private static String generateNeo4jName(ClassInfo classInfo) {

        String defaultNeo4jName = classInfo.getSimpleName();

        if (DomainInfoUtils.isNodeEntity(classInfo)) {
            AnnotationParameterValueList parameterValues = classInfo
                .getAnnotationInfo(NodeEntity.class.getName())
                .getParameterValues();
            String v = (String) parameterValues.getValue(VALUE_ATTRIBUTE);
            if (v.isEmpty()) {
                v = (String) parameterValues.getValue(NodeEntity.LABEL);
            }
            return v.isEmpty() ? defaultNeo4jName : v;
        }

        if (DomainInfoUtils.isRelationshipEntity(classInfo)) {
            AnnotationParameterValueList parameterValues = classInfo
                .getAnnotationInfo(RelationshipEntity.class.getName())
                .getParameterValues();
            String v = (String) parameterValues.getValue(VALUE_ATTRIBUTE);
            if (v.isEmpty()) {
                v = (String) parameterValues.getValue(RelationshipEntity.TYPE);
            }
            return v.isEmpty() ? defaultNeo4jName.toUpperCase() : v;
        }

        if (classInfo.isAbstract()) {
            return null;
        }

        return defaultNeo4jName;
    }

    private static Set<String> computeStaticLabels(ClassInfo classInfo) {

        final Predicate<ClassInfo> include = ((Predicate<ClassInfo>) ClassInfo::isAbstract).negate()
            .or(DomainInfoUtils::isNodeEntity);

        final Stream<ClassInfo> baseClass = Stream.of(classInfo).filter(include);
        final Stream<ClassInfo> superClasses = classInfo.getSuperclasses().stream()
            .filter(include)
            .filter(DomainInfoUtils::includeInClassHierarchy);
        final Stream<ClassInfo> interfaces = classInfo.getInterfaces().stream()
            .filter(include)
            .filter(underTest -> !underTest.isExternalClass());

        return Stream.of(baseClass, superClasses, interfaces).flatMap(Function.identity())
            .map(ClassInfoAccessor::generateNeo4jName)
            .filter(Objects::nonNull)
            .collect(Collectors.collectingAndThen(Collectors.toSet(), Collections::unmodifiableSet));
    }
}
