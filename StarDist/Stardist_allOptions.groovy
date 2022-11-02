/* Last tested on QuPath-0.3.2
 * 
 * This scripts requires qupath-extension-cellpose 
 * cf https://qupath.readthedocs.io/en/stable/docs/advanced/stardist.html
 */
clearDetections()

/*
 * To Detect Nuclei
 */
// Specify the model file (you will need to specify where you saved it !)
pathModel = 'C:/QuPath_Common_Data_0.3/models/dsb2018_heavy_augment.pb'

var stardist = StarDist2D.builder(pathModel)
        .channels("DAPI")
        .threshold(0.1)              // Probability (detection) threshold
        .normalizePercentiles(1, 99) // Percentile normalization
        .pixelSize(0.1)              // Resolution for detection
        //.tileSize(1024)              // Specify width & height of the tile used for prediction
        .cellExpansion(2)          // Approximate cells based upon nucleus expansion
        //.cellConstrainScale(1.5)     // Constrain cell expansion using nucleus size
        //.ignoreCellOverlaps(false)   // Set to true if you don't care if cells expand into one another
        //.measureShape()              // Add shape measurements
        //.measureIntensity()          // Add cell measurements (in all compartments)
        //.includeProbability(true)    // Add probability as a measurement (enables later filtering)
        //.nThreads(4)                 // Limit the number of threads used for (possibly parallel) processing
        //.simplify(1)                 // Control how polygons are 'simplified' to remove unnecessary vertices
        //.doLog()                     // Use this to log a bit more information while running the script
        //.createAnnotations()       // Generate annotation objects using StarDist, rather than detection objects
        //.constrainToParent(true)    // Prevent nuclei/cells expanding beyond any parent annotations (default is true)
        //.classify("Nuclei")          // Automatically assign all created objects as 'Tumor'
        .build()

def annotations = getSelectedObjects() 
if (annotations.isEmpty()){
    createSelectAllObject(true)
    annotations =  getSelectedObjects()
}

// Run detection for the selected object
annotations.each{ stardist.detectObjects(getCurrentImageData(), it , true)}
        
import qupath.ext.stardist.StarDist2D