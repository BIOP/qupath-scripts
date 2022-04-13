def qp_dir = new File (System.getProperty("user.dir"))

def folder = Dialogs.promptForDirectory( qp_dir )

def convertedFolder = new File( folder.getParent(), "converted")
convertedFolder.mkdirs()

def fileList = folder.listFiles().findAll{ it.getName().endsWith("nd2") }


fileList.each{ file ->
    println file
		
    def qp_console = new File ( qp_dir, "QuPath-0.3.2 (console).exe" )
    
    def cmd = qp_console.toString()
    
    def outputFile = new File (folder, file.getName() + "_info.txt")
		
    def pb = new ProcessBuilder(cmd, "convert-ome", file.getAbsolutePath(), new File( convertedFolder, file.getName()+".ome.tiff").getAbsolutePath(), "-p", "-t", "1")
						.redirectErrorStream(true)
						.redirectOutput(outputFile)
		
    println pb.command()
    pb.start().waitFor()
}

