/* 
 * Last tested on QuPath-0.3.2
 */
 
 // Create cytoplasm ROI before adding it to the Roi Manager?
def getcytoplasm = true


// START OF SCRIPT

// Use ImageJ's RoiManager
def rm = RoiManager.getInstance() == null ? new RoiManager() : RoiManager.getInstance()
rm.reset()

// Pick up active image name
def name = getProjectEntry().getImageName()

// Prepare a folder inside the QuPath Project called 'ROIs'
def saveFolder = new File( getProject().getPath().toFile().getParent(), "ROIs" )
saveFolder.mkdirs()


// Get the cells and iterate through them
def cells = getCellObjects()
if ( !cells.isEmpty() ){
    cells.eachWithIndex{ cell, idx ->
    
        // Get the ROIs, convert them to ImageJ at full resolution, assuming that the 
        // top left corner has coordinates 0,0 ( will not work for crops)
        def nuc = cell.getNucleusROI()
        def ce  = cell.getROI()
    
        def ijnuc  = IJTools.convertToIJRoi( nuc, 0,0, 1 )
        def ijcell = IJTools.convertToIJRoi( ce, 0,0, 1 )
        
        // Name and add to the RoiManager
        ijnuc.setName( "Nucleus " + idx )
        ijcell.setName( "Cell " + idx )
        
        rm.addRoi(ijnuc)
        rm.addRoi(ijcell)
        
        // Create a Cytoplasm ROI if requested
        if( getcytoplasm ) {
            def cytoGeom = ce.getGeometry().difference( nuc.getGeometry() )
            
            def cyto = GeometryTools.geometryToROI( cytoGeom, cell.getROI().getImagePlane() )
            def ijcyto = IJTools.convertToIJRoi( cyto, 0,0, 1 )
            
            ijcyto.setName( "Cytoplasm " + idx )
            rm.addRoi(ijcyto)
        }
    }
    
} else{
    def detections = getDetectionObjects()
    detections.eachWithIndex{ det, idx ->
     def det_roi  = det.getROI()
     
     def ij_roi = IJTools.convertToIJRoi(det_roi, 0,0, 1 )
     ij_roi.setName( "Roi_" + idx )
     rm.addRoi(ij_roi)
    }
}


// Save everything in the RoiManager to a zip file
rm.runCommand( "Save", new File( saveFolder, name + "_Rois.zip" ).getAbsolutePath() );

println "Done"

// Imports
import ij.plugin.frame.RoiManager