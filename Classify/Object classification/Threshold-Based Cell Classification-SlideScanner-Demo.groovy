// This script assumes that you have run a QuPath StarDist detection and have cell objects in your currently open image


// Define multimeasurements you wish to use. You can add as many as you want as follows: 
// [new SingleMeasurement (...), SingleMeasurement(...), SingleMeasurement(...), ...]

def multimeasurements = [ new SingleMeasurement( measurement: "Leica/ALEXA 568: Mean",
                                                 pathClass: "A",
                                                 threshold: 100
                                                ),
                          new SingleMeasurement( measurement: "Leica/ALEXA 488: Mean",
                                                 pathClass: "B",
                                                 threshold: 100
                                               ),
                          new SingleMeasurement( measurement: "Leica/Cy5: Mean",
                                                 pathClass: "C",
                                                 threshold: 100
                                               )          
                        ]


// Start of Script, which will classify all cells
// Create the individual classifiers
// Here the classifier assumes that objects of exactly the same value as the threshold are to be classified as the class you set as 'pathClassAbove'
def classifiers = multimeasurements.collect {
    return new ObjectClassifiers.ClassifyByMeasurementBuilder( it.measurement )
					.threshold(it.threshold)
					.aboveEquals( it.pathClassAbove )
					.below( it.pathClassBelow )
					.build()
}

// Build a composite classifier out of this
def composite = ObjectClassifiers.createCompositeClassifier(classifiers)

// Get the image data we need to run the classification
def cells = getCellObjects()
def imageData = getCurrentImageData()

// Here the magic happens, run the classification
composite.classifyObjects( imageData, cells, true )

// Update the view
fireHierarchyUpdate()


println("Done")


// Class to help create the single measurement classifiers.
// the idea is that this is easy to read by the user
class SingleMeasurement {

    def measurement = ""
    def pathClass = PathClassFactory.getPathClassUnclassified()

    def threshold = 0.0
}

import qupath.lib.classifiers.object.*