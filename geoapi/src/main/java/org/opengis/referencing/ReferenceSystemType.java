/*
 *    GeoAPI - Java interfaces for OGC/ISO standards
 *    Copyright © 2014-2024 Open Geospatial Consortium, Inc.
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
package org.opengis.referencing;

import java.util.List;
import org.opengis.annotation.UML;
import org.opengis.util.CodeList;
import org.opengis.referencing.cs.CartesianCS;
import org.opengis.referencing.cs.CoordinateSystem;
import org.opengis.referencing.cs.EllipsoidalCS;
import org.opengis.referencing.crs.CompoundCRS;
import org.opengis.referencing.crs.EngineeringCRS;
import org.opengis.referencing.crs.GeodeticCRS;
import org.opengis.referencing.crs.ParametricCRS;
import org.opengis.referencing.crs.ProjectedCRS;
import org.opengis.referencing.crs.SingleCRS;
import org.opengis.referencing.crs.TemporalCRS;
import org.opengis.referencing.crs.VerticalCRS;
import org.opengis.geoapi.internal.Vocabulary;

import static org.opengis.annotation.Obligation.*;
import static org.opengis.annotation.Specification.ISO_19115;


/**
 * Defines type of reference system used.
 *
 * @author  ISO 19115 (for abstract model and documentation)
 * @author  Rémi Maréchal (Geomatys)
 * @version 3.1
 *
 * @see ReferenceSystem#getReferenceSystemType()
 *
 * @since 3.1
 */
@Vocabulary(capacity=28)
@UML(identifier="MD_ReferenceSystemTypeCode", specification=ISO_19115)
public final class ReferenceSystemType extends CodeList<ReferenceSystemType> {
    /**
     * Serial number for compatibility with different versions.
     */
    private static final long serialVersionUID = 5574086630226193267L;

    /**
     * Compound spatio-parametric coordinate reference system containing an
     * engineering coordinate reference system and a parametric reference system.
     *
     * <div class="note"><b>Example:</b> x, y, pressure.</div>
     */
    @UML(identifier="compoundEngineeringParametric", obligation=CONDITIONAL, specification=ISO_19115)
    public static final ReferenceSystemType COMPOUND_ENGINEERING_PARAMETRIC =
            new ReferenceSystemType("COMPOUND_ENGINEERING_PARAMETRIC", (byte) 0, null,
                                    EngineeringCRS.class, ParametricCRS.class);

    /**
     * Compound spatio-parametric-temporal coordinate reference system containing an
     * engineering, a parametric and a temporal coordinate reference system.
     *
     * <div class="note"><b>Example:</b> x, y, pressure, time.</div>
     */
    @UML(identifier="compoundEngineeringParametricTemporal", obligation=CONDITIONAL, specification=ISO_19115)
    public static final ReferenceSystemType COMPOUND_ENGINEERING_PARAMETRIC_TEMPORAL =
            new ReferenceSystemType("COMPOUND_ENGINEERING_PARAMETRIC_TEMPORAL", (byte) 0, null,
                                    EngineeringCRS.class, ParametricCRS.class, TemporalCRS.class);

    /**
     * Compound spatio-temporal coordinate reference system containing an
     * engineering coordinate reference system and a temporal reference system.
     *
     * <div class="note"><b>Example:</b> x, y, time.</div>
     */
    @UML(identifier="compoundEngineeringTemporal", obligation=CONDITIONAL, specification=ISO_19115)
    public static final ReferenceSystemType COMPOUND_ENGINEERING_TEMPORAL =
            new ReferenceSystemType("COMPOUND_ENGINEERING_TEMPORAL", (byte) 0, null,
                                    EngineeringCRS.class, TemporalCRS.class);

