/*
 * This script assumes that you have detections and have measurements per detection
 * The goal here is to reuse a previous K-Means clustering algorithm result
 *
 * @author Olivier Burri
 * Date: 2022.11.18
 */

def measurements = ['Solidity', 'Max diameter µm']
def names = ["A", "B", "C"]
def centroids = [[-2.8606701475127583, -0.32797788935118627], [0.26932692767541283, 0.976571888297062], [0.25961464207840873, -0.7220892952523503]]
def doNorm = true

// Start of script
def detections = getDetectionObjects()
def distanceFunction = new EuclideanDistance()

// Collect and normalize
def allValues = measurements.collect{ m ->
    detections.collect{ d -> d.getMeasurementList().get( m ) } as double[]
}

// Perform Normalization
if ( doNorm ) allValues = allValues.collect{ StatUtils.normalize( it ) }

def values = (0 ..< detections.size()).collect{ j ->
    new DoublePoint( (0 ..< allValues.size() ).collect { i -> allValues[i][j] } as double[] )
}

// Build a hashmap to find which centroid has which name
def map = new LinkedHashMap<ArrayList<Double>, String>( centroids.size() )
[names, centroids].transpose().each{ n, c -> map.put( c, n ) }

// Assign detections to these clusters
[detections, values].transpose().each{ detection, value ->
    // Find nearest cluster 
    def centroid = centroids.min{ distanceFunction.compute( it as double[], value.getPoint() ) }
    
    detection.setClassification( map.get( centroid ) )
}


import org.apache.commons.math3.ml.clustering.DoublePoint
import org.apache.commons.math3.ml.distance.EuclideanDistance
import org.apache.commons.math3.stat.StatUtils