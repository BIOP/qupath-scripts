/*
 * Create a series of new measurements for a QuPath pathObject
 * NOTE that measurements can only be numeric
 * 
 * @author Olivier Burri
 * @date 2022.11.03
 * Last tested on QuPath-0.6.0
 */
 
def objects = getAnnotationObjects()
 
// Example: Set an ID to each Annotation, unique per annotation and per annotation classification
// Get classes of each object and make a counter for each as a Map<PathClass, Integer>
def counters = new HashMap<PathClass, Integer>()
objects.collect{ it.getPathClass() }.unique().each{ counters.put( it, 0 ) }

objects.each{ o ->
    // Get the counter value
    id = counters.get( o.getPathClass() )
    //Set the ID measurement
    o.getMeasurementList().put( "ID", id )
 
    //Increment the counter
    counters.replace( o.getPathClass(), id + 1 ) 
}