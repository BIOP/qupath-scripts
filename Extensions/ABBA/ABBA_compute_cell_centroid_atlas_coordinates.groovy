/**
 * Computes the centroid coordinates of each detection within the atlas
 * then adds these coordinates onto the measurement list.
 * Measurements names: "Atlas_X", "Atlas_Y", "Atlas_Z"
 */

def pixelToAtlasTransform = 
    AtlasTools
    .getAtlasToPixelTransform(getCurrentImageData())
    .inverse() // pixel to atlas = inverse of atlas to pixel

getDetectionObjects().forEach(detection -> {
    RealPoint atlasCoordinates = new RealPoint(3);
    MeasurementList ml = detection.getMeasurementList();
    atlasCoordinates.setPosition([detection.getROI().getCentroidX(),detection.getROI().getCentroidY(),0] as double[]);
    pixelToAtlasTransform.apply(atlasCoordinates, atlasCoordinates);
    ml.putMeasurement("Atlas_X", atlasCoordinates.getDoublePosition(0) )
    ml.putMeasurement("Atlas_Y", atlasCoordinates.getDoublePosition(1) )
    ml.putMeasurement("Atlas_Z", atlasCoordinates.getDoublePosition(2) )
})

import qupath.ext.biop.warpy.Warpy
import net.imglib2.RealPoint
import qupath.lib.measurements.MeasurementList
import qupath.ext.biop.abba.AtlasTools

import static qupath.lib.gui.scripting.QPEx.* // For intellij editor autocompletion