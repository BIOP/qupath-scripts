/*
 * Short Script to import relative projects from QuPath 0.1.4
 * If you do not nave relative paths, use 
 * File > Project > Import images from v0.1.2
 *
 * @author Olivier Burri
 * 
 * Last tested with QuPath 0.2.x
 */

import qupath.lib.gui.commands.*
import qupath.lib.gui.dialogs.Dialogs
import org.apache.commons.io.FileUtils


def qupath = getQuPath()
def new_project = qupath.getProject()
def new_project_directory = Projects.getBaseDirectory( new_project )
def title = "Import legacy project with relative paths"
if ( new_project == null ) {
    Dialogs.showNoProjectError( title )
    return
}
		
// Prompt for the old project
def project_file = Dialogs.promptForFile( title, null, "Project (v0.1.4)", ".qpproj")
if( project_file == null ) return false
		    

// Read the entries
def project_directory = project_file.getParent()
println( "Getting Project from &project_directory" )

def reader = new FileReader( project_file )
def old_project = GsonTools.getInstance().fromJson( reader, ProjectCommands.LegacyProject.class )

// Replace any instance of {$PROJECT_DIR} with the current project folder
old_project.getEntries().each{ entry ->
    if( entry.path.contains( "{\$PROJECT_DIR}" ) )
        entry.path = entry.path.replace( "{\$PROJECT_DIR}", project_directory )
    else 
        println( "Entry did not contain a relative Path" )
}

println( "Importing the following Images:" )

old_project.getEntries().each{ println("    "+it) }

def data_directory = new File( project_file.getParent(), "data" )
if ( !data_directory.exists() ) {
    Dialogs.showErrorMessage(title, "No data directory found for the legacy project!")
    return
}

// Import the old project into the currently open one
def task = new ProjectCommands.LegacyProjectTask( new_project, old_project.getEntries(), project_file.getParentFile() )
Platform.runLater( task )

// Copy the scripts and results directories if they exist. Other folders must be copied manually. 
def scripts_directory = new File( project_directory, "scripts" )
def results_directory = new File( project_directory, "results" )
if( scripts_directory.exists() ) {
    println "Copying scripts Directory" 
    def new_scripts_directory = new File ( new_project_directory, "scripts" )
    FileUtils.copyDirectory( scripts_directory, new_scripts_directory )
}

if( results_directory.exists() ) {
    println "Copying results Directory" 
    def new_results_directory = new File ( new_project_directory, "results" )
    FileUtils.copyDirectory( results_directory, new_results_directory )
}

try {
    new_project.syncChanges()
    //In case the user makes a mistake, make sure that each image has a unique name
    new_project.images = new_project.images.toUnique{ it.getImageName() }
} catch (def e) {
    logger.error("Error syncing project: " + e.getLocalizedMessage(), e)
}
    qupath.refreshProject()
    
println "Old project imported. Please make sure to copy any custom files or folders from your old project manually!"