    /**
     * Compound spatial reference system containing a horizontal engineering
     * coordinate reference system and a vertical coordinate reference system.
     *
     * <div class="note"><b>Example:</b> x, y, height.</div>
     */
    @UML(identifier="compoundEngineeringVertical", obligation=CONDITIONAL, specification=ISO_19115)
    public static final ReferenceSystemType COMPOUND_ENGINEERING_VERTICAL =
            new ReferenceSystemType("COMPOUND_ENGINEERING_VERTICAL", (byte) 0, null,
                                    EngineeringCRS.class, VerticalCRS.class);

    /**
     * Compound spatio-temporal coordinate reference system containing an
     * engineering, a vertical, and a temporal coordinate reference system.
     *
     * <div class="note"><b>Example:</b> x, y, height, time.</div>
     */
    @UML(identifier="compoundEngineeringVerticalTemporal", obligation=CONDITIONAL, specification=ISO_19115)
    public static final ReferenceSystemType COMPOUND_ENGINEERING_VERTICAL_TEMPORAL =
            new ReferenceSystemType("COMPOUND_ENGINEERING_VERTICAL_TEMPORAL", (byte) 0, null,
                                    EngineeringCRS.class, VerticalCRS.class, TemporalCRS.class);

    /**
     * Compound spatio-parametric coordinate reference system containing a 2D
     * geographic horizontal coordinate reference system and a parametric reference system.
     *
     * <div class="note"><b>Example:</b> latitude, longitude, pressure.</div>
     */
    @UML(identifier="compoundGeographic2DParametric", obligation=CONDITIONAL, specification=ISO_19115)
    public static final ReferenceSystemType COMPOUND_GEOGRAPHIC2D_PARAMETRIC =
            new ReferenceSystemType("COMPOUND_GEOGRAPHIC2D_PARAMETRIC", (byte) 2, EllipsoidalCS.class,
                                    GeodeticCRS.class, ParametricCRS.class);

    /**
     * Compound spatio-parametric-temporal coordinate reference system containing a 2D
     * geographic horizontal, a parametric and a temporal coordinate reference system.
     *
     * <div class="note"><b>Example:</b> latitude, longitude, pressure, time.</div>
     */
    @UML(identifier="compoundGeographic2DParametricTemporal", obligation=CONDITIONAL, specification=ISO_19115)
    public static final ReferenceSystemType COMPOUND_GEOGRAPHIC2D_PARAMETRIC_TEMPORAL =
            new ReferenceSystemType("COMPOUND_GEOGRAPHIC2D_PARAMETRIC_TEMPORAL", (byte) 2, EllipsoidalCS.class,
                                    GeodeticCRS.class, ParametricCRS.class, TemporalCRS.class);

    /**
     * Compound spatio-temporal coordinate reference system containing a 2D geographic horizontal
     * coordinate reference system and a temporal reference system.
     *
     * <div class="note"><b>Example:</b> latitude, longitude, time.</div>
     */
    @UML(identifier="compoundGeographic2DTemporal", obligation=CONDITIONAL, specification=ISO_19115)
    public static final ReferenceSystemType COMPOUND_GEOGRAPHIC2D_TEMPORAL =
            new ReferenceSystemType("COMPOUND_GEOGRAPHIC2D_TEMPORAL", (byte) 2, EllipsoidalCS.class,
                                    GeodeticCRS.class, TemporalCRS.class);

    /**
     * Compound coordinate reference system in which one constituent coordinate
     * reference system is a horizontal geodetic coordinate reference system
     * and one is a vertical coordinate reference system.
     *
     * <div class="note"><b>Example:</b> latitude, longitude, [gravity related] height or depth.</div>
     */
    @UML(identifier="compoundGeographic2DVertical", obligation=CONDITIONAL, specification=ISO_19115)
    public static final ReferenceSystemType COMPOUND_GEOGRAPHIC2D_VERTICAL =
            new ReferenceSystemType("COMPOUND_GEOGRAPHIC2D_VERTICAL", (byte) 2, EllipsoidalCS.class,
                                    GeodeticCRS.class, VerticalCRS.class);

