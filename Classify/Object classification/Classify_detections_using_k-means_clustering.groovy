/*
 * This script assumes that you have detections and have measurements per detection
 * We can build a k-means classifier using any measure we want. 
 * While this example is in 1D, you can create classifiers in ND
 * Because the K-means will give us the centroids, we choose the separating thresholds as 
 * the half-distance between the centroids
 * NOTE: No normalization is done, that's up to you
 *
 * @author Olivier Burri
 * Date: 2022.11.18
 */

def measurements = ["Solidity"]
def k = 3
def names = ["A", "B", "C"] // Arbitrary, and cannot be ordered if the clustering vector has more than 2 dimensions
def detections = getDetectionObjects()


// Start of script
def maxIterations = 5000
def distanceFunction = new EuclideanDistance()
def randomGenerator = new JDKRandomGenerator()
def emptyClusterStrategy = KMeansPlusPlusClusterer.EmptyClusterStrategy.ERROR



def kmeans = new KMeansPlusPlusClusterer( k, maxIterations, distanceFunction, randomGenerator, emptyClusterStrategy )


def values = detections.collect{ detection ->
    new DoublePoint( measurements.collect{ m -> detection.getMeasurementList().getMeasurementValue( m ) } as double[] )
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
    
    detection.setPathClass( getPathClass(map.get( centroid )) )
}

// Print the result
println "Perfomed K-Means clustering using [${measurements.join(',')}] with $k clusters"
println "Resulting clusters:"
map.each{ println "Cluster center ${it.getKey().getCenter()} as Class ${it.getValue()}" }
fireHierarchyUpdate()

import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer
import org.apache.commons.math3.ml.clustering.DoublePoint
import org.apache.commons.math3.ml.clustering.CentroidCluster
import org.apache.commons.math3.ml.distance.EuclideanDistance
import org.apache.commons.math3.random.JDKRandomGenerator