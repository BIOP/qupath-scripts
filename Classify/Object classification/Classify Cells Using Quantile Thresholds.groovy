def ecad_channel = 2
def ki67_channel = 3

def multi_measurements = [ new SingleThresholder( measurement: "CFP: Cytoplasm: Mean",
                                                  pathClass: "ECad",
                                                  quantile: 0.20,
                                                  multiplier: 3 
                                                ),
                           new SingleThresholder( measurement: "FITC: Cytoplasm: Mean",
                                                  pathClass: "GFP",
                                                  quantile: 0.20,
                                                  multiplier: 3 
                                                ),
                           new SingleThresholder( measurement: "RFP: Nucleus: Mean",
                                                  pathClass: "Ki67",
                                                  quantile: 0.20,
                                                  multiplier: 3 
                                                ),
                           new SingleThresholder( measurement: "CY5: Nucleus: Mean",
                                                  pathClass: "PR",
                                                  quantile: 0.20,
                                                  multiplier: 3 
                                                )
                         ]


//Start of Scripts 

// Clean cell classifications
resetDetectionClassifications()

def cells = getCellObjects()

multi_measurements.each{ m ->

// Get the threshold to use for this measurement
def threshold = quantileBasedThr( cells, m.measurement, m.quantile, m.multiplier)


    
    cells.each{ cell ->
        def posClass = PathClassFactory.getDerivedPathClass( cell.getPathClass(),  m.pathClass+"+", null )
        def negClass = PathClassFactory.getDerivedPathClass( cell.getPathClass(),  m.pathClass+"-", null )
        cell.getMeasurementList().getMeasurementValue( m.measurement ) >= threshold ? cell.setPathClass( posClass ) : cell.setPathClass( negClass )
    }
}

fireHierarchyUpdate()


class SingleThresholder {

    def measurement = ""
    def pathClass = PathClassFactory.getPathClassUnclassified()
    def quantile = 0.1 // Default is 10%
    def multiplier = 3 // Default is 3x
}


double quantileBasedThr( def cells, def measure, def quantile, def multiplier ) {

    // Sort by mean intensity in the channel of interst
    def sorted = cells.sort{ measurement( it, measure ) }
    
    def n = cells.size()
    
    // Remove 5% smallest intensities
    cells = cells.drop( ( n * 0.05 ) as int )
    
    // Keep x percent of what is left
    def quant = cells.take( ( n * quantile ) as int )
    
    // Get baseline
    def baseline = quant.sum{ measurement( it, measure ) } / quant.size()
    
    // new threshold
    threshold = multiplier * baseline
    
    println "Threshold for "+measure+" ("+ (quantile*100)+"% quantile with "+multiplier+"x multiplier):\t"+threshold
    
    return threshold
}                 

