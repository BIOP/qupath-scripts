/**
 * This small script to create an ICE Gateway object from
 * the current OMERO connection
 * 
 * 1. Import an image from OMERO to QuPath using the qupath-extension-omero 
 *    (i.e. Extension -> OMERO -> Browse server ->...)
 * 2. Open it (double-click on the image)
 * 3. Run the script
 * 
 * @author: Rémy Dornier
 * @date: 2025.06.30
 * Last tested in QuPath-0.6.0
 */



def host = "omero-server-poc.epfl.ch"
def port = 4064

def sessionId = getCurrentServer().getClient().getApisHandler().getSessionUuid().get()

def gateway = new Gateway(new IceLogger())
def cred = new LoginCredentials(sessionId, sessionId, host, port)
def connectedUser = gateway.connect(cred);
def ctx = null

if(gateway.isConnected()) {
   try{
       println "I'm connected"
       ctx = new SecurityContext(connectedUser.getGroupId())
       ctx.setExperimenter(connectedUser);
       ctx.setServerInformation(cred.getServer());
       
   }finally {
       gateway.disconnect()
       println "I'm disconnected"
       if(ctx != null) {
           ctx = new SecurityContext(-1)
           ctx.setExperimenter(new ExperimenterData());
       }
   }
}


import qupath.ext.omero.core.pixelapis.ice.IceLogger
import omero.gateway.Gateway;
import omero.gateway.LoginCredentials;
import omero.gateway.SecurityContext;
import omero.gateway.model.ExperimenterData;