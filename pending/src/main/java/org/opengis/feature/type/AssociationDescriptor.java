package org.opengis.feature.type;


/**
 * Indicating a named association.
 * <p>
 * This class carries the ComplexType specific information requried
 * for using an association. Name, type and multiplicity are defined.
 * <p>
 * The goal of associations is to allow for a graph of data, this is contrast
 * to attribtues which indicate containement.
 * <p>
 * Please see the description of AssociationType for more guidelines on capturing
 * your data modeling needs with association.
 * </p>
 * @author Jody Garnett, Refractions Research
 */
public interface AssociationDescriptor<T extends AttributeType, A extends AssociationType<T>> extends PropertyDescriptor {
	
	/** Captures cadinality */
	public int getMinOccurs();
	
	/** Captures cadinality */
	public int getMaxOccurs();
	
	/** Indicates the type of this attribute */
	A getType();

	/**
	 * Indicates Name of defined attribute in a ComplexType, this method may 
	 * never return a null value.
	 */
	Name getName();	
}