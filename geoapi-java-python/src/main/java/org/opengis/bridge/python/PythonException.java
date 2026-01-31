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
package org.opengis.bridge.python;


/**
 * Thrown when the Python interpreter threw an exception.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @version 4.0
 * @since   4.0
 */
public class PythonException extends RuntimeException {
    /**
     * For cross-version compatibility.
     */
    private static final long serialVersionUID = 584282142207436969L;

    /**
     * Creates a new exception with the given message.
     *
     * @param message  a description of the problem.
     */
    public PythonException(final String message) {
        super(message);
    }

    /**
     * Creates a new exception with the given message and cause.
     *
     * @param message  a description of the problem.
     * @param cause    the cause of this problem.
     */
    public PythonException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
