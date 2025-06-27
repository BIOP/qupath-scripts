/*
 * Add a new measurement to each object based on the area coverage
 * from a given pixel classifier
 * You need objects and a valid pixel classifier
 * 
 * @author Olivier Burri
 * @date: 2022.11.03
 * Last tested on QuPath-0.6.0
 */
 
def detections = getDetectionObjects()
def imageData = getCurrentImageData()

def classifier = loadPixelClassifier( "LowMidHigh" )

def manager = PixelClassifierTools.createMeasurementManager( imageData, classifier )

PixelClassifierTools.addMeasurements( detections, manager, "LowMidHigh Classifier" )
