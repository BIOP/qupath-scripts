import qupath.ext.stardist.StarDist2D

// Specify the model file (you will need to change this!)
//var pathModel = 'C:/QuPath_Common_Data_0.3/models/he_heavy_augment.pb'
var pathModel = 'C:/QuPath_Common_Data_0.3/models/dsb2018_heavy_augment.pb'

// Get current image - assumed to have color deconvolution stains set
var imageData = getCurrentImageData()
var stains = imageData.getColorDeconvolutionStains()

var stardist = StarDist2D.builder(pathModel)
        .preprocess( // Extra preprocessing steps, applied sequentially
            ImageOps.Channels.deconvolve(stains),
            ImageOps.Channels.extract(1)// specify which channel to use (0,1,2?)
         )
        .threshold(0.5)              // Probability (detection) threshold
        .normalizePercentiles(1, 99) // Percentile normalization
        .pixelSize(0.34)              // Resolution for detection
        .tileSize(1024)              // Specify width & height of the tile used for prediction
        .cellExpansion(5.0)          // Approximate cells based upon nucleus expansion
        .cellConstrainScale(1.5)     // Constrain cell expansion using nucleus size
        .ignoreCellOverlaps(false)   // Set to true if you don't care if cells expand into one another
        .measureShape()              // Add shape measurements
        .measureIntensity()          // Add cell measurements (in all compartments)
        .includeProbability(true)    // Add probability as a measurement (enables later filtering)
        .nThreads(4)                 // Limit the number of threads used for (possibly parallel) processing
        .simplify(1)                 // Control how polygons are 'simplified' to remove unnecessary vertices
        .doLog()                     // Use this to log a bit more information while running the script
        .createAnnotations()         // Generate annotation objects using StarDist, rather than detection objects
        .constrainToParent(false)    // Prevent nuclei/cells expanding beyond any parent annotations (default is true)
        .classify("Tumor")           // Automatically assign all created objects as 'Tumor'
        .build()

// Run detection for the selected objects

var pathObjects = getSelectedObjects()
if (pathObjects.isEmpty()) {
    Dialogs.showErrorMessage("StarDist", "Please select a parent object!")
    return
}
stardist.detectObjects(imageData, pathObjects)
println 'Done!'