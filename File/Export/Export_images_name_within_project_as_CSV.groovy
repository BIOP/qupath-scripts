/* 
 * List the name of images within the qupath project and save it as a csv file.
 * The csv file is saved in the project base directory.
 * 
 * 
 * Step by step tuto
 * 1. Open a project
 * 2. Open this script
 * 3. Run the script
 *
 * @author Remy Dornier
 * @date 2023-07-11
 * Last tested on QuPath-0.6.0
 */


/*************************************************************
 * 
 ****************** Beginning of the script ******************
 * 
 * **********************************************************/

// create the file
def baseDir = new File(buildFilePath(PROJECT_BASE_DIR))

def file = buildFilePath(PROJECT_BASE_DIR, baseDir.getParentFile().getName()+"_Images_list.csv")
BufferedWriter buffer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));

// read the image names
getProject().getImageList().each{ qpImage->
   buffer.write(qpImage.getImageName() + "\n");
}

// close the file
buffer.close();

println "Image names have been saved as a csv file"


/**
 * imports
 */
 import java.io.BufferedWriter;
 import java.io.FileOutputStream;
 import java.io.OutputStreamWriter;
