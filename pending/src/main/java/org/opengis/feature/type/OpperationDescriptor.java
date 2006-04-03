package org.opengis.feature.type;

import java.lang.reflect.InvocationTargetException;

import org.opengis.feature.Attribute;

/**
 * Indicating an implementation of a opperation type for a specific type.
 * <p>
 * This class carries the ComplexType specific information requried
 * for using an opperation. Name, type and evaulation are defined.
 * <p>
 * Please see the description of OpperationType for more guidelines on capturing
 * opperations for use with your model.
 * </p>
 * @author Jody Garnett, Refractions Research
 */
public interface OpperationDescriptor<B, T extends AttributeType<B>, O extends OpperationType<B,T>> extends PropertyDescriptor {
	
	/** Indicates the type of this attribute */
	O getType();

	/**
	 * Indicates Name of defined opperation in a ComplexType, this method may 
	 * never return a null value.
	 */
	Name getName();
	
	/**
	 * Indicates if invoke may be called.
	 * <p>
	 * In order allow for faithful modeling of a software system we will need
	 * construct models dynamically at runtime, possibly when no implementation
	 * has been discouvered for the required opperations. (for example we may be
	 * modeling a schema where the opperations are only available when the information
	 * is executed on a remote web processing service. We still need to track the
	 * opperations, even those we cannot execute them in the current environment.
	 * </p>
	 * @return true if invoke may be called.
	 */
	boolean isImplemented();
	
	/**
	 * Call the opperation with an attribtue and a set of parameters.
	 * <p>
	 * The state of the opperation may be used and / or updated during
	 * the execution of the opperation.
	 * </p>
	 * <p>
	 * Please check to ensure that isImplemented returns <code>true</code> before
	 * calling invoke.
	 * 
	 * @param anAttribute This is the attribute used for context when evaulating the opperation
	 * @param params
	 * @return the result of the opperation
	 * @throws InvoationTargetException if an error occured while processing
	 */
	Object invoke( Attribute<B,T> anAttribute, Object params[] ) throws InvocationTargetException;
	
}