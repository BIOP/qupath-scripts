/* 
 * Grow objects by a certain amount, avoiding them from touching using
 * QuPath's DelaunayTools.
 * NOTE: One limitation is that the VoronoiROIs are computed from the centroid of the objects
 * Unlike in ImageJ, which is based on the distance map. So very long objects will look "cut"
 * As long as your objects are roundish, this should work
 * 
 * @author Olivier Burri
 * @date 20221103
 * Last tested on QuPath-0.3.2
 */
 
import qupath.lib.analysis.DelaunayTools

def enlargeByPx = 200


 
def server = getCurrentServer()

 // Get the bounds of the image to limit the voronoi cells (Or something else)
def imageBounds = GeometryTools.createRectangle( 0, 0, server.getWidth(), server.getHeight() )

// Make a voronoi diagram of the selected objects
def regions = getAnnotationObjects()

// This will build our voronoi cells that will serve to limit the growth of the regions
def voronois = DelaunayTools.newBuilder( regions ).build().getVoronoiROIs( imageBounds )

// voronois is a map that contains the annotation as a key and its corresponding voronoi cell as a value
voronois.each{ object, bounds ->
    def enlargedGeometry = object.getROI().getGeometry().buffer( enlargeByPx )

    // Make the difference between the larger and smaller ones to get the ring
    def ringGeometry = enlargedGeometry.difference( object.getROI().getGeometry() )
           
    // Finally use the voronoi cell to clip the ring in case it has neighbord
    def intersectedGeometry = ringGeometry.intersection( bounds.getGeometry() )
        
    // Create the final detection that will be added to the image, and let it have the same class as the parent
    def enlarged = PathObjects.createDetectionObject( GeometryTools.geometryToROI( intersectedGeometry, null ), object.getPathClass() )

    // Add as a child of the original, even tough it is larger.
    object.addPathObject( enlarged )
}

fireHierarchyUpdate()   