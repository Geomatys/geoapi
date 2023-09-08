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

import static org.opengis.annotation.Obligation.*;
import static org.opengis.annotation.Specification.*;


/**
 * Data quality measure.
 *
 * <h2>Where measures are stored</h2>
 * Measures may be verbose and may not be of interest when only the {@linkplain Element#getResults() result}
 * of data quality measures is desired. For allowing more compact {@link Element}s, ISO 19157 does not store
 * {@code Measure} instance directly into {@link Element}, but instead stores {@link MeasureReference} which
 * can be used for fetching full {@link Measure} description from a measure register or catalogue if desired.
 *
 * <p>GeoAPI extends the ISO 19157 model by allowing {@link Element} to provide directly a {@link Measure} instance.
 * This optional feature gives access to full measure description without forcing users to connect to a catalogue or
 * measure registry. Implementers can fetch the measure description only when first requested, for example by
 * connecting themselves to a catalogue when {@link Element#getMeasure()} is first invoked.</p>
 *
 * @author  Alexis Gaillard (Geomatys)
 * @author  Martin Desruisseaux (Geomatys)
 * @version 3.1
 *
 * @see MeasureReference
 * @see Element#getMeasure()
 *
 * @since 3.1
 *
 * @deprecated Renamed {@link QualityMeasure}.
 */
@Deprecated
@UML(identifier="Measure", specification=ISO_19157, version=2013)
public interface Measure extends QualityMeasure {
    /**
     * Description of the data quality measure.
     * Includes methods of calculation, with all formulae and/or illustrations
     * needed to establish the result of applying the measure.
     *
     * <p>If the measure uses the concept of errors, it should be stated how an item is classified as incorrect.
     * This is the case when the quality only can be reported as correct or incorrect.</p>
     *
     * @condition mandatory if the {@linkplain #getDefinition() definition} is not sufficient
     *            for the understanding of the data quality measure concept.
     *
     * @return description of data quality measure, or {@code null} if none.
     *
     * @see MeasureReference#getMeasureDescription()
     *
     * @deprecated Replaced by {@link #getDescriptions()} as of ISO 19157:2023.
     */
    @Deprecated
    @UML(identifier="description", obligation=CONDITIONAL, specification=ISO_19157, version = 2013)
    Description getDescription();

}