    /**
     * Compound spatio-temporal coordinate reference system containing a 2D
     * geographic horizontal, a vertical, and a temporal coordinate reference system.
     *
     * <div class="note"><b>Example:</b> latitude, longitude, height, time.</div>
     */
    @UML(identifier="compoundGeographic2DVerticalTemporal", obligation=CONDITIONAL, specification=ISO_19115)
    public static final ReferenceSystemType COMPOUND_GEOGRAPHIC2D_VERTICAL_TEMPORAL =
            new ReferenceSystemType("COMPOUND_GEOGRAPHIC2D_VERTICAL_TEMPORAL", (byte) 2, EllipsoidalCS.class,
                                    GeodeticCRS.class, VerticalCRS.class, TemporalCRS.class);

    /**
     * Compound spatio-temporal coordinate reference system containing a 3D
     * geographic and temporal coordinate reference system.
     *
     * <div class="note"><b>Example:</b> latitude, longitude, ellipsoidal height, time.</div>
     */
    @UML(identifier="compoundGeographic3DTemporal", obligation=CONDITIONAL, specification=ISO_19115)
    public static final ReferenceSystemType COMPOUND_GEOGRAPHIC3D_TEMPORAL =
            new ReferenceSystemType("COMPOUND_GEOGRAPHIC3D_TEMPORAL", (byte) 3, EllipsoidalCS.class,
                                    GeodeticCRS.class, TemporalCRS.class);

    /**
     * Compound spatio-parametric coordinate reference system containing a
     * projected horizontal coordinate reference system and a parametric reference system.
     *
     * <div class="note"><b>Example:</b> easting, northing, density.</div>
     */
    @UML(identifier="compoundProjected2DParametric", obligation=CONDITIONAL, specification=ISO_19115)
    public static final ReferenceSystemType COMPOUND_PROJECTED2D_PARAMETRIC =
            new ReferenceSystemType("COMPOUND_PROJECTED2D_PARAMETRIC", (byte) 2, null,
                                    ProjectedCRS.class, ParametricCRS.class);

    /**
     * Compound statio-parametric-temporal coordinate reference system containing
     * a projected horizontal, a parametric, and a temporal coordinate reference system.
     *
     * <div class="note"><b>Example:</b> easting, northing, density, time.</div>
     */
    @UML(identifier="compoundProjected2DParametricTemporal", obligation=CONDITIONAL, specification=ISO_19115)
    public static final ReferenceSystemType COMPOUND_PROJECTED2D_PARAMETRIC_TEMPORAL =
            new ReferenceSystemType("COMPOUND_PROJECTED2D_PARAMETRIC_TEMPORAL", (byte) 2, null,
                                    ProjectedCRS.class, ParametricCRS.class, TemporalCRS.class);

    /**
     * Compound spatio-temporal reference system containing a projected horizontal and a temporal coordinate
     * reference system.
     *
     * <div class="note"><b>Example:</b> easting, northing, time.</div>
     */
    @UML(identifier="compoundProjectedTemporal", obligation=CONDITIONAL, specification=ISO_19115)
    public static final ReferenceSystemType COMPOUND_PROJECTED_TEMPORAL =
            new ReferenceSystemType("COMPOUND_PROJECTED_TEMPORAL", (byte) 0, null,
                                    ProjectedCRS.class, TemporalCRS.class);

    /**
     * Compound spatial reference system containing a horizontal projected coordinate
     * reference system and a vertical coordinate reference.
     *
     * <div class="note"><b>Example:</b> easting, northing, height or depth.</div>
     */
    @UML(identifier="compoundProjectedVertical", obligation=CONDITIONAL, specification=ISO_19115)
    public static final ReferenceSystemType COMPOUND_PROJECTED_VERTICAL =
            new ReferenceSystemType("COMPOUND_PROJECTED_VERTICAL", (byte) 2, null,
                                    ProjectedCRS.class, VerticalCRS.class);

