package org.opengis.metadata.quality;

import org.opengis.annotation.UML;
import org.opengis.util.InternationalString;

import static org.opengis.annotation.Obligation.MANDATORY;
import static org.opengis.annotation.Specification.ISO_19157;

/**
 * Description of the formula used for quality measure.
 *
 * @author  Erwan Roussel (Geomatys)
 * @author  Martin Desruisseaux (Geomatys)
 * @version 4.0
 * @since   4.0
 */
@UML(identifier="FormulaType", specification=ISO_19157)
public interface FormulaType {
    /**
     * Formula explanation.
     *
     * @return text description of formula explanation.
     */
    @UML(identifier="key", obligation=MANDATORY, specification=ISO_19157)
    InternationalString getKey();

    /**
     * Language in which the formula is expressed.
     *
     * @return formula language description.
     */
    @UML(identifier="language", obligation=MANDATORY, specification=ISO_19157)
    FormulaLanguage getLanguage();

    /**
     * Language version in which the formula is expressed.
     *
     * @return text description.
     */
    @UML(identifier="languageVersion", obligation=MANDATORY, specification=ISO_19157)
    InternationalString getLanguageVersion();

    /**
     * Formula expression in the chosen language.
     *
     * @return text description of the formula.
     */
    @UML(identifier="mathematicalFormula", obligation=MANDATORY, specification=ISO_19157)
    InternationalString getMathematicalFormula();

}
