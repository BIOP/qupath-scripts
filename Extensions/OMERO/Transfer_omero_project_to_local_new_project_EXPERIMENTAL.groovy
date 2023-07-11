/* 
 * Transfer all objects & metadata from a QuPath-OMERO project, to the current project 
 * 
 * /// Requirements ///
 * - You need to create and open an empty QuPath project. This project is use as the end-point of the script
 * - You need to have another qupath project containing ONLY images from OMERO.
 * - The name of the image within the QuPath-OMERO project must include the name of the serie (for fileset images)
 * 
 * Step by step tuto
 * 1. Open an empty project
 * 2. Open this script
 * 3. Change the "omeroProjectPath" variable with the path to your omero-qupath project containing omero images
 * 4. Change the "localDownloadPath" variable with the path where you want to download images.
 * 4. Run the script
 * 
 * 5. For MAC users, if your project is located on a server, then the path should begin with /Volumes/...
 *
 * @author Remy Dornier
 * @date 2023-07-10
 * Last tested on QuPath-0.4.3
 */
 
 
 /*************************************************************
 * 
 ****************** Variables to change ******************
 * 
 * **********************************************************/


// The omero project path that has the annotations. 
def omeroProjectPath = "D:\\Remy\\QuPath\\Migration Local-OMERO\\omeroProject\\project.qpproj"

// Path of the folder where to download the images
def localDownloadPath = "D:\\Remy\\QuPath\\Migration Local-OMERO\\LocalProject\\images"


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

