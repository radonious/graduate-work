package edu.plag.service

import edu.plag.entity.Project
import edu.plag.repository.ProjectRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProjectService(
    private val projectRepository: ProjectRepository
) {

    fun getProjectsByUser(userId: Long): List<Project> = projectRepository.findByUserId(userId)

    fun getProjectById(id: Long): Project? = projectRepository.findById(id).orElse(null)

    @Transactional
    fun createProject(project: Project): Project = projectRepository.save(project)

    @Transactional
    fun deleteProject(id: Long) = projectRepository.deleteById(id)
}