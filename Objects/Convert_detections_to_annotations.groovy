// Can be useful when generating ground truths for training
// Most exporters we have only export annotations and not detections
// @author Olivier Burri

def cells = getDetectionObjects()

def annots = cells.collect{ cell ->
    return PathObjects.createAnnotationObject( cell.getROI(), cell.getPathClass() )
    }
clearDetections()

addObjects(annots)