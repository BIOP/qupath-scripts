import qupath.ext.biop.cellpose.Cellpose2D

clearDetections()

// Specify the model name (cyto, nuc, cyto2, omni_bact or a path to your custom model)
def pathModel_cyto = 'cyto2'
def cellpose_cyto = Cellpose2D.builder( pathModel_cyto )
        .channels("HCS","DAPI")
        .pixelSize( 0.3 )              // Resolution for detection
        .diameter(30)                  // Median object diameter. Set to 0.0 for the `bact_omni` model or for automatic computation
        .measureShape()                // Add shape measurements
        .measureIntensity()            // Add cell measurements (in all compartments) 
        .useGPU()
        .build()

def pathModel_nuc = 'cyto2'
def cellpose_nuc = Cellpose2D.builder( pathModel_nuc )
        .channels("DAPI")
        .pixelSize( 0.3 )              // Resolution for detection
        .diameter(10)                  // Median object diameter. Set to 0.0 for the `bact_omni` model or for automatic computation
        .useGPU()
        .build()


// Run detection for the selected objects
def imageData = getCurrentImageData()
def pathObjects = getSelectedObjects()
if (pathObjects.isEmpty()) {
    createSelectAllObject(true)
}

cellpose_cyto.detectObjects(imageData, pathObjects)
cytos = getDetectionObjects()
//cytos.each{ it.setPathClass(getPathClass("Cyto"))}

cellpose_nuc.detectObjects(imageData, pathObjects)
nucs = getDetectionObjects()
//nucs.each{ it.setPathClass(getPathClass("Nuc"))}

clearDetections()

// Combine cytos detections and nuclei detections to create cell objects
// (we simply check that the nuclei center is inside the cell center) 
cells = []
cytos.each{ cyto ->
    nucs.each{ nuc ->      
        if ( cyto.getROI().contains( nuc.getROI().getCentroidX() , nuc.getROI().getCentroidY())){
            cells.add(PathObjects.createCellObject(cyto.getROI(), nuc.getROI(), getPathClass("Cellpose"), null ));
        }
    }
}

addObjects(cells)
// adapted from : https://forum.image.sc/t/transferring-segmentation-predictions-from-custom-masks-to-qupath/43408/12
def server = getCurrentServer()
def downsample = 1.0
def cal = server.getPixelCalibration()

def measurements = ObjectMeasurements.Measurements.values() as List
def compartments = ObjectMeasurements.Compartments.values() as List // Won't mean much if they aren't cells...
def shape = ObjectMeasurements.ShapeFeatures.values() as List

def cells = getCellObjects()

// Intensity & Shape Measurements
for ( cell in cells) {
    ObjectMeasurements.addIntensityMeasurements( server, cell, downsample, measurements, compartments )
    ObjectMeasurements.addCellShapeMeasurements( cell, cal,  shape )
}

fireHierarchyUpdate()
println 'Done!'

import qupath.lib.analysis.features.ObjectMeasurements