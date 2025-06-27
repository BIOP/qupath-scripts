/* 
 * Transfer all objects from another QuPath project to the current Image Entry
 * Script to transfer QuPath objects from one image in another project to the currently open image in this project,
 * All objects in the source images should be imported into the destination image. 
 *
 * @author Olivier Burri
 * @date 2022.11.03
 * Last tested on QuPath-0.6.0
 */
 
// Remove objects from the active ImageEntry or keep it as is, and just add?
def deleteExisting = true

// The other project that has the annotations. 
// It should have an image with the SAME NAME as the one currently open in QuPath.
def projectWithAnnotations = "C:/Users/dornier/Desktop/New folder - Copy (2)/project.qpproj"

//START OF SCRIPT
def name = getProjectEntry().getImageName()

// Get the project & the requested image name
def project = ProjectIO.loadProject(new File( projectWithAnnotations ), BufferedImage.class )

def entry = project.getImageList().find { it.getImageName().equals( name ) }
if ( entry == null ) {
    println 'Could not find image with name ' + name
    return
}

println 'Opening Hierarchy for ' + name
def otherHierarchy = entry.readHierarchy()

def pathObjects = otherHierarchy.getRootObject().getChildObjects()

// Start transfer
if (deleteExisting)
    removeAllObjects()

// Use the transformObject to read everything in. It is borrowed from transfering objects with an affine transform
def newObjects = []
for (pathObject in pathObjects) {
    newObjects << transformObject(pathObject, true)
}
addObjects(newObjects)

fireHierarchyUpdate()

print 'Done!'

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