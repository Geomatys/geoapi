/*
 *    GeoAPI - Java interfaces for OGC/ISO standards
 *    Copyright © 2003-2023 Open Geospatial Consortium, Inc.
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
package org.opengis.geometry.coordinate;

import java.util.List;
import org.opengis.geometry.primitive.Curve;
import org.opengis.geometry.primitive.Surface;
import org.opengis.geometry.primitive.SurfacePatch;
import org.opengis.geometry.primitive.SurfaceBoundary;
import org.opengis.geometry.complex.Complex;
import org.opengis.annotation.UML;

import static org.opengis.annotation.Obligation.*;
import static org.opengis.annotation.Specification.*;


/**
 * A surface patch that is defined by a set of boundary curves and an underlying surface to
 * which these curves adhere. The default is that the curves are coplanar and the polygon
 * uses planar interpolation in its interior.
 *
 * @version <A HREF="http://www.opengeospatial.org/standards/as">ISO 19107</A>
 * @author Martin Desruisseaux (IRD)
 * @since GeoAPI 2.0
 */
@UML(identifier="GM_Polygon", specification=ISO_19107)
public interface Polygon extends SurfacePatch {
    /**
     * The surface boundary that is the boundary of this polygon.
     * The boundary of a surface patch need not be in the same {@linkplain Complex complex}
     * as the containing {@linkplain Surface surface}. The curves that are contained in the
     * interior of the {@linkplain Surface surface} (act as common boundary to 2 surface patches)
     * are not part of any {@linkplain Complex complex} in which the {@linkplain Surface surface}
     * is contained. They are purely constructive and would not play in any topological relation
     * between {@linkplain Surface surface} and {@linkplain Curve curve} that defines the connectivity
     * of the {@linkplain Complex complex}.
     */
    @UML(identifier="boundary", obligation=MANDATORY, specification=ISO_19107)
    SurfaceBoundary getBoundary();

    /**
     * Provides a mechanism for spanning the interior of the polygon.
     * The spanning surface should have no boundary components that intersect the boundary of the
     * polygon, and there should be no ambiguity as to which portion of the surface is described by
     * the bounding curves for the polygon. The most common spanning surface is an elevation model,
     * which is not directly described in this standard, although {@linkplain Tin tins} and
     * {@linkplain GriddedSurface gridded surfaces} are often used in this role.
     */
    @UML(identifier="spanningSurface", obligation=OPTIONAL, specification=ISO_19107)
    List<Surface> getSpanningSurface();

    /**
     * Returns the patch which own this surface patch.
     */
    @UML(identifier="surface", obligation=MANDATORY, specification=ISO_19107)
    PolyhedralSurface getSurface();
}
