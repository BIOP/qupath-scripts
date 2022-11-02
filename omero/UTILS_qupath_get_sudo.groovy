import qupath.ext.biop.servers.omero.raw.*
import fr.igred.omero.*

/*
 * = DEPENDENCIES =
 *  - qupath-extension-biop-omero :https://github.com/BIOP/qupath-extension-biop-omero/releases/download/v0.1.2/qupath-extension-biop-omero-0.1.2.jar
 *  - simple-omero-client : https://github.com/GReD-Clermont/simple-omero-client/releases/download/v5.9.1/simple-omero-client-5.9.1.jar
 *
 * = REQUIREMENTS =
 *  - A project must be open in QuPath
 *  - The connection to omero-server.epfl.ch needs to be established (with credentials) before running the script
*/


// get a simple-omero-client instance already connected to the current session (no credentials needed)
Client user_client = OmeroRawTools.getSimpleOmeroClientInstance()
if (user_client.isConnected()){
	println "Connected to OMERO \n"
}

// get sudo user
Client sudo = user_client.sudoGetUser("username")