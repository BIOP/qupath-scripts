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
def downscale = 4

// Scalebar parameters
def scalebar_um = 40
def scalebar_height_px = 8
def scalebar_color = "White"

// Output format
def image_format = "png" //Other option is jpeg or tif



////// Start of Script //////

def filePath = buildFilePath( PROJECT_BASE_DIR, "with_scalebar" )
    
mkdirs( filePath )
    
def annotations = getAnnotationObjects()

def server = getCurrentServer()

annotations.eachWithIndex {selected, idx ->
    def image = GUIUtils.getImagePlus( selected, downscale, false, false )

//    def request = RegionRequest.createInstance( server.getPath(), downscale, selected.getROI() )
    
//    def image = IJTools.convertToImagePlus( server, request ).getImage()
    IJ.run( image, "RGB Color", "" )
   
    IJ.run( image, "Scale Bar...", "width=" + scalebar_um + " height=" + scalebar_height_px + " font=20 color=" + scalebar_color + " background=None location=[Lower Right] bold" )

    def file = new File( filePath, image.getTitle()+"_i"+idx )
    //image.show()
    
      
    IJ.saveAs( image, image_format, file.getAbsolutePath() )
    println "Image : "+file.getName()+" Saved."
}
return

// Imports used
import qupath.lib.regions.*
import qupath.imagej.tools.IJTools
import qupath.imagej.gui.IJExtension
import ij.*
import ch.epfl.biop.qupath.utils.*