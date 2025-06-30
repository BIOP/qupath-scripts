/* 
 * Creates metadata fields for each image entry in the current project
 * based on the "SlideName" column in an Olympus 'SlideProperties' .csv file
 * If an entry's name matches the given column, all the values in that row's column are appended
 * to the image entry. 
 * 
 * @author Olivier Burri
 * @date 2022.11.03
 * Last tested in QuPath-0.6.0
 */
 
 
def csvFile = FileChoosers.promptForFile( "Olympus SlideProperties File", new FileChooser.ExtensionFilter(".csv","*.csv") )

// Start of script 

def first = true
def keys = []
def allMetadata = []

// We are parsing the file manually and expect it to be separated with tabs
csvFile.splitEachLine("\t") { fields ->
    
    // First row, here are the column names
    if(first) {
        keys = fields
        first=false
     
    } else {
        // Not the first row, these are the values, which we add to a map
        def metadataMap = new HashMap<String, String>()
        fields.eachWithIndex{ f,i  ->
            metadataMap.put(keys[i], f)
        }
        allMetadata.add(metadataMap)
    }
}

// Add these to the project
def project = getProject()

// Add these to the entries whose names match the SlideName column
project.getImageList().each { entry ->
    def name = entry.getImageName()
    
    allMetadata.each{ line ->
        if (line.keySet().contains("SlideName") ){
            if (name.contains line['SlideName']) {

                // Add metadata here
                line.each{ key, value ->
                    entry.getMetadata().put(key, value)
                }
            }
        }
    }
}

project.syncChanges()