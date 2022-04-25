import groovyx.gpars.GParsPool
#@File(style="directory") folder

def convertedFolder = new File( folder.getParent(), "converted")
convertedFolder.mkdirs()

def fileList = folder.listFiles().findAll{ it.getName().endsWith("nd2") }


GParsPool.withPool(6) {
	fileList.eachParallel{ file ->
		println file
		
		def cmd = "C:/QuPath-0.3.2/QuPath-0.3.2 (console).exe"
		def outputFile = new File (folder, file.getName() + "_info.txt")
		
		def pb = new ProcessBuilder(cmd, "convert-ome", file.getAbsolutePath(), new File( convertedFolder, file.getName()+".ome.tiff").getAbsolutePath(), "-y", "2.0", "-p", "-t", "1")
						.redirectErrorStream(true)
						.redirectOutput(outputFile)
		
		println pb.command()
		pb.start().waitFor()
	}
}
