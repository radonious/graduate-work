package edu.plag.service

import edu.plag.entity.File
import edu.plag.repository.FileRepository
import edu.plag.util.FileUtils
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.isDirectory

@Service
class FileService(
    private val fileRepository: FileRepository
) {

    fun getAllFiles(): List<Path> {
        return Files.walk(FileUtils.getUploadPath())
            .filter { !it.isDirectory() }
            .toList()
    }

    fun getFilesByProject(projectId: Long): List<File> = fileRepository.findByProjectId(projectId)

    fun getFileByPath(path: String): File? = fileRepository.findByPath(path)

    @Transactional
    fun saveFile(file: File): File = fileRepository.save(file)

    @Transactional
    fun deleteFile(id: Long) = fileRepository.deleteById(id)
}