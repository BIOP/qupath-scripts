
// Choose the objects to work from
def objects = getDetectionObjects() // or getAnnotationObjects()

// If something is selected, only run on the selected ones
def test = getSelectedObjects().size() > 0
if( test ) {
    // work only on selected cells
    objects = getSelectedObjects()
}

// Define coloc parameters
def coloc = new JACoP_B()

// JACoP B' settings are internal and a bit messy, but you need to enter all these in order to get measurements.
// Not all parameters are currently here, suh as randomization adn fluorogram settings

// Channels are 1-based
coloc.channelA = 2  // Channel A
coloc.channelB = 3  // Channel B
coloc.thrA = "Use Manual Threshold Below"  // Threshold for Channel A
coloc.thrB = "Use Manual Threshold Below"  // Threshold for Channel B
coloc.mThrA = 9500  // Manual Threshold A
coloc.mThrB = 4000  // Manual Threshold B 

coloc.doCropRois=true  // Crop ROIs
coloc.doSeparateZ=false // Consider Z Slices Separately
coloc.is_stack_hist_z = true  // Set Auto Thresholds On Stack Histogram
coloc.doCostesThr=true
coloc.doPearsons=true // Get Pearsons Correlation
coloc.doManders=true  // Get Manders Coefficients
coloc.doOverlap=true  // Get Overlap Coefficients

coloc.doFluorogram = true // Get Fluorogram


// Send to ImageJ (without the ROIs)

def request = RegionRequest.createInstance( getCurrentServer() )

def image = IJTools.convertToImagePlus( getCurrentServer(), request )



objects.each{ object ->
    setSelectedObject( object ) // For display purposes, in case of errors helps to see what object we were processing
    
    def roi = IJTools.convertToIJRoi( object.getROI(), image )
    coloc.imp = image.getImage()
    coloc.imp.setRoi( roi )
    coloc.roi = roi
   
    coloc.runColoc()

    // Get the results back
    def results = ResultsTable.getResultsTable()
    
    // Grab the last row and add it to the object's measurements
    def headers = results.getHeadings()
    def lastResult = results.size() - 1
    
    // Ignore the columns we do not need
    headers = headers - ["Image A", "Image B", "ROI"]
    
    headers.each {
       object.measurements[ it ] = results.getValue( it, lastResult ) 
    }

    if( !test ) IJ.run( "Close All", "" )
}

// Necessary importsd
import ch.epfl.biop.coloc.JACoP_B
import ij.measure.ResultsTable
import ij.IJ