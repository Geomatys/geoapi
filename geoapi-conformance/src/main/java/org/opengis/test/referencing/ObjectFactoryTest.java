/*
 *    GeoAPI - Java interfaces for OGC/ISO standards
 *    Copyright © 2009-2024 Open Geospatial Consortium, Inc.
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
package org.opengis.test.referencing;

import java.util.Map;
import java.util.Optional;
import javax.measure.Unit;
import javax.measure.quantity.Angle;
import javax.measure.quantity.Length;

import org.opengis.parameter.*;
import org.opengis.referencing.cs.*;
import org.opengis.referencing.crs.*;
import org.opengis.referencing.datum.*;
import org.opengis.referencing.operation.*;
import org.opengis.referencing.IdentifiedObject;
import org.opengis.referencing.ObjectFactory;
import org.opengis.referencing.RegisterOperations;
import org.opengis.util.Factory;
import org.opengis.util.FactoryException;
import org.opengis.test.Configuration;
import org.opengis.test.Units;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.opengis.test.Assertions.assertAxisDirectionsEqual;
import static org.opengis.referencing.cs.AxisDirection.*;


/**
 * Tests the creation of referencing objects from object factories.
 * The factories to test are obtained by calls to {@link RegisterOperations#getFactory(Class)}
 * with subtypes of {@link ObjectFactory} in argument.
 *
 * <h2>Usage example:</h2>
 * in order to specify their factories and run the tests in a JUnit framework, implementers can
 * define a subclass in their own test suite as in the example below:
 *
 * {@snippet lang="java" :
 * import org.opengis.test.referencing.ObjectFactoryTest;
 *
 * public class MyTest extends ObjectFactoryTest {
 *     public MyTest() {
 *         super(new MyRegisterOperations());
 *     }
 * }}
 *
 * @see AuthorityFactoryTest
 *
 * @author  Cédric Briançon (Geomatys)
 * @author  Martin Desruisseaux (Geomatys)
 * @version 3.1
 * @since   2.3
 */
