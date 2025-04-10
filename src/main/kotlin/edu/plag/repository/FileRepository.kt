package edu.plag.repository

import edu.plag.entity.File
import org.springframework.data.jpa.repository.JpaRepository

interface FileRepository : JpaRepository<File, Long> {
    fun findByProjectId(projectId: Long): List<File>
    fun findByPath(path: String): File?
}