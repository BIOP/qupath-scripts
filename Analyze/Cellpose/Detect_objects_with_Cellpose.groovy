/* Last tested on QuPath-0.3.2
 * 
* REQUIREMENTS
 * ============
 * You need the QuPath Cellpose Extension installed
 *   https://github.com/BIOP/qupath-extension-cellpose
 *
 * Make sure that you follow the ReadMe, install Cellpose itself 
 * and Configure QuPath to use your Cellpose installation.
 * 
 * All builder options are at 
 * https://biop.github.io/qupath-extension-cellpose/qupath/ext/biop/cellpose/CellposeBuilder.html
 */
import qupath.ext.biop.cellpose.Cellpose2D

// Specify the model name (cyto, nuc, cyto2, omni_bact or a path to your custom model)
def pathModel = 'cyto2'
def cellpose = Cellpose2D.builder( pathModel )
        .pixelSize( 0.5 )             // Resolution for detection in um
	.channels( 'DAPI' )	      // Select detection channel(s)
        .preprocess( ImageOps.Filters.median(1) )                // List of preprocessing ImageOps to run on the images before exporting them
        .normalizePercentilesGlobal(0.1, 99.8, 10)
        .tileSize(1024)                  // If your GPU can take it, make larger tiles to process fewer of them. Useful for Omnipose
//        .cellposeChannels(1,2)         // Overwrites the logic of this plugin with these two values. These will be sent directly to --chan and --chan2
//        .cellprobThreshold(0.0)        // Threshold for the mask detection, defaults to 0.0
//        .flowThreshold(0.4)            // Threshold for the flows, defaults to 0.4 
        .diameter(15)                    // Median object diameter. Set to 0.0 for the `bact_omni` model or for automatic computation
//        .addParameter("save_flows")      // Any parameter from cellpose not available in the builder. See https://cellpose.readthedocs.io/en/latest/command.html
//        .addParameter("anisotropy", "3") // Any parameter from cellpose not available in the builder. See https://cellpose.readthedocs.io/en/latest/command.html
//        .cellExpansion(5.0)              // Approximate cells based upon nucleus expansion
//        .cellConstrainScale(1.5)       // Constrain cell expansion using nucleus size
//        .classify("My Detections")     // PathClass to give newly created objects
        .measureShape()                // Add shape measurements
        .measureIntensity()             // Add cell measurements (in all compartments)  
        .createAnnotations()           // Make annotations instead of detections. This ignores cellExpansion
        .simplify(0)                   // Simplification 1.6 by default, set to 0 to get the cellpose masks as precisely as possible
        .build()


// Run detection for the selected objects
def imageData = getCurrentImageData()
def annotations = getSelectedObjects() 

if (annotations.isEmpty()){
    createSelectAllObject(true)
    annotations =  getSelectedObjects()
}
cellpose.detectObjects(imageData, annotations)
println 'Done!'
