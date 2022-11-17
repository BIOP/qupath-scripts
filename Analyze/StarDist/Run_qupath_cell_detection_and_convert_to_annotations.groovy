/**
 * This script will run the cell detection on all the annotations of the image, 
 * and then reconvert each cell nucleus into an annotation.
 * NOTE: It will delete all non-rectangle annotations before starting.
 * This is so you can run it multiple times on the same image withougt accumulating annotations.
 * Careful if you created non-rectangular annotations.
 *
 * @author Olivier Burri
 * Date: 2020.09.15
 */

clearDetections()

// Delete all non-rectangle annotations
def toDelete = getAllObjects().findAll{ !(it.getROI() instanceof RectangleROI ) }
removeObjects( toDelete, false )

// Put the cell detection script between the accolades {}
cellDetection = { runPlugin('qupath.imagej.detect.cells.WatershedCellDetection', '{"detectionImage": "DAPI",  "requestedPixelSizeMicrons": 0.1302,  "backgroundRadiusMicrons": 5.0,  "medianRadiusMicrons": 0.0,  "sigmaMicrons": 2.4,  "minAreaMicrons": 40.0,  "maxAreaMicrons": 1000.0,  "threshold": 1600.0,  "watershedPostProcess": true,  "cellExpansionMicrons": 1.1,  "includeNuclei": true,  "smoothBoundaries": true,  "makeMeasurements": true}'); }

// Script starts below

// Select all annotations, ideally 
selectAnnotations()

// Run the cell detection
cellDetection.run()

// Get all the cells and turn them into annotations, so they can be edited

def cells = getCellObjects()

def annotations = cells.collect {
    def roi = it.getNucleusROI()
    def a = PathObjects.createAnnotationObject( roi, null )
}

//Remove the cells and replace them with the annotations
removeObjects( cells, false )
addObjects( annotations )

fireHierarchyUpdate()

println "The deed is done."

import qupath.lib.roi.RectangleROI