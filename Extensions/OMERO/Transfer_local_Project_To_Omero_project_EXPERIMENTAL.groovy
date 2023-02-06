/* 
 * Transfer all objects & metadata from another QuPath project, matching images name to set objects to the 
 * correct image.
 * All objects & metadata in the source images should be imported into the destination images. 
 *
 * @author Olivier Burri & Remy Dornier
 * @date 2023-01-20
 * Last tested on QuPath-0.4.1
 */
 
 
 /*************************************************************
 * 
 ****************** Variables to change ******************
 * 
 * **********************************************************/
 
// Remove objects from the active ImageEntry or keep it as is, and just add?
def deleteExisting = true

// The other project that has the annotations. 
// It should have an image with the SAME NAME as the one currently open in QuPath.
def omeroProject = "D:\\Remy\\QuPath\\Migration Local-OMERO\\LocalProject\\project.qpproj"


/*************************************************************
 * 
 ****************** Beginning of the script ******************
 * 
 * **********************************************************/



// Get the project & the requested image name
def project = ProjectIO.loadProject(new File(omeroProject), BufferedImage.class)

//START OF SCRIPT
getProject().getImageList().each { omeroImage->
    // get the name of theimage comming from omero
    def name = omeroImage.getImageName()
    def omeroShorterName = name.replace(" ","").replace("-","").replace("[","").replace("]","")
    
    // open QuPath project with local images
    def localEntry = project.getImageList().find { it.getImageName().replace(" ","").replace("-","").replace("[","").replace("]","").equals( omeroShorterName ) }
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
    if ( pathObject.hasChildren() ) {
        newObject.addPathObjects(pathObject.getChildObjects().collect{ transformObject( it, copyMeasurements ) } )
    }
    return newObject
}