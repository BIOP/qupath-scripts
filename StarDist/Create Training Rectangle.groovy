/**
 * Make a rectangle at the center of the image, of defined width and height
 * You can move the rectangle after it is created, and duplicate it using 
 * SHIFT + D to create more and move them across the image.
 * @author Olivier Burri
 * Date: 2020.09.15
 */

// PARAMETERS
def size = 256 // in pixels: the size of the rectangle (width and height) to create.


// Script start
def cx = getCurrentServer().getWidth()
def cy = getCurrentServer().getHeight()

// Create a ROI in the center of the opened image
def roi = ROIs.createRectangleROI( ( cx - size ) / 2,  ( cy - size ) / 2, size, size, null)

// Make it into an annoation, with no class
def annot = PathObjects.createAnnotationObject( roi, null)

// Add it to the hierarchy and update.
addObject( annot )

fireHierarchyUpdate()

println "As per your request, a new rectangle has been created. Joy."
