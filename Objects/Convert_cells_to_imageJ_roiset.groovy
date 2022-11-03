/* 
 * Import QuPath Cell objects as ImageJ Rois with the possibility
 * to create a 'cytoplasm' roi for each cell
 * These are added to the Roi manager
 * @author Olivier Burri
 * @date 20221103
 * Last tested on QuPath-0.3.2
 */
 
 // Create cytoplasm Roi and add it to the Roi Manager?
def makeCytoplasmRoi = true


// START OF SCRIPT

// Use ImageJ's RoiManager
def rm = RoiManager.getInstance() ?: new RoiManager()
rm.reset()

// Pick up active image name
def name = getProjectEntry().getImageName()
name = GeneralTools.stripInvalidFilenameChars( name )

// Prepare a folder inside the QuPath Project called 'Cells as IJ ROIs'
def saveFolder = new File( getProject().getPath().toFile().getParent(), "Cells as IJ Rois" )
saveFolder.mkdirs()

// Get the cells and iterate through them
def cells = getCellObjects()
if ( !cells.isEmpty() ){
    cells.eachWithIndex{ cell, idx ->
    
        // Get the ROIs, convert them to ImageJ at full resolution, assuming that the 
        // top left corner has coordinates 0,0 (will not work for crops)
        // That way we avoid having to request an ImagePlus, and only use ROIs and Rois
        def nucleusROI = cell.getNucleusROI()
        def cellROI  = cell.getROI()
    
        def ijNucleursRoi  = IJTools.convertToIJRoi( nucleusROI, 0,0, 1 )
        def ijCellRoi = IJTools.convertToIJRoi( cellROI, 0,0, 1 )
        
        // Name and add to the RoiManager
        ijNucleursRoi.setName( "Nucleus " + idx )
        ijCellRoi.setName( "Cell " + idx )
        
        rm.addRoi( ijNucleursRoi )
        rm.addRoi( ijCellRoi )
        
        // Create a Cytoplasm Roi if requested
        if( makeCytoplasmRoi ) {
        
            // Use locationtech's Geometry functions
            def cytoplasmGeometry = cellROI.getGeometry().difference( nucleusROI.getGeometry() )
            
            def cytoplasmROI = GeometryTools.geometryToROI( cytoplasmGeometry, cellROI.getImagePlane() )
            def ijCytoplasmRoi = IJTools.convertToIJRoi( cytoplasmROI, 0,0, 1 )
            
            ijCytoplasmRoi.setName( "Cytoplasm " + idx )
            rm.addRoi( ijCytoplasmRoi )
        }
    }

// It's not cells, it's regular detections
} else{
    def detections = getDetectionObjects()
    detections.eachWithIndex{ det, idx ->
     
     def detectionROI  = det.getROI()
     
     def ijRoi = IJTools.convertToIJRoi( detectionROI, 0,0, 1 )
     ijRoi.setName( "Roi_" + idx )
     rm.addRoi( ijRoi )
    }
}

// Save everything in the RoiManager to a zip file
def roisetFile =  new File( saveFolder, name + "_Rois.zip" )
rm.runCommand( "Save", roisetFile.getAbsolutePath() );

println "ImageJ Roiset saved under\n${roisetFile}"

// Imports
import ij.plugin.frame.RoiManager