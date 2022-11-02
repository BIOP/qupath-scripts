/**
 * Creates a jpeg or png file of each annotation
 * with a scalebar of the specified color at the 
 * specified length using ImageJ.
 * The resulting images will be in a directory
 * called 'with_scalebar' in your QuPath Project
 * 
 * @author Olivier Burri
 * Date: 2020.10.29
 */
 
////// Parameters for export //////
def downscale = 1

////// Start of Script //////

def filePath = buildFilePath( PROJECT_BASE_DIR, "image_export" )
    
mkdirs( filePath )
    
def annotations = getAnnotationObjects()

def server = getCurrentServer()

annotations.eachWithIndex {selected, idx ->
    def image = GUIUtils.getImagePlus( selected, downscale, false, false )
    def file = new File( filePath, image.getTitle()+"_i"+idx )
    IJ.saveAs(image, "tiff", file.getAbsolutePath() )
}


import ch.epfl.biop.qupath.utils.*
import ij.IJ