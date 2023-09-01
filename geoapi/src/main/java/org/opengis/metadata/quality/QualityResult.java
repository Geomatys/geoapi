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

import java.time.temporal.Temporal;
import org.opengis.annotation.UML;
import org.opengis.annotation.Classifier;
import org.opengis.annotation.Stereotype;
import org.opengis.metadata.maintenance.Scope;

import static org.opengis.annotation.Obligation.*;
import static org.opengis.annotation.Specification.*;


/**
 * Base interface of more specific result classes.
 * At least one data quality result shall be provided for each {@linkplain QualityElement data quality element}.
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
 * @see QualityElement#getQualityResults()
 *
 * @since 2.0
 */
@Classifier(Stereotype.ABSTRACT)
@UML(identifier="QualityResult", specification=ISO_19157)
public interface QualityResult {
    /**
     * Scope of the result.
     * Quality frequently differs between various parts of the data set for which quality is evaluated.
     * Therefore several evaluations may be applied for the same {@linkplain QualityElement data quality element}.
     * To avoid repeating the measure and evaluation procedure descriptions in several instances of {@link QualityElement},
     * several results with individual result scopes can be used.
     *
     * @return scope of the result, or {@code null} if unspecified.
     *
     * @see DataQuality#getScope()
     *
     * @since 3.1
     */
    @UML(identifier="resultScope", obligation=OPTIONAL, specification=ISO_19157)
    default Scope getResultScope() {
        return null;
    }

    /**
     * Date when the result was generated.
     * This is typically a {@link java.time.LocalDate}, {@link java.time.LocalDateTime}
     * or {@link java.time.ZonedDateTime} depending on whether the hour of the day and
     * the time zone are provided.
     *
     * @return date of the result, or {@code null} if none.
     *
     * @since 3.1
     */
    @UML(identifier="dateTime", obligation=OPTIONAL, specification=ISO_19157)
    default Temporal getDateTime() {
        return null;
    }
}
