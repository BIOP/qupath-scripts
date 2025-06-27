/**
 * Can be useful when generating ground truths for training
 * Most exporters we have only export annotations and not detections
 * 
 * @author Olivier Burri
 * Last tested on QuPath-0.6.0
 */


def cells = getDetectionObjects()

def annots = cells.collect{ cell ->
    return PathObjects.createAnnotationObject( cell.getROI(), cell.getPathClass() )
    }
removeDetections()

addObjects(annots)