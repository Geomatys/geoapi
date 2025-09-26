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

import java.util.Map;
import java.util.Set;
import org.opengis.example.metadata.MetadataProxyFactory;
import org.opengis.example.metadata.SimpleCitation;
import org.opengis.metadata.Metadata;
import org.opengis.metadata.citation.Party;
import org.opengis.metadata.citation.Responsibility;
import org.opengis.util.InternationalString;


/**
 * Factory of test instances of {@link org.opengis.metadata.Metadata} interfaces.
 * This class is invoked from Python code in the {@code java-python/src/test/python} directory.
 *
 * @author Matthieu Bastianelli (Geomatys)
 */
public final class PythonBridgeHelper {
    /**
     * Do not allow instantiation of this class.
     */
    private PythonBridgeHelper() {
    }

    /**
     * Factory of metadata objects.
     */
    private static final MetadataProxyFactory factory = MetadataProxyFactory.INSTANCE;

    /**
     * Creates an "Aristotle" responsible party.
     *
     * @return an "Aristotle" responsible party.
     */
    public static Responsibility responsibility() {
        InternationalString name = new SimpleCitation("Aristotle");
        Party party = factory.create(Party.class, Map.of("name", name));
        return factory.create(Responsibility.class, Map.of("party", Set.of(party)));
    }

    /**
     * Creates a metadata with only an "Aristotle" responsible party.
     *
     * @return a metadata with an "Aristotle" responsible party.
     */
    public static Metadata metadata() {
        return factory.create(Metadata.class, Map.of("contact", Set.of(responsibility())));
    }
}
