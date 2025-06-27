/*
 * Convert image files to pyramidal OME-Tiffs using QuPath's embedded 'convert-ome' console command
 * This script runs inside QuPath, but does not use nor modify an open project
 * The goal of this script is to create a new folder 'converted' in the same folder as the original images
 * These images can then be imported into a new QuPath project if desired. 
 *
 * Made for Windows PCs
 *
 * @author Olivier Burri
 * @date 2022.11.03
 * Last tested on QuPath-0.6.0
 */

// What is the extension of images being converted? For filtering the input folder
def imageExtension = "ndpi"

// Start of Script 

// Figure out where QuPath is to locate the executable. If we are running from within QuPath, this line works
def qpDirectory = new File( System.getProperty( "user.dir" ) )
def qpExeFile = "QuPath-0.6.0 (console).exe"

// Request where the images to convert are
def folder = FileChoosers.promptForDirectory( "Select folder", qpDirectory )

// Create a 'converted' folder to store the new ome-tiff images
def convertedFolder = new File( folder.getParent(), "converted" )
convertedFolder.mkdirs()

// Find the images
def fileList = folder.listFiles().findAll{ it.getName().endsWith( imageExtension ) }

// Run the conversion
fileList.each{ file ->
    println "Converting $file"
    
    def qpConsole = new File( qpDirectory, qpExeFile )
    
    def cmd = qpConsole.toString()
    
    // Output the progress to a file
    def outputFile = new File (folder, file.getName() + "_info.txt")
    
    // If your data is 3D or has time, you may need to change the parameters, especially -t 1 
    
    def pb = new ProcessBuilder(cmd, "convert-ome", file.getAbsolutePath(), new File( convertedFolder, file.getName()+".ome.tiff").getAbsolutePath(), "-p", "-y", "2.0", "-t", "1")
						.redirectErrorStream(true)
						.redirectOutput(outputFile)
		
    
    println "Running Command: ${pb.command()}"
    def process = pb.start()
    process.waitFor()
    // Destroy explicitely
    process.destroy()
}

println "Script Done"

// imports
import qupath.fx.dialogs.FileChoosers