package edu.plag.service

import edu.plag.entity.File
import edu.plag.repository.FileRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class FileService(
    private val fileRepository: FileRepository
) {

    fun getFilesByProject(projectId: Long): List<File> = fileRepository.findByProjectId(projectId)

    fun getFileByPath(path: String): File? = fileRepository.findByPath(path)

    @Transactional
    fun saveFile(file: File): File = fileRepository.save(file)

    @Transactional
    fun deleteFile(id: Long) = fileRepository.deleteById(id)
}