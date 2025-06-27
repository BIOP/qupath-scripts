/* 
 * Creates metadata fields for each image entry in the current project
 * based on the columns in the provided CSV file.
 * This script requires that a column called "Image Name" exists and will try to match
 * the names in that column with the image entries of the currently open project.
 * 
 * REQUIREMENTS
 * ============
 * You need the QuPath Extension BIOP for this script to work
 * https://github.com/BIOP/qupath-extension-biop
 * 
 * The format of the CSV file should be openable by Fiji/ImageJ as a ResultsTable
 * Try opening your CSV in Fiji first if you encounter errors. The separator may be incorrect. 
 * 
 * @author Olivier Burri
 * @date 2022.11.03
 * Last tested on QuPath-0.6.0
 */


 
def csvFile = FileChoosers.promptForFile( "CSV File", new FileChooser.ExtensionFilter(".csv","*.csv") )

def project = getProject()

Results.addMetadataToProject(project, csvFile)

println "Script Done"

//imports
import qupath.ext.biop.utils.Results
import javafx.stage.FileChooser