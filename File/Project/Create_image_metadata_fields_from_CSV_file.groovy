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
 * @date 20221103
 */

import qupath.ext.biop.utils.Results
 
def csvFile = Dialogs.promptForFile( "CSV File", null," *.csv", ".csv" )

def project = getProject()

new Results().addMetadataToProject(project, csvFile)

println "Script Done"