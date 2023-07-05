import qupath.ext.biop.servers.omero.raw.*
import qupath.lib.scripting.QP
import omero.gateway.model.DatasetData;

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
 *  - Should run like this :)
 *  
 * = AUTHOR INFORMATION =
 * Code written by Rémy Dornier, EPFL - SV -PTECH - BIOP 
 * 11.03.2023
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
 *  - 2023-04-19 : update for HCS data
 *  - 2023.07.05 : close the imageServer to release OMERO ressources
 *  
*/


/**
 * Add image hierarchy (project/dataset or screen/plate/well) as qp metadata fields
 **/
 
 
def qpproject = getProject()
for (entry in qpproject.getImageList()) {
    def imageData = entry.readImageData()
    
    // get the current displayed image on QuPath
    ImageServer<?> server = imageData.getServer()  

    // check if the current server is an OMERO server. If not, throw an error
    if((server instanceof OmeroRawImageServer)){
        
        // get the parent container
        def parentList = OmeroRawTools.getParent(server.getClient(), "Image", server.getId())
        if(parentList.size() > 0){
            def parent = parentList.get(0)
            
            // if parent is a dataset
            if(parent instanceof DatasetData) {
                entry.putMetadataValue("Dataset", parent.getName());
            
                // get parent project
                def projectList = OmeroRawTools.getParent(server.getClient(), "Dataset", parent.getId())
                if(projectList.size() > 0){
                    def project = projectList.get(0)
                    entry.putMetadataValue("Project", project.getName());
                }
                   
            } else {
                // if the parent is a well
                def wellName = "" + (char)(parent.getRow() + 65) + (parent.getColumn() < 9 ? ""+ 0 + (parent.getColumn() + 1) : ""+(parent.getColumn() + 1))
                entry.putMetadataValue("Well", wellName);
                
                // get the parent plate
                def plateList = OmeroRawTools.getParent(server.getClient(), "Well", parent.getId())
                if(plateList.size() > 0){
                    def plate = plateList.get(0)

                    entry.putMetadataValue("Plate", plate.getName());
                    
                    // get the parent screen
                    def screenList = OmeroRawTools.getParent(server.getClient(), "Plate", plate.getId())
                    
                    if(screenList.size() > 0){
                        def screen = screenList.get(0)
                        entry.putMetadataValue("Screen", screen.getName());
                    }
                }
            } 
        }
        
        println "The current image "+server.getId()+" has been processed \n"
        server.close()
    }
}
 
// display success
println "Metadata imported and updated in QuPath \n"



