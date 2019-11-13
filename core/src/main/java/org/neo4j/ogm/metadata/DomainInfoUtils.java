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

import io.github.classgraph.ClassInfo;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.RelationshipEntity;

/**
 * Several utilities to make working with ClassGraph and domain info related tasks more fun.
 *
 * @author Michael J. Simons
 * @soundtrack From Dusk Till Dawn - Music From The Motion Picture
 */
final class DomainInfoUtils {

    static final String VALUE_ATTRIBUTE = "value";

    private static final Set<String> EXCLUDED_SUPER_CLASSES;
    static {
        Set<String> excludedSuperClasses = new HashSet<>();
        excludedSuperClasses.add("java.lang.Object");
        excludedSuperClasses.add("java.lang.Enum");

        EXCLUDED_SUPER_CLASSES = Collections.unmodifiableSet(excludedSuperClasses);
    }

    /**
     * @param classInfo The {@link ClassInfo class info instance} to be checked if it's a node Entity
     * @return True if the class is an explicit node entity (having the annotation directly on the class)
     */
    static boolean isNodeEntity(ClassInfo classInfo) {
        return classInfo.getAnnotationInfo().directOnly().containsName(NodeEntity.class.getName());
    }

    /**
     * @param classInfo The {@link ClassInfo class info instance} to be checked if it's a relationship Entity
     * @return True if this class is an explicit relationship entity  (having the annotation directly on the class)
     */
    static boolean isRelationshipEntity(ClassInfo classInfo) {
        return classInfo.getAnnotationInfo().directOnly().containsName(RelationshipEntity.class.getName());
    }

    static boolean includeInClassHierarchy(ClassInfo classInfo) {
        return !EXCLUDED_SUPER_CLASSES.contains(classInfo.getName());
    }

    private DomainInfoUtils() {
    }
}
