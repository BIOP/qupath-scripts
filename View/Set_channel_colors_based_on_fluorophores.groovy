def fluorophores = ["DAPI":"#334db3", "Cy5":"#ff00ff", "TRITC":"#ff000", "FITC":"#00ffff", "Cy7":"#ffff00"]

def channels = getCurrentViewer().getImageDisplay().availableChannels()

def colors = channels.collect{ channel ->
    // What fluorophore does it match?
    def fluo = fluorophores.find{ channel.getName().contains( it.getKey() ) }
    
    // Get the color
    def c = java.awt.Color.decode(fluo.getValue())
    def color = makeRGB(c.getRed(), c.getGreen(), c.getBlue())
}

setChannelColors( *colors )