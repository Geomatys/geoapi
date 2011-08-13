/*
 *    GeoAPI - Java interfaces for OGC/ISO standards
 *    http://www.geoapi.org
 *
 *    Copyright (C) 2011 Open Geospatial Consortium, Inc.
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
package org.opengis.test.referencing;

import java.util.Map;
import java.util.Set;
import java.util.Collections;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;
import javax.measure.unit.NonSI;
import javax.measure.quantity.Angle;
import javax.measure.quantity.Length;

import org.opengis.parameter.*;
import org.opengis.referencing.*;
import org.opengis.referencing.cs.*;
import org.opengis.referencing.crs.*;
import org.opengis.referencing.datum.*;
import org.opengis.referencing.operation.*;
import org.opengis.metadata.citation.Citation;
import org.opengis.util.FactoryException;
import org.opengis.util.InternationalString;
import org.opengis.test.util.PseudoFactory;

import static org.junit.Assume.*;
import static org.opengis.test.Validators.*;


/**
 * Creates referencing objects for a limited set of hard-coded EPSG codes
 * using {@link ObjectFactory} and {@link MathTransformFactory}. This pseudo-factory
 * can be used with implementation that do not support (or don't want to test) a "real"
 * {@link CRSAuthorityFactory} for the EPSG database.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @version 3.1
 * @since   3.1
 */
