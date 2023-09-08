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

import java.util.*;
import java.time.Instant;
import java.time.temporal.ChronoField;
import java.time.temporal.Temporal;
import org.opengis.metadata.Identifier;
import org.opengis.metadata.citation.Citation;
import org.opengis.util.InternationalString;
import org.opengis.annotation.Classifier;
import org.opengis.annotation.Stereotype;
import org.opengis.annotation.UML;

import static org.opengis.annotation.Obligation.*;
import static org.opengis.annotation.Specification.*;


/**
 * Aspect of quantitative quality information.
 * A data quality element is a component describing a certain aspect of the quality of geographic data.
 * An evaluation of a data quality element is described by the following:
 * <ul>
 *   <li>{@linkplain #getMeasureReference() Measure}: the type of evaluation;</li>
 *   <li>{@linkplain #getEvaluationMethod() Evaluation method}: the procedure used to evaluate the measure;</li>
 *   <li>{@linkplain #getQualityResults() Result:} the output of the evaluation.</li>
 * </ul>
 *
 * Elements are organized into different categories, which are identified by the following subtypes:
 * {@link Completeness}, {@link LogicalConsistency}, {@link PositionalAccuracy}, {@link TemporalQuality},
 * {@link ThematicAccuracy}, or {@link Metaquality}.
 *
 * @author  Martin Desruisseaux (IRD)
 * @author  Cory Horner (Refractions Research)
 * @author  Alexis Gaillard (Geomatys)
 * @version 3.1
 * @since   2.0
 */
@Classifier(Stereotype.ABSTRACT)
@UML(identifier="QualityElement", specification=ISO_19157)
public interface QualityElement {

    /**
     * Full description of a data quality measure.
     *
     * @departure easeOfUse
     *   This method is not part of ISO 19157 specification.
     *   Instead, the standard provides only a {@link MeasureReference}
     *   that clients can use for finding the full measure description in a measure register or catalogue.
     *   Because Java interfaces can execute code (as opposed to static data encoded in XML or JSON documents),
     *   implementers are free to do themselves the work of fetching this information from an external source
     *   when {@code getMeasure()} is invoked. This method is added in the {@link QualityElement} interface for making
     *   that feature possible. This is an optional feature; implementers can ignore this method and implement
     *   only the {@link #getMeasureReference()} method.
     *
     * @return a measure of data quality, or {@code null} if none.
     *
     * @since 3.1
     */
    default QualityMeasure getMeasure() {
        return null;
    }

    /**
     * Identifier of a measure fully described elsewhere.
     * The full description is given by {@link #getMeasure()},
     * but that description may not be available to this {@code QualityElement}.
     * Instead, the whole description may be found within a measure register or catalogue,
     * in which case this reference can be used for finding the whole description.
     *
     * <p>If a full measure is {@linkplain #getMeasure() contained in this element},
     * then by default this method returns the {@linkplain QualityMeasure#getName() name},
     * {@linkplain QualityMeasure#getMeasureIdentifier() identifier} and
     * {@linkplain QualityMeasure#getDefinition() definition} of that measure.</p>
     *
     * @departure rename
     *   The ISO 19157 property name is {@code measure}.
     *   This is renamed {@code measureReference} in GeoAPI for reflecting the return type
     *   and for making room for a {@code measure} property for the full {@link QualityMeasure} description.
     *
     * @return reference to the measure used.
     *
     * @since 3.1
     */
    @UML(identifier="measure", obligation=MANDATORY, specification=ISO_19157)
    MeasureReference getMeasureReference();

    /**
     * Evaluation information, recognising that there can be a collection of methods.
     *
     * @return information about the evaluation method.
     *
     * @since 4.0
     */
    @UML(identifier="evaluationMethod", obligation=MANDATORY, specification=ISO_19157)
    Collection<? extends EvaluationMethod> getEvaluationMethods();

    /**
     * Value (or set of values) obtained from applying a data quality measure.
     * May be an outcome of evaluating the obtained value (or set of values)
     * against a specified acceptable conformance quality level.
     *
     * @return set of values obtained from applying a data quality measure.
     */
    @UML(identifier="result", obligation=MANDATORY, specification=ISO_19157)
    Collection<? extends QualityResult> getQualityResults();

    /**
     * In case of aggregation or derivation, indicates the original elements.
     *
     * @return original element(s) when there is an aggregation or derivation, or {@code null}.
     *
     * @since 3.1
     */
    @UML(identifier="derivedElement", obligation=OPTIONAL, specification=ISO_19157)
    default Collection<? extends QualityElement> getDerivedElements() {
        return Collections.emptyList();
    }
}
