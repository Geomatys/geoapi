/*************************************************************************************************
**
** $Id$
**
** $Source$
**
** Copyright (C) 2003-2005 Open GIS Consortium, Inc.
** All Rights Reserved. http://www.opengis.org/legal/
**
*************************************************************************************************/
package org.opengis.coverage.processing;

//J2SE dependencies
import java.util.Collection;

//OpenGIS direct dependencies
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.coverage.Coverage;
import org.opengis.coverage.SampleDimensionType; // For javadoc
import org.opengis.coverage.MetadataNameNotFoundException;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.parameter.ParameterNotFoundException;
import org.opengis.parameter.InvalidParameterNameException;
import org.opengis.parameter.InvalidParameterValueException;

//Annotations
import org.opengis.annotation.UML;
import static org.opengis.annotation.Obligation.*;
import static org.opengis.annotation.Specification.*;


/**
* Provides operations for different ways of accessing the grid coverage values as well as
* image processing functionality. The list of available processing operations is implementation
* dependent. The interface has a discovery mechanism to determine the available processing
* operations.
*
* These processing operations will transform values within a single sample dimension, and
* leave the values in other sample dimensions unaffected. The modified sample dimension may
* also change its type (e.g. from {@link SampleDimensionType#UNSIGNED_4BITS UNSIGNED_4BITS} to
* {@link SampleDimensionType#UNSIGNED_1BIT UNSIGNED_1BIT}). The actual underlying grid data
* remains unchanged.
* <p>
* The interface has been designed to allow the adaptations to be done in a "pipe-lined" manner.
* The interface operates on {@link GridCoverage} to create new a {@link GridCoverage}. The
* interface does not need to make a copy of the source grid data. Instead, it can return a
* grid coverage object which applies the adaptations on the original grid coverage whenever
* a block of data is requested. In this way, a pipeline of several grid coverages can be
* constructed cheaply.
* <p>
* This interface can perform any of the following:
* <ul>
*   <li>Change the number of bands being accessed.</li>
*   <li>Change the value sequencing in which the grid values are retrieved.</li>
*   <li>Allow re-sampling of the grid coverage for a different geometry.
*       Creating a new {@link GridCoverage} with different grid geometry allows for reprojecting
*       the grid coverage to another projection and another georeferencing type, resampling to
*       another cell resolution and subsetting the grid coverage.</li>
*   <li>Modify the way the grid values are accessed (filtered, classified...).</li>
*   <li>Change the interpolation method used when evaluating points which fall between grid cells.</li>
*   <li>Filtering.</li>
*   <li>Image enhancements.</li>
*   <li><i>etc.</i></li>
* </ul>
*
* <P>&nbsp;</P>
* <TABLE WIDTH="80%" ALIGN="center" CELLPADDING="18" BORDER="4" BGCOLOR="#FFE0B0">
*   <TR><TD>
*     <P align="justify"><STRONG>WARNING: THIS CLASS IS CHANGING AND UNDER DISCUSSION.</STRONG> 
*     Current API is derived from OGC
*     <A HREF="http://www.opengis.org/docs/01-004.pdf">Grid Coverages Implementation specification 1.0</A>.
*     We plan to replace it by new interfaces derived from ISO 19123 (<CITE>Schema for coverage geometry
*     and functions</CITE>). Current interfaces should be considered as legacy and are included in this
*     distribution only because they were part of GeoAPI 1.0 release. We will try to preserve as much 
*     compatibility as possible, but no migration plan has been determined yet.</P>
*   </TD></TR>
* </TABLE>
*
* @version <A HREF="http://www.opengis.org/docs/01-004.pdf">Grid Coverage specification 1.0</A>
* @author Martin Desruisseaux (IRD)
* @author Wim Koolhoven
* 
* @todo The interface GridCoverage does no longer exist in geoapi 2.0, thus this class has to change.  
* Proposal to rename GridCoverageProcessor to CoverageProcessor and remove the limitation
* of only allowing GridCoverages.
* This is comparable to a SV_Service (see ISO19119) which has a set of SV_Operation  
* and a SV_ServiceIdentification has a set of SV_OperationMetaData
* Should this be integrated with the specs of ISO19119 or can it stay seperately?
*/
@UML(identifier="GP_GridCoverageProcessor", specification=OGC_01004)
public interface CoverageProcessor {
   /**
    * Retrieves the list of metadata keywords for the interface.
    * An empty list will returned if no metadata is available.
    *
    * @return The list of metadata keywords for the interface.
    */
   @UML(identifier="metadataNames", obligation=MANDATORY, specification=OGC_01004)
   String[] getMetadataNames();

   /**
    * Retrieves the metadata value for a given metadata name.
    *
    * @param  name Metadata keyword for which to retrieve metadata.
    * @return The metadata value for a given metadata name.
    * @throws MetadataNameNotFoundException if there is no value for the specified metadata name.
    */
   @UML(identifier="getMetadataValue", obligation=MANDATORY, specification=OGC_01004)
   String getMetadataValue(String name) throws MetadataNameNotFoundException;

   /**
    * The number of operations supported by the {@code GridCoverageProcessor}.
    *
    * @return The number of operations supported by the {@code GridCoverageProcessor}.
    *
    * @deprecated Use {@link #getOperations} instead.
    */
//   removed deprecated function because the interface name is changing anyway, no need to stay compatible   
//   @Deprecated
//   @UML(identifier="numOperations", obligation=MANDATORY, specification=OGC_01004)
//   int getNumOperations();

   /**
    * Retrieves a grid processing operation information.
    * The operation information will contain the name of the operation as well
    * as a list of its parameters.
    *
    * @param  index Index for which to retrieve the operation information.
    * @return A grid processing operation information.
    * @throws IndexOutOfBoundsException if {@code index} is out of bounds.
    *
    * @deprecated Use {@link #getOperations} instead.
    */
// removed deprecated function because the interface name is changing anyway, no need to stay compatible   
//   @Deprecated
//   @UML(identifier="getOperation", obligation=MANDATORY, specification=OGC_01004)
//   Operation getOperation(int index) throws IndexOutOfBoundsException;

   /**
    * Retrieves grid processing operations information.
    * Each operation information will contain the name of the operation as well
    * as a list of its parameters.
    */
   @UML(identifier="getOperation", obligation=MANDATORY, specification=OGC_01004)
   Collection<Operation> getOperations();

   /**
    * Creates a {@link GridAnalysis} interface from a grid coverage.
    * This allows grid analysis functions to be performed on a grid coverage.
    *
    * @param  gridCoverage Grid coverage on which the analysis will be performed.
    * @return A new {@link GridAnalysis} interface.
    * @todo return type has to change when not only grid coverages but all coverages are allowed.
    * Analysis will in some way depend on the type of coverage, 
    * do we want to specify all variants or should we delay this till a later moment?
    */
   @UML(identifier="analyze", obligation=MANDATORY, specification=OGC_01004)
   GridAnalysis analyze(Coverage coverage);

   /**
    * Applies a process operation to a coverage.
    *
    * @param  operationName Name of the operation to be applied to the coverage.
    * @param  parameters List of name value pairs for the parameters required for the operation.
    * @return The grid coverage which has been applied the process operation.
    * @throws OperationNotFoundException if {@code name} is not a know operation.
    * @throws ParameterNotFoundException if a parameter was required for the operation but was
    *         not provided in the {@code parameters} list.
    * @throws InvalidParameterNameException if a parameter doesn't have a recognized name.
    * @throws InvalidParameterValueException if a parameter doesn't have a valid value.
    * @todo question: why was this function deprecated?
    */
   @UML(identifier="doOperation", obligation=MANDATORY, specification=OGC_01004)
   Coverage doOperation(String operationName, GeneralParameterValue[] parameters)
           throws OperationNotFoundException, ParameterNotFoundException,
                  InvalidParameterNameException, InvalidParameterValueException;
}