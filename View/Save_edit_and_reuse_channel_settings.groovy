/*
 * How to save and reuse channel settings, based on a discussion on the Image.sc forum 
 * https://forum.image.sc/t/setting-channel-colors-ranges-and-visibility/74273
 * 
 * This code requires the QuPath Extension BIOP 
 * https://github.com/BIOP/qupath-extension-biop
 *
 * @author Olivier Burri
 * Date: 2022.12.21
 */
 
import qupath.ext.biop.utils.Channels
import qupath.lib.gui.dialogs.Dialogs;

// Pick up the current channel settings
def currentSettings = Channels.getCurrentChannelSettings()

// These settings can be saved to a simple text file
def currentSettingsFile = new File(buildFilePath(PROJECT_BASE_DIR, 'channel_settings_previous.txt'))
Channels.writeChannelSettings(currentSettings, currentSettingsFile)

// Confirm we can read the old file
oldSettings = Channels.readChannelSettings(currentSettingsFile)

println "Reloaded file and current settings match? ${currentSettings == oldSettings}"

// Load new settings
//def newSettingsFile = new File(buildFilePath(PROJECT_BASE_DIR, 'other_channe_settings.txt'))
//def newSettings = Channels.readChannelSettings( newSettingsFile )

// Or write them yourself.
// Note that this will give out a warning because the first line is empty
def manualSettingsString = '''
1, First, #0300ff, 0, 100
3, Second, #ffff00, 0, 200
4, Sample, green, 10, 300
5, Other, #ff00ff, 0, 123
'''
def manualSettings = Channels.readChannelSettings(manualSettingsString)

// Apply new settings
Channels.setChannelSettings(manualSettings)

Dialogs.showConfirmDialog("Reverting", "Reverting to Original Channels");

// Reload original settings
Channels.setChannelSettings(currentSettings)