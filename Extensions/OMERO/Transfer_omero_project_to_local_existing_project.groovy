/* 
 * Transfer all objects & metadata from a QuPath-OMERO project, matching images name to set objects to the 
 * correct image, based on a csv file listing the correspondance local name <> omero name.
 * 
 * The csv file has to be formatted with local qpimage names in the first column and the corresponding
 * omero qpimage names in the second column.
 * 
 * A project must be open in QuPath. This project must contain at least all the images you want 
 * to transfer annotation to (i.e. those listed in the csv file, in the first column).
 * 
 * You can use the script "Export_Images_Name_Within_Project_as_CSV.groovy" (Automate -> shared scripts -> File -> Export)
 * to list all image name within a project. Then, you can make your correspondance csv file yourself.
 * 
 * 
 * Step by step tuto
 * 1. Open the project containing images stored locally
 * 2. Open this script
 * 3. Change the "omeroProjectPath" variable with the path to your qp project containing omero images
 * 4. Change the "imagesMapPath" variable with the path to your csv file containing the local <> omero qpimage names
 * 5. Run the script
 * 
 * 6. For MAC users, if your project is located on a server, then the path should begin with /Volumes/...
 *
 * @author Remy Dornier
 * @date 2023-07-11
 * Last tested on QuPath-0.6.0
 * 
 * History 
 *  - 2025.02.17 : Copy the rest of the local project in the OMERO project
 *  - 2025.02.17 : Dissociate metadata and object deletion
 *  - 2025.06.30 : Update for QuPath-0.6.0
 * 
 */
 
 
 /*************************************************************
 * 
 ****************** Variables to change ******************
 * 
 * **********************************************************/
 
// Remove objects from the active OMERO ImageEntry or keep it as is, and just add?
def deleteExistingObjects = true
def deleteExistingMetadata = false

// The local project path that has the annotations. 
// Image name in the omero project must match the ones in the local project
def omeroProjectPath = "D:\\Remy\\QuPath\\Migration Local-OMERO\\OmeroProject\\project.qpproj"

// Image name in the omero project must match the ones in the local project
def imagesMapPath = "D:\\Remy\\QuPath-OMERO\\Migration Local-OMERO\\Correspondance-local-omero.csv"


/*************************************************************
 * 
 ****************** Beginning of the script ******************
 * 
 * **********************************************************/

// read the csv file with the correspondance local <> omero
Map<String, String> imagesMap = readImagesMap(imagesMapPath)

// Get the omero project
def omeroProject = ProjectIO.loadProject(new File(omeroProjectPath), BufferedImage.class)

// list all the images within both projects
def localImageList = getProject().getImageList()
def omeroImageList = omeroProject.getImageList()

imagesMap.keySet().each{localImageName ->
    println "//////// Working on image "+localImageName+" ////////////"
    def omeroImageName = imagesMap.get(localImageName)
    
    // select the right image based the corresponding names
    def localImageEntry = localImageList.find{ entry-> entry.getImageName().equals(localImageName) }
    def omeroImageEntry = omeroImageList.find{ entry-> entry.getImageName().equals(omeroImageName) }
    
    if (localImageEntry == null) {
        println 'Could not find the image in the local project with name ' + name
        return
    }
    
    if (omeroImageEntry == null) {
        println 'Could not find the image in the omero project with name ' + name
        return
    }
    
    // get omeroProject image's name
    def name = omeroImageEntry.getImageName()
    println 'Opening Hierarchy in omero project for ' + name
    
    // get omeroProject image's hierarchy and pathObjects
    def omeroHierarchy = omeroImageEntry.readHierarchy()
    def omeroPathObjects = omeroHierarchy.getRootObject().getChildObjects()
    
    // read local image data and hierarchy
    println 'Opening Hierarchy in current project for ' + localImageEntry.getImageName()
    def localImageData = localImageEntry.readImageData()
    def localHierarchy = localImageData.getHierarchy()
    
    // delete lcoal image hierarchy & metadata
    if (deleteExistingObjects) {
        println 'Delete local previous hierarchy '
        localHierarchy.clearAll();
    }
        // delete omero image hierarchy & metadata
    if (deleteExistingMetadata) {
        println 'Delete local previous metadata '
        localImageEntry.getMetadata().clear();
    }
    
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
    def omeroMetadata = omeroImageEntry.getMetadata()
    def localMetadata = localImageEntry.getMetadata()
    omeroMetadata.each{localMetadata.put(it.getKey(), it.getValue())}

    // close the hidden server
    localImageData.getServer().close()
    
    println 'Done! for image  '+name
}

println "Copying all files from qp local project to qp omero project..."
def localProjDir = Projects.getBaseDirectory(getProject())
def omeroProjDir = Projects.getBaseDirectory(omeroProject)

copyFiles(omeroProjDir, omeroProjDir.getAbsolutePath(), localProjDir.getAbsolutePath())
println "Finished !"
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
 * Read the provided csv file with images names and store them in a map
 * Map<local_name, omero_name>
 *
 * @param imagesMapPath
 * @return the map of image names
 */
Map<String,String> readImagesMap(imagesMapPath) {
    Map<String, String> images = new HashMap<>()
    def file = new File(imagesMapPath)
    if(file.exists()) {
       file.eachLine{ line ->
           if(!line.isEmpty()){
               def tokens = line.split(",")
               if(tokens.length > 1)
                   images.put(tokens[0], tokens[1])
           }
       }
    } else {
       println "The file "+ file.getName() + " does not exists in "+imagesMapPath
    }
    
    return images
}

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption