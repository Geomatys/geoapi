/*
 *    GeoAPI - Java interfaces for OGC/ISO standards
 *    Copyright © 2026 Open Geospatial Consortium, Inc.
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

import org.opengis.metadata.Metadata;
import org.opengis.metadata.citation.Responsibility;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.util.FactoryException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import static org.junit.jupiter.api.Assertions.*;


/**
 * Tests {@link PythonBridgeHelper}, the helper class used from Python tests.
 *
 * @author Matthieu Bastianelli (Geomatys)
 */
@SuppressWarnings("exports")
public final class PythonBridgeHelperTest {
    /**
     * Creates a new test case.
     */
    public PythonBridgeHelperTest() {
    }

    /**
     * Tests {@link PythonBridgeHelper#responsibility()}.
     */
    @Test
    public void testResponsibility() {
        final Responsibility responsibility = PythonBridgeHelper.responsibility();
        assertEquals("CI_Responsibility{party=[CI_Party{name=Aristotle}]}", responsibility.toString());
    }

    /**
     * Tests {@link PythonBridgeHelper#metadata()}.
     */
    @Test
    public void testMetadata() {
        final Metadata md = PythonBridgeHelper.metadata();
        assertTrue(md.getSpatialRepresentationInfo().isEmpty(),
                "Null value should have been replaced by empty collection.");
        assertEquals("MD_Metadata{contact=[CI_Responsibility{party=[CI_Party{name=Aristotle}]}]}", md.toString());
    }

    /**
     * Tests {@link PythonHelper#findCoordinateReferenceSystem(String)}.
     *
     * @throws FactoryException if an error occurred while fetching the <abbr>CRS</abbr> definition.
     */
    @Test
    @Disabled("Pending --add-reads geoapi-example")
    public void testReferencing() throws FactoryException {
        final CoordinateReferenceSystem crs = PythonHelper.findCoordinateReferenceSystem("EPSG:3395");
        assertEquals("WGS 84 / World Mercator", crs.getName().getCode());
    }
}
