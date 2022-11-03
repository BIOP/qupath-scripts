/*
 * This script assumes that you have detections and have measurements per detection
 * It uses fixed thresholds for each measurement in order to classify the objects, 
 * like using a Single measurement classifier, 
 *
 * @author Olivier Burri
 * Date: 2022.11.03
 */

import qupath.lib.classifiers.object.ObjectClassifiers

def pathObjects = getDetectionObjects() // or getCellObjects()

def thresholdBuilders = [ new ThresholdClassifierBuilder( measurement: "CY3: Mean", pathClass: "CY3", threshold: 20 ),
                          new ThresholdClassifierBuilder( measurement: "CY5: Mean", pathClass: "CY5", threshold: 34 ) ]
                        

// Start of script 

// Get the single classifiers 
def singleClassifiers = thresholdBuilders.collect{ it.getClassifier( ) }

// Build a composite classifier out of this
def compositeClassifier = ObjectClassifiers.createCompositeClassifier( singleClassifiers )

def imageData = getCurrentImageData()

// Here the magic happens, run the classification
compositeClassifier.classifyObjects( imageData, pathObjects, true )


// Save all the classifiers 
try {
    def classifierManager = getProject().getObjectClassifiers()
    
    // Single Classifiers
    [ thresholdBuilders, singleClassifiers ].transpose().each{ builder, classifier ->
        def name = GeneralTools.stripInvalidFilenameChars( builder.pathClass + " - Threshold=${builder.threshold}" )
        classifierManager.put( name, classifier )
    }
    
    // Save the composite classifier
    def compositeName = "Composite " + thresholdBuilders.collect{ it.pathClass }.join(" and ") + " using Thresholds"
    compositeName =  GeneralTools.stripInvalidFilenameChars( compositeName )
    classifierManager.put( compositeName, compositeClassifier )
    
} catch ( IOException ex ){
    Dialogs.showErrorNotification( "Error saving classifiers", ex ) 
}
    
fireHierarchyUpdate()

println "Quantile Classification done"

// Threshold Classifier class 
class ThresholdClassifierBuilder {

    def measurement = ""
    def pathClass = ""
    def threshold = 10
    
    def getClassifier( ) {
    
        def posClass = getPathClass( this.pathClass + "+" )
        def negClass = getPathClass( this.pathClass + "-" )
        
        logger.info( "New Threshold Classifier $pathClass' with threshold = $threshold" )
    
        return new ObjectClassifiers.ClassifyByMeasurementBuilder( measurement )
                                        .threshold( threshold )
					.aboveEquals( posClass )
					.below( negClass )
					.build()
    }      
}


  

