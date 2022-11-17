

import qupath.tensorflow.stardist.StarDist2D

// Specify the model directory (you will need to change this!)
//def pathModel = 'Z:\\public\\0 - BIOP Data\\StarDist Models\\silvia_rat_brain_deconvolved_usr_augment_r32_p256_g2_e400_se100_b8_aug_challenge'
def pathModel = 'C:\\Users\\oburri\\Desktop\\he_heavy_augment.pb'


def stardist = StarDist2D.builder(pathModel)
        .threshold(0.4)              // Probability (detection) threshold
        .channels('Original')            // Select detection channel
        .normalizePercentiles(1, 99.8) // Percentile normalization
        .pixelSize(0.5)              // Resolution for detection
//        .tileSize(2048)              // Specify width & height of the tile used for prediction
//        .cellExpansion(5.0)          // Approximate cells based upon nucleus expansion
//        .cellConstrainScale(1.5)     // Constrain cell expansion using nucleus size
        .ignoreCellOverlaps(false)   // Set to true if you don't care if cells expand into one another
        .measureShape()              // Add shape measurements
        .measureIntensity()          // Add cell measurements (in all compartments)
        .includeProbability(true)    // Add probability as a measurement (enables later filtering)
       // .nThreads(4)                 // Limit the number of threads used for (possibly parallel) processing
        .simplify(1)                 // Control how polygons are 'simplified' to remove unnecessary vertices
        .doLog()                     // Use this to log a bit more information while running the script
        .build()
        
        
// Run detection for the selected objects
def imageData = getCurrentImageData()
def pathObjects = getSelectedObjects()
if (pathObjects.isEmpty()) {
    Dialogs.showErrorMessage("StarDist", "Please select a parent object!")
    return
}
stardist.detectObjects(imageData, pathObjects)
println 'Done!'