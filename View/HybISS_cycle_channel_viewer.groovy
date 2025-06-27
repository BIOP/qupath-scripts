
/*
 * Hybiss channel viewer
 *
 * @author Olivier Burri
 * Date: 2022.12.21
 * Last tested on QuPath-0.6.0
 */

def channels = getCurrentViewer().getImageDisplay().availableChannels()

// The channel names are structured as Fluorophore_Cycle#, ex. "TRITC_Cycle1 (C3)"
// Split by the underscore and then by the space to remove the channel number, ie(" (C3)")
def cycles = channels.collect{it.getName().split("_")[1].split(" ")[0]}.toUnique().sort()

// The DAPI Channel is ever present
def dapiChannel = channels.find{ it.getName().contains("DAPI") }

println cycles

//Build the GUI

// Arrange the buttons in a grid type layout
def gridPane = new GridPane()
gridPane.setPadding(new Insets(10, 10, 10, 10))
gridPane.setVgap(2)
gridPane.setHgap(10)

// When we click on one cycle, we want to make sure the others become unchecked
// So we add them all to the same ToggleGroup
def cycleGroup = new ToggleGroup()

// Make a toggle button for each cycle
cycles.eachWithIndex{ cycle, idx ->  
    
    // The button with the name of the cycle
    def cycleBtn = new ToggleButton( cycle )
    
    // This ID, which is also the name, will be used when we click on the button, in order to find which channels to display
    cycleBtn.setId( cycle )
    
    // Assign it to the toggle group
    cycleBtn.setToggleGroup( cycleGroup )

    //Setting action to check boxes
    cycleBtn.selectedProperty().addListener( ( ov, oldval, newval ) -> {
        // Only trigger if it it toggled
        if( newval ) {
            def cycleID = cycleBtn.getId()
               // Build the list of channels we want to have as active
               def relevantChannels = []
                relevantChannels.add( dapiChannel )
                
                // Find all channels with the cycleID in the channel name
                relevantChannels.addAll(channels.findAll{channel -> channel.getName().contains( cycleID )})
                
                logger.info( "Channels to activate: {}", relevantChannels )
                
                // Finally this will go through all channels, activating the desired ones and deactivating the others
                channels.each{ getCurrentViewer().getImageDisplay().setChannelSelected( it, relevantChannels.contains( it ) ) }
                
                // Make sure to update, otherwise it will only show if you change the viewer (move, zoom, etc...)
                getCurrentViewer().repaintEntireImage()
        }
    })

    // Add a column to the grid pane
    gridPane.add( cycleBtn, idx+1, 1, 1,1)
}

// This builds the last step of the GUI and displays it
Platform.runLater {
    def stage = new Stage()
    stage.initOwner( QuPathGUI.getInstance().getStage() )
    stage.setScene( new Scene( gridPane ) )
    stage.setTitle( "Select Cycles" )
    stage.setResizable( false )
    stage.show()

}

import javafx.application.Platform
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.control.ToggleButton
import javafx.scene.control.ToggleGroup
import javafx.scene.layout.GridPane
import javafx.stage.Stage