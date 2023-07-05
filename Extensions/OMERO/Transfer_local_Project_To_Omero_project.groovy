/* 
 * Transfer all objects & metadata from another QuPath project, matching images name to set objects to the 
 * correct image.
 * All objects & metadata in the source images should be imported into the destination images. 
 * 
 * Step by step tuto
 * 1. Open the project containing images coming from OMERO
 * 2. Open this script
 * 3. Change the "localProjectPath" variable with the path to your qp project containing local images
 * 4. Run the script
 * 
 * 5. For MAC users, if your project is located on a server, then the path should begin with /Volumes/...
 *
 * @author Olivier Burri & Remy Dornier
 * @date 2023-04-05
 * Last tested on QuPath-0.4.3
 * 
 * History 
 *  - 2023.07.05 : close the imageServer to release OMERO ressources
 */
 
 
 /*************************************************************
 * 
 ****************** Variables to change ******************
 * 
 * **********************************************************/
 
// Remove objects from the active OMERO ImageEntry or keep it as is, and just add?
def deleteExisting = true

// The local project path that has the annotations. 
// Image name in the local project must match the ones in the OMERO project
def localProjectPath = "D:\\Remy\\QuPath\\Migration Local-OMERO\\LocalProject\\project.qpproj"


/*************************************************************
 * 
 ****************** Beginning of the script ******************
 * 
 * **********************************************************/


// Get the local project
def localProject = ProjectIO.loadProject(new File(localProjectPath), BufferedImage.class)

//START OF SCRIPT
getProject().getImageList().each { omeroImage->
    // get the name of the image comming from OMERO
    def name = omeroImage.getImageName()
    def omeroShorterName = name.replace(" ","").replace("-","").replace("[","").replace("]","")
    
    // open QuPath project with local images
    def localEntry = localProject.getImageList().find { it.getImageName().replace(" ","").replace("-","").replace("[","").replace("]","").equals( omeroShorterName ) }
    if ( localEntry == null ) {
        println 'Could not find image with name ' + name
        return
    }
    
    // read local image hierarchy
    println 'Opening Hierarchy for ' + localEntry.getImageName()
    def localHierarchy = localEntry.readHierarchy()
    def localPathObjects = localHierarchy.getRootObject().getChildObjects()
    
    // get omeroProject image's hierarchy and imageData
    def omeroImageData = omeroImage.readImageData()
    def omeroHierarchy = omeroImageData.getHierarchy()
    
    // delete omero image hierarchy & metadata
    if (deleteExisting) {
        println 'Delete previous hierarchy '
        omeroHierarchy.clearAll();
        omeroImage.clearMetadata();
    }
    
    // Use the transformObject to read everything in. It is borrowed from transfering objects with an affine transform
    def omeroNewObjects = []
    for (pathObject in localPathObjects) {
        omeroNewObjects << transformObject(pathObject, true)
    }

    // local hierarchy to omero hierarchy
    omeroHierarchy.addObjects(omeroNewObjects)
    omeroImage.saveImageData(omeroImageData)
    fireHierarchyUpdate(omeroHierarchy)
    
    // set metadata on omero image
    def metadata = localEntry.getMetadataMap()
    metadata.each{omeroImage.putMetadataValue(it.getKey(), it.getValue())}
    
    // close the hidden imageServer
    omeroImageData.getServer().close()
    
    print 'Done! for image  '+name
}

/**
 * Transform object, recursively transforming all child objects
 *
 * @param pathObject
 * @param transform
 * @return
 */
PathObject transformObject( def pathObject, boolean copyMeasurements ) {
    def newObject = PathObjectTools.transformObject(pathObject, null, copyMeasurements)
    if ( pathObject.getName() != null ) newObject.setName( pathObject.getName() )
    // Handle child objects
    if ( pathObject.hasChildObjects() ) {
        newObject.addChildObjects(pathObject.getChildObjects().collect{ transformObject( it, copyMeasurements ) } )
    }
    return newObject
}