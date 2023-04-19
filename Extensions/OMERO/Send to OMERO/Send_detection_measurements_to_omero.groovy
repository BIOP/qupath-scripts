import qupath.ext.biop.servers.omero.raw.*
import qupath.lib.scripting.QP
import qupath.lib.gui.measure.ObservableMeasurementTableData;

/*
 * = DEPENDENCIES =
 *  - qupath-extension-biop-omero - latest version : https://github.com/BIOP/qupath-extension-biop-omero/releases
 *
 * = REQUIREMENTS =
 *  - A project must be open in QuPath
 *  - The connection to omero-server.epfl.ch needs to be established (with credentials) before running the script
 *  - The current image should have been imported from OMERO.
 *  
 * = TO MAKE THE SCRIPT RUN =
 *  - You need to have some detections on the current image. 
 *  ---- If it is not the case, create a rectangle annotation. 
 *  ---- click on Analyze->cell detection->cell detection
 *  ---- click on Run (modify parameters to make some detections appear)
 *  - Run the script. 
 *  
 * = AUTHOR INFORMATION =
 * Code written by Rémy Dornier, EPFL - SV -PTECH - BIOP 
 * 20.10.2022
 * 
 * = COPYRIGHT =
 * © All rights reserved. ECOLE POLYTECHNIQUE FEDERALE DE LAUSANNE, Switzerland, BioImaging And Optics Platform (BIOP), 2022
 * 
 * Licensed under the BSD-3-Clause License:
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided 
 * that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer 
 *    in the documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products 
 *     derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, 
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; 
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, 
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF 
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * History
 *  - 2022-11-03 : update documentation 
 *  - 2023-04-19 : add file deletion and update documentation
 * 
*/


/**
 * There is many implementations to send pathObjects to OMERO : 
 * 
 * /// sending an OMERO.table 
 * 		1. sendDetectionMeasurementTable(pathObjects, server, image_data) ==> send measurement table with only the specified pathObjects 
 * 		2. sendDetectionMeasurementTable(server, image_data) ==> send measurement table with all detections
 * 		
 * /// sending a csv file
 * 		1. sendDetectionMeasurementTableAsCSV(pathObjects, server, image_data) ==> send measurement table with only the specified pathObjects 
 * 		2. sendDetectionMeasurementTableAsCSV(server, image_data) ==> send measurement table with all detections
 * 
 * /// delete files
 *              1. deleteDetectionFiles(server)
 *              2. deleteDetectionFiles(server, fileAnnotationData)
 * 
 * /// readOmeroFile
 *              1. readFilesAttachedToCurrentImageOnOmero(server)
 * 
 */
 

/**
 * Send the detection measurement table to OMERO as an OMERO.table and a csv file, 
 * attached to the current opened image.
 * 
 **/

// get the current displayed image on QuPath
ImageServer<?> server = QP.getCurrentServer()

// check if the current server is an OMERO server. If not, throw an error
if(!(server instanceof OmeroRawImageServer)){
	Dialogs.showErrorMessage("Measurement table sending","Your image is not from OMERO ; please use an image that comes from OMERO to use this script");
	return
}

// get all detection objects
Collection<PathObject> pathObjects = QP.getDetectionObjects()

// get image data
def imageData = QP.getCurrentImageData()


/** 
 *                 Option 1
 * 1. first, read the files on the current image.
 * 2. send detection tables
 * 3. Delete previous file versions
 **/
    def files
    if(deleteTable) {
        files = OmeroRawScripting.readFilesAttachedToCurrentImageOnOmero(server)
    }
    
    // send the table to OMERO as OMERO.table
    boolean tableWasSent = OmeroRawScripting.sendDetectionMeasurementTable(pathObjects, server, imageData);
    boolean csvWasSent = OmeroRawScripting.sendDetectionMeasurementTableAsCSV(pathObjects, server, imageData);
    
    if(deleteTable) {
        OmeroRawScripting.deleteDetectionFiles(server, files)
    }
    
    if(tableWasSent && csvWasSent)
    	println "Detection table sent to OMERO as OMERO.table"
    else
    	println "An issue occurs when trying to send table to OMERO"



/** 
 *                 Option 2
 * 1. first, delete all annotation and detection files related to the current QuPath project
 * 2. send annotations and detection tables
 **/
    /*if(deleteTable) {
        OmeroRawScripting.deleteDetectionFiles(server)
    }
    
    // send the table to OMERO as OMERO.table
    boolean tableWasSent = OmeroRawScripting.sendDetectionMeasurementTable(pathObjects, server, imageData);
    boolean csvWasSent = OmeroRawScripting.sendDetectionMeasurementTableAsCSV(pathObjects, server, imageData);
    
    
    if(tableWasSent && csvWasSent)
    	println "Detection table sent to OMERO as OMERO.table"
    else
    	println "An issue occurs when trying to send table to OMERO"*/



