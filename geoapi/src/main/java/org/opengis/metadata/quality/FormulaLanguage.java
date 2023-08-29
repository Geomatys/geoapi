package org.opengis.metadata.quality;


import org.opengis.annotation.UML;
import org.opengis.util.CodeList;

import java.util.ArrayList;
import java.util.List;

import static org.opengis.annotation.Obligation.CONDITIONAL;
import static org.opengis.annotation.Specification.ISO_19157;

/**
 * Language in which the formula used for quality measure is coded.
 *
 * @author  Erwan Roussel (Geomatys)
 * @author  Martin Desruisseaux (Geomatys)
 * @version 4.0
 * @since   4.0
 */
@UML(identifier="FormulaLanguage", specification=ISO_19157)
public final class FormulaLanguage extends CodeList<FormulaLanguage> {
    /**
     * Serial number for compatibility with different versions.
     */
    private static final long serialVersionUID = 8953305311173050136L;


    /**
     * List of all enumerations of this type.
     * Must be declared before any enum declaration.
     */
    private static final List<FormulaLanguage> VALUES = new ArrayList<>(3);


    /**
     * JSON format used to represent mathematical formulas (see w3c.github for examples).
     */
    @UML(identifier="mathJSON", obligation=CONDITIONAL, specification=ISO_19157)
    public static final FormulaLanguage MATH_JSON = new FormulaLanguage("MATH_JSON");

    /**
     * XML language used to describe mathematical notations (see mozilla.org).
     */
    @UML(identifier="mathML", obligation=CONDITIONAL, specification=ISO_19157)
    public static final FormulaLanguage MATH_ML = new FormulaLanguage("MATH_ML");

    /**
     * Extensible standard used to represent semantics of mathematical objects (see openmath.org).
     */
    @UML(identifier="openMath", obligation=CONDITIONAL, specification=ISO_19157)
    public static final FormulaLanguage OPEN_MATH = new FormulaLanguage("OPEN_MATH");

    /**
     * Constructs an element of the given name. The new element is
     * automatically added to the list returned by {@link #values()}.
     *
     * @param name  the name of the new element. This name shall not be in use by another element of this type.
     */
    private FormulaLanguage(final String name) {
        super(name, VALUES);
    }

    /**
     * Returns the list of {@code FormulaLanguage}s.
     *
     * @return the list of codes declared in the current JVM.
     */
    public static FormulaLanguage[] values() {
        synchronized (VALUES) {
            return VALUES.toArray(FormulaLanguage[]::new);
        }
    }

    /**
     * Returns the list of codes of the same kind as this code list element.
     * Invoking this method is equivalent to invoking {@link #values()}, except that
     * this method can be invoked on an instance of the parent {@code CodeList} class.
     *
     * @return all code {@linkplain #values() values} for this code list.
     */
    @Override
    public FormulaLanguage[] family() {
        return values();
    }

    /**
     * Returns the evaluation method type that matches the given string, or returns a
     * new one if none match it. More specifically, this method returns the first instance for
     * which <code>{@linkplain #name() name()}.{@linkplain String#equals equals}(code)</code>
     * returns {@code true}. If no existing instance is found, then a new one is created for
     * the given name.
     *
     * @param  code  the name of the code to fetch or to create.
     * @return a code matching the given name.
     */
    public static FormulaLanguage valueOf(String code) {
        return valueOf(FormulaLanguage.class, code);
    }

}
