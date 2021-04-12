/*
 *    GeoAPI - Java interfaces for OGC/ISO standards
 *    http://www.geoapi.org
 *
 *    Copyright (C) 2021 Open Geospatial Consortium, Inc.
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

import java.util.Collection;
import org.opengis.util.InternationalString;
import org.opengis.util.TypeName;
import org.opengis.annotation.UML;
import org.opengis.metadata.Identifier;

import static org.opengis.annotation.Obligation.*;
import static org.opengis.annotation.Specification.*;


/**
 * Data quality measure.
 *
 * @author  Alexis Gaillard (Geomatys)
 * @author  Martin Desruisseaux (Geomatys)
 * @version 3.1
 * @since   3.1
 */
@UML(identifier="DQM_Measure", specification=ISO_19157)
public interface Measure {
    /**
     * Value uniquely identifying the measure within a namespace.
     *
     * @return returns the value identifying the measure.
     */
    @UML(identifier="measureIdentifier", obligation=MANDATORY, specification=ISO_19157)
    Identifier getMeasureIdentifier();

    /**
     * Name of the data quality measure applied to the data.
     *
     * @return returns the name of data quality measure.
     */
    @UML(identifier="Name", obligation=MANDATORY, specification=ISO_19157)
    InternationalString getName();

    /**
     * Another recognized name, an abbreviation or a short name for the same data quality measure.
     *
     * @return an abbreviation for the same data quality measure.
     */
    @UML(identifier="alias", obligation=OPTIONAL, specification=ISO_19157)
    default InternationalString getAlias() {
        return null;
    }

    /**
     * Name of the data quality element for which quality is reported.
     *
     * @return returns the name of the data quality element.
     */
    @UML(identifier="elementName", obligation=MANDATORY, specification=ISO_19157)
    TypeName getElementName();

    /**
     * Definition of the fundamental concept for the data quality measure.
     *
     * @return returns the definition of fundamental concept .
     */
    @UML(identifier="definition", obligation=MANDATORY, specification=ISO_19157)
    InternationalString getDefinition();

    /**
     * Description of the data quality measure, including all formulae and/
     * or illustrations needed to establish the result of applying the measure.
     *
     * @return returns the description of data quality measure.
     */
    @UML(identifier="description", obligation=CONDITIONAL, specification=ISO_19157)
    Description getDescription();

    /**
     * Value type for reporting a data quality result (shall be one of the data types defined in ISO/19103:2005).
     *
     * @return value type for reporting a data quality result.
     */
    @UML(identifier="valueType", obligation=MANDATORY, specification=ISO_19157)
    TypeName getValueType();

    /**
     * Structure for reporting a complex data quality result.
     *
     * @return structure about a complex data quality result.
     */
    @UML(identifier="valueStructure", obligation=OPTIONAL, specification=ISO_19157)
    default ValueStructure getValueStructure() {
        return null;
    }

    /**
     * Illustration of the use of a data quality measure.
     *
     * @return illustration of what a data quality measure is used for.
     */
    @UML(identifier="example", obligation=OPTIONAL, specification=ISO_19157)
    default Collection<? extends Description> getExample() {
        return null;
    }

    /**
     * Definition of the fundamental concept for the data quality measure.
     *
     * @return definition of fundamental concept.
     */
    @UML(identifier="basicMeasure", obligation=CONDITIONAL, specification=ISO_19157)
    BasicMeasure getBasicMeasure();

    /**
     * Reference to the source of an item that has been adopted from an external source.
     *
     * @return reference the source of an item.
     */
    @UML(identifier="sourceReference", obligation=CONDITIONAL, specification=ISO_19157)
    Collection<? extends SourceReference> getSourceReference();

    /**
     * Auxiliary variable used by the data quality measure, including its name, definition and optionally its description.
     *
     * @return variable used by data quality mesure.
     */
    @UML(identifier="parameter", obligation=CONDITIONAL, specification=ISO_19157)
    Collection<? extends Parameter> getParameter();
}
