
def first = true
def keys = []
def allMetadata = []

def csvFile = Dialogs.promptForFile( "Olympus SlideProperties File", null," *.csv", ".csv" )

csvFile.splitEachLine("\t") { fields ->
    
    if(first) {
        keys = fields
        first=false
     
    } else {
        def metadataMap = new HashMap<String, String>()
        fields.eachWithIndex{ f,i  ->
            metadataMap.put(keys[i], f)
        }
        allMetadata.add(metadataMap)
    }
}

// Add these to the project
def project = getProject()

project.getImageList().each { entry ->
    def name = entry.getImageName()
    allMetadata.each{ line ->
        if (line.keySet().contains("SlideName") ){
            if (name.contains line['SlideName']) {
                // Add metadata here
                line.each{ key, value ->
                    entry.putMetadataValue(key, value)
                }
            }
        }
    }
}

project.syncChanges()