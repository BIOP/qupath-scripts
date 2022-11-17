/* 
 * Get the distance of each detection to its parent's edge
 * 
 * @author Olivier Burri
 * @date 202211009
 * Last tested on QuPath-0.3.2
 */
 
def annotations = getAnnotationObjects()

def px = getCurrentServer().getPixelCalibration().getPixelWidthMicrons()
def py = getCurrentServer().getPixelCalibration().getPixelHeightMicrons()
def um = GeneralTools.micrometerSymbol()

annotations.each{ annotation ->
    if ( annotation.hasChildren() ) {
    // We need the inverse of the annotation. Just make it a bit larger and take a ring
    def larger = annotation.getROI().getGeometry().buffer( 10 )
    
    def ring = larger.difference( annotation.getROI().getGeometry() )
    
    // Create the final detection that will be added to the image, and let it have the same class as the parent
    def enlarged = PathObjects.createDetectionObject( GeometryTools.geometryToROI( ring, null ), annotation.getPathClass() )

    // Compute the calibrated distance
    DistanceTools.centroidToBoundsDistance2D( annotation.getChildObjects(), [enlarged], px, py, "Distance to parent edge $um")
    }
}