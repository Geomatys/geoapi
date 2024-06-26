/*
 *    GeoAPI - Java interfaces for OGC/ISO standards
 *    Copyright © 2008-2024 Open Geospatial Consortium, Inc.
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

import java.util.Optional;
import org.opengis.referencing.*;
import org.opengis.referencing.cs.*;
import org.opengis.referencing.crs.*;
import org.opengis.referencing.datum.*;
import org.opengis.referencing.operation.*;
import org.opengis.parameter.GeneralParameterDescriptor;
import org.opengis.metadata.extent.Extent;
import org.opengis.metadata.Identifier;
import org.opengis.util.InternationalString;

import org.opengis.test.Units;
import org.opengis.test.Validator;
import org.opengis.test.ValidatorContainer;
import static org.junit.jupiter.api.Assertions.assertNotNull;


/**
 * Base class for validators of {@link IdentifiedObject} and related objects from the
 * {@code org.opengis.referencing} package.
 *
 * <p>This class is provided for users wanting to override the validation methods. When the default
 * behavior is sufficient, the {@link org.opengis.test.Validators} static methods provide a more
 * convenient way to validate various kinds of objects.</p>
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @version 3.1
 * @since   2.2
 */
public abstract class ReferencingValidator extends Validator {
    /**
     * Provider of units of measurement (degree, metre, second, <i>etc</i>).
     * This field is set to the {@linkplain Units#getDefault() default provider} for now
     * (it may be revisited in a future GeoAPI-conformance version).
     */
    final Units units = Units.getDefault();

    /**
     * Creates a new validator instance.
     *
     * @param container    the set of validators to use for validating other kinds of objects
     *                     (see {@linkplain #container field javadoc}).
     * @param packageName  the name of the package containing the classes to be validated.
     */
    protected ReferencingValidator(final ValidatorContainer container, final String packageName) {
        super(container, packageName);
    }

    /**
     * For each interface implemented by the given object, invokes the corresponding
     * {@code validate(…)} method defined in this package (if any).
     *
     * @param  object  the object to dispatch to {@code validate(…)} methods, or {@code null}.
     */
    public final void dispatchObject(final IdentifiedObject object) {
        int n = 0;
        if (object != null) {
            if (object instanceof CoordinateReferenceSystem)  {container.validate((CoordinateReferenceSystem)  object); n++;}
            if (object instanceof CoordinateSystem)           {container.validate((CoordinateSystem)           object); n++;}
            if (object instanceof CoordinateSystemAxis)       {container.validate((CoordinateSystemAxis)       object); n++;}
            if (object instanceof Datum)                      {container.validate((Datum)                      object); n++;}
            if (object instanceof Ellipsoid)                  {container.validate((Ellipsoid)                  object); n++;}
            if (object instanceof PrimeMeridian)              {container.validate((PrimeMeridian)              object); n++;}
            if (object instanceof GeneralParameterDescriptor) {container.validate((GeneralParameterDescriptor) object); n++;}
            if (object instanceof CoordinateOperation)        {container.validate((CoordinateOperation)        object); n++;}
            if (object instanceof OperationMethod)            {container.validate((OperationMethod)            object); n++;}
            if (n == 0) {
                validateIdentifiedObject(object);
            }
        }
    }

    /**
     * Ensures that the given domain has a scope and a domain of validity.
     *
     * @param  object  the object to validate, or {@code null}.
     *
     * @since 3.1
     */
    public void validate(final ObjectDomain object) {
        if (object == null) {
            return;
        }
        final InternationalString scope = object.getScope();
        mandatory(scope, "ObjectDomain: shall have a scope.");
        final Extent domain = object.getDomainOfValidity();
        mandatory(domain, "ObjectDomain: shall have a domain of validity.");
        container.validate(scope);
        container.validate(domain);
    }

    /**
     * Performs the validation that are common to all identified objects. This method is
     * invoked by {@code validate} methods after they have determined the type of their
     * argument.
     *
     * @param  object  the object to validate (cannot be null).
     */
    final void validateIdentifiedObject(final IdentifiedObject object) {
        final Identifier name = object.getName();
        mandatory(name, "IdentifiedObject: shall have a name.");
        container.validate(name);
        validate("identifier", object.getIdentifiers(), ValidatorContainer::validate, false);
        validate("alias",      object.getAlias(),       ValidatorContainer::validate, false);
        validate("domain",     object.getDomains(),     ValidatorContainer::validate, false);
        container.validate(getRemarks(object));
    }

    /**
     * Returns the remarks of the given object.
     *
     * @param  object  the object for which to get the remarks.
     * @return the remarks, or {@code null} if none.
     */
    static InternationalString getRemarks(final IdentifiedObject object) {
        Optional<InternationalString> remarks = object.getRemarks();
        assertNotNull(remarks, "`Optional` cannot be null.");
        return remarks.orElse(null);
    }
}
