/*
 * This script assumes that you have detections and have measurements per detection
 * The goal here is to reuse a previous K-Means clustering algorithm result
 *
 * @author Olivier Burri
 * Date: 2022.11.18
 */

def measurements = ["Solidity"]
def names = ["A", "B", "C"]
def centroids = [[0.7414517393468115],[0.8920453087112608],[0.9939777434386593]]

// Start of script
def detections = getDetectionObjects()
def distanceFunction = new EuclideanDistance()

def values = detections.collect{ detection ->
    new DoublePoint( measurements.collect{ m -> detection.getMeasurementList().getMeasurementValue( m ) } as double[] )
}


// Build a hashmap to find which centroid has which name
def map = new LinkedHashMap<ArrayList<Double>, String>( centroids.size() )
[names, centroids].transpose().each{ n, c -> map.put( c, n ) }

// Assign detections to these clusters
[detections, values].transpose().each{ detection, value ->
    // Find nearest cluster 
    def centroid = centroids.min{ distanceFunction.compute( it as double[], value.getPoint() ) }
    
    detection.setPathClass( getPathClass(map.get( centroid )) )
}


import org.apache.commons.math3.ml.clustering.DoublePoint
import org.apache.commons.math3.ml.distance.EuclideanDistance