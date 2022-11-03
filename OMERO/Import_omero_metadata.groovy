import qupath.ext.biop.servers.omero.raw.*
import qupath.lib.scripting.QP

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
 *  - If no key-values are attached to your image on OMERO, you can add a key-value and then run the script.
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
 *  
*/


/**
 * There is three implementations of the method 
 * 
 * 		1. importOmeroKeyValues(server) ===> import OMERO key value pairs WITHOUT updating current QuPath metadata
 * 
 *  	2. addMetadata(metadata) ===> Add new metadata to the image in QuPath
 *  	
 *  	3. addAndUpdateMetadata(metadata) ===> Update current metadata in QuPath and add new ones
 *  	
 *  	4. addAndDeleteMetadata(metadata) ===> Delete all existing metadata in QuPath (for the current image only) and add new ones
 * 
 * 		5. addOmeroKeyValues(server) ===> import OMERO key value pairs and add them as metadata to the current image in QuPath
 * 		
 * 		6. addOmeroKeyValuesAndUpdateMetadata(server) ===> import OMERO key value pairs, update current metadata in QuPath and 
 * 		add new ones as metadata to the current image in QuPath
 * 		
 * 		7. addOmeroKeyValuesAndDeleteMetadata(server) ===> import OMERO key value pairs, delete all current metadata in QuPath and 
 * 		add new ones as metadata to the current image in QuPath
 * 
 */



/**
 * Import key value pairs to qupath from OMERO as metadata, 
 * attached to the current opened image.
 * 
 **/
 
// get the current displayed image on QuPath
ImageServer<?> server = QP.getCurrentServer()

// check if the current server is an OMERO server. If not, throw an error
if(!(server instanceof OmeroRawImageServer)){
	Dialogs.showErrorMessage("Key value pairs importation","Your image is not from OMERO ; please use an image that comes from OMERO to use this script");
	return
}
		
		// OPTION 1 === import metadata from OMERO 
		Map<String,String> metadata = OmeroRawScripting.importOmeroKeyValues(server)
		OmeroRawScripting.addAndUpdateMetadata(metadata)
		
		// OPTION 2 === import metadata from OMERO 
		OmeroRawScripting.addOmeroKeyValuesAndUpdateMetadata(server)

// display success
println "Metadata imported and updated in QuPath \n"



