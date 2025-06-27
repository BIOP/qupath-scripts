/*
 * Save annotations of a selected class as SVGs with embeded raster
 * NOTE: As we use the current viewer, if you run this in a project, 
 *       it will use the current display settings and not use the ones 
 *       from other images
 *
 * NOTE: Due to a bug where detections are sometimes exported as 
 *       bounding boxes, everything gets turned into annotations 
 *       before making the SVG and restored at the end
 *
 * @author Olivier Burri
 * @date 2022.09.27
 * Last tested on QuPath-0.6.0
 */

//////// SCRIPT SETTINGS ////////
//////// --------------- ////////

// Classification to filter and export
def selectedClass = "Small"

// Raster downsample
def downsample = 8

// Try to use names of regions for export, otherwise just use an index
def useAnnotationNames = false

// Define the line thickness for export.
def selectedThickness = 0.1

//////// START OF SCRIPT ////////
//////// --------------- ////////

def viewer = getCurrentViewer()
def server = getCurrentServer()

def imageName = getProjectEntry().getImageName()
imageName = GeneralTools.stripInvalidFilenameChars( imageName )

// Prepare the save folder in the project
def saveFolder = new File( getProject().getPath().getParent().toFile(), "SVG Export" )

saveFolder.mkdirs()

// Filter the annotations to the selected class only
def annotations = getAnnotationObjects().findAll{ it.getPathClass() == getPathClass( selectedClass ) }

// Set annotation thickness
def originalThickness = PathPrefs.annotationStrokeThicknessProperty().getValue()

PathPrefs.annotationStrokeThicknessProperty().setValue( selectedThickness )

// Convert all detections to annotations, temporarily
def cells = getDetectionObjects()

def annots = cells.collect{ cell ->
    return PathObjects.createAnnotationObject( cell.getROI(), cell.getPathClass() )
    }
removeDetections()

addObjects(annots)


annotations.eachWithIndex { annotation, index ->
    // Build a name
    def name = "${imageName}_Region_${selectedClass}"
    name+= ( annotation.getName() == null || !useAnnotationNames ) ? "_#${index+1}" : "_${annotation.getName()}"
    name+= ".svg"

    // Create a request
    def request = RegionRequest.createInstance( server.getPath(), downsample, annotation.getROI() )
    
    def file = new File (saveFolder, name )
    
    new SvgTools.SvgBuilder(viewer)
        .images( SvgTools.SvgBuilder.ImageIncludeType.EMBED )
        .region(request)
        .downsample( request.getDownsample() ) // Apparently, we need the downsample again?
        .showSelection( false )
        .writeSVG(file)
    
    logger.info( "Saved file: ----> \"{}\"", file )
}

// Remove extra annotations
removeObjects( annots, false )
addObjects( cells )
PathPrefs.annotationStrokeThicknessProperty().setValue( originalThickness )

logger.info( "SVG Export Done for {}", imageName )
import qupath.lib.extension.svg.*
import qupath.lib.gui.prefs.PathPrefs