/**
 * create new annotations by merging the detections of the same class together.
 * 
 * @author Rémy Dornier
 * @date 2025.06.27
 * Last tested on QuPath-0.6.0
 */ 
 


// get detections
def detections = getDetectionObjects()
Map<String, List<PathObject>> detectionsToMerge = new HashMap<>()

// convert detections to annotations
def annots = detections.collect{ cell ->
    return PathObjects.createAnnotationObject( cell.getROI(), cell.getPathClass() )
    }
removeDetections()
addObjects(annots)

// group annotations per class
annots.each {
   def pathClass = it.getPathClass()
   def detList = []
   if(detectionsToMerge.containsKey(pathClass)) {
       detList = detectionsToMerge.get(pathClass)
   }
   detList.add(it)
   detectionsToMerge.put(pathClass, detList)   
}

// merge annotations of the same class together.
detectionsToMerge.keySet().each {
   def detList = detectionsToMerge.get(it)
   selectObjects(detList)
   mergeSelectedAnnotations()
}

