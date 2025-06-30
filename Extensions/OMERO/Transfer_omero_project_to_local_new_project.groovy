/* 
 * Transfer all objects & metadata from a QuPath-OMERO project, to the current project 
 * 
 * /// Requirements ///
 * - You need to create and open an empty QuPath project. This project is use as the end-point of the script
 * - You need to have another qupath project containing ONLY images from OMERO.
 * - The name of the image within the QuPath-OMERO project must include the name of the serie (for fileset images)
 * 
 * Step by step tuto
 * 1. Connect to OMERO (Extension -> OMERO -> Browse server -> https://omero.epfl.ch)
 * 2. Open an empty project
 * 3. Open this script
 * 4. Change the "omeroProjectPath" variable with the path to your omero-qupath project containing omero images
 * 5. Change the "localDownloadPath" variable with the path where you want to download images.
 * 6. Run the script
 * 
 * NOTE: For MAC users, if your project is located on a server, then the path should begin with /Volumes/...
 *
 * @author Remy Dornier
 * @date 2023-07-10
 * Last tested on QuPath-0.6.0
 * version v4.0
 * 
 * REQUIRED DEPENDENCY : omero-ij.5.8.6-all.jar (previous versions won't work to download vsi files)
 * 
 * /// History ////
 * 2023.07.10 : first version --v1.0
 * 2023.09.04 : Use embedded download method from OMERO java gateway after bug fix for filset images --v2.0
 * 2023.09.04 : fix bug on annotation transfer --v2.0
 * 2023.09.04 : use the up-to-date dependency - omero-ij.5.8.2-all.jar --v2.0
 * 2025.02.17 : Copy the rest of the omero project in the local project
 * 2025.02.17 : update dependencies to omero-ij.5.8.6-all.jar --v3.0
 * 2025.06.30 : Update for QuPath-0.6.0 --v4.0
 * 2025.06.30 : Migration towards qupath-extension-omero --v4.0
 */
 
 
 /*************************************************************
 * 
 ****************** Variables to change ******************
 * 
 * **********************************************************/


// The omero project path that has the annotations. 
def omeroProjectPath = "D:\\Remy\\QuPath\\Migration Local-OMERO\\OmeroProject\\project.qpproj"
// Path of the folder where to download the images
def localDownloadPath = "D:\\Remy\\QuPath-OMERO\\Migration Local-OMERO\\localProject\\images"
// OMERO host for ICE api
def host = "omero-server.epfl.ch"
// OMERO port fo ICE API
def port = 4064


/*************************************************************
 * 
 ****************** Beginning of the script ******************
 * 
 * **********************************************************/


// Get the omero project
def omeroProject = ProjectIO.loadProject(new File(omeroProjectPath), BufferedImage.class)

// the current QuPath GUI
def qupath = QPEx.getQuPath()

def filesetOmeroImagesMap = new HashMap<>()
def qpOmeroImageList = omeroProject.getImageList()

if(qpOmeroImageList.isEmpty()) {
   println "No images on the OMERO project to download ; stop here the script execution"
   return
}

// setup the ICE OMERO server
def gateway = new Gateway(new IceLogger())
def ctx = null
def omeroClient = qpOmeroImageList.get(0).readImageData().getServer().getClient()
def sessionId = omeroClient.getApisHandler().getSessionUuid().get()
def cred = new LoginCredentials(sessionId, sessionId, host, port)        
def connectedUser = gateway.connect(cred)

