/*
 * Convert image files to pyramidal OME-Tiffs using QuPath's embedded 'convert-ome' console command
 * This script runs inside Fiji and makes use of the fact that it comes shipped with GPars
 * to run the conversion in parallel. 
 * The goal of this script is to create a new folder 'converted' in the same folder as the original images
 * These images can then be imported into a new QuPath project if desired. 
 *
 * Made for Windows PCs
 * WARNING: This has very large memory requirements. Make sure you have lots of RAM or limit the number of cores 
 *
 * @author Olivier Burri
 * @date 2022.11.03
 * Last tested on QuPath-0.6.0
 */
import groovyx.gpars.GParsPool

#@ File (style="directory") folder
#@ String (label="Image File Extension") imageExtension
 
// Create a 'converted' folder to store the new ome-tiff images
def convertedFolder = new File( folder.getParent(), "converted")
convertedFolder.mkdirs()

def fileList = folder.listFiles().findAll{ it.getName().endsWith( imageExtension ) }


GParsPool.withPool(6) {
    fileList.eachParallel{ file ->
		
        println file
		
        def cmd = "C:/QuPath-0.6.0/QuPath-0.6.0 (console).exe"
        def outputFile = new File (folder, file.getName() + "_info.txt")
        def pb = new ProcessBuilder(cmd, "convert-ome", file.getAbsolutePath(), new File( convertedFolder, file.getName()+".ome.tiff").getAbsolutePath(), "-y", "2.0", "-p", "-t", "1")
        				.redirectErrorStream(true)
        				.redirectOutput(outputFile)
        println "Running Command: &{pb.command()}"
        def process = pb.start()
        pb.waitFor()
        // Destroy explicitely
        pb.destroy()
    }
}
