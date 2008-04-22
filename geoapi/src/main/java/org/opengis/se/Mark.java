/*$************************************************************************************************
 **
 ** $Id$
 **
 ** $URL$
 **
 ** Copyright (C) 2008 Open GIS Consortium, Inc.
 ** All Rights Reserved. http://www.opengis.org/legal/
 **
 *************************************************************************************************/
package org.opengis.se;

import org.opengis.filter.expression.Expression;
import org.opengis.annotation.XmlElement;


/**
 * Indicate that one of a few predefined shapes will be drawn at the points of the geometry.
 *
 * @version <A HREF="http://www.opengeospatial.org/standards/symbol">Symbology Encoding Implementation Specification 1.1.0</A>
 * @author Open Geospatial Consortium
 * @author Johann Sorel (Geomatys)
 * @author Chris Dillard (SYS Technologies)
 * @since GeoAPI 2.2
 */
@XmlElement("Mark")
public interface Mark extends GraphicSymbol {
    
    /**
     * Returns the expression whose value will indicate the symbol to draw.
     * The WellKnownName element gives the well-known name of the shape of the mark.
     * Allowed values include at least “square”, “circle”, “triangle”, “star”, “cross”, and “x”,
     * though map servers may draw a different symbol instead if they don't have a shape for all
     * of these. The default WellKnownName is “square”. Renderings of these marks may be
     * made solid or hollow depending on Fill and Stroke elements.
     * 
     * if the WellKnowname is null, check the ExternalMark before using the default square
     * symbol.
     * 
     * Both WellKnowName and ExternalMark canot be set, but both can be null.
     * If none are set then the default square symbol is used.
     * 
     * @return Expression or null
     */
    @XmlElement("WellKnownName")
    Expression getWellKnownName();

    /**
     * Sets the expression whose value will indicate the symbol to draw.
     * See {@link #getWellKnownName} for details.
     * @param name 
     */
    @XmlElement("WellKnownName")
    void setWellKnownName(Expression name);
    
    /**
     * The alternative to a WellKnownName is an external mark format.
     * See {@link ExternalMark} for details.
     * 
     * Both WellKnowName and ExternalMark canot be set, but both can be null.
     * If none are set then the default square symbol is used.
     * 
     * @return ExternalMark or null
     */
    ExternalMark getExternalMark();
    
    /**
     * Set the externalMark.
     * See {@link #getExternalMark} for details.
     * 
     * @param mark 
     */
    void setExternalMark(ExternalMark mark);
    
    /**
     * Returns the object that indicates how the mark should be filled.
     * Null means no fill.
     * @return Fill or null
     */
    @XmlElement("Fill")
    Fill getFill();

    /**
     * Sets the object that indicates how the mark should be filled.
     * See {@link #getFill} for details.
     * @param fill 
     */
    @XmlElement("Fill")
    void setFill(Fill fill);

    /**
     * Returns the object that indicates how the edges of the mark will be
     * drawn.  Null means that the edges will not be drawn at all.
     * 
     * @return stroke or null
     */
    @XmlElement("Stroke")
    Stroke getStroke();

    /**
     * Sets the object that indicates how the edges of the mark will be drawn.
     * See {@link #getStroke} for details.
     * @param stroke 
     */
    @XmlElement("Stroke")
    void setStroke(Stroke stroke);
    
}