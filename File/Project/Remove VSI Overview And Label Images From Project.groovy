// Cleaning out VSI Imports
// Removes all the 'overview' and 'label' images
// When project is refreshed, it is automatically saved
// NOTE: If you need to undo these changes, QuPath will always generate a .backup file before the last modification 
//       Delete the 'project.qpproj' and remove the last extension of `project.qpproj.backup`
// Date: 2020.07
// Author: Olivier Burri
// Last modification: Simplified code and made it compatible with QuPath 0.2.1

def project = getProject()

project.getImageList().each { entry ->
    if ( ( entry.getImageName() =~ 'overview' ) || ( entry.getImageName() =~ 'label' ) ) {
        project.removeImage( entry, false )
    }
}

getQuPath().refreshProject()

//ProjectIO.writeProject(project)
