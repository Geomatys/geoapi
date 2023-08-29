/*
 *    GeoAPI - Java interfaces for OGC/ISO standards
 *    Copyright © 2019-2023 Open Geospatial Consortium, Inc.
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
import org.opengis.metadata.citation.Citation;
import org.opengis.util.InternationalString;

import java.util.Collection;

import static org.opengis.annotation.Obligation.*;
import static org.opengis.annotation.Specification.*;


/**
 * Reference to a quality evaluation report.
 * In order to provide more details than reported as metadata,
 * a quality evaluation report may additionally be created.
 * If it is created, it should be cited by the metadata and should provide a reference to the quality evaluation report,
 * either as a citation or as an online resource.
 * Its structure is free. However, the quality evaluation report shall not replace the metadata.
 *
 * @author  Alexis Gaillard (IRD)
 * @author  Martin Desruisseaux (Geomatys)
 * @version 3.1
 * @since   3.1
 *
 */
@UML(identifier="QualityEvaluationReportInformation", specification=ISO_19157)
public interface QualityEvaluationReportInformation {
    /**
     * Reference to the associated quality evaluation report.
     *
     * @return reference of the quality evaluation report.
     */
    @UML(identifier="reportReference", obligation=MANDATORY, specification=ISO_19157)
    Citation getReportReference();

    /**
     * Abstract for the associated quality evaluation report.
     *
     * @return abstract of the quality evaluation report.
     */
    @UML(identifier="abstract", obligation=MANDATORY, specification=ISO_19157)
    InternationalString getAbstract();

    /**
     * Reference to original results in the associated quality evaluation report.
     *
     * @since 4.0
     *
     * @return details of the quality evaluation report.
     */
    @UML(identifier="qualityEvaluationReportDetails", obligation=OPTIONAL, specification=ISO_19157)
    default Collection<? extends InternationalString> getQualityEvaluationReportDetails() {
        return null;
    }

    /**
     * Quality information for each quality element mentioned in the quality evaluation report.
     *
     * @since 4.0
     *
     * @return quality element for the data covered in the report(s).
     */
    @UML(identifier="elementReport", obligation=OPTIONAL, specification=ISO_19157)
    default Collection<? extends QualityElement> getElementReports() {
        return null;
    }
}
