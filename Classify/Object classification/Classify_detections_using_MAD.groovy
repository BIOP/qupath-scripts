/*
 * This script assumes that you have detections and have measurements per detection
 * Based on the Median Absolute Deviation (MAD) from:
 * https://petebankhead.github.io/qupath/tips/2018/08/06/multichannel-fluorescence.html#one-method-for-determining-a-threshold
 *
 * @author Olivier Burri
 * Date: 2022.11.03
 */

import qupath.lib.classifiers.object.ObjectClassifiers

def pathObjects = getDetectionObjects() // or getCellObjects()

def madBuilders = [ new MADClassifierBuilder( measurement: "CY3: Mean", pathClass: "CY3", k: 3 ),
                    new MADClassifierBuilder( measurement: "CY5: Mean", pathClass: "CY5", k: 3 ) ]
                        

// Start of script 

// Get the single classifiers 
def singleClassifiers = madBuilders.collect{ it.getClassifier( pathObjects ) }

// Build a composite classifier out of this
def compositeClassifier = ObjectClassifiers.createCompositeClassifier( singleClassifiers )

def imageData = getCurrentImageData()

// Here the magic happens, run the classification
compositeClassifier.classifyObjects( imageData, pathObjects, true )


// Save all the classifiers 
try {
    def classifierManager = getProject().getObjectClassifiers()
    
    // Single Classifiers
    [ madBuilders, singleClassifiers ].transpose().each{ builder, classifier ->
        def name = GeneralTools.stripInvalidFilenameChars( builder.pathClass + " - MAD k=${builder.k}" )
        classifierManager.put( name, classifier )
    }
    
    // Save the composite classifier
    def compositeName = "Composite " + madBuilders.collect{ it.pathClass }.join(" and ") + " using MAD"
    compositeName =  GeneralTools.stripInvalidFilenameChars( compositeName )
    classifierManager.put( compositeName, compositeClassifier )
} catch ( IOException ex ){
    Dialogs.showErrorNotification( "Error saving classifiers", ex ) 
}
    

fireHierarchyUpdate()

println "MAD Classification done"


// MAD Classifier class 
class MADClassifierBuilder {

    def measurement = ""
    def pathClass = ""
    def k = 3 // Default is 3x
    def threshold // Store the threshold if someone wants to read it
    
    def getClassifier( def pathObjects ) {
    
        def posClass = getPathClass( this.pathClass + "+" )
        def negClass = getPathClass( this.pathClass + "-" )
        
        // Determine the thresholds based on the MAD and k
        def allMeasurements = pathObjects.collect{ p -> p.getMeasurementList().get( measurement ) }.findAll{ m -> !Double.isNaN( m ) } as double[]
        def median = getMedian( allMeasurements )

        // Subtract median & get absolute value
        def absMedianSubtracted = allMeasurements.collect{ d -> Math.abs( d - median ) } as double[]

        // Compute median absolute deviation & convert to standard deviation approximation
        double medianAbsoluteDeviation = getMedian( absMedianSubtracted )
        double sigma = medianAbsoluteDeviation / 0.6745

        // We can now determine the threshold
        threshold = median + k * sigma
        
        logger.info("New MAD classifier '$pathClass' based on '$measurement' and k = $k : threshold = $threshold")
    
        return new ObjectClassifiers.ClassifyByMeasurementBuilder( measurement )
                                        .threshold( threshold )
					.aboveEquals( posClass )
					.below( negClass )
					.build()
    }
    
    /**
     * Get median value from array (this will sort the array!)
     */
    double getMedian(double[] vals) {
        if (vals.length == 0)
            return Double.NaN
        Arrays.sort(vals)
        if (vals.length % 2 == 1)
            return vals[(int)(vals.length / 2)]
        else
            return (vals[(int)(vals.length / 2)-1] + vals[(int)(vals.length / 2)]) / 2.0
    }
}

