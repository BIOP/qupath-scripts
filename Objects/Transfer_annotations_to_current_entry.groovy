/* 
 * Transfer annotations from a given ImageEntry to the current Entry 
 * @author Olivier Burri
 * @date 20221103
 * Last tested on QuPath-0.3.2
 */

// Image from which to transfer all annotations from
name = "Image_03.vsi - 20x"


// Script Start
def project = getProject()

project.getImageList().each { entry ->
    if( entry.getImageName() =~ name ) {

    def hierarchy = entry.readHierarchy()
    def objects = hierarchy.getAnnotationObjects()
    if( objects.size() > 0 )
        println "Adding "+objects
        addObjects(objects)
    }
}
fireHierarchyUpdate()
