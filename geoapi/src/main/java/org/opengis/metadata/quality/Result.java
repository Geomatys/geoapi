/*
 *    GeoAPI - Java interfaces for OGC/ISO standards
 *    Copyright © 2004-2023 Open Geospatial Consortium, Inc.
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
package org.opengis.metadata.quality;

import org.opengis.annotation.UML;
import org.opengis.annotation.Classifier;
import org.opengis.annotation.Stereotype;
import static org.opengis.annotation.Specification.*;


/**
 * Base interface of more specific result classes.
 * At least one data quality result shall be provided for each {@linkplain Element data quality element}.
 * Different types of results can be provided for the same data quality elements. This could be
 * a {@linkplain QuantitativeResult quantitative result},
 * a {@linkplain ConformanceResult conformance result},
 * a {@linkplain DescriptiveResult descriptive result} or
 * a {@linkplain CoverageResult coverage result}.
 *
 * @author  Martin Desruisseaux (IRD)
 * @author  Alexis Gaillard (Geomatys)
 * @version 3.1
 *
 * @see Element#getResults()
 *
 * @since 2.0
 *
 * @deprecated Renamed {@link QualityResult}.
 */
@Deprecated
@Classifier(Stereotype.ABSTRACT)
@UML(identifier="Result", specification=ISO_19157, version=2013)
public interface Result extends QualityResult {
}