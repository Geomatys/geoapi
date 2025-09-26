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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.opengis.example.metadata.MetadataProxyFactory;
import org.opengis.example.metadata.SimpleCitation;
import org.opengis.metadata.Metadata;
import org.opengis.metadata.citation.Citation;
import org.opengis.metadata.citation.Party;
import org.opengis.metadata.citation.Responsibility;

/**
 * Helper class use to create {@link org.opengis.metadata.Metadata} instances in order to test the python binding in
 * geoapi-java-python/src/test/python package.
 */
public final class PythonBridgeHelper {

    private PythonBridgeHelper() {
    }

    private static final MetadataProxyFactory factory = MetadataProxyFactory.INSTANCE;

    private static Citation testSimpleCitation() {
        return new SimpleCitation("Aristotle");
    }

    private static Party party() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("name", testSimpleCitation());
        return factory.create(Party.class, attributes);
    }

    static Responsibility responsibility() {
        final Party party = party();
        final Map<java.lang.String, Object> attributes = new HashMap<>();
        attributes.put("party", Set.of(party));
        return factory.create(Responsibility.class, attributes);
    }

    public static Metadata metadata() {
        final Map<String, Set<Responsibility>> attributes = new HashMap<>();
        attributes.put("contact", Set.of(responsibility()));
        return factory.create(Metadata.class, attributes);
    }
}
