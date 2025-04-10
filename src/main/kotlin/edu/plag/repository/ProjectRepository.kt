package edu.plag.repository

import edu.plag.entity.Project
import org.springframework.data.jpa.repository.JpaRepository

interface ProjectRepository : JpaRepository<Project, Long> {
    fun findByUserId(userId: Long): List<Project>
}