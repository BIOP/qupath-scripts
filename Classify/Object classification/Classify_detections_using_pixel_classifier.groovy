/*
 * Classify objects based on the pixel classifier class they overlap with
 * You need objects and a valid pixel classifier
 * @author Olivier Burri
 * Date: 2022.11.03
 */
 
def detections = getDetectionObjects()
def imageData = getCurrentImageData()

def classifier = loadPixelClassifier( "LowMidHigh" )

// classifyObjectsByCentroid​(ImageData<BufferedImage> imageData, PixelClassifier classifier, Collection<PathObject> pathObjects, boolean preferNucleusROI)
PixelClassifierTools.classifyObjectsByCentroid( imageData, classifier, detections, false )