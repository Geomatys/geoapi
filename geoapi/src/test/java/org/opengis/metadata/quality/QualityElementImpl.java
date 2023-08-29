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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


/**
 * A simple implementation of {@link QualityElement} for testing purposes.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @version 3.1
 * @since   3.1
 */
final class QualityElementImpl implements QualityElement {
    /**
     * Information about the evaluation method, or {@code null} if none.
     */
    private final EvaluationMethod method;

    /**
     * Creates a new element.
     *
     * @param  method  information about the evaluation method, or {@code null} if none.
     */
    QualityElementImpl(final EvaluationMethod method) {
        this.method = method;
    }

    /**
     * Returns the evaluation information.
     */
    @Override
    public List<EvaluationMethod> getEvaluationMethods() {
        List<EvaluationMethod> methods = new ArrayList<>();
        methods.add(method);
        return methods;
    }

    @Override
    public EvaluationMethod getEvaluationMethod() {
        return method;
    }

    /**
     * No yet implemented.
     */
    @Override
    public MeasureReference getMeasureReference() { return null; }


    /**
     * No yet implemented.
     */
    @Override
    public Collection<? extends QualityResult> getQualityResults() {
        return Collections.emptyList();
    }
}
