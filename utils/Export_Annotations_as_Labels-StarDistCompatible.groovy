/*
// ABOUT 
Exports Annotations for StarDist (Or other Deep Learning frameworks) 

// REQUIREMENTS
You will need to install the BIOP Extension for QuPath which contains methods needed to run this code
https://github.com/BIOP/qupath-biop-extensions/releases/tag/v2.0.0


// INPUTS
You need rectangular annotations that have classes "Training" and "Validation"
After you have placed these annotations, lock them and start drawing the objects inside

// OUTPUTS
----------
The script will export each annotation and whatever is contained within as an image-label pair
These will be placed in the folder specified by the user in the main project directory.
Inside that directory, you will find 'train' and 'test' directories that contain the images with 
class 'Training' and 'Validation', respectively. 
Inside each, you will find 'images' and 'masks' folders containing the exported image and the labels, 
respectively. The naming convention was chosen to match the one used for the StarDist DSBdataset

//PARAMETERS
------------
- channel_of_interest: You can export a single channel or all of them, currently no option for _some_channels only
- downsample: you can downsample your image in case it does not make sense for you to train on the full resolution
- export_directory: name of the directory which will contain the 'train' and 'test' subdirectories

Authors: Olivier Burri, Romain Guiet BioImaging and Optics Platform (EPFL BIOP)

Tested on QuPath 0.3.2, 2022.04.13

Due to the simple nature of this code, no copyright is applicable
*/

// USER SETTINGS
def channel_of_interest = 2 // null to export all the channels 
def downsample = 1

 

// START OF SCRIPT

def training_regions = getAnnotationObjects().findAll { it.getPathClass() == getPathClass("train") }

def validation_regions = getAnnotationObjects().findAll { it.getPathClass() == getPathClass("test") }

if (training_regions.size() > 0 ) saveRegions( training_regions, channel_of_interest, downsample, 'train')

if (validation_regions.size() > 0 ) saveRegions( validation_regions, channel_of_interest, downsample, 'test')



def saveRegions( def regions, def channel, def downsample, def type ) {
    // Randomize names
    def randomizeImageName = false
    def is_randomized = getProject().getMaskImageNames()
    getProject().setMaskImageNames(randomizeImageName)
    def rm = RoiManager.getRoiManager() ?: new RoiManager()
    // Get the image name
    def image_name = getProjectEntry().getImageName()
    
    regions.eachWithIndex{ region, region_idx ->
        println("Processing Region #"+(  region_idx + 1 ) )
        
        def file_name =  image_name+"_r"+( region_idx + 1 )  
        imageData = getCurrentImageData();
        server = imageData.getServer();
        viewer = getCurrentViewer();
        hierarchy = getCurrentHierarchy();

        //def image = GUIUtils.getImagePlus( region, downsample, false, true )
        request = RegionRequest.createInstance(imageData.getServerPath(), downsample, region.getROI())
        pathImage = null;
        pathImage = IJExtension.extractROIWithOverlay(server, region, hierarchy, request, false, viewer.getOverlayOptions());
        image = pathImage.getImage()
        println("Image received" )
        //image.show()
        // Create the Labels image
        def labels = IJ.createImage( "Labels", "16-bit black", image.getWidth(), image.getHeight() ,1 );
        rm.reset()
        
        IJ.run(image, "To ROI Manager", "")
        
        def rois = rm.getRoisAsArray() as List
        println("Creating Labels" )
        
        def label_ip = labels.getProcessor()
        def idx = 0
        rois.each{ roi ->
            if (roi.getType() == Roi.RECTANGLE) {
                println("Ignoring Rectangle")
            } else {
                label_ip.setColor( ++idx )
                label_ip.setRoi( roi )
                label_ip.fill( roi )


            }
        }
        labels.setProcessor( label_ip )
        
        //labels.show()
        
        // Split to keep only channel of interest
        def output = image
        if  ( channel != null){
            imp_chs =  ChannelSplitter.split( image )
            output = imp_chs[  channel - 1 ]
        }
        
      
        
        saveImages(output, labels, file_name, type)
                
        println( file_name + " Image and Mask Saved." )
        
        // Save some RAM
        output.close()
        labels.close()
        image.close()
    }
    
    // Return Project setup as it was before
    getProject().setMaskImageNames( is_randomized )
}

// This will save the images in the selected folder
def saveImages(def images, def labels, def name, def type) {
    def source_folder = new File ( buildFilePath( PROJECT_BASE_DIR, 'ground_truth', type, 'images' ) )
    def target_folder = new File ( buildFilePath( PROJECT_BASE_DIR, 'ground_truth', type, 'masks' ) )
    mkdirs( source_folder.getAbsolutePath() )
    mkdirs( target_folder.getAbsolutePath() )
    
    IJ.save( images , new File ( source_folder, name ).getAbsolutePath()+'.tif' )
    IJ.save( labels , new File ( target_folder, name ).getAbsolutePath()+'.tif' )

}

// Manage Imports
import qupath.lib.roi.RectangleROI
import qupath.imagej.gui.IJExtension;
import ij.IJ
import ij.gui.Roi
import ij.plugin.ChannelSplitter
import ij.plugin.frame.RoiManager
print "done"