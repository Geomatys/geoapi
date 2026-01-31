/*
 *    GeoAPI - Java interfaces for OGC/ISO standards
 *    Copyright © 2018-2023 Open Geospatial Consortium, Inc.
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

/**
 * GeoAPI bridge between Java and Python.
 * This bridge allows the use of a Python implementation from Java, and conversely.
 * The following rules are applied:
 *
 * <ul>
 *   <li>For any Java method, the name of the corresponding Python attribute is given
 *     by the {@link org.opengis.annotation.UML} annotation associated to the method.
 *     If a method has no such annotation, then the Java method name is used after
 *     conversion from camel case to snake case.</li>
 *   <li>GeoAPI-specific property types are supported ({@link org.opengis.util.CodeList} and
 *     {@link org.opengis.util.InternationalString}) in addition to some Java standard types
 *     such as {@link java.lang.Enum} and {@link java.util.Collection}.</li>
 * </ul>
 *
 * <h2>Requirements</h2>
 * This package requires Java 22 or later and Python 3.14 or later.
 * The implementation assumes 64-bits machine.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @version 4.0
 * @since   4.0
 */
package org.opengis.bridge.python;
