/* TRAIN A CELLPOSE MODEL FROM Images In QuPath
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
 * Last tested on QuPath-0.4.0
 */


// Build a Cellpose instance for training
def cellpose = Cellpose2D.builder( "cyto2" ) // Can choose "None" if you want to train from scratch
                .channels( "DAPI", "CY3" )  // or use work with .cellposeChannels( channel1, channel2 ) and follow the cellpose way
//                .preprocess( ImageOps.Filters.gaussianBlur( 1 ) ) // Optional preprocessing QuPath Ops 
                .epochs( 500 )              // Optional: will default to 500
                .learningRate( 0.2 )        // Optional: Will default to 0.2
                .batchSize( 8 )             // Optional: Will default to 8
//                .modelDirectory( new File( "My/location" ) ) // Optional place to store resulting model. Will default to QuPath project root, and make a 'models' folder 
                .build()

// Once ready for training you can call the train() method
// train() will:
// 1. Go through the current project and save all "Training" and "Validation" regions into a temp folder (inside the current project)
// 2. Run the cellpose training via command line
// 3. Recover the model file after training, and copy it to where you defined in the builder, returning the reference to it

def resultModel = cellpose.train()

// Pick up results to see how the training was performed
println "Model Saved under "
println resultModel.getAbsolutePath().toString()

// You can get a ResultsTable of the training. 
def results = cellpose.getTrainingResults()
results.show("Cellpose Training Results")

// Finally you have access to a very simple graph 
cellpose.showTrainingGraph()

import qupath.ext.biop.cellpose.Cellpose2D
