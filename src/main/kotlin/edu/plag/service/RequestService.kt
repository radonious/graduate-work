package edu.plag.service

import edu.plag.entity.Request
import edu.plag.repository.RequestRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service


@Service
class RequestService(private val requestRepository: RequestRepository) {

    fun getRequestsByUser(userId: Long): List<Request> = requestRepository.findByUserId(userId)

    fun getRequestById(id: Long): Request? = requestRepository.findById(id).orElse(null)

    @Transactional
    fun createRequest(request: Request): Request = requestRepository.save(request)
}