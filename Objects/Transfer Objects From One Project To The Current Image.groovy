/**
 * Script to transfer QuPath objects from one image in another project to the currently open image in this project 
 * the image names must match.
 * All objects in the source images should be imported into the destination image. 
 */
 
// The other project that has the annotations. It should have an image with the same name as the one currently open in QuPath.
projectWithAnnotations = Dialogs.promptForFile( "The other project that has the annotations", null," *.qpproj", ".qpproj" )

// Alternatively, put below the name of the image (in the other project) that you would like to import the annotations from
def name = getProjectEntry().getImageName()
// def name = "My Image.vsi - 20x"

// Get the project & the requested image name
def project = ProjectIO.loadProject( projectWithAnnotations, BufferedImage.class )

def entry = project.getImageList().find { it.getImageName().equals( name ) }
if ( entry == null ) {
    print 'Could not find image with name ' + name
    return
}

println 'Opened Hierarchy for ' + name
def otherHierarchy = entry.readHierarchy()
def pathObjects = otherHierarchy.getAnnotationObjects()

//clearAllObjects()
addObjects(pathObjects)
println(pathObjects)
print 'Done!'

