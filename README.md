# QuPath Scripts
This repository contains Scripts we found useful when using QuPath.

They are intended for use with the latest version of QuPath and perhaps require extra dependencies. All dependencies are written inside the header of each script. 


## OMERO scripts
Under `QuPath` folder, you will find scripts to get an instance of OMERO and communicate with it.

### Dependencies and installation
To make scripts work, you will need three dependencies : 
- [simple-omero-client](https://github.com/GReD-Clermont/simple-omero-client)
- [qupath-extension-biop-omero](https://github.com/BIOP/qupath-extension-biop-omero)
- [OMERO.java](https://www.openmicroscopy.org/omero/downloads/)

For `simple-omero-client`, download the latest version of [simple-omero-client-[version].jar](https://github.com/GReD-Clermont/simple-omero-client/releases) and copy it in the folder `C:\QuPath_Common_Data_0.3\extensions`

For the last two dependencies, look at the readme of [qupath-extension-biop-omero](https://github.com/BIOP/qupath-extension-biop-omero) to know how to install.

### How to use scripts
- All the necessary methods to communicate with OMERO from QuPath are available under the static class `OmeroRawScripting`.
- Before running any script, you will need to have an image, coming from OMERO, open in QuPath.
- On QuPath, go under `Automate -> Shared script -> OMERO`.
- Click on the script you want.
- Read the small headers `= REQUIREMENTS =` and `= TO MAKE THE SCRIPT RUN =` to know what should be configured on the image to make the script run.
- Click on `run->run`.
