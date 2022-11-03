/*
 * This script assumes that you have detections and have measurements per detection
 * It uses a quantile based approcah to set dynamic thresholds for the different measurements
 * in order to classify the objects.
 * It has a simple implementation which asks: What is the quantile you want to use?
 * and how many times should the measurement value be above that quantile (k)
 * in order to consider the cell positive?
 *
 * @author Olivier Burri
 * Date: 2022.11.03
 */

import qupath.lib.classifiers.object.ObjectClassifiers

def pathObjects = getDetectionObjects() // or getCellObjects()

def quantileBuilders = [ new QuantileClassifierBuilder( measurement: "CY3: Mean", pathClass: "CY3", quantile: 0.2, k: 3 ),
                         new QuantileClassifierBuilder( measurement: "CY5: Mean", pathClass: "CY5", quantile: 0.3, k: 3 ) ]
                        

// Start of script 

// Get the single classifiers 
def singleClassifiers = quantileBuilders.collect{ it.getClassifier( pathObjects ) }

// Build a composite classifier out of this
def compositeClassifier = ObjectClassifiers.createCompositeClassifier( singleClassifiers )

def imageData = getCurrentImageData()

// Here the magic happens, run the classification
compositeClassifier.classifyObjects( imageData, pathObjects, true )


// Save all the classifiers 
try {
    def classifierManager = getProject().getObjectClassifiers()
    
    // Single Classifiers
    [ quantileBuilders, singleClassifiers ].transpose().each{ builder, classifier ->
        def name = GeneralTools.stripInvalidFilenameChars( builder.pathClass + " - Quantile q=${builder.quantile} k=${builder.k}" )
        classifierManager.put( name, classifier )
    }
    
    // Save the composite classifier
    def compositeName = "Composite " + quantileBuilders.collect{ it.pathClass }.join(" and ") + " using Quantiles"
    compositeName =  GeneralTools.stripInvalidFilenameChars( compositeName )
    classifierManager.put( compositeName, compositeClassifier )
    
} catch ( IOException ex ){
    Dialogs.showErrorNotification( "Error saving classifiers", ex ) 
}
    

fireHierarchyUpdate()

println "Quantile Classification done"

// Quantile Classifier class 
class QuantileClassifierBuilder {

    def measurement = ""
    def pathClass = ""
    def quantile = 0.3
    def k = 3 // Default is 3x
    def threshold // Store the threshold if someone wants to read it
    
    def getClassifier( def pathObjects ) {
    
        def posClass = getPathClass( this.pathClass + "+" )
        def negClass = getPathClass( this.pathClass + "-" )
        
        // Determine the thresholds based on the MAD and k
        def allMeasurements = pathObjects.collect{ p -> p.getMeasurementList().getMeasurementValue( measurement ) }.findAll{ m -> !Double.isNaN( m ) }
        
        def threshold = quantileBasedThr( allMeasurements, quantile, k )
        
        logger.info( "New Quantile Classifier $pathClass' based on '$measurement', quantile = $quantile, k = $k : threshold = $threshold" )
    
        return new ObjectClassifiers.ClassifyByMeasurementBuilder( measurement )
                                        .threshold( threshold )
					.aboveEquals( posClass )
					.below( negClass )
					.build()
    }
    
    double quantileBasedThr( def measurements, def quantile, def multiplier ) {

        // Sort by mean intensity in the channel of interst
        def sorted = measurements.sort()
        
        def n = sorted.size()
        
        // Remove 5% smallest intensities
        sorted =sorted.drop( ( n * 0.05 ) as int )
        
        // Keep x percent of what is left
        def quant = sorted.take( ( n * quantile ) as int )
        
        // Get baseline average
        def baseline = quant.sum{ it } / quant.size()
        
        // new threshold
        threshold = multiplier * baseline
        
        return threshold
    }      
}


  