    /**
     * Compound spatio-temporal coordinate reference system containing a projected horizontal,
     * a vertical, and a temporal coordinate reference system.
     *
     * <div class="note"><b>Example:</b> easting, northing, height, time.</div>
     */
    @UML(identifier="compoundProjectedVerticalTemporal", obligation=CONDITIONAL, specification=ISO_19115)
    public static final ReferenceSystemType COMPOUND_PROJECTED_VERTICAL_TEMPORAL =
            new ReferenceSystemType("COMPOUND_PROJECTED_VERTICAL_TEMPORAL", (byte) 2, null,
                                    ProjectedCRS.class, VerticalCRS.class, TemporalCRS.class);

    /**
     * Coordinate reference system based on an engineering datum (datum describing
     * the relationship of a coordinate system to a local reference).
     */
    @UML(identifier="engineering", obligation=CONDITIONAL, specification=ISO_19115)
    public static final ReferenceSystemType ENGINEERING=
            new ReferenceSystemType("ENGINEERING", (byte) 0, null, EngineeringCRS.class);

    /**
     * Engineering coordinate reference system in which the base representation
     * of a moving object is specified.
     */
    @UML(identifier="engineeringDesign", obligation=CONDITIONAL, specification=ISO_19115)
    public static final ReferenceSystemType ENGINEERING_DESIGN =
            new ReferenceSystemType("ENGINEERING_DESIGN");

    /**
     * Coordinate reference system based on an image datum (engineering datum which
     * defines the relationship of a coordinate reference system to an image).
     *
     * <div class="note"><b>Example:</b> row, column.</div>
     */
    @UML(identifier="engineeringImage", obligation=CONDITIONAL, specification=ISO_19115)
    public static final ReferenceSystemType ENGINEERING_IMAGE =
            new ReferenceSystemType("ENGINEERING_IMAGE");

    /**
     * Geodetic CRS having a 3D Cartesian coordinate system.
     *
     * <div class="note"><b>Example:</b> [geocentric] X, Y, Z.</div>
     */
    @UML(identifier="geodeticGeocentric", obligation=CONDITIONAL, specification=ISO_19115)
    public static final ReferenceSystemType GEODETIC_GEOCENTRIC =
            new ReferenceSystemType("GEODETIC_GEOCENTRIC", (byte) 3, CartesianCS.class, GeodeticCRS.class);

    /**
     * Geodetic CRS having an ellipsoidal 2D coordinate system.
     */
    @UML(identifier="geodeticGeographic2D", obligation=CONDITIONAL, specification=ISO_19115)
    public static final ReferenceSystemType GEODETIC_GEOGRAPHIC2D =
            new ReferenceSystemType("GEODETIC_GEOGRAPHIC2D", (byte) 2, EllipsoidalCS.class, GeodeticCRS.class);

    /**
     * Geodetic CRS having an ellipsoidal 3D coordinate system.
     */
    @UML(identifier="geodeticGeographic3D", obligation=CONDITIONAL, specification=ISO_19115)
    public static final ReferenceSystemType GEODETIC_GEOGRAPHIC3D =
            new ReferenceSystemType("GEODETIC_GEOGRAPHIC3D", (byte) 3, EllipsoidalCS.class, GeodeticCRS.class);

    /**
     * Spatial reference in the form of a label or code that identifies a location.
     *
     * <div class="note"><b>Example:</b> post code.</div>
     */
    @UML(identifier="geographicIdentifier", obligation=CONDITIONAL, specification=ISO_19115)
    public static final ReferenceSystemType GEOGRAPHIC_IDENTIFIER =
            new ReferenceSystemType("GEOGRAPHIC_IDENTIFIER");   // TODO: ISO 19112

    /**
     * Set of Linear Referencing Methods and the policies, records and procedures
     * for implementing them reference system that identifies a location by reference
     * to a segment of a linear geographic feature and distance along that segment from a given point.
     *
     * <div class="note"><b>Example:</b> <var>x</var> km along road.</div>
     */
    @UML(identifier="linear", obligation=CONDITIONAL, specification=ISO_19115)
    public static final ReferenceSystemType LINEAR =
            new ReferenceSystemType("LINEAR");

    /**
     * Coordinate reference system based on a parametric datum (datum describing
     * the relationship of parametric coordinate reference system to an object).
     *
     * <div class="note"><b>Example:</b> pressure.</div>
     */
    @UML(identifier="parametric", obligation=CONDITIONAL, specification=ISO_19115)
    public static final ReferenceSystemType PARAMETRIC =
            new ReferenceSystemType("PARAMETRIC", (byte) 0, null, ParametricCRS.class);

    /**
     * Coordinate reference system derived from a two dimensional geodetic coordinate
     * reference system by applying a map projection.
     *
     * <div class="note"><b>Example:</b> easting, northing.</div>
     */
    @UML(identifier="projected", obligation=CONDITIONAL, specification=ISO_19115)
    public static final ReferenceSystemType PROJECTED =
            new ReferenceSystemType("PROJECTED", (byte) 0, null, ProjectedCRS.class);

    /**
     * Reference system against which time is measured.
     *
     * <div class="note"><b>Example:</b> time.</div>
     */
    @UML(identifier="temporal", obligation=CONDITIONAL, specification=ISO_19115)
    public static final ReferenceSystemType TEMPORAL =
            new ReferenceSystemType("TEMPORAL", (byte) 0, null, TemporalCRS.class);

    /**
     * One-dimensional coordinate reference system based on a vertical datum.
     * Vertical datums describe the relation of gravity-related heights or depths to the planet.
     *
     * <div class="note"><b>Example:</b> height or depths.</div>
     */
    @UML(identifier="vertical", obligation=CONDITIONAL, specification=ISO_19115)
    public static final ReferenceSystemType VERTICAL =
            new ReferenceSystemType("VERTICAL", (byte) 0, null, VerticalCRS.class);

    /**
     * Number of dimensions of the first component (usually geographic or projected), or 0 for any.
     */
    private final byte dimOfFirst;

    /**
     * Type of coordinate system of the first component, or {@code null} for any.
     * May also be null because it is unnecessary to check the coordinate system.
     */
    private final Class<? extends CoordinateSystem> csOfFirst;

    /**
     * Interface implemented by each component (in order), or {@code null} if unknown.
     * If the array length is 1, then this is the type of the CRS itself.
     */
    private final Class<?>[] components;

    /**
     * Constructs an element of the given name and unknown type.
     *
     * @param name  the name of the new element. This name shall not be in use by another element of this type.
     */
    private ReferenceSystemType(final String name) {
        super(name);
        dimOfFirst = 0;
        csOfFirst  = null;
        components = null;
    }

    /**
     * Constructs an element of the given name.
     *
     * @param name        the name of the new element. This name shall not be in use by another element of this type.
     * @param dimOfFirst  number of dimensions of the first component (usually geographic or projected), or 0 for any.
     * @param csOfFirst   type of coordinate system of the first component, or {@code null} for any.
     * @param components  interface implemented by each component (in order), or {@code null} if unknown.
     */
    private ReferenceSystemType(final String name, final byte dimOfFirst,
            final Class<? extends CoordinateSystem> csOfFirst, final Class<?>... components)
    {
        super(name);
        this.dimOfFirst = dimOfFirst;
        this.csOfFirst  = csOfFirst;
        this.components = components;
    }

    /**
     * Returns the list of {@code ReferenceSystemType}s.
     *
     * @return the list of codes declared in the current JVM.
     */
    public static ReferenceSystemType[] values() {
        return values(ReferenceSystemType.class);
    }

    /**
     * Returns the list of codes of the same kind as this code list element.
     * Invoking this method is equivalent to invoking {@link #values()}, except that
     * this method can be invoked on an instance of the parent {@code CodeList} class.
     *
     * @return all code {@linkplain #values() values} for this code list.
     */
    @Override
    public ReferenceSystemType[] family() {
        return values();
    }

    /**
     * Returns the {@link ReferenceSystemType} that matches the given string, or returns a new one if none match it.
     * This methods returns the first instance (in declaration order) for which the {@linkplain #name() name}
     * is {@linkplain String#equalsIgnoreCase(String) equals, ignoring case}, to the given name.
     * If no existing instance is found, then a new one is created for the given name.
     *
     * @param  code  the name of the code to fetch or to create.
     * @return a code matching the given name.
     */
    public static ReferenceSystemType valueOf(String code) {
        return valueOf(ReferenceSystemType.class, code, ReferenceSystemType::new).get();
    }

    /**
     * Checks whether the given reference system is an instance of this type.
     * This method always checks the type of the given {@link ReferenceSystem}
     * and, in the case of {@link CompoundCRS}, the type of each component.
     * It may also check the type and the number of dimensions of the coordinate system.
     * Examples:
     *
     * <ul>
     *   <li>For {@link #VERTICAL}, returns {@code true} if {@code candidate} is an instance of {@link VerticalCRS}.</li>
     *   <li>For {@link #TEMPORAL}, returns {@code true} if {@code candidate} is an instance of {@link TemporalCRS}.</li>
     *   <li>For {@link #PROJECTED}, returns {@code true} if {@code candidate} is an instance of {@link ProjectedCRS}.</li>
     *   <li>For {@link #GEODETIC_GEOCENTRIC}, returns {@code true} if {@code candidate} is an instance of {@link GeodeticCRS}
     *       and the coordinate system is an instance of {@link CartesianCS}.</li>
     *   <li>For {@link #GEODETIC_GEOGRAPHIC2D}, returns {@code true} if {@code candidate} is an instance of {@link GeodeticCRS},
     *       the coordinate system is an instance of {@link EllipsoidalCS}, and the number of dimensions is 2.</li>
     *   <li>For {@link #GEODETIC_GEOGRAPHIC3D}, returns {@code true} if {@code candidate} is an instance of {@link GeodeticCRS},
     *       the coordinate system is an instance of {@link EllipsoidalCS}, and the number of dimensions is 3.</li>
     *   <li>For {@link #COMPOUND_GEOGRAPHIC2D_VERTICAL}, returns {@code true} if {@code candidate} is an instance of {@link CompoundCRS},
     *       the first component is an instance of {@link GeodeticCRS} associated to a two-dimensional {@link EllipsoidalCS},
     *       and the second component is an instance of {@link VerticalCRS}.</li>
     * </ul>
     *
     * @param  candidate  the reference system to check, or {@code null}.
     * @return whether the given reference system is non-null and an instance of the type described by this value.
     */
    public boolean isInstance(final ReferenceSystem candidate) {
        if (components != null) {
            if (components.length == 1) {
                if (components[0].isInstance(candidate)) {
                    if (candidate instanceof SingleCRS) {
                        return isExpectedCS((SingleCRS) candidate);
                    } else {
                        return true;
                    }
                }
            } else if (candidate instanceof CompoundCRS) {
                List<SingleCRS> singles = ((CompoundCRS) candidate).getSingleComponents();
                if (singles.size() == components.length) {
                    for (int i=0; i < components.length; i++) {
                        final SingleCRS single = singles.get(i);
                        if (components[i].isInstance(single)) {
                            if (i != 0 || isExpectedCS(single)) {
                                continue;   // Check next single CRS.
                            }
                        }
                        return false;
                    }
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns whether the given <abbr>CRS</abbr> is associated to a coordinate system
     * of the expected type and with the expected number of dimensions.
     * This method should be invoked only for the first component.
     */
    private boolean isExpectedCS(final SingleCRS crs) {
        if (csOfFirst == null && dimOfFirst == 0) {
            return true;
        }
        CoordinateSystem cs = crs.getCoordinateSystem();
        return (dimOfFirst == 0 || cs.getDimension() == dimOfFirst) &&
                (csOfFirst == null || csOfFirst.isInstance(cs));
    }
}
