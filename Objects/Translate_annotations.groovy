/* 
 * Translate annotations from one point to another
 * @author Rémy Dornier
 * @date 2025.06.19
 * Last tested on QuPath-0.6.0
 * 
 * 
 * Description
 * - Only works on annotations
 * - Select, in the pool of existing annotations, one reference annotation and
 *     - Give it a name 
 * - Create a new annotation and move it to the right position and 
 *     - LOCK it => WARNING : ONLY this annotation should be locked !
 *     - give it the same name as the reference annotation
 * - Run the script
 * 
 */


// read current object
def objects = getAnnotationObjects()

// get transformed object
def transformedObject = null
objects.each {
    if(it.isLocked()) {
        transformedObject = it
    }
}

if(transformedObject == null) {
    println "Cannot find the transformed object in the annotation list. It should be the only one locked"
    return
}

// get reference object
def refObject = null
objects.each {
    if(transformedObject.getName() == it.getName() && !it.isLocked()) {
        refObject = it
    }
}

// get the transformed centroid
def transformedX = transformedObject.getROI().getCentroidX()
def transformedY = transformedObject.getROI().getCentroidY()

// get the reference centroid
def referenceX = refObject.getROI().getCentroidX()
def referenceY = refObject.getROI().getCentroidY()

// get the vector direction
def moveX = transformedX - referenceX
def moveY = transformedY - referenceY

// remove reference object
objects = objects.collect{e-> 
    if(!e.isLocked()) {
       return e 
  }
}.findAll()


// translate objects
def transformedObjects = []
def cal = getCurrentImageData().getServer().getPixelCalibration();
objects.each {annotation->
    def transformedRoi = annotation.getROI().translate(moveX, moveY)
    def transformedAnnotation = PathObjects.createAnnotationObject(transformedRoi, annotation.getPathClass(), null);
    ObjectMeasurements.addShapeMeasurements(transformedAnnotation, cal);
    transformedAnnotation.setName(annotation.getName())
    transformedObjects.add(transformedAnnotation)
}

// adding translated objects and removing previous ones
addObjects(transformedObjects)
removeObjects(objects, false)


// imports
import qupath.ext.biop.utils.Results
import qupath.lib.objects.PathObject;
import qupath.lib.analysis.features.ObjectMeasurements;