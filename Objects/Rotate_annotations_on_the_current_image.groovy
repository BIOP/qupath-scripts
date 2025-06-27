import org.locationtech.jts.geom.util.AffineTransformation


/*
 * This script rotates all annotations of the current open image by 180° 
 * around the image center.
 * 
 * @author Remy Dornier (from https://forum.image.sc/t/qupath-rotate-multiple-annotations/31547/4)
 * @date 2023.04.06
 * Last tested on QuPath-0.6.0
 */ 

def rotation_angle = 180 // in degree

// get the current image
def server = getCurrentServer()

// create a 180° rotation around image center
def affine = AffineTransformation.rotationInstance(rotation_angle * Math.PI / 180, server.getWidth()/2, server.getHeight()/2)
def transform = GeometryTools.convertTransform(affine)

// get current annotations
def hierarchy = getCurrentHierarchy()

//rotate all annotations
def newRoot = PathObjectTools.transformObjectRecursive(hierarchy.getRootObject(), transform, true)

// clear previous un-rotated annotations
removeAllObjects()

// add new rotated annotations
addObjects(newRoot.getChildObjects() as List)

// update
fireHierarchyUpdate()