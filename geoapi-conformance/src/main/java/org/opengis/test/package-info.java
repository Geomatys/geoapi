/*
 *    GeoAPI - Java interfaces for OGC/ISO standards
 *    Copyright © 2008-2024 Open Geospatial Consortium, Inc.
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
 * The GeoAPI conformance testing framework. This package does not provide any test by itself,
 * but provides the base classes for all tests defined in sub-packages.
 * The classes in this package can be grouped in 6 categories:
 *
 * <ul>
 *   <li><p><b>Test cases</b><br>
 *       {@link org.opengis.test.TestCase} is the base class of all GeoAPI tests. Implementers can extend directly
 *       the {@code TestCase} subclasses of their choice for gaining some control, for example in order to specify
 *       whether {@linkplain org.opengis.test.referencing.TransformTestCase#isDerivativeSupported math transform
 *       derivatives are supported} by their implementation.</p></li>
 *
 *   <li><p><b>Validation</b><br>
 *       {@link org.opengis.test.Validators} class provides static {@code validate(…)} methods that can be used for
 *       validating existing instances of various kinds. Those methods can be conveniently imported in a test class
 *       with the following Java statement:</p>
 *
 *       {@snippet lang="java" :
 *       import static org.opengis.test.Validators.*;
 *       }
 *
 *       <p>{@link org.opengis.test.Validator} and {@link org.opengis.test.ValidatorContainer} are support classes for
 *       the above, but usually do not need to be considered unless the validation process needs to be customized.</p></li>
 *
 *   <li><p><b>Assertions</b><br>
 *       {@link org.opengis.test.Assertions} provides some assertion methods.</p></li>
 *
 *   <li><p><b>Configuration</b><br>
 *       {@link org.opengis.test.Configuration}, {@link org.opengis.test.CalculationType} and
 *       {@link org.opengis.test.ToleranceModifier} allow implementers to alter the tests.</p></li>
 * </ul>
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @version 4.0
 * @since   2.2
 */
package org.opengis.test;
