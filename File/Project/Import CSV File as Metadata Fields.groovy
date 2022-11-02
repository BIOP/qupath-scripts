def csvFile = Dialogs.promptForFile( "Olympus SlideProperties File", null," *.csv", ".csv" )

def project = getProject()


new Results().addMetadataToProject(project, csvFile)

import qupath.ext.biop.utils.Results