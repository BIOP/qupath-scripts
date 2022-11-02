
setImageType('FLUORESCENCE')

// You can replace the names with your stainings if you want

def mins = [ 0, 0, 0, 0 ]
def maxs = [ 8000, 7500, 6000, 820 ]
def names = ['DAPI', 'FITC', 'CY3', 'CY5']

def color1 = getColorRGB( 0, 128, 255 )
def color2 = getColorRGB( 0, 255, 128 )
def color3 = getColorRGB( 128, 255, 0 )
def color4= getColorRGB( 255, 0, 128 )


def colors = [ color1, color2, color3, color4 ]

getCurrentImageData().removeProperty("qupath.lib.display.ImageDisplay"); // necessary ? absent in other version

setChannelNames( *names )

setChannelColors( *colors )

[mins, maxs].transpose().eachWithIndex{ mima, i -> setChannelDisplayRange(i, mima[0], mima[1]) }