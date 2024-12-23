package com.virtualrealm.our.gameMarketPlaces.service.impl

import com.jcraft.jsch.*
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.*

@Service
class SftpService {
    private fun getChannelSftp(server: String, port: Int, username: String, password: String): ChannelSftp? {
        return try {
            val jsch = JSch()
            val session = jsch.getSession(username, server, port)

            // Configure strict host key checking
            val config = Properties()
            config["StrictHostKeyChecking"] = "no"
            session.setConfig(config)

            session.setPassword(password)
            session.connect(30000)

            val channel = session.openChannel("sftp")
            channel.connect()
            channel as ChannelSftp
        } catch (e: Exception) {
            e.printStackTrace()
            println("SFTP Connection Error: ${e.message}")
            null
        }
    }

    fun testLogin(server: String, port: Int, username: String, password: String): Boolean {
        var channelSftp: ChannelSftp? = null
        return try {
            channelSftp = getChannelSftp(server, port, username, password)
            channelSftp != null
        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            try {
                channelSftp?.disconnect()
                channelSftp?.session?.disconnect()
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    fun listFilesInDirectory(server: String, port: Int, username: String, password: String, directory: String): List<String> {
        var channelSftp: ChannelSftp? = null
        val fileList = mutableListOf<String>()

        try {
            channelSftp = getChannelSftp(server, port, username, password)
            if (channelSftp != null) {
                println("Login successful")
                try {
                    channelSftp.cd(directory)
                    println("Changed working directory to: $directory")
                } catch (e: SftpException) {
                    println("Error changing directory: ${e.message}")
                    return fileList
                }

                val entries = channelSftp.ls(directory) as Vector<ChannelSftp.LsEntry>
                for (entry in entries) {
                    if (!entry.filename.startsWith(".")) {
                        fileList.add(entry.filename)
                    }
                }

                if (fileList.isNotEmpty()) {
                    println("Files found: ${fileList.joinToString(", ")}")
                } else {
                    println("No files found in the directory")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            println("SFTP Error: ${e.message}")
        } finally {
            try {
                channelSftp?.disconnect()
                channelSftp?.session?.disconnect()
                println("Disconnected from SFTP server.")
            } catch (ex: Exception) {
                ex.printStackTrace()
                println("Error closing SFTP connection: ${ex.message}")
            }
        }
        return fileList
    }


    fun uploadFileToSftp(
        server: String,
        port: Int,
        username: String,
        password: String,
        file: MultipartFile,
        remoteFilePath: String
    ): Boolean {
        var channelSftp: ChannelSftp? = null
        println("=== Start SFTP Upload Process ===")

        return try {
            channelSftp = getChannelSftp(server, port, username, password)
            if (channelSftp == null) {
                println("Failed to establish SFTP connection")
                return false
            }
            println("Connected to SFTP server successfully!")

            // Set target directory
            val targetDirectory = "domains/virtual-realm.my.id/public_html/uploads/images"
            try {
                channelSftp.cd(targetDirectory)
            } catch (e: SftpException) {
                println("Target directory $targetDirectory does not exist.")
                return false
            }
            println("Changed working directory to: $targetDirectory")

            // Get filename from remoteFilePath
            val fileName = remoteFilePath.substringAfterLast('/')
            println("Preparing to upload file: $fileName")

            // Upload file with retry mechanism
            var attempts = 0
            var result = false
            while (attempts < 4 && !result) {
                attempts++
                println("Upload attempt $attempts of 4")

                try {
                    val inputStream = ByteArrayInputStream(file.bytes)
                    channelSftp.put(inputStream, fileName)
                    inputStream.close()
                    result = true
                    println("File uploaded successfully!")
                } catch (e: Exception) {
                    println("Upload attempt $attempts failed: ${e.message}")
                    if (attempts < 4) {
                        println("Retrying in 1 second...")
                        Thread.sleep(1000)
                    }
                }
            }
            result

        } catch (e: Exception) {
            e.printStackTrace()
            println("SFTP Error: ${e.message}")
            false
        } finally {
            try {
                channelSftp?.disconnect()
                channelSftp?.session?.disconnect()
                println("SFTP connection closed.")
            } catch (ex: Exception) {
                ex.printStackTrace()
                println("Error closing SFTP connection: ${ex.message}")
            }
        }
    }

    fun listDirectoriesInDirectory(server: String, port: Int, username: String, password: String, directory: String): List<String> {
        var channelSftp: ChannelSftp? = null
        val directoryList = mutableListOf<String>()

        try {
            channelSftp = getChannelSftp(server, port, username, password)
            if (channelSftp != null) {
                println("Login successful")
                channelSftp.cd(directory)
                println("Changed working directory to: $directory")

                val entries = channelSftp.ls(directory) as Vector<ChannelSftp.LsEntry>
                for (entry in entries) {
                    if (entry.attrs.isDir && !entry.filename.startsWith(".")) {
                        directoryList.add(entry.filename)
                    }
                }

                if (directoryList.isNotEmpty()) {
                    println("Directories found: ${directoryList.joinToString(", ")}")
                } else {
                    println("No directories found in the directory")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            println("SFTP Error: ${e.message}")
        } finally {
            try {
                channelSftp?.disconnect()
                channelSftp?.session?.disconnect()
                println("Disconnected from SFTP server.")
            } catch (ex: Exception) {
                ex.printStackTrace()
                println("Error closing SFTP connection: ${ex.message}")
            }
        }
        return directoryList
    }
}