@SuppressWarnings("strictfp")   // Because we still target Java 11.
public strictfp class ObjectFactoryTest extends ReferencingTestCase {
    /**
     * The message when a test is disabled because no factory has been found.
     */
    static final String NO_CRS_FACTORY   = "No Coordinate Reference System (CRS) factory found.",
                        NO_CS_FACTORY    = "No Coordinate System (CS) factory found.",
                        NO_DATUM_FACTORY = "No Datum factory found.",
                        NO_COP_FACTORY   = "No Coordinate Operation factory found.";

    /**
     * Provider of factories to use for building geodetic objects.
     * {@code ObjectFactoryTest} will use only {@link ObjectFactory} subtypes
     * and {@link CoordinateOperationAuthorityFactory} for operation methods.
     *
     * @see RegisterOperations#getFactory(Class)
     *
     * @since 3.1
     */
    protected final RegisterOperations factories;

    /**
     * Creates a new test which will use object factories provided by the given {@code factories} argument.
     * The {@link ObjectFactory} instances will be obtained by calls to {@link RegisterOperations#getFactory(Class)}.
     * If the latter method returns an empty value, then the tests that depend on the missing factory will throw
     * {@link org.opentest4j.TestAbortedException}.
     *
     * @param  factories   provider of factories for creating the geodetic objects to test.
     *
     * @since 3.1
     */
    public ObjectFactoryTest(final RegisterOperations factories) {
        this.factories = factories;
    }

    /**
     * Returns information about the configuration of the test which has been run.
     * This method returns a map containing:
     *
     * <ul>
     *   <li>All the entries defined in the {@link ReferencingTestCase#configuration() ReferencingTestCase} class.</li>
     *   <li>All the following values:
     *     <ul>
     *       <li>{@link #factories} (associated to {@link org.opengis.test.Configuration.Key#registerOperations})</li>
     *     </ul>
     *   </li>
     * </ul>
     *
     * @return the configuration of the test being run.
     *
     * @since 3.1
     */
    @Override
    public Configuration configuration() {
        final Configuration op = super.configuration();
        assertNull(op.put(Configuration.Key.registerOperations, factories));
        return op;
    }

    /**
     * Returns the factory of the given type if present, or interrupts the test otherwise.
     *
     * @param  <T>      compile-time value of the {@code type} argument.
     * @param  type     the desired type of factory.
     * @param  message  message to report if the desired factory is not present.
     * @return factory of the specified type.
     */
    private <T extends Factory> T getFactoryOrAbort(final Class<T> type, final String message) throws FactoryException {
        final Optional<T> factory = factories.getFactory(type);
        assumeTrue(factory.isPresent(), message);
        return factory.get();
    }

    /**
     * {@return the authority factory tests backed by the object factories}.
     */
    private AuthorityFactoryTest createAuthorityFactoryTest() {
        return new AuthorityFactoryTest(new PseudoEpsgFactory(Units.getDefault(), factories, validators));
    }

    /**
     * Builds a map containing only one value, composed by the {@link IdentifiedObject#NAME_KEY}
     * identifier and the value specified.
     *
     * @param  value  the value for the name key.
     * @return a map containing only the value specified for the name key.
     */
    private static Map<String,String> name(final String value) {
        return Map.of(IdentifiedObject.NAME_KEY, value);
    }

    /**
     * Tests the creation of the EPSG:4326 {@link GeographicCRS}. This method wraps the
     * object factories in an {@link PseudoEpsgFactory} instance, then delegates to the
     * {@link AuthorityFactoryTest#testWGS84()} method.
     *
     * @throws FactoryException if a factory fails to create a referencing object.
     */
    @Test
    public void testWGS84() throws FactoryException {
        createAuthorityFactoryTest().testWGS84();
    }

    /**
     * Tests the creation of the WGS84 {@linkplain CoordinateReferenceSystem CRS} with ellipsoidal height, and
     * verifies that the axes are in the given (<var>latitude</var>, <var>longitude</var>, <var>height</var>) order.
     *
     * @throws FactoryException if a factory fails to create a referencing object.
     */
    @Test
    public void testWGS84_3D() throws FactoryException {
        CoordinateSystemAxis λ, φ, h;
        EllipsoidalCS cs;
        GeographicCRS crs;              // The final product of this method.
        GeodeticDatum datum;

        final Unit<Length> metre = units.metre();
        final Unit<Angle> degree = units.degree();

        // Build a geodetic reference frame.
        final var datumFactory = getFactoryOrAbort(DatumFactory.class, NO_DATUM_FACTORY);
        validators.validate(datum = datumFactory.createGeodeticDatum(name("World Geodetic System 1984"),
                                    datumFactory.createEllipsoid    (name("WGS 84"), 6378137.0, 298.257223563, metre),
                                    datumFactory.createPrimeMeridian(name("Greenwich"), 0.0, degree)));

        // Build an ellipsoidal coordinate system.
        final var csFactory = getFactoryOrAbort(CSFactory.class, NO_CS_FACTORY);
        validators.validate(λ  = csFactory.createCoordinateSystemAxis(name("Geodetic longitude"), "λ", EAST,  degree));
        validators.validate(φ  = csFactory.createCoordinateSystemAxis(name("Geodetic latitude"),  "φ", NORTH, degree));
        validators.validate(h  = csFactory.createCoordinateSystemAxis(name("Ellipsoidal height"), "h", UP,    metre));
        validators.validate(cs = csFactory.createEllipsoidalCS(name("WGS 84"), φ, λ, h));

        // Finally build the geographic coordinate reference system.
        final var crsFactory = getFactoryOrAbort(CRSFactory.class, NO_CRS_FACTORY);
        validators.validate(crs = crsFactory.createGeographicCRS(name("WGS84(DD)"), datum, cs));

        datum = crs.getDatum();
        verifyIdentification(datum, "World Geodetic System 1984", null);
        verifyPrimeMeridian(datum.getPrimeMeridian(), "Greenwich", 0.0, degree);

        cs = crs.getCoordinateSystem();
        verifyCoordinateSystem(cs, EllipsoidalCS.class,
                new AxisDirection[] {
                    AxisDirection.NORTH,
                    AxisDirection.EAST,
                    AxisDirection.UP
                }, degree, degree, metre);
        verifyIdentification(cs.getAxis(0), "Geodetic latitude", null);
        verifyIdentification(cs.getAxis(1), "Geodetic longitude", null);
        verifyIdentification(cs.getAxis(2), "Ellipsoidal height", null);
    }

    /**
     * Tests the creation of a geocentric CRS.
     *
     * @throws FactoryException if a factory fails to create a referencing object.
     */
    @Test
    public void testGeocentric() throws FactoryException {
        final CoordinateSystemAxis X, Y, Z;
        final CartesianCS cs;
        final GeodeticCRS crs;              // The final product of this method.
        final GeodeticDatum datum;
        final PrimeMeridian greenwich;
        final Ellipsoid     ellipsoid;

        final Unit<Length> metre = units.metre();
        final Unit<Angle> degree = units.degree();

        final var datumFactory = getFactoryOrAbort(DatumFactory.class, NO_DATUM_FACTORY);
        validators.validate(greenwich = datumFactory.createPrimeMeridian  (name("Greenwich Meridian"), 0, degree));
        validators.validate(ellipsoid = datumFactory.createFlattenedSphere(name("WGS84 Ellipsoid"), 6378137, 298.257223563, metre));
        validators.validate(datum     = datumFactory.createGeodeticDatum  (name("WGS84 Datum"), ellipsoid, greenwich));

        final var csFactory = getFactoryOrAbort(CSFactory.class, NO_CS_FACTORY);
        validators.validate(X  = csFactory.createCoordinateSystemAxis(name("Geocentric X"), "X", GEOCENTRIC_X, metre));
        validators.validate(Y  = csFactory.createCoordinateSystemAxis(name("Geocentric Y"), "Y", GEOCENTRIC_Y, metre));
        validators.validate(Z  = csFactory.createCoordinateSystemAxis(name("Geocentric Z"), "Z", GEOCENTRIC_Z, metre));
        validators.validate(cs = csFactory.createCartesianCS(name("Geocentric CS"), X, Z, Y));

        final var crsFactory = getFactoryOrAbort(CRSFactory.class, NO_CRS_FACTORY);
        validators.validate(crs = crsFactory.createGeodeticCRS(name("Geocentric CRS"), datum, null, cs));
        assertAxisDirectionsEqual(crs.getCoordinateSystem(), new AxisDirection[] {GEOCENTRIC_X, GEOCENTRIC_Z, GEOCENTRIC_Y}, "GeodeticCRS");
    }

    /**
     * Tests the creation of a compound CRS made of a projected CRS with a gravity-related height.
     *
     * @throws FactoryException if a factory fails to create a referencing object.
     */
    @Test
    public void testProjectedWithGeoidalHeight() throws FactoryException {
        final CoordinateSystemAxis axisN, axisE, axisH, axisφ, axisλ;

        final EllipsoidalCS   baseCS;
        final GeographicCRS   baseCRS;
        final GeodeticDatum   baseDatum;
        final PrimeMeridian   greenwich;
        final Ellipsoid       ellipsoid;

        final CartesianCS     projectedCS;
        final ProjectedCRS    projectedCRS;
        final OperationMethod projectionMethod;
        final Conversion      baseToUTM;
        final int             utmZone = 12;

        final VerticalCS      heightCS;
        final VerticalCRS     heightCRS;
        final VerticalDatum   heightDatum;
        final CompoundCRS     crs3D;            // The final product of this method.

        final Unit<Length> metre = units.metre();
        final Unit<Angle> degree = units.degree();

        final var datumFactory = getFactoryOrAbort(DatumFactory.class, NO_DATUM_FACTORY);
        validators.validate(greenwich   = datumFactory.createPrimeMeridian  (name("Greenwich Meridian"), 0, degree));
        validators.validate(ellipsoid   = datumFactory.createFlattenedSphere(name("WGS84 Ellipsoid"), 6378137, 298.257223563, metre));
        validators.validate(baseDatum   = datumFactory.createGeodeticDatum  (name("WGS84 Datum"), ellipsoid, greenwich));
        validators.validate(heightDatum = datumFactory.createVerticalDatum  (name("WGS84 geoidal height"), RealizationMethod.GEOID));

        final var csFactory = getFactoryOrAbort(CSFactory.class, NO_CS_FACTORY);
        validators.validate(axisN       = csFactory.createCoordinateSystemAxis(name("Northing"),               "N", NORTH, metre));
        validators.validate(axisE       = csFactory.createCoordinateSystemAxis(name("Easting"),                "E", EAST,  metre));
        validators.validate(axisH       = csFactory.createCoordinateSystemAxis(name("Gravity-related Height"), "H", UP,    metre));
        validators.validate(axisφ       = csFactory.createCoordinateSystemAxis(name("Geodetic Latitude"),      "φ", NORTH, degree));
        validators.validate(axisλ       = csFactory.createCoordinateSystemAxis(name("Geodetic Longitude"),     "λ", EAST,  degree));
        validators.validate(baseCS      = csFactory.createEllipsoidalCS       (name("2D ellipsoidal"),  axisλ, axisφ));
        validators.validate(projectedCS = csFactory.createCartesianCS         (name("2D Cartesian CS"), axisN, axisE));
        validators.validate(heightCS    = csFactory.createVerticalCS          (name("Height CS"),       axisH));

        final var crsFactory = getFactoryOrAbort(CRSFactory.class, NO_CRS_FACTORY);
        baseCRS   = crsFactory.createGeographicCRS(name("2D geographic CRS"), baseDatum, baseCS);
        heightCRS = crsFactory.createVerticalCRS  (name("Height CRS"),      heightDatum, heightCS);

        final var copAuthorityFactory = getFactoryOrAbort(CoordinateOperationAuthorityFactory.class, NO_COP_FACTORY);
        validators.validate(projectionMethod = copAuthorityFactory.createOperationMethod("Transverse_Mercator"));
        final ParameterValueGroup paramUTM = projectionMethod.getParameters().createValue();
        paramUTM.parameter("central_meridian")  .setValue(-180 + utmZone*6 - 3);
        paramUTM.parameter("latitude_of_origin").setValue(0.0);
        paramUTM.parameter("scale_factor")      .setValue(0.9996);
        paramUTM.parameter("false_easting")     .setValue(500000.0);
        paramUTM.parameter("false_northing")    .setValue(0.0);
        validators.validate(paramUTM);

        final var copFactory = getFactoryOrAbort(CoordinateOperationFactory.class, NO_COP_FACTORY);
        validators.validate(baseToUTM    = copFactory.createDefiningConversion(name("Transverse_Mercator"), projectionMethod, paramUTM));
        validators.validate(projectedCRS = crsFactory.createProjectedCRS(name("WGS 84 / UTM Zone 12 (2D)"), baseCRS, baseToUTM, projectedCS));
        validators.validate(crs3D        = crsFactory.createCompoundCRS(name("3D Compound WGS 84 / UTM Zone 12"), projectedCRS, heightCRS));
        assertAxisDirectionsEqual(crs3D.getCoordinateSystem(), new AxisDirection[] {NORTH, EAST, UP}, "CompoundCRS");
    }
}