// list all filesets
println "Read OMERO project from : "+omeroProjectPath
qpOmeroImageList.each{ qpOmeroImage->

    // read image in omero qupath project
    def imageData = qpOmeroImage.readImageData()
    def server = imageData.getServer()
    def client = server.getClient()
    def imageId = server.getId()
    
    // read image on omero and get its fileset
    long filesetId = OmeroRawTools.readOmeroImage(client, imageId).getFilesetId()
    
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
    def omeroClient = omeroImageServer.getClient()
    def omeroImageId = omeroImageServer.getId()
    
    // close the hidden open server
    omeroImageServer.close()
    
    // download the image from OMERO
    println "Download from OMERO"
    def files = downloadImage(omeroClient, localDownloadPath, omeroImageId)
    //def files = omeroClient.getGateway().getFacility(TransferFacility.class).downloadImage(omeroClient.getContext(), localDownloadPath, omeroImageId);
    
    // filter to get only the omage file (and not files in sub-folders)
    def localImagePath = files.stream().filter(e->e.isFile() && e.getParentFile().getAbsolutePath().equals(localDownloadPath)).collect(Collectors.toList()).get(0).getAbsolutePath()
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
                println "Matches fils "+matchedFiles
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
            def metadata = matchedOmeroEntry.getMetadataMap()
            metadata.each{localImageEntry.putMetadataValue(it.getKey(), it.getValue())}
        
            // close the hidden server
            localImageData.getServer().close()
            
            println 'Done! for image  '+name
        } else {
            println "No omero entry can be read. Skip this one"
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
 * ////////////////////////////////////////////////////////
 * WARNING : THIS METHOD HAS BEEN COPYED AND ADAPTED FROM 
 * https://github.com/ome/omero-gateway-java/blob/master/src/main/java/omero/gateway/facility/TransferFacilityHelper.java
 * //////////////////////////////////////////////////////
 * 
 * 
 * Downloads the original file of an image from the server.
 *
 * client : OmeroClient.
 * targetPath : Path to the file.
 * imageId : The identifier of the image.
 * @return the list of all downloaded files (does not take care if they are inside folders or not)
 *
 */
def downloadImage(omeroClient, targetPath, imageId) {
    List<File> files = new ArrayList<File>();
    def gateway = omeroClient.getGateway()
    def browse = gateway.getFacility(BrowseFacility.class)
    def context = omeroClient.getContext()
    
    ImageData image = browse.findObject(context, ImageData.class, imageId,true);

    String query;
    List<?> filesets;
    try {
        IQueryPrx service = gateway.getQueryService(context);
        ParametersI param = new ParametersI();
        long id;
        if (image.isFSImage()) {
            id = image.getId();
            List<RType> l = new ArrayList<RType>();
            l.add(rlong(id));
            param.add("imageIds", rlist(l));
            query = createFileSetQuery();
        } else {// Prior to FS
            if (image.isArchived()) {
                StringBuffer buffer = new StringBuffer();
                id = image.getDefaultPixels().getId();
                buffer.append("select ofile from OriginalFile as ofile ");
                buffer.append("join fetch ofile.hasher ");
                buffer.append("left join ofile.pixelsFileMaps as pfm ");
                buffer.append("left join pfm.child as child ");
                buffer.append("where child.id = :id");
                param.map.put("id", rlong(id));
                query = buffer.toString();
            } else
                return null;
        }
        filesets = service.findAllByQuery(query, param);
    } catch (Exception e) {
        throw new DSAccessException("Cannot retrieve original file", e);
    }

    Map<Boolean, Object> result = new HashMap<Boolean, Object>();
    if (CollectionUtils.isEmpty(filesets))
        return files;
    List<File> downloaded = new ArrayList<File>();
    List<String> notDownloaded = new ArrayList<String>();
    result.put(Boolean.valueOf(true), downloaded);
    result.put(Boolean.valueOf(false), notDownloaded);

    if (image.isFSImage()) {
        for (Object tmp : filesets) {
            Fileset fs = (Fileset) tmp;

            String repoPath = fs.getTemplatePrefix().getValue();
            for (FilesetEntry fse: fs.copyUsedFiles()) {
                OriginalFile of = fse.getOriginalFile();
                String ofDir = of.getPath().getValue().replace(repoPath, "");
                File outDir = new File(targetPath+File.separator+ofDir);
                outDir.mkdirs();
                File saved = saveOriginalFile(gateway, context, of, outDir);
                if (saved != null)
                    downloaded.add(saved);
                else
                    notDownloaded.add(of.getName().getValue());
            }
        }
    }
    else { //Prior to FS
        for (Object tmp : filesets) {
            OriginalFile of = (OriginalFile) tmp;
            File outDir = new File(targetPath);
            File saved = saveOriginalFile(gateway, context, of, outDir);
            if (saved != null)
                downloaded.add(saved);
            else
                notDownloaded.add(of.getName().getValue());
        }
    }

    return downloaded;
}


/**
 * ////////////////////////////////////////////////////////
 * WARNING : THIS METHOD HAS BEEN COPYED AND ADAPTED FROM 
 * https://github.com/ome/omero-gateway-java/blob/master/src/main/java/omero/gateway/facility/TransferFacilityHelper.java
 * //////////////////////////////////////////////////////
 * 
 * 
 * Save an OriginalFile of into directory dir
 * @param gateway The OMERO gateway
 * @param ctx The SecurityContext
 * @param of The OriginalFile
 * @param dir The output directory
 * @return The File if the operation was successfull, null if it wasn't.
 */
def saveOriginalFile(gateway, ctx, of, dir) {
    def INC = 262144
    File out = new File(dir, of.getName().getValue());
    if (out.exists()) {
        return null;
    }

    try {
        RawFileStorePrx store = gateway.getRawFileService(ctx);
        store.setFileId(of.getId().getValue());

        long size = of.getSize().getValue();
        long offset = 0;
        try (FileOutputStream stream = new FileOutputStream(out))
        {
            for (offset = 0; (offset+INC) < size;) {
                stream.write(store.read(offset, INC));
                offset += INC;
            }
            stream.write(store.read(offset, (int) (size-offset)));
        }
    } catch (Exception e) {

        return null;
    }
    return out;
}



/**
 * ////////////////////////////////////////////////////////
 * WARNING : THIS METHOD HAS BEEN COPYED FROM 
 * https://github.com/ome/omero-gateway-java/blob/master/src/main/java/omero/gateway/facility/TransferFacilityHelper.java
 * //////////////////////////////////////////////////////
 * 
 * 
 * Creates the query to load the file set corresponding to a given image.
 *
 * @return the query.
 */
private String createFileSetQuery() {
    StringBuffer buffer = new StringBuffer();
    buffer.append("select fs from Fileset as fs ");
    buffer.append("join fetch fs.images as image ");
    buffer.append("left outer join fetch fs.usedFiles as usedFile ");
    buffer.append("join fetch usedFile.originalFile as f ");
    buffer.append("join fetch f.hasher ");
    buffer.append("where image.id in (:imageIds)");
    return buffer.toString();
}


/**
 * imports
 */
import omero.gateway.facility.TransferFacility
import omero.gateway.facility.BrowseFacility
import qupath.ext.biop.servers.omero.raw.*
import java.util.stream.Collectors
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import omero.RType;
import omero.api.IQueryPrx;
import omero.api.RawFileStorePrx;
import omero.gateway.Gateway;
import omero.gateway.SecurityContext;
import omero.gateway.exception.DSAccessException;
import omero.gateway.exception.DSOutOfServiceException;
import omero.gateway.model.ImageData;
import omero.model.Fileset;
import omero.model.FilesetEntry;
import omero.model.OriginalFile;
import omero.sys.ParametersI;

import org.apache.commons.collections.CollectionUtils;
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

import static omero.rtypes.rlist
import static omero.rtypes.rlong
