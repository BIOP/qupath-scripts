import qupath.lib.gui.scripting.QPEx

// saved changes
QPEx.getQuPath().getProject().syncChanges();

// get the current image data
def newImageData = QPEx.getQuPath().getViewer().getImageDisplay().getImageData();

// generate thumbnail
def thumbnail = QPEx.getQuPath().getViewer().getRGBThumbnail();

// get and save the new thumbnail
def entry = QPEx.getQuPath().getProject().getEntry(newImageData);
entry.setThumbnail(thumbnail);
entry.saveImageData(newImageData);

// save changes
QPEx.getQuPath().getProject().syncChanges();