public strictfp class PseudoEpsgFactory extends PseudoFactory implements DatumAuthorityFactory,
        CSAuthorityFactory, CRSAuthorityFactory
{
    /**
     * The reciprocal of the conversion from US feets to metres.
     */
    static final double R_US_FEET = 3.2808333333333333333;

    /**
     * Conversion from Clarke's 1865 feet to metres.
     */
    static final double CLARKE_KEET = 0.3047972654;

    /**
     * Conversion from feets to metres.
     */
    static final double FEET = 0.3048;

    /**
     * Conversion from links to metres
     */
    static final double LINKS = 0.66 * FEET;

    /**
     * Factory to build {@link Datum} instances, or {@code null} if none.
     */
    protected final DatumFactory datumFactory;

    /**
     * Factory to build {@link CoordinateSystem} instances, or {@code null} if none.
     */
    protected final CSFactory csFactory;

    /**
     * Factory to build {@link CoordinateReferenceSystem} instances, or {@code null} if none.
     */
    protected final CRSFactory crsFactory;

    /**
     * Factory to build {@link Conversion} instances, or {@code null} if none.
     */
    protected final CoordinateOperationFactory opFactory;

    /**
     * Factory to build {@link MathTransform} instances, or {@code null} if none.
     */
    protected final MathTransformFactory mtFactory;

    /**
     * Creates a new pseudo-factory which will use the given factories.
     *
     * @param datumFactory Factory for creating {@link Datum} instances.
     * @param csFactory    Factory for creating {@link CoordinateSystem} instances.
     * @param crsFactory   Factory for creating {@link CoordinateReferenceSystem} instances.
     * @param opFactory    Factory for creating {@link Conversion} instances.
     * @param mtFactory    Factory for creating {@link MathTransform} instances.
     */
    public PseudoEpsgFactory(
            final DatumFactory            datumFactory,
            final CSFactory                  csFactory,
            final CRSFactory                crsFactory,
            final CoordinateOperationFactory opFactory,
            final MathTransformFactory       mtFactory)
    {
        this.datumFactory = datumFactory;
        this.csFactory    = csFactory;
        this.crsFactory   = crsFactory;
        this.opFactory    = opFactory;
        this.mtFactory    = mtFactory;
    }

    /**
     * Returns the given EPSG code as an integer.
     *
     * @param  code The EPSG code to parse.
     * @return The EPSG code as an integer.
     * @throws NoSuchAuthorityCodeException if the given code can not be parsed as an integer.
     */
    private static int parseCode(String code) throws NoSuchAuthorityCodeException {
        final int s = code.lastIndexOf(':');
        if (s >= 0) {
            final String authority = code.substring(0, s).trim();
            if (!authority.equalsIgnoreCase("EPSG")) {
                throw new NoSuchAuthorityCodeException("Unsupported \"" + authority + "\" authority.", "EPSG", code);
            }
            code = code.substring(s+1).trim();
        }
        try {
            return Integer.parseInt(code);
        } catch (NumberFormatException cause) {
            NoSuchAuthorityCodeException e = new NoSuchAuthorityCodeException(
                    "Unparseable EPSG code: " + code, "EPSG", code);
            e.initCause(cause);
            throw e;
        }
    }

    /**
     * Creates the exception to be thrown when the given code has not been recognized.
     *
     * @param  code The code which has been requested.
     * @return The exception to throw.
     */
    private static NoSuchAuthorityCodeException noSuchAuthorityCode(final int id, final String code) {
        final String idAsText = String.valueOf(id);
        return new NoSuchAuthorityCodeException("No case implemented for EPSG:" + idAsText,
                "EPSG", idAsText, code);
    }

    /**
     * The default implementation returns {@code null}.
     */
    @Override
    public Citation getAuthority() {
        return null;
    }

    /**
     * The default implementation returns an empty set.
     *
     * @todo Needs to be implemented.
     */
    @Override
    public Set<String> getAuthorityCodes(final Class<? extends IdentifiedObject> type) throws FactoryException {
        return Collections.emptySet();
    }

    /**
     * The default implementation returns {@code null}.
     */
    @Override
    public InternationalString getDescriptionText(final String code) throws FactoryException {
        return null;
    }

    /**
     * Builds a map of properties for a referencing object to be build. The map shall contains
     * at least the {@link IdentifiedObject#NAME_KEY} identifier associated to the given value.
     * Subclasses can override this method in order to provide more information if they wish.
     *
     * @param  code  The EPSG code of the object being build.
     * @param  value The value for the name key.
     * @return A map containing only the value specified for the name key.
     */
    protected Map<String,?> createPropertiesMap(final int code, final String value) {
        return Collections.singletonMap(IdentifiedObject.NAME_KEY, value);
    }

    /**
     * Returns an arbitrary object from a code.
     * The supported codes are:
     * <p>
     * <table border="1" cellspacing="0" cellpadding="2">
     *   <tr><th>Code</th> <th>Name</th></tr>
     *   <tr><td>4326</td> <td>WGS 84</td></tr>
     *   <tr><td>6326</td> <td>World Geodetic System 1984</td></tr>
     *   <tr><td>6422</td> <td>Ellipsoidal 2D CS. Axes: latitude, longitude. Orientations: north, east. UoM: degree</td></tr>
     * </table>
     *
     * @param  code Value allocated by authority.
     * @return The datum for the given code.
     * @throws FactoryException if the object creation failed.
     */
    @Override
    public IdentifiedObject createObject(final String code) throws FactoryException {
        final int id = parseCode(code);
        switch (id) {
            case 6326: return createDatum(code);
            case 6422: return createCoordinateSystem(code);
            case 4326: return createCoordinateReferenceSystem(code);
            default:   throw noSuchAuthorityCode(id, code);
        }
    }




    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////                                 ///////////////////////////////
    ///////////////////////////////    D A T U M   F A C T O R Y    ///////////////////////////////
    ///////////////////////////////                                 ///////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Returns an arbitrary {@linkplain Datum datum} from a code.
     * The supported codes are:
     * <p>
     * <table border="1" cellspacing="0" cellpadding="2">
     *   <tr><th>Code</th> <th>Name</th></tr>
     *   <tr><td>6326</td> <td>World Geodetic System 1984</td></tr>
     * </table>
     *
     * @param  code Value allocated by authority.
     * @return The datum for the given code.
     * @throws FactoryException if the object creation failed.
     */
    @Override
    public Datum createDatum(final String code) throws FactoryException {
        final int id = parseCode(code);
        switch (id) {
            case 6326: return createGeodeticDatum(code);
            default:   throw noSuchAuthorityCode(id, code);
        }
    }

    /**
     * The default implementation throws {@link NoSuchAuthorityCodeException} unconditionally.
     */
    @Override
    public EngineeringDatum createEngineeringDatum(String code) throws FactoryException {
        final int id = parseCode(code);
        switch (id) {
            default:   throw noSuchAuthorityCode(id, code);
        }
    }

    /**
     * The default implementation throws {@link NoSuchAuthorityCodeException} unconditionally.
     */
    @Override
    public ImageDatum createImageDatum(String code) throws FactoryException {
        final int id = parseCode(code);
        switch (id) {
            default:   throw noSuchAuthorityCode(id, code);
        }
    }

    /**
     * The default implementation throws {@link NoSuchAuthorityCodeException} unconditionally.
     */
    @Override
    public VerticalDatum createVerticalDatum(String code) throws FactoryException {
        final int id = parseCode(code);
        switch (id) {
            default:   throw noSuchAuthorityCode(id, code);
        }
    }

    /**
     * The default implementation throws {@link NoSuchAuthorityCodeException} unconditionally.
     */
    @Override
    public TemporalDatum createTemporalDatum(String code) throws FactoryException {
        final int id = parseCode(code);
        switch (id) {
            default:   throw noSuchAuthorityCode(id, code);
        }
    }

    /**
     * Returns a {@linkplain GeodeticDatum geodetic datum} from a code.
     * The supported codes are:
     * <p>
     * <table border="1" cellspacing="0" cellpadding="2">
     *   <tr><th>Code</th> <th>Name</th></tr>
     *   <tr><td>6326</td> <td>World Geodetic System 1984</td></tr>
     * </table>
     *
     * @param  code Value allocated by authority.
     * @return The datum for the given code.
     * @throws FactoryException if the object creation failed.
     */
    @Override
    public GeodeticDatum createGeodeticDatum(final String code) throws FactoryException {
        final String name;
        final int ellipsoid;
        final int primeMeridian;
        final int id = parseCode(code);
        switch (id) {
            case 6326: name="World Geodetic System 1984"; ellipsoid=7030; primeMeridian=8901; break;
            default:   throw noSuchAuthorityCode(id, code);
        }
        assumeNotNull(datumFactory);
        final GeodeticDatum object = datumFactory.createGeodeticDatum(createPropertiesMap(id, name),
                createEllipsoid    (String.valueOf(ellipsoid)),
                createPrimeMeridian(String.valueOf(primeMeridian)));
        validate(object);
        return object;
    }

    /**
     * Returns an {@linkplain Ellipsoid ellipsoid} from a code.
     * The supported codes are:
     * <p>
     * <table border="1" cellspacing="0" cellpadding="2">
     *   <tr><th>Code</th> <th>Name</th></tr>
     *   <tr><td>7030</td> <td>WGS 84</td></tr>
     * </table>
     *
     * @param  code Value allocated by authority.
     * @return The ellipsoid for the given code.
     * @throws FactoryException if the object creation failed.
     */
    @Override
    public Ellipsoid createEllipsoid(final String code) throws FactoryException {
        final String name;
        final double semiMajorAxis;
        final double inverseFlattening;
        final int    unit;
        final int id = parseCode(code);
        switch (id) {
            case 7030: name="WGS 84"; semiMajorAxis=6378137; inverseFlattening=298.257223563; unit=9001; break;
            default:   throw noSuchAuthorityCode(id, code);
        }
        assumeNotNull(datumFactory);
        final Ellipsoid object = datumFactory.createFlattenedSphere(createPropertiesMap(id, name),
                semiMajorAxis, inverseFlattening, createUnit(String.valueOf(unit)).asType(Length.class));
        validate(object);
        return object;
    }

    /**
     * Returns a {@linkplain PrimeMeridian prime meridian} from a EPSG code.
     * The supported codes are:
     * <p>
     * <table border="1" cellspacing="0" cellpadding="2">
     *   <tr><th>Code</th> <th>Name</th></tr>
     *   <tr><td>8901</td> <td>Greenwich</td></tr>
     * </table>
     *
     * @param  code Value allocated by authority.
     * @return The prime meridian for the given code.
     * @throws FactoryException if the object creation failed.
     */
    @Override
    public PrimeMeridian createPrimeMeridian(final String code) throws FactoryException {
        final String name;
        final double longitude;
        final int    unit;
        final int id = parseCode(code);
        switch (id) {
            case 8901: name="Greenwich"; longitude=0.0; unit=9102; break;
            default:   throw noSuchAuthorityCode(id, code);
        }
        assumeNotNull(datumFactory);
        final PrimeMeridian object = datumFactory.createPrimeMeridian(createPropertiesMap(id, name),
                longitude, createUnit(String.valueOf(unit)).asType(Angle.class));
        validate(object);
        return object;
    }




    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////                                                         ///////////////////
    ///////////////////    C O O R D I N A T E   S Y S T E M   F A C T O R Y    ///////////////////
    ///////////////////                                                         ///////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Returns an arbitrary {@linkplain CoordinateSystem coordinate system} from a code.
     * The supported codes are:
     * <p>
     * <table border="1" cellspacing="0" cellpadding="2">
     *   <tr><th>Code</th> <th>Name</th></tr>
     *   <tr><td>6422</td> <td>Ellipsoidal 2D CS. Axes: latitude, longitude. Orientations: north, east. UoM: degree</td></tr>
     * </table>
     *
     * @param  code Value allocated by authority.
     * @return The coordinate system for the given code.
     * @throws FactoryException if the object creation failed.
     */
    @Override
    public CoordinateSystem createCoordinateSystem(final String code) throws FactoryException {
        final int id = parseCode(code);
        switch (id) {
            case 6422: return createEllipsoidalCS(code);
            default:   throw noSuchAuthorityCode(id, code);
        }
    }

    /**
     * The default implementation throws {@link NoSuchAuthorityCodeException} unconditionally.
     */
    @Override
    public CartesianCS createCartesianCS(final String code) throws FactoryException {
        final int id = parseCode(code);
        switch (id) {
            default:   throw noSuchAuthorityCode(id, code);
        }
    }

    /**
     * The default implementation throws {@link NoSuchAuthorityCodeException} unconditionally.
     */
    @Override
    public PolarCS createPolarCS(final String code) throws FactoryException {
        final int id = parseCode(code);
        switch (id) {
            default:   throw noSuchAuthorityCode(id, code);
        }
    }

    /**
     * The default implementation throws {@link NoSuchAuthorityCodeException} unconditionally.
     */
    @Override
    public CylindricalCS createCylindricalCS(final String code) throws FactoryException {
        final int id = parseCode(code);
        switch (id) {
            default:   throw noSuchAuthorityCode(id, code);
        }
    }

    /**
     * The default implementation throws {@link NoSuchAuthorityCodeException} unconditionally.
     */
    @Override
    public SphericalCS createSphericalCS(final String code) throws FactoryException {
        final int id = parseCode(code);
        switch (id) {
            default:   throw noSuchAuthorityCode(id, code);
        }
    }

    /**
     * Creates an ellipsoidal coordinate system from a code.
     * The supported codes are:
     * <p>
     * <table border="1" cellspacing="0" cellpadding="2">
     *   <tr><th>Code</th> <th>Name</th></tr>
     *   <tr><td>6422</td> <td>Ellipsoidal 2D CS. Axes: latitude, longitude. Orientations: north, east. UoM: degree</td></tr>
     * </table>
     *
     * @param  code Value allocated by authority.
     * @return The coordinate system for the given code.
     * @throws FactoryException if the object creation failed.
     */
    @Override
    public EllipsoidalCS createEllipsoidalCS(final String code) throws FactoryException {
        final String name;
        final int axis0;
        final int axis1;
        final int id = parseCode(code);
        switch (id) {
            case 6422: name="Ellipsoidal 2D CS. Axes: latitude, longitude. Orientations: north, east. UoM: degree"; axis0=106; axis1=107; break;
            default:   throw noSuchAuthorityCode(id, code);
        }
        assumeNotNull(csFactory);
        final EllipsoidalCS object = csFactory.createEllipsoidalCS(createPropertiesMap(id, name),
                createCoordinateSystemAxis(String.valueOf(axis0)),
                createCoordinateSystemAxis(String.valueOf(axis1)));
        validate(object);
        return object;
    }

    /**
     * The default implementation throws {@link NoSuchAuthorityCodeException} unconditionally.
     */
    @Override
    public VerticalCS createVerticalCS(final String code) throws FactoryException {
        final int id = parseCode(code);
        switch (id) {
            default:   throw noSuchAuthorityCode(id, code);
        }
    }

    /**
     * The default implementation throws {@link NoSuchAuthorityCodeException} unconditionally.
     */
    @Override
    public TimeCS createTimeCS(final String code) throws FactoryException {
        final int id = parseCode(code);
        switch (id) {
            default:   throw noSuchAuthorityCode(id, code);
        }
    }

    /**
     * Returns a {@linkplain CoordinateSystemAxis coordinate system axis} from a code.
     * The supported codes are:
     * <p>
     * <table border="1" cellspacing="0" cellpadding="2">
     *   <tr><th>Code</th> <th>Name</th></tr>
     *   <tr><td>106</td>  <td>Geodetic latitude</td></tr>
     *   <tr><td>107</td>  <td>Geodetic longitude</td></tr>
     * </table>
     *
     * @param  code Value allocated by authority.
     * @return The axis for the given code.
     * @throws FactoryException if the object creation failed.
     */
    @Override
    public CoordinateSystemAxis createCoordinateSystemAxis(final String code) throws FactoryException {
        final String name;
        final String abbreviation;
        final AxisDirection direction;
        final int unit;
        final int id = parseCode(code);
        switch (id) {
            case 106: name="Geodetic latitude";  abbreviation="Lat";  direction=AxisDirection.NORTH; unit=9122; break;
            case 107: name="Geodetic longitude"; abbreviation="Long"; direction=AxisDirection.EAST;  unit=9122; break;
            default:   throw noSuchAuthorityCode(id, code);
        }
        assumeNotNull(csFactory);
        final CoordinateSystemAxis object = csFactory.createCoordinateSystemAxis(createPropertiesMap(id, name),
                abbreviation, direction, createUnit(String.valueOf(unit)));
        validate(object);
        return object;
    }

    /**
     * Returns an {@linkplain Unit unit} from a code.
     * The supported codes are:
     * <p>
     * <table border="1" cellspacing="0" cellpadding="2">
     *   <tr><th>Code</th> <th>Name</th></tr>
     *   <tr><td>9001</td> <td>metre</td></tr>
     *   <tr><td>9102</td> <td>degree</td></tr>
     *   <tr><td>9122</td> <td>degree (supplier to define representation)</td></tr>
     * </table>
     *
     * @param  code Value allocated by authority.
     * @return The unit for the given code.
     * @throws FactoryException if the object creation failed.
     */
    @Override
    public Unit<?> createUnit(final String code) throws FactoryException {
        final Unit<?> unit;
        final int id = parseCode(code);
        switch (id) {
            case 9001: unit=SI.METRE; break;
            case 9122: // Fall through
            case 9102: unit=NonSI.DEGREE_ANGLE; break;
            default:   throw noSuchAuthorityCode(id, code);
        }
        return unit;
    }




    ///////////////////////////////////////////////////////////////////////////////////////////////
    /////////                                                                             /////////
    /////////    C O O R D I N A T E   R E F E R E N C E   S Y S T E M   F A C T O R Y    /////////
    /////////                                                                             /////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Returns an arbitrary {@linkplain CoordinateReferenceSystem coordinate reference system}
     * from a code. The supported codes are:
     * <p>
     * <table border="1" cellspacing="0" cellpadding="2">
     *   <tr><th>Code</th> <th>Name</th></tr>
     *   <tr><td>4326</td> <td>WGS 84</td></tr>
     * </table>
     *
     * @param  code Value allocated by authority.
     * @return The coordinate reference system for the given code.
     * @throws FactoryException if the object creation failed.
     */
    @Override
    public CoordinateReferenceSystem createCoordinateReferenceSystem(final String code) throws FactoryException {
        final int id = parseCode(code);
        switch (id) {
            case 4326: return createGeographicCRS(code);
            default:   throw noSuchAuthorityCode(id, code);
        }
    }

    /**
     * The default implementation throws {@link NoSuchAuthorityCodeException} unconditionally.
     */
    @Override
    public CompoundCRS createCompoundCRS(String code) throws FactoryException {
        final int id = parseCode(code);
        switch (id) {
            default:   throw noSuchAuthorityCode(id, code);
        }
    }

    /**
     * The default implementation throws {@link NoSuchAuthorityCodeException} unconditionally.
     */
    @Override
    public DerivedCRS createDerivedCRS(String code) throws FactoryException {
        final int id = parseCode(code);
        switch (id) {
            default:   throw noSuchAuthorityCode(id, code);
        }
    }

    /**
     * The default implementation throws {@link NoSuchAuthorityCodeException} unconditionally.
     */
    @Override
    public EngineeringCRS createEngineeringCRS(String code) throws FactoryException {
        final int id = parseCode(code);
        switch (id) {
            default:   throw noSuchAuthorityCode(id, code);
        }
    }

    /**
     * Returns a {@linkplain GeographicCRS geographic coordinate reference system} from a code.
     * The supported codes are:
     * <p>
     * <table border="1" cellspacing="0" cellpadding="2">
     *   <tr><th>Code</th> <th>Name</th></tr>
     *   <tr><td>4326</td> <td>WGS 84</td></tr>
     * </table>
     *
     * @param  code Value allocated by authority.
     * @return The coordinate reference system for the given code.
     * @throws FactoryException if the object creation failed.
     */
    @Override
    public GeographicCRS createGeographicCRS(final String code) throws FactoryException {
        final String name;
        final int datum;
        final int coordinateSystem;
        final int id = parseCode(code);
        switch (id) {
            case 4326: name="WGS 84"; datum=6326; coordinateSystem=6422; break;
            default:   throw noSuchAuthorityCode(id, code);
        }
        assumeNotNull(crsFactory);
        final GeographicCRS object = crsFactory.createGeographicCRS(createPropertiesMap(id, name),
                createGeodeticDatum(String.valueOf(datum)),
                createEllipsoidalCS(String.valueOf(coordinateSystem)));
        validate(object);
        return object;
    }

    /**
     * The default implementation throws {@link NoSuchAuthorityCodeException} unconditionally.
     */
    @Override
    public GeocentricCRS createGeocentricCRS(String code) throws FactoryException {
        final int id = parseCode(code);
        switch (id) {
            default:   throw noSuchAuthorityCode(id, code);
        }
    }

    /**
     * The default implementation throws {@link NoSuchAuthorityCodeException} unconditionally.
     */
    @Override
    public ImageCRS createImageCRS(String code) throws FactoryException {
        final int id = parseCode(code);
        switch (id) {
            default:   throw noSuchAuthorityCode(id, code);
        }
    }

    /**
     * The default implementation throws {@link NoSuchAuthorityCodeException} unconditionally.
     */
    @Override
    public ProjectedCRS createProjectedCRS(String code) throws FactoryException {
        final int id = parseCode(code);
        switch (id) {
            default:   throw noSuchAuthorityCode(id, code);
        }
    }

    /**
     * The default implementation throws {@link NoSuchAuthorityCodeException} unconditionally.
     */
    @Override
    public TemporalCRS createTemporalCRS(String code) throws FactoryException {
        final int id = parseCode(code);
        switch (id) {
            default:   throw noSuchAuthorityCode(id, code);
        }
    }

    /**
     * The default implementation throws {@link NoSuchAuthorityCodeException} unconditionally.
     */
    @Override
    public VerticalCRS createVerticalCRS(String code) throws FactoryException {
        final int id = parseCode(code);
        switch (id) {
            default:   throw noSuchAuthorityCode(id, code);
        }
    }




    ///////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////                                                               ////////////////
    ////////////////    C O O R D I N A T E   O P E R A T I O N   F A C T O R Y    ////////////////
    ////////////////                                                               ////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Returns the parameters to use for creating the {@linkplain CoordinateOperation coordinate
     * operation} identified by the given EPSG code. The coordinate operation is typically a map
     * projection used by exactly one {@linkplain ProjectedCRS projected CRS}, which is listed in
     * the second column for information purpose.
     * <p>
     * The supported codes are determined from the set of examples published in the EPSG guidance
     * document, augmented with other sources. The other sources are identified by the
     * {@code "IGNF:"} prefix before the CRS name. The supported codes are:
     * <p>
     * <table border="1" cellspacing="0" cellpadding="2">
     *   <tr><th>Code</th>      <th>Used by CRS</th><th>CRS Name</th>                           <th>Operation method</th></tr>
     *   <tr><td>19905</td>     <td>3002</td>      <td>Makassar / NEIEZ</td>                    <td>Mercator (variant A)</td></tr>
     *   <tr><td>19884</td>     <td>3388</td>      <td>Pulkovo 1942 / Caspian Sea Mercator</td> <td>Mercator (variant B)</td></tr>
     *   <tr><td>3856</td>      <td>3857</td>      <td>WGS 84 / Pseudo-Mercator</td>            <td>Popular Visualisation Pseudo Mercator</td></tr>
     *   <tr><td>19910</td>     <td>24200</td>     <td>JAD69 / Jamaica National Grid</td>       <td>Lambert Conic Conformal (1SP)</td></tr>
     *   <tr><td>14204</td>     <td>32040</td>     <td>NAD27 / Texas South Central</td>         <td>Lambert Conic Conformal (2SP)</td></tr>
     *   <tr><td>19902</td>     <td>31300</td>     <td>Belge 1972 / Belge Lambert 72</td>       <td>Lambert Conic Conformal (2SP Belgium)</td></tr>
     *   <tr><td>19986</td>     <td>3035</td>      <td>ETRS89 / LAEA Europe</td>                <td>Lambert Azimuthal Equal Area</td></tr>
     *   <tr><td>19975</td>     <td>2314</td>      <td>Trinidad 1903 / Trinidad Grid</td>       <td>Cassini-Soldner</td></tr>
     *   <tr><td>19952</td>     <td>2065</td>      <td>CRS S-JTSK (Ferro) / Krovak</td>         <td>Krovak</td></tr>
     *   <tr><td>310642901</td> <td>310642901</td> <td>IGNF:MILLER</td>                         <td>Miller_Cylindrical</td></tr>
     * </table>
     *
     * @param  code The EPSG code of the {@linkplain CoordinateOperation coordinate operation} to create.
     * @return The coordinate operation (typically a map projection) parameters.
     * @throws FactoryException If the given EPSG code is unknown to this factory.
     */
    protected ParameterValueGroup createParameters(final int code) throws FactoryException {
        return createParameters(mtFactory, code);
    }

    /**
     * Implementation of the above {@link #createParameters(int)} method,
     * as a static method for direct access by {@link MathTransformTest}.
     */
    static ParameterValueGroup createParameters(final MathTransformFactory factory, final int code)
            throws FactoryException
    {
        final ParameterValueGroup parameters;
        switch (code) {
            case 19905: { // "Makassar / NEIEZ" using operation method 9804
                parameters = factory.getDefaultParameters("Mercator (variant A)"); // Alias "Mercator (1SP)"
                parameters.parameter("semi-major axis").setValue(6377397.155); // Bessel 1841
                parameters.parameter("semi-minor axis").setValue(6377397.155 * (1 - 1/299.1528128));
                parameters.parameter("Latitude of natural origin")    .setValue(  0.0);
                parameters.parameter("Longitude of natural origin")   .setValue(110.0);
                parameters.parameter("Scale factor at natural origin").setValue(0.997);
                parameters.parameter("False easting").setValue(3900000.0);
                parameters.parameter("False northing").setValue(900000.0);
                break;
            }
            case 19884: { // "Pulkovo 1942 / Caspian Sea Mercator" using operation method 9805
                parameters = factory.getDefaultParameters("Mercator (variant B) "); // Alias "Mercator (2SP)"
                parameters.parameter("semi-major axis").setValue(6378245.0); // Krassowski 1940
                parameters.parameter("semi-minor axis").setValue(6378245.0 * (1 - 1/298.3));
                parameters.parameter("Latitude of 1st standard parallel").setValue(42.0);
                parameters.parameter("Longitude of natural origin")      .setValue(51.0);
                break;
            }
            case 3856: { // "WGS 84 / Pseudo-Mercator" using operation method 1024
                parameters = factory.getDefaultParameters("Popular Visualisation Pseudo Mercator");
                parameters.parameter("semi-major axis").setValue(6378137.0); // WGS 84
                parameters.parameter("semi-minor axis").setValue(6378137.0 * (1 - 1/298.2572236));
                break;
            }
            case 310642901: { // "IGNF:MILLER" (not an official EPSG code)
                parameters = factory.getDefaultParameters("Miller_Cylindrical");
                parameters.parameter("semi-major axis").setValue(6378137);
                parameters.parameter("semi-minor axis").setValue(6378137);
                break;
            }
            case 19910: { // "JAD69 / Jamaica National Grid" using operation method 9801
                parameters = factory.getDefaultParameters("Lambert Conic Conformal (1SP)");
                parameters.parameter("semi-major axis").setValue(6378206.4); // Clarke 1866
                parameters.parameter("semi-minor axis").setValue(6356583.8);
                parameters.parameter("Latitude of natural origin")    .setValue( 18.0);
                parameters.parameter("Longitude of natural origin")   .setValue(-77.0);
                parameters.parameter("Scale factor at natural origin").setValue(  1.0);
                parameters.parameter("False easting") .setValue(250000.00);
                parameters.parameter("False northing").setValue(150000.00);
                break;
            }
            case 14204: { // "NAD27 / Texas South Central" using operation method 9802
                parameters = factory.getDefaultParameters("Lambert Conic Conformal (2SP)");
                parameters.parameter("semi-major axis").setValue(6378206.4); // Clarke 1866
                parameters.parameter("semi-minor axis").setValue(6356583.8);
                parameters.parameter("Latitude of 1st standard parallel").setValue(28 + 23.0/60); // 28°23'00"N
                parameters.parameter("Latitude of 2nd standard parallel").setValue(30 + 17.0/60); // 30°17'00"N
                parameters.parameter("Latitude of false origin")         .setValue(27 + 50.0/60); // 27°50'00"N
                parameters.parameter("Longitude of false origin")        .setValue(-99.0);        // 99°00'00"W
                parameters.parameter("Easting at false origin") .setValue(2000000 / R_US_FEET);
                parameters.parameter("Northing at false origin").setValue(      0 / R_US_FEET);
                break;
            }
            case 19902: { // "Belge 1972 / Belge Lambert 72" using operation method 9803
                parameters = factory.getDefaultParameters("Lambert Conic Conformal (2SP Belgium)");
                parameters.parameter("semi-major axis").setValue(6378388); // International 1924
                parameters.parameter("semi-minor axis").setValue(6378388 * (1 - 1/297.0));
                parameters.parameter("Latitude of 1st standard parallel").setValue(49 + 50.0/60); // 49°50'00"N
                parameters.parameter("Latitude of 2nd standard parallel").setValue(51 + 10.0/60); // 51°10'00"N
                parameters.parameter("Latitude of false origin")         .setValue(90.0);         // 90°00'00"N
                parameters.parameter("Longitude of false origin")        .setValue(4 + (21 + 24.983/60)/60); // 4°21'24.983"E
                parameters.parameter("Easting at false origin") .setValue( 150000.01);
                parameters.parameter("Northing at false origin").setValue(5400088.44);
                break;
            }
            case 19986: { // "Europe Equal Area 2001" using operation method 9820
                parameters = factory.getDefaultParameters("Lambert Azimuthal Equal Area");
                parameters.parameter("semi-major axis").setValue(6378137.0);
                parameters.parameter("semi-minor axis").setValue(6378137.0 * (1 - 1/298.2572221));
                parameters.parameter("Latitude of natural origin") .setValue(52.0);
                parameters.parameter("Longitude of natural origin").setValue(10.0);
                parameters.parameter("False easting") .setValue(4321000.00);
                parameters.parameter("False northing").setValue(3210000.00);
                break;
            }
            case 19975: { // "Trinidad 1903 / Trinidad Grid" using operation method 9806
                parameters = factory.getDefaultParameters("Cassini-Soldner");
                parameters.parameter("semi-major axis").setValue(20926348 * FEET); // Clarke 1858
                parameters.parameter("semi-minor axis").setValue(20855233 * FEET);
                parameters.parameter("Latitude of natural origin") .setValue(10 + (26 + 30.0/60)/60); // 10°26'30"N
                parameters.parameter("Longitude of natural origin").setValue(-(61 + 20.0/60));        // 61°20'00"W
                parameters.parameter("False easting") .setValue(430000.00 * LINKS);
                parameters.parameter("False northing").setValue(325000.00 * LINKS);
                break;
            }
            case 19952: { // "CRS S-JTSK (Ferro) / Krovak" using operation method 9819
                parameters = factory.getDefaultParameters("Krovak");
                parameters.parameter("semi-major axis").setValue(6377397.155);  // Bessel
                parameters.parameter("semi-minor axis").setValue(6377397.155 * (1 - 1/299.15281));
                parameters.parameter("Latitude of projection centre").setValue(49.5);  // 49°30'00"N
                parameters.parameter("Longitude of origin").setValue(24 + 50.0/60);    // 24°30'00"E
                parameters.parameter("Co-latitude of cone axis").setValue(30 + (17 + 17.3031/60)/60);
                parameters.parameter("Latitude of pseudo standard parallel").setValue(78.5);
                parameters.parameter("Scale factor on pseudo standard parallel").setValue(0.99990);
                break;
            }
            default: {
                throw noSuchAuthorityCode(code, String.valueOf(code));
            }
        }
        validate(parameters);
        return parameters;
    }
}