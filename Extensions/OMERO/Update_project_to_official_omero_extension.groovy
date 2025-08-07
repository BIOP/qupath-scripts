/**
 * Update the project.qpproj file to migrate from qupath-extension-biop-omero to
 * qupath-extension-omero standards
 * 
 * Step by step tutorial:
 * 1. Copy the entire project folder to update (just to save the current state, in case something happens)
 * 2. Open QuPath, but DON'T OPEN THE PROJECT IN QUPATH
 * 3. Open this script
 * 4. Change the "oldOmeroProjectPath" variable with the path to your omero-qupath project file to update
 * 5. Change the "oldIceServerHost" variable with the omero server host address (the one required for qupath-extension-biop-omero)
 * 6. Change the "newWebServerHost" variable with the omero webclient address (can be the same or not)
 * 7. Change the "oldOmeroServerPort" variable with the omero server port (the one required for qupath-extension-biop-omero)
 * 8. Run the script
 * 
 * NOTE: For MAC users, if your project is located on a server, then the path should begin with /Volumes/...
 * 
 * @author Remy Dornier, Nicolas Chiaruttini, Claude.ai
 * @date 2025.06.30
 * Last tested on QuPath-0.6.0
 * 
 */ 

// imports
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import com.google.gson.*

/*************************************************************
 * 
 ****************** Variables to change ******************
 * 
 ***********************************************************/
// The omero project path created using the qupath-extension-biop-omero
def oldOmeroProjectPath = "F:/IAGitLab/yllza.jasiqi_UPDANGELO/aligncycles/qupath_project_converted/project.qpproj"
// omero host pointing to the omero server
def oldIceServerHost = "omero-server.epfl.ch"
// omero host pointing to the webclient
def newWebServerHost = "omero.epfl.ch"
// the OMERO server port used in qupath-extension-biop-omero
def oldOmeroServerPort = 4064

/*************************************************************
 * 
 ****************** Beginning of the script ******************
 * 
 ***********************************************************/

def qpprojFile = new File(oldOmeroProjectPath)

// Validation checks
if (!qpprojFile.exists()) {
    println "ERROR: Project file does not exist: ${oldOmeroProjectPath}"
    return
}

if (!qpprojFile.canRead()) {
    println "ERROR: Cannot read project file: ${oldOmeroProjectPath}"
    return
}

if (!qpprojFile.canWrite()) {
    println "ERROR: Cannot write to project file: ${oldOmeroProjectPath}"
    return
}

println "Starting migration of project: ${qpprojFile.name}"

// Constants for the migration
def oldServerProvider = "qupath.ext.biop.servers.omero.raw.OmeroRawImageServerBuilder"
def newServerProvider = "qupath.ext.omero.core.imageserver.OmeroImageServerBuilder"

// Track migration statistics
def migratedCount = 0

// Recursive function to process nested serverBuilders
def processServerBuilder = null
processServerBuilder = { jsonElement ->
    if (jsonElement.isJsonObject()) {
        def jsonObj = jsonElement.getAsJsonObject()
        
        // Check if this object IS a serverBuilder that needs migration
        if (jsonObj.has("providerClassName")) {
            def providerClassName = jsonObj.get("providerClassName").getAsString()
            if (providerClassName.equals(oldServerProvider)) {
                println "    Found nested serverBuilder to migrate"
                
                // Update provider class name
                jsonObj.addProperty("providerClassName", newServerProvider)
                
                // Update URI if it contains the old server host
                if (jsonObj.has("uri")) {
                    def oldUri = jsonObj.get("uri").getAsString()
                    def newUri = oldUri.replace(oldIceServerHost, newWebServerHost)
                    jsonObj.addProperty("uri", newUri)
                    
                    if (!oldUri.equals(newUri)) {
                        println "      Updated URI: ${oldUri} -> ${newUri}"
                    }
                }
                
                // Add new arguments
                def args = new JsonArray()
                args.add("--serverAddress")
                args.add(oldIceServerHost)
                args.add("--serverPort")
                args.add(String.valueOf(oldOmeroServerPort))
                args.add("--pixelAPI")
                args.add("Ice")
                
                jsonObj.add("args", args)
                migratedCount++
            }
        }
        
        // Now recursively process ALL properties of this object
        jsonObj.entrySet().each { entry ->
            def value = entry.value
            processServerBuilder(value)
        }
        
    } else if (jsonElement.isJsonArray()) {
        def jsonArray = jsonElement.getAsJsonArray()
        jsonArray.each { arrayElement ->
            processServerBuilder(arrayElement)
        }
    }
}

try {
    // Create backup with specific name
    def backupFile = new File(qpprojFile.parent, "project.qproj.backup_before_convertion")
    Files.copy(qpprojFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
    println "Backup created: ${backupFile.absolutePath}"
    
    // Read and parse JSON using Gson
    def gson = new Gson()
    String content = qpprojFile.getText(StandardCharsets.UTF_8.toString())
    def jsonElement = JsonParser.parseString(content)
    def rootObject = jsonElement.getAsJsonObject()
    
    // Track migration statistics
    int totalImages = 0
    
    if (rootObject.has("images") && rootObject.get("images").isJsonArray()) {
        def imagesArray = rootObject.get("images").getAsJsonArray()
        totalImages = imagesArray.size()
        
        println "Found ${totalImages} images to process"
        
        // Process each image and all its nested serverBuilders
        for (int i = 0; i < imagesArray.size(); i++) {
            println "  Processing image ${i + 1}/${totalImages}"
            def imageElement = imagesArray.get(i)
            processServerBuilder(imageElement)
        }
    } else {
        println "No images found in project file"
    }
    
    // Save the updated file with pretty printing
    def prettyGson = new GsonBuilder().setPrettyPrinting().create()
    try (BufferedWriter buffer = new BufferedWriter(
            new OutputStreamWriter(
                new FileOutputStream(qpprojFile), 
                StandardCharsets.UTF_8))) {
        buffer.write(prettyGson.toJson(jsonElement))
    }
    
    // Print migration summary
    println "\n" + "="*50
    println "MIGRATION SUMMARY"
    println "="*50
    println "Total images processed: ${totalImages}"
    println "Total serverBuilders migrated: ${migratedCount}"
    println "Backup file: ${backupFile.absolutePath}"
    
    if (migratedCount > 0) {
        println "\n✓ Migration completed successfully!"
        println "  ${migratedCount} serverBuilder(s) were updated across all nesting levels"
    } else {
        println "\n⚠ No serverBuilders found that needed migration."
        println "  Either the project was already migrated or uses different providers."
    }
    
} catch (Exception e) {
    println "FATAL ERROR during migration: ${e.message}"
    e.printStackTrace()
    println "\nThe original file should be intact. Check for backup files if needed."
}
