/*
 *    GeoAPI - Java interfaces for OGC/ISO standards
 *    Copyright © 2022-2023 Open Geospatial Consortium, Inc.
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

import java.util.Collection;
import java.util.Collections;
import org.opengis.metadata.Identifier;
import org.opengis.util.InternationalString;


/**
 * A {@link MeasureReference} which delegates all methods to an existing {@link QualityMeasure} instance.
 * This is used when an {@link QualityElement} provides a full measure description instead of a reference
 * to a registry.
 *
 * <h2>Purpose</h2>
 * ISO 19157 provides no way to get a {@link QualityMeasure} from an {@link QualityElement} or any other data quality object.
 * The only association defined by the standard is a {@link MeasureReference} property inside {@link QualityElement}.
 * The standard expects that users will look for that reference in a registry or catalog for getting the full
 * {@link QualityMeasure} object. This approach makes XML documents smaller, but is not needed for Java interfaces.
 * GeoAPI extends ISO 19157 by allowing implementers to provide {@link QualityMeasure} objects directly.
 * This {@code MeasureReferenceToInstance} class makes the task easier for implementers who choose to provide a
 * {@link QualityMeasure} instead of a {@link MeasureReference}, by inferring automatically the latter from the former.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @version 3.1
 *
 * @see <a href="https://github.com/opengeospatial/geoapi/issues/75">Issue #75 on GitHub</a>
 *
 * @since 3.1
 */
final class MeasureInstanceReference implements MeasureReference {
    /**
     * The full measure object from which to derive a reference.
     */
    private final QualityMeasure measure;

    /**
     * Creates a new {@code MeasureReference} from the given full measure object.
     *
     * @param  measure  the full measure object from which to derive a reference.
     */
    MeasureInstanceReference(final QualityMeasure measure) {
        this.measure = measure;
    }

    /**
     * Returns the identifier of the measure.
     */
    @Override
    public Identifier getMeasureIdentification() {
        return measure.getMeasureIdentifier();
    }

    /**
     * Returns the name of the test applied to the data.
     */
    @Override
    public Collection<? extends InternationalString> getNamesOfMeasure() {
        InternationalString name = measure.getName();
        return (name != null) ? Collections.singletonList(name) : Collections.emptyList();
    }

    /**
     * Returns descriptions of the measure. This method delegates to the measure <em>definition</em>
     * instead of the measure description because the definition is a small text while the description
     * is very verbose, including formulas, which does not seem to be the intent of this method.
     * The examples given in annex E of ISO 19157:2013 contains the definitions, not the descriptions,
     * of standard measures defined in annex D.
     *
     *  @deprecated Replaced by {@link #getMeasuresDescription()} as of ISO 19157:2023.
     */
    @Override
    @Deprecated(forRemoval = true, since = "ISO 19157:2023")
    public InternationalString getMeasureDescription() {
        InternationalString def = measure.getDefinition();
        if (def == null) {
            final MeasureDescription description = measure.getDescription();
            if (description != null) {
                def = description.getTextDescription();
            }
        }
        return def;
    }

    //todo : validation of suggestion proposed: measure definition {@linkplain QualityMeasure.#getDefinitions()} is mandatory,
    // so it can not be null if specification is respected. Hence it is very unlikely that there is a need for calling
    // {@linkplain QualityMeasure.#getDescriptions()} and it seems logical to return directly this method result.
    // If accepted, this method should be renamed to override {@linkplain MeasureReference.#getMeasureDescription()}.
    /**
     * Returns descriptions of the measure. This method delegates to the measure <em>definition</em>
     * instead of the measure description because the definition is a small text while the description
     * is very verbose, including formulas, which does not seem to be the intent of this method.
     * The examples given in annex E of ISO 19157:2013 contains the definitions, not the descriptions,
     * of standard measures defined in annex D.
     */
    public InternationalString getMeasuresDescription() {
        return measure.getDefinition();
    }

    /**
     * Compares this reference with the given object for equality.
     */
    @Override
    public boolean equals(final Object other) {
        return (other instanceof MeasureInstanceReference) &&
                measure.equals(((MeasureInstanceReference) other).measure);
    }

    /**
     * Returns a hash code value for this measure reference.
     */
    @Override
    public int hashCode() {
        return measure.hashCode() ^ -786366399;
    }

    /**
     * Returns a string representation of this measure reference for debugging purposes.
     */
    @Override
    public String toString() {
        String code = null;
        Identifier id = getMeasureIdentification();
        if (id != null) {
            code = id.getCode();
        }
        if (code == null) {
            InternationalString name = measure.getName();
            if (name != null) code = name.toString();
        }
        final StringBuilder b = new StringBuilder("MeasureReference");
        if (code != null) {
            b.append("[\"").append(code).append("\"]");
        } else {
            b.append("[]");
        }
        return b.toString();
    }
}
