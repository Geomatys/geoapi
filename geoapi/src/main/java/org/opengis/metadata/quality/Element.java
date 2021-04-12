/*
 *    GeoAPI - Java interfaces for OGC/ISO standards
 *    http://www.geoapi.org
 *
 *    Copyright (C) 2004-2021 Open Geospatial Consortium, Inc.
 *    All Rights Reserved. http://www.opengeospatial.org/ogc/legal
 *
 *    Permission to use, copy, and modify this software and its documentation, with
 *    or without modification, for any purpose and without fee or royalty is hereby
 *    granted, provided that you include the following on ALL copies of the software
 *    and documentation or portions thereof, including modifications, that you make:
 *
 *    1. The full text of this NOTICE in a location viewable to users of the
 *       redistributed or derivative work.
 *    2. Notice of any changes or modifications to the OGC files, including the
 *       date changes were made.
 *
 *    THIS SOFTWARE AND DOCUMENTATION IS PROVIDED "AS IS," AND COPYRIGHT HOLDERS MAKE
 *    NO REPRESENTATIONS OR WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
 *    TO, WARRANTIES OF MERCHANTABILITY OR FITNESS FOR ANY PARTICULAR PURPOSE OR THAT
 *    THE USE OF THE SOFTWARE OR DOCUMENTATION WILL NOT INFRINGE ANY THIRD PARTY
 *    PATENTS, COPYRIGHTS, TRADEMARKS OR OTHER RIGHTS.
 *
 *    COPYRIGHT HOLDERS WILL NOT BE LIABLE FOR ANY DIRECT, INDIRECT, SPECIAL OR
 *    CONSEQUENTIAL DAMAGES ARISING OUT OF ANY USE OF THE SOFTWARE OR DOCUMENTATION.
 *
 *    The name and trademarks of copyright holders may NOT be used in advertising or
 *    publicity pertaining to the software without specific, written prior permission.
 *    Title to copyright in this software and any associated documentation will at all
 *    times remain with copyright holders.
 */
package org.opengis.metadata.quality;

import java.util.Date;
import java.util.Collection;
import java.util.Collections;
import org.opengis.metadata.Identifier;
import org.opengis.metadata.citation.Citation;
import org.opengis.util.InternationalString;
import org.opengis.annotation.Classifier;
import org.opengis.annotation.Stereotype;
import org.opengis.annotation.UML;

import static org.opengis.annotation.Obligation.*;
import static org.opengis.annotation.Specification.*;


/**
 * Type of test applied to the data specified by a data quality scope.
 *
 * @author  Martin Desruisseaux (IRD)
 * @author  Cory Horner (Refractions Research)
 * @author  Alexis Gaillard (Geomatys)
 * @version 3.1
 * @since   2.0
 */
@Classifier(Stereotype.ABSTRACT)
@UML(identifier="DQ_Element", specification=ISO_19157)
public interface Element {
    /**
     * Name of the test applied to the data.
     *
     * @return name of the test applied to the data.
     *
     * @deprecated Replaced by {@link MeasureReference#getNamesOfMeasure()}.
     */
    @Deprecated
    @UML(identifier="nameOfMeasure", obligation=OPTIONAL, specification=ISO_19115, version=2003)
    default Collection<? extends InternationalString> getNamesOfMeasure() {
        MeasureReference ref = getMeasure();
        return (ref != Collections.emptyList()) ? ref.getNamesOfMeasure() : Collections.emptyList();
    }

    /**
     * Code identifying a registered standard procedure.
     *
     * @return code identifying a registered standard procedure
     *
     * @deprecated Replaced by {@link MeasureReference#getMeasureIdentification()}.
     */
    @Deprecated
    @UML(identifier="measureIdentification", obligation=OPTIONAL, specification=ISO_19115, version=2003)
    default Identifier getMeasureIdentification() {
        MeasureReference ref = getMeasure();
        return (ref != null) ? ref.getMeasureIdentification() : null;
    }

    /**
     * Description of the measure being determined.
     *
     * @return description of the measure being determined.
     *
     * @deprecated Replaced by {@link MeasureReference#getMeasureDescription()}.
     */
    @Deprecated
    @UML(identifier="measureDescription", obligation=OPTIONAL, specification=ISO_19115, version=2003)
    default InternationalString getMeasureDescription() {
        MeasureReference ref = getMeasure();
        return (ref != null) ? ref.getMeasureDescription() : null;
    }

    /**
     * Type of method used to evaluate quality of the dataset.
     *
     * @return type of method used to evaluate quality.
     *
     * @deprecated Replaced by {@link EvaluationMethod#getEvaluationMethodType()}.
     */
    @Deprecated
    @UML(identifier="evaluationMethodType", obligation=OPTIONAL, specification=ISO_19115, version=2003)
    default EvaluationMethodType getEvaluationMethodType() {
        EvaluationMethod ref = getEvaluationMethod();
        return (ref != null) ? ref.getEvaluationMethodType() : null;
    }

    /**
     * Description of the evaluation method.
     *
     * @return description of the evaluation method.
     *
     * @deprecated Replaced by {@link EvaluationMethod#getEvaluationMethodDescrition()}.
     */
    @Deprecated
    @UML(identifier="evaluationMethodDescription", obligation=OPTIONAL, specification=ISO_19115, version=2003)
    default InternationalString getEvaluationMethodDescription() {
        EvaluationMethod ref = getEvaluationMethod();
        return (ref != null) ? ref.getEvaluationMethodDescription() : null;
    }

    /**
     * Reference to the procedure information.
     *
     * @return reference to the procedure information.
     *
     * @deprecated Replaced by {@link EvaluationMethod#getEvaluationProcedure()}.
     */
    @Deprecated
    @UML(identifier="evaluationProcedure", obligation=OPTIONAL, specification=ISO_19115, version=2003)
    default Citation getEvaluationProcedure() {
        EvaluationMethod ref = getEvaluationMethod();
        return (ref != null) ? ref.getEvaluationProcedure() : null;
    }

    /**
     * Date or range of dates on which a data quality measure was applied.
     * The collection size is 1 for a single date, or 2 for a range.
     * Returns an empty collection if this information is not available.
     *
     * <div class="warning"><b>Upcoming API change — temporal schema</b><br>
     * The element type of this method may change in GeoAPI 4.0 release. It may be replaced by a
     * type matching more closely either ISO 19108 (<cite>Temporal Schema</cite>) or ISO 19103.
     * </div>
     *
     * @return date or range of dates on which a data quality measure was applied.
     *
     * @deprecated Replaced by {@link EvaluationMethod#getDates()}.
     */
    @Deprecated
    @UML(identifier="dateTime", obligation=OPTIONAL, specification=ISO_19115, version=2003)
    default Collection<? extends Date> getDates() {
        EvaluationMethod ref = getEvaluationMethod();
        return (ref != Collections.emptyList()) ? ref.getDates() : Collections.emptyList();
    }

    /**
     * Clause in the standaloneQualityReport where this data quality
     * element or any related data quality element (original results in case
     * of derivation or aggregation) is described.
     *
     * @return any data quality element is described.
     *
     * @since 3.1
     */
    @UML(identifier="standaloneQualityReportDetails", obligation=OPTIONAL, specification=ISO_19157)
    default InternationalString getStandaloneQualityReportDetails() {
        return null;
    }

    /**
     * Reference to measure used.
     *
     * @return returns the reference to the actual measure.
     *
     * @since 3.1
     */
    @UML(identifier="measure", obligation=OPTIONAL, specification=ISO_19157)
    default MeasureReference getMeasure() {
        return null;
    }

    /**
     * Evaluation information.
     *
     * @return returns the information about the current evaluation.
     *
     * @since 3.1
     */
    @UML(identifier="evaluationMethod", obligation=OPTIONAL, specification=ISO_19157)
    default EvaluationMethod getEvaluationMethod() {
        return null;
    }

    /**
     * Value (or set of values) obtained from applying a data quality measure or the
     * outcome of evaluating the obtained value (or set of values) against a specified
     * acceptable conformance quality level.
     *
     * @return set of values obtained from applying a data quality measure.
     */
    @UML(identifier="result", obligation=MANDATORY, specification=ISO_19157)
    Collection<? extends Result> getResults();

    /**
     * In case of aggregation or derivation, indicates the original element.
     *
     * @return the original element when there is an aggreagation or derivation.
     *
     * @since 3.1
     */
    @UML(identifier="derivedElement", obligation=OPTIONAL, specification=ISO_19157)
    default Collection<? extends Element> getDerivedElement() {
        return Collections.emptyList();
    }
}