if(gateway.isConnected()) {
    try{
        // setup the OMERO context
        ctx = new SecurityContext(connectedUser.getGroupId())
        ctx.setExperimenter(connectedUser);
        ctx.setServerInformation(cred.getServer());
        
        // list all filesets
        println "Read OMERO project from : "+omeroProjectPath
        qpOmeroImageList.each{ qpOmeroImage->
            // read image in omero qupath project
            def imageData = qpOmeroImage.readImageData()
            def server = imageData.getServer()
            def client = server.getClient()
            def imageId = server.getId()
             
            // read image on omero and get its fileset
            long filesetId = gateway.getFacility(BrowseFacility.class).getImage(ctx, imageId).getFilesetId()
            
            // add the image to the list of filesets
            if(filesetOmeroImagesMap.containsKey(filesetId)) {
                List<String> value = filesetOmeroImagesMap.get(filesetId)
                value.add(qpOmeroImage)
                filesetOmeroImagesMap.replace(filesetId, value)
            } else {
                List<String> value = new ArrayList()
                value.add(qpOmeroImage)
                filesetOmeroImagesMap.put(filesetId, value)
            }
            
            // close the hidden open server
            server.close()
        } 
        
        println "" + qpOmeroImageList.size() + " omero-qupath entries were read from the qupath project "
        println "" + filesetOmeroImagesMap.size() + " original files will be downloaded from omero"
        
        def filesetLocalImagesMap = new HashMap<>()
        filesetOmeroImagesMap.keySet().each{filesetId ->
            def qpOmeroImage = filesetOmeroImagesMap.get(filesetId).get(0)
            
            println "//////// Working on image "+qpOmeroImage.getImageName()+" ////////////"
            
            // read image in omero qupath project
            def omeroImageData = qpOmeroImage.readImageData()
            def omeroImageServer = omeroImageData.getServer()
            def omeroImageId = omeroImageServer.getId()
            
            // close the hidden open server
            omeroImageServer.close()
            
            // download the image from OMERO
            println "Downloading from OMERO"
            def files = gateway.getFacility(TransferFacility.class).downloadImage(ctx, localDownloadPath, omeroImageId);
            println files
            println files.size()
            println files.get(0).getParentFile().getParentFile().getAbsolutePath()
            println localDownloadPath
            // filter to get only the image file (and not files in sub-folders)
            def localImagePath = files.stream().filter(e->e.isFile() && e.getParentFile().getParentFile().getAbsolutePath().equals(localDownloadPath)).collect(Collectors.toList()).get(0).getAbsolutePath()
            println "Local image path : "+localImagePath
            
            // add image to the QuPath project and to the map of local imageEntries.
            print "Add to qupath project"
            filesetLocalImagesMap.put(filesetId, toQuPath(qupath, localImagePath))
        }
        
         println "*** Transfer data from omero project to current project ***"
        
        // loop over all local ImageEntries
        filesetLocalImagesMap.keySet().each{filesetId ->
            def qpOmeroImages = filesetOmeroImagesMap.get(filesetId)
            def qpLocalImages = filesetLocalImagesMap.get(filesetId)
          
            qpLocalImages.each{localImageEntry ->
                println "////////////// Working on local image "+localImageEntry.getImageName() + " ////////////////"
                def matchedOmeroEntry = null
                
                // if the image on OMERO is not a fileset, use the omero entry as is
                if(qpLocalImages.size() == 1) {
                    println "Single image" 
                    matchedOmeroEntry = qpOmeroImages.get(0)
                } else {
                    println "Fileset image" 
                    
                    // if fileset, then extract the name of serie (from the local entry) and
                    // find the corresponding omero entry
                    def localSerieName = localImageEntry.getImageName()
                    def localTokens = localSerieName.split(" - ")
                    
                    def nameToMatch = ""
                    if(localTokens.length > 1)
                        nameToMatch = localTokens[-1].toLowerCase()
                    else if (localTokens.length > 0)
                        nameToMatch = localTokens[0].toLowerCase()
                    
                    println "Need to match '"+ nameToMatch + "' in one of the omero entries"
                    
                    if(!nameToMatch.isEmpty()) {
                        def matchedFiles = qpOmeroImages.collect{e-> if(e.getImageName().toLowerCase().contains(nameToMatch)) return e}
                        matchedFiles = matchedFiles - null
                        println "Matched files "+matchedFiles
                        if(matchedFiles.size() > 0 && matchedFiles.get(0) != null) {
                            println "Found one !" 
                            matchedOmeroEntry = matchedFiles.get(0)
                        } else {
                          println "WARNING : No omero entry contains '"+nameToMatch+"'"
                        }
                    } else {
                        println "WARNING : the serie name to match is empty"
                    }
                }
                
                if(matchedOmeroEntry != null) {
                    
                    // get omeroProject image's name
                    def name = matchedOmeroEntry.getImageName()
                    println 'Opening Hierarchy in omero project for ' + name
                    
                    // get omeroProject image's hierarchy and pathObjects
                    def omeroHierarchy = matchedOmeroEntry.readHierarchy()
                    def omeroPathObjects = omeroHierarchy.getRootObject().getChildObjects()
                    
                    // read local image data and hierarchy
                    println 'Opening Hierarchy in current project for ' + localImageEntry.getImageName()
                    def localImageData = localImageEntry.readImageData()
                    def localHierarchy = localImageData.getHierarchy()
                    
                    print "Transfer annotations from omero project to current project"
                    // Use the transformObject to read everything in. It is borrowed from transfering objects with an affine transform
                    def localNewObjects = []
                    for (pathObject in omeroPathObjects) {
                        localNewObjects << transformObject(pathObject, true)
                    }
                
                    // omero hierarchy to local hierarchy
                    localHierarchy.addObjects(localNewObjects)
                    localImageEntry.saveImageData(localImageData)
                    fireHierarchyUpdate(localHierarchy)
                    
                    // set metadata on local image from omero image
                    print "Transfer metadata from omero project to current project"
                    def omeroMetadata = matchedOmeroEntry.getMetadata()
                    def localMetadata = matchedOmeroEntry.getMetadata()
                    omeroMetadata.each{localMetadata.put(it.getKey(), it.getValue())}
                
                    // close the hidden server
                    localImageData.getServer().close()
                    
                    println 'Done! for image  '+name
                } else {
                    println "No omero entry can be read. Skip this one"
                }
            }
        }
        
        
        println "Copying all files from qp local project to qp omero project..."
        def localProjDir = Projects.getBaseDirectory(getProject())
        def omeroProjDir = Projects.getBaseDirectory(omeroProject)
        
        copyFiles(omeroProjDir, omeroProjDir.getAbsolutePath(), localProjDir.getAbsolutePath())
        println "Finished !"
    }catch(Exception e) {
        println "An error occured during the script execution"
        println e
        println e.printStackTrace()
    }finally{ 
        println "Closing gateway connection..."
        gateway.disconnect()
        if(ctx != null) {
            ctx = new SecurityContext(-1)
            ctx.setExperimenter(new ExperimenterData());
        }
    }
    println "End of script"
}else {
   println "Cannot connect to OMERO via the ICE API" 
}
return


