package edu.plag.repository

import edu.plag.entity.File
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface FileRepository : JpaRepository<File, String>
