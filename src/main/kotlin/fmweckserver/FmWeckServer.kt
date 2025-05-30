package fmweckserver

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.IOException

class FmWeckServer {
    lateinit var process : Process

    private val log: Logger = LogManager.getLogger(FmWeckServer::class.java)

    // TODO: implement proper shut-down
    fun startFmWeckServer(port: Int) {
        val processBuilder = ProcessBuilder(
            "fm-weck",
            "server",
            "--port",
            port.toString(),
            "--listen",
            "localhost"
        )
        processBuilder.redirectErrorStream(true)
        try {
            process = processBuilder.start()
            log.info("fm-weck server started on port $port")
            // TODO: redirect server output to IDE output-log
        } catch (e: IOException) {
            log.error("Failed to start fm-weck server", e)
            TODO("proper error handling (startFmWeckServer)")
        }
    }
}