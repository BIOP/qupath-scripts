# PUBLIC ARCHIVE 
**This repo is now archived and its content has been integrated into the [QuPath-BIOP extension](https://github.com/BIOP/qupath-extension-biop) in order to avoid complexity with catalog installation.
This repo will no longer be maintained. Scripts are now accessible under `Extensions -> BIOP -> scripts`**

**Please consider using the QuPath-BIOP extension instead to benefit from latest scripts / fixes**

## QuPath Scripts
This repository contains Scripts we found useful when using QuPath.

They are intended for use with the latest version of QuPath and perhaps require extra dependencies. All dependencies are written inside the header of each script. 

>[!NOTE]
> The `main` branch contains only scripts that are compatible with the **latest version of QuPath** (i.e. QuPath 0.6.x). If you are using an older version of QuPath (i.e. QuPath 0.5.x), please checkout the corresponding branch

### How to use scripts
- Clone this repository on your computer
- Open QuPath, enter preferences (`Edit -> Preferences -> Scripting`) and add the path to the `qupath-scripts` repo
- Before running any script, you will need to have an image open in QuPath.
- On QuPath, go under `Automate -> Shared script`.
- Click on the script you want.
- Read the small headers `= REQUIREMENTS =` and `= TO MAKE THE SCRIPT RUN =` to know what should be configured on the image to make the script run.
- Click on `run->run`.
