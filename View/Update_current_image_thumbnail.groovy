// Saved changes
getProject().syncChanges();

// get the current image data
def newImageData = getCurrentViewer().getImageDisplay().getImageData();

// generate thumbnail
def thumbnail = getCurrentViewer().getRGBThumbnail();

Platform.runLater {
    // get and save the new thumbnail
    def entry = getProject().getEntry(newImageData);
    entry.setThumbnail(thumbnail);
    entry.saveImageData(newImageData);
}

// save changes
getProject().syncChanges();