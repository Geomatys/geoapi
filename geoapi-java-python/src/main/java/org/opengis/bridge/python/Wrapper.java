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

import java.lang.foreign.MemorySegment;


/**
 * Wrappers around a native object.
 *
 * @author  Martin Desruisseaux (Geomatys)
 */
interface Wrapper {
    /**
     * Returns the address of the native object if using the given bindings.
     * The {@code bindings} argument is a safety for making sure that we get
     * the address of an object managed by the right interpreter engine.
     *
     * @param  bindings  the bindings used for accessing native objects.
     * @return address of the native object.
     * @throws PythonException if the native object is not managed by the specified bindings.
     */
    MemorySegment address(CPython bindings);
}
