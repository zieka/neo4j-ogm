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
package org.neo4j.ogm.metadata.accessors;

import static org.assertj.core.api.Assertions.*;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.neo4j.ogm.metadata.ClassInfoAccessor;

/**
 * The idea of this test is to compare the output of the previous scanning solution with the new one.
 *
 * @author Michael J. Simons
 */
class AccessorsResultComparisionTest {

    @BeforeAll
    static void generateTestData() {

        /*
        MetaData metaData = new MetaData("org.neo4j.ogm.domain");
        metaData.persistentEntities().forEach(classInfo -> {

            System.out.println(classInfo.name() + ","  + (classInfo.neo4jName() == null? "" : classInfo.neo4jName()) + "," + classInfo.staticLabels().stream().collect(Collectors.joining(";")));
        });
*/

    }

    @TestInstance(TestInstance.Lifecycle.PER_CLASS) // Make sure we only scan once.
    @DisplayName("ClassInfoAccessor should produce the same information as OGM 3.2. ClassInfo...")
    @Nested
    class ClassInfoAccessorTest {

        ScanResult scanResult;

        @BeforeAll
        void scanValidClasses() {
            this.scanResult = new ClassGraph()
                .enableAllInfo()
                .whitelistPackages("org.neo4j.ogm.domain")
                .scan();
        }

        @DisplayName("...for neo4jnames and static labels on")
        @ParameterizedTest(name = "{0}")
        @CsvFileSource(resources = "/org/neo4j/ogm/metadata/accessors/neo4jname_and_static_labels_ogm32.csv")
        void computationOfNeo4jNamesAndStaticLabelsMustProduceSameResults(
            String domainClass,
            String neo4jName,
            String expectedLabels
        ) {
            ClassInfo classInfo = scanResult.getClassInfo(domainClass);
            ClassInfoAccessor classInfoAccessor = new ClassInfoAccessor(classInfo);

            assertThat(classInfoAccessor.getNeo4jName()).isEqualTo(neo4jName);
            if (expectedLabels == null || expectedLabels.isEmpty()) {
                assertThat(classInfoAccessor.getStaticLabels()).isEmpty();
            } else {
                assertThat(classInfoAccessor.getStaticLabels()).containsExactlyInAnyOrder(expectedLabels.split(";"));
            }
        }

        @AfterAll
        void closeScanResult() {
            scanResult.close();
        }
    }
}
