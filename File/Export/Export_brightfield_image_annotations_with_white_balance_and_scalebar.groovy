/**
 * Creates a jpeg or png file of each annotation with a scalebar of the specified color at the 
 * specified length using ImageJ.
 * The resulting images will be in a directory called 'with_scalebar' or 'with_scalebar_wb'
 * in your QuPath Project
 * 
 * This only works on RGB images
 * 
 * This includes a cheap white balance that you can use by setting the color deconvolution
 * stains background value
 *
 * @author Olivier Burri
 * Date: 2020.09.15
 */
 
////// Parameters for export //////

def doWhiteBalance = true
def downscale = 1

// Scalebar parameters
def scalebar_um = 20
def scalebar_height_px = 12
def scalebar_color = "Black"

// Output format
def image_format = "png" //Other option is jpeg or tif

////// Start of Script //////

def imageName = getProjectEntry().getImageName()
imageName = GeneralTools.stripInvalidFilenameChars( imageName )

// Default background
def background = [255, 255, 255]
if ( doWhiteBalance ) {
    def stains = getCurrentImageData().getColorDeconvolutionStains()
    background = [ stains.getMaxRed(), stains.getMaxGreen(), stains.getMaxBlue() ]
}

def folderName = "with_scalebar"
if ( doWhiteBalance ) folderName += "_wb"

def filePath = buildFilePath( PROJECT_BASE_DIR, folderName )
mkdirs( filePath )
    
def annotations = getAnnotationObjects()

def server = getCurrentServer()

annotations.eachWithIndex {selected, idx ->
    def request = RegionRequest.createInstance( server.getPath(), downscale, selected.getROI() )
    
    // Use ImageJ
    def image = IJTools.convertToImagePlus( server, request ).getImage()
    
    // White balance
    IJ.run( image, "RGB Stack", "" )
    background.eachWithIndex{ val, c -> 
        image.setC( c + 1 )
        IJ.run( image, "Multiply...", "value="+(240/val)+" slice" )
    }
    
    IJ.run( image, "RGB Color", "" )
    
    IJ.run( image, "Scale Bar...", "width=" + scalebar_um + " height=" + scalebar_height_px + " font=20 color=" + scalebar_color + " background=None location=[Lower Right] hide bold" )
    
    def fileName = new File( filePath, imageName + "_" + idx + "_scalebar_" + scalebar_um + "um." + image_format )

      
    IJ.saveAs( image, image_format, fileName.getAbsolutePath() )
    logger.info( "Region saved to {}", fileName )
}

// Imports used
import qupath.lib.regions.*
import qupath.imagej.tools.IJTools
import qupath.imagej.gui.IJExtension
import ij.*