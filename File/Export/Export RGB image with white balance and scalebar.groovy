/**
 * Creates a jpeg or png file of each annotation
 * with a scalebar of the specified color at the 
 * specified length using ImageJ.
 * The resulting images will be in a directory
 * called 'with_scalebar' in your QuPath Project
 * 
 * This only works on RGB images
 * 
 * This includes a cheap white balance that you can use
 * by giving an RGB estimate of the background as [R G B]
 *
 * @author Olivier Burri
 * Date: 2020.09.15
 */
 
////// Parameters for export //////

def bg_value = [219, 219, 219] // set to [0,0,0] for no correction

def downscale = 1

// Scalebar parameters
def scalebar_um = 100
def scalebar_height_px = 12
def scalebar_color = "Black"

// Output format
def image_format = "png" //Other option is jpeg or tif



////// Start of Script //////

def filePath = buildFilePath( PROJECT_BASE_DIR, "with_scalebar" )
    
mkdirs( filePath )
    
def annotations = getAnnotationObjects()

def server = getCurrentServer()

annotations.eachWithIndex {selected, idx ->
    def request = RegionRequest.createInstance( server.getPath(), downscale, selected.getROI() )
    
    def image = IJTools.convertToImagePlus( server, request ).getImage()
    
    // White balance fix
    IJ.run( image, "RGB Stack", "" )
    bg_value.eachWithIndex{ val, c -> 
        image.setC( c + 1 )
        IJ.run( image, "Multiply...", "value="+(240/val)+" slice" )
    }
    
    IJ.run( image, "RGB Color", "" )
    
    
    IJ.run( image, "Scale Bar...", "width=" + scalebar_um + " height=" + scalebar_height_px + " font=20 color=" + scalebar_color + " background=None location=[Lower Right] hide bold" )
    
    def file = new File( filePath, image.getTitle()+"_i"+idx )
      
    IJ.saveAs( image, image_format, file.getAbsolutePath() )
}

// Imports used
import qupath.lib.regions.*
import qupath.imagej.tools.IJTools
import qupath.imagej.gui.IJExtension
import ij.*