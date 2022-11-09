/* 
 * Set channel names, colors and ranges for a fluorescent image entry
 * Can be run for a project to set all images to the same channel names (IMORTANT)
 * as well as the same displays and colors. 
 *
 * @author Olivier Burri
 * @date 20221103
 * Last tested on QuPath-0.3.2
 */
 
setImageType('FLUORESCENCE')

// You can replace the names with your stainings if you want

// Channel names in order
def names = ['DAPI', 'FITC', 'CY3', 'CY5']

// Channel minimum display values, in order
def mins = [ 0, 0, 0, 0 ]

// Channel maximum display values, in order
def maxs = [ 8000, 7500, 6000, 820 ]

// Channel colors: Make sure that the number of colors in this list
// matches the number of channels
def colors = [ getColorRGB( 0, 128, 255 ),
               getColorRGB( 0, 255, 128 ),
               getColorRGB( 128, 255, 0 ),
               getColorRGB( 255, 0, 128 )
             ]


// Start of script 
getCurrentImageData().removeProperty("qupath.lib.display.ImageDisplay"); // necessary ? absent in other version

setChannelNames( *names )

setChannelColors( *colors )

[mins, maxs].transpose().eachWithIndex{ mima, i -> setChannelDisplayRange(i, mima[0], mima[1]) }