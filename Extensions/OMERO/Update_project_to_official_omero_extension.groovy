 /**
  * Update the project.qpproj file to migrate from qupath-extension-biop-omero to
  * qupath-extension-omero standards
  * 
  * Step by step tuto
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
  * @author Remy Dornier
  * @date 2025.06.30
  * Last tested on QuPath-0.6.0
  * 
  */ 
 

 /*************************************************************
 * 
 ****************** Variables to change ******************
 * 
 * **********************************************************/


// The omero project path created using the quapth-extension-biop-omero
def oldOmeroProjectPath = "C:/Users/dornier/Desktop/New folder - Copy/project.qpproj"
// omero host pointing to the omero server
def oldIceServerHost = "omero-server.epfl.ch"
// omero host pointing to the webclient
def newWebServerHost = "omero.epfl.ch"
// the OMERO server port used in quapth-extension-biop-omero
def oldOmeroServerPort = 4064


/*************************************************************
 * 
 ****************** Beginning of the script ******************
 * 
 * **********************************************************/
 
 def oldServerProvider = "qupath.ext.biop.servers.omero.raw.OmeroRawImageServerBuilder"
 def newServerProvider = "qupath.ext.omero.core.imageserver.OmeroImageServerBuilder"
 
def qpprojFile = new File(oldOmeroProjectPath)
if(qpprojFile.exists()) {
     // get content og qpproj file
     String content = qpprojFile.getText(StandardCharsets.UTF_8.toString())
    
     // read JSON
     JSONObject obj = new JSONObject(content);
     obj.getJSONArray("images").each {
         def serverBuilder = it.get("serverBuilder")
         
         // check server builder
         if(serverBuilder.get("providerClassName").equals(oldServerProvider)) {
             // update provider, uri and args
             serverBuilder.put("providerClassName", newServerProvider)
             serverBuilder.put("uri", serverBuilder.get("uri").replace(oldIceServerHost, newWebServerHost))
             serverBuilder.put("args", new JSONArray(["--serverAddress", oldIceServerHost, "--serverPort", String.valueOf(oldOmeroServerPort), "--pixelAPI", "Ice"]))
         }
     }
    
    // save the file back
    try (BufferedWriter buffer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(qpprojFile), StandardCharsets.UTF_8))) {
        buffer.write(obj.toString());
    }catch(Exception e){
        throw e
    }
}
 
 
 // imports
 import java.nio.charset.StandardCharsets;
 import org.json.*; 