// Transfer Annotations

// Image from which to transfer all annotatins from
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