/**
 * Copying all files and folders from a source to destination
 * EXECPT "project.qpproj" and "project.qpproj.backup" files and "data" folder, which are very important 
 * to not modify.
 * 
 */
def copyFiles(srcDir, srcPath, destPath){
    def srcChildFiles = srcDir.listFiles()
    srcChildFiles.each{src ->
        if(src.isFile()) {
            def fname = src.getName() 
            if(!fname.equalsIgnoreCase("project.qpproj") && !fname.equalsIgnoreCase("project.qpproj.backup")){
                def absSrcPath = src.getAbsolutePath()
                def absDestPath = absSrcPath.replace(srcPath, destPath)
                File absDestFile = new File(absDestPath)
        
                if(absDestFile.getParentFile().exists()){
                    println "File '"+absSrcPath+"' is copied in the existing destination"
                    Files.copy(src.toPath(), absDestFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
                }else{
                    println "File '"+absSrcPath+"' is copied in the newly created destination"
                    Path path = Paths.get(absDestFile.getParent());
                    Files.createDirectories(path);
                    Files.copy(src.toPath(), absDestFile.toPath())
                }
            }
        }else{
            if(src.isDirectory() && !src.getName().equals("data")){
                def nFilesSrc = src.list().length
                copyFiles(src, srcPath, destPath)
            }
        }
    }    
}


/**
 * ////////////////////////////////////////////////////////
 * WARNING : THIS METHOD HAS BEEN COPYED AND ADAPTED FROM 
 * https://github.com/qupath/qupath/blob/main/qupath-gui-fx/src/main/java/qupath/lib/gui/commands/ProjectImportImagesCommand.java#L125
 * //////////////////////////////////////////////////////
 * 
 * Add an image to the current QuPath project
 */
def toQuPath(qupath, imageURI) {
    List<ProjectImageEntry<BufferedImage>> projectImages = new ArrayList<>();
    List<ProjectImageEntry<BufferedImage>> finalImages = new ArrayList<>();
    
    Project<BufferedImage> project = qupath.getProject();
    ImageServerBuilder<BufferedImage> imageServerBuilder = null

    URI uri = null;
    try {
        uri = GeneralTools.toURI(imageURI);
        var tempProject = ProjectIO.loadProject(uri, BufferedImage.class);
        projectImages = new ArrayList<>(tempProject.getImageList());
    } catch (Exception e) {
        logger.warn("Unable to add images from {} ({})", imageURI, e.getLocalizedMessage());
    }

    // If we have projects, try adding images from these first
    if (!projectImages.isEmpty()) {
        for (var temp : projectImages) {
            try {
                project.addDuplicate(temp, true);
            } catch (Exception e) {
                logger.error("Unable to copy images to the current project");
            }
        }
    }

    // define the builder
    ImageServerBuilder.UriImageSupport<BufferedImage> support;
    if(imageServerBuilder == null)
        support = ImageServers.getImageSupport(uri, "");
    else
        support = ImageServers.getImageSupport(imageServerBuilder, uri, "");

    if (support != null){
        List<ImageServerBuilder.ServerBuilder<BufferedImage>> builders = support.getBuilders();

        // Add everything in order first
        List<ProjectImageEntry<BufferedImage>> entries = new ArrayList<>();
        for (var builder : builders) {
            entries.add(project.addImage(builder));
        }

        // Initialize (the slow bit)
        for (var entry : entries) {
            // initialize entry
            try (ImageServer<BufferedImage> server = entry.getServerBuilder().build()){
                // Set the image name
                String name = ServerTools.getDisplayableImageName(server);
                entry.setImageName(name);

                // Pyramidalize this if we need to
                ImageServer<BufferedImage> server2 = server;
                int minPyramidDimension = PathPrefs.minPyramidDimensionProperty().get();
                if (server.nResolutions() == 1 && Math.max(server.getWidth(), server.getHeight()) > minPyramidDimension) {
                    var serverTemp = ImageServers.pyramidalize(server);
                    if (serverTemp.nResolutions() > 1) {
                        logger.debug("Auto-generating image pyramid for " + name);
                        server2 = serverTemp;
                    } else
                        serverTemp.close();
                }

                if (server != server2)
                    server2.close();

            } catch (Exception e) {
                logger.warn("Exception adding " + entry, e);
            }

            finalImages.add(entry)
        }

        // refresh the project
        try {
            project.syncChanges();
        } catch (IOException e1) {
            Dialogs.showErrorMessage("Sync project", e1);
        }
        qupath.refreshProject();
    }
    
    return finalImages
}



/**
 * Transform object, recursively transforming all child objects
 *
 * @param pathObject
 * @param transform
 * @return
 */
PathObject transformObject( def pathObject, boolean copyMeasurements ) {
    def newObject = PathObjectTools.transformObject(pathObject, null, copyMeasurements)
    if ( pathObject.getName() != null ) newObject.setName( pathObject.getName() )
    // Handle child objects
    if ( pathObject.hasChildObjects() ) {
        newObject.addChildObjects(pathObject.getChildObjects().collect{ transformObject( it, copyMeasurements ) } )
    }
    return newObject
}


/**
 * imports
 */
import omero.gateway.facility.TransferFacility
import omero.gateway.facility.BrowseFacility
import qupath.ext.omero.core.pixelapis.ice.IceLogger
import omero.gateway.Gateway;
import omero.gateway.LoginCredentials;
import omero.gateway.SecurityContext;
import omero.gateway.model.ExperimenterData;
import java.util.stream.Collectors
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption

import qupath.lib.common.GeneralTools;
import qupath.lib.gui.QuPathGUI;
import qupath.lib.gui.dialogs.Dialogs;
import qupath.lib.gui.prefs.PathPrefs;
import qupath.lib.gui.tools.PaneTools;
import qupath.lib.images.servers.ImageServer;
import qupath.lib.images.servers.ImageServerBuilder;
import qupath.lib.images.servers.ImageServers;
import qupath.lib.images.servers.ServerTools;
import qupath.lib.projects.Project;
import qupath.lib.projects.ProjectIO;
import qupath.lib.projects.ProjectImageEntry;
import qupath.lib.gui.scripting.QPEx
