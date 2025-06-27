/*
 * This script assumes that you have detections and have measurements per detection
 * We can build a k-means classifier using any measure we want. 
 * While this example is in 1D, you can create classifiers in ND
 * Because the K-means will give us the centroids, we choose the separating thresholds as 
 * the half-distance between the centroids
 *
 * @author Olivier Burri
 * Date: 2022.11.18
 */

def measurements = ["Solidity","Max diameter µm"]
def k = 3
def names = ["A", "B", "C"] // Arbitrary, and will not be ordered if the clustering vector has more than 2 dimensions
def detections = getDetectionObjects()
def doNorm = true

// Start of script
def maxIterations = 5000
def distanceFunction = new EuclideanDistance()
def randomGenerator = new JDKRandomGenerator()
def emptyClusterStrategy = KMeansPlusPlusClusterer.EmptyClusterStrategy.ERROR

def kmeans = new KMeansPlusPlusClusterer( k, maxIterations, distanceFunction, randomGenerator, emptyClusterStrategy )

// Collect and normalize
def allValues = measurements.collect{ m ->
    detections.collect{ d -> d.getMeasurementList().get( m ) } as double[]
}

// Perform Normalization
if ( doNorm ) allValues = allValues.collect{ StatUtils.normalize( it ) }

def values = (0 ..< detections.size()).collect{ j ->
    new DoublePoint( (0 ..< allValues.size() ).collect { i -> allValues[i][j] } as double[] )
}
        
// Get the centroids and assign the objects
def centroids = kmeans.cluster( values )

// If there is only one measurement then we can sort the clusters
if( measurements.size() == 1) centroids = centroids.sort{ it.getCenter().getPoint()[0] }
println centroids

// Build a hashmap to find which centroid has which name
def map = new LinkedHashMap<CentroidCluster, String>( centroids.size() )
[names, centroids].transpose().each{ n, c -> map.put( c, n ) }

// Assign detections to these clusters
[detections, values].transpose().each{ detection, value ->
    // Find nearest cluster 
    def centroid = centroids.min{ distanceFunction.compute( it.getCenter().getPoint(), value.getPoint() ) }
    
    detection.setClassification( map.get( centroid ) )
}

// Print the result
println "Perfomed K-Means clustering"
println "Normalization:\n\t\t$doNorm"
println "Measurements: \n\t\t["+ measurements.collect{ '\'' + it + '\''}.join(', ') + "]"
println "Resulting cluster centers:\n\t\t[" + map.keySet().collect{ it.getCenter() }.join(', ') +"]"

fireHierarchyUpdate()

import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer
import org.apache.commons.math3.ml.clustering.DoublePoint
import org.apache.commons.math3.ml.clustering.CentroidCluster
import org.apache.commons.math3.ml.distance.EuclideanDistance
import org.apache.commons.math3.random.JDKRandomGenerator
import org.apache.commons.math3.stat.StatUtils