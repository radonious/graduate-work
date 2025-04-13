package edu.plag.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "files")
data class File(
    @Id
    @Column(name = "file_hash", length = 64)
    val hash: String,

    @Column(name = "file_name", nullable = false)
    val name: String
)
