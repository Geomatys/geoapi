/*
 *    GeoAPI - Java interfaces for OGC/ISO standards
 *    Copyright © 2018-2024 Open Geospatial Consortium, Inc.
 *    http://www.geoapi.org
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.opengis.bridge.python;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.opengis.metadata.Metadata;
import org.opengis.metadata.citation.Responsibility;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.util.FactoryException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.opengis.bridge.python.PythonBridgeHelper.metadata;

/**
 * Java tests corresponding to the java code executed from python tests os this module located in :
 * geoapi-java-python/src/test/python/tests
 */
public class PythonBridgeHelperTest {

    @Test
    public void testResponsibility() {
        final Responsibility responsibility = PythonBridgeHelper.responsibility();
        assertEquals("CI_Responsibility{party=[CI_Party{name=Aristotle}]}", responsibility.toString());
    }

    @Test
    public void testCreatedMetadata() {
        final Metadata md = metadata();
        assertTrue(md.getSpatialRepresentationInfo().isEmpty(),
                "Null value should have been replaced by empty collection.");
        assertEquals("MD_Metadata{contact=[CI_Responsibility{party=[CI_Party{name=Aristotle}]}]}", md.toString());
    }

    @Disabled("Disabled as cause : java.lang.NullPointerException: Cannot invoke \"org.opengis.referencing.crs.CRSAuthorityFactory.createCoordinateReferenceSystem(String)\" because \"<local1>\" is null")
    @Test()
    public void testReferencing() throws FactoryException {
        final CoordinateReferenceSystem crs = PythonHelper.findCoordinateReferenceSystem("EPSG:3395");
        Assertions.assertEquals("WGS 84 / World Mercator", crs.getName().getCode());
    }
}
