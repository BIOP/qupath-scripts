def cells = getDetectionObjects()

def annots = cells.collect{ cell ->
    return PathObjects.createAnnotationObject( cell.getROI() )
    }
clearDetections()

addObjects(annots)