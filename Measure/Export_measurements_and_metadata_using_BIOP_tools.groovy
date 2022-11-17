/*
 * Export selected measurements for selected objects using BIOP Extension
 * This will include the metadata fields associated to each image entry
 * Requires BIOP QuPath Extension 
 * https://github.com/BIOP/qupath-extension-biop
 * 
 * NOTE: Subsequent calls to the same file location will append results
 * Desirable with "Run for project", but be sure to delete the results file
 * Before starting again in order to avoid duplicate entries. 
 *
 * @author Olivier Burri
 * @date 20221103
 */
import qupath.ext.biop.utils.Results

// Choose objects to export
def objects = getAnnotationObjects() // or detections, or a filtered list

// Option 1: Send every measurement and metadata key value pairs, it will create a file
// results.txt in the a 'results' folder of the project.
Results.sentResultsToFile( objects )


// Option 2: Define the names of the measurements that you wish to export explicitely
// These are the same names as the columns in "Show annotation measurements" or "Show detection measurements"
def measurements = ["Image", "Parent", "Class", "Num detections"]

Results.sendResultsToFile( measurements, objects )


// Option 3: You can define the file name yourself
def resultsFolder = new File( Projects.getBaseDirectory(QP.getProject()), "Results folder" )
resultsFolder.mkdirs()
def resultsFile = new File( resultsFolder, "My_Results.txt" )

Results.sendResultsToFile( measurements, objects, resultsFile )