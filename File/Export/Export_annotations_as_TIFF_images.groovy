/**
 * See 
 * https://qupath.readthedocs.io/en/stable/docs/advanced/exporting_images.html
 * for more examples
 * Last tested on QuPath-0.4.0
 * @author Olivier Burri
 * Date: 2020.10.29
 */
 
import ch.epfl.biop.qupath.utils.*
import ij.IJ

////// Parameters for export //////
def downsample = 1

////// Start of Script //////

def filePath = buildFilePath( PROJECT_BASE_DIR, "image_export" )
mkdirs( filePath )
    
def annotations = getAnnotationObjects()
def server = getCurrentServer()

def imageName = getProjectEntry().getImageName()
imageName = GeneralTools.stripInvalidFilenameChars( imageName )

annotations.eachWithIndex { selected, idx ->
    // Write the region of the image corresponding to the currently-selected object
    def requestROI = RegionRequest.createInstance( server.getPath(), downsample, selected.getROI() )
    
    def fileName = new File( filePath, imageName+"_"+idx + ".tif" )
    
    writeImageRegion( server, requestROI, fileName.getAbsolutePath() )
    logger.info( "Region saved to {}", fileName )
}