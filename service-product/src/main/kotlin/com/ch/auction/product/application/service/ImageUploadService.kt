package com.ch.auction.product.application.service

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.CannedAccessControlList
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.services.s3.model.PutObjectRequest
import com.ch.auction.product.config.ImageProperties
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.IOException
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors

@Service
class ImageUploadService(
    private val amazonS3: AmazonS3,
    private val imageProperties: ImageProperties
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor()

    /**
     * 단일 파일 업로드
     */
    fun uploadFile(
        file: MultipartFile,
        folder: String = "products"
    ): String {
        val fileName = generateFileName(file.originalFilename ?: "file")
        val key = "$folder/$fileName"

        try {
            val metadata = ObjectMetadata().apply {
                contentType = file.contentType
                contentLength = file.size
            }

            val putObjectRequest = PutObjectRequest(imageProperties.bucket, key, file.inputStream, metadata)
                .withCannedAcl(CannedAccessControlList.PublicRead)

            amazonS3.putObject(putObjectRequest)
            
            val fileUrl = "${imageProperties.endpoint}/${imageProperties.bucket}/$key"
            logger.info("File uploaded successfully: $fileUrl")
            
            return fileUrl
        } catch (e: IOException) {
            logger.error("Failed to upload file", e)
            throw RuntimeException("Failed to upload file: ${e.message}")
        }
    }

    /**
     * 다중 파일 업로드
     */
    fun uploadFiles(
        files: List<MultipartFile>,
        folder: String = "products"
    ): List<String> {
        if (files.isEmpty()) {
            return emptyList()
        }

        logger.info("Starting upload of ${files.size} files using Virtual Threads")

        // 병렬 업로드
        val futures = files.map { file ->
            CompletableFuture.supplyAsync(
                { uploadFile(file = file, folder = folder) },
                virtualThreadExecutor
            )
        }

        // 완료 대기
        val uploadedUrls = CompletableFuture.allOf(*futures.toTypedArray())
            .thenApply {
                futures.map { it.join() }
            }
            .join()

        logger.info("Successfully uploaded ${uploadedUrls.size} files")

        return uploadedUrls
    }

    /**
     * 파일 삭제
     */
    fun deleteFile(
        fileUrl: String
    ): Boolean {
        return try {
            val key = fileUrl.substringAfter("${imageProperties.bucket}/")
            amazonS3.deleteObject(imageProperties.bucket, key)
            logger.info("File deleted successfully: $key")

            true
        } catch (e: Exception) {
            logger.error("Failed to delete file: $fileUrl", e)

            false
        }
    }

    /**
     * 다중 파일 삭제
     */
    fun deleteFiles(
        fileUrls: List<String>
    ): Int {
        if (fileUrls.isEmpty()) {
            return 0
        }

        logger.info("Starting deletion of ${fileUrls.size} files using Virtual Threads")

        val futures = fileUrls.map { fileUrl ->
            CompletableFuture.supplyAsync(
                { deleteFile(fileUrl = fileUrl) },
                virtualThreadExecutor
            )
        }

        val results = CompletableFuture.allOf(*futures.toTypedArray())
            .thenApply {
                futures.map { it.join() }
            }
            .join()

        val successCount = results.count { it }
        logger.info("Successfully deleted $successCount out of ${fileUrls.size} files")

        return successCount
    }

    /**
     * 고유한 파일 이름 생성
     */
    private fun generateFileName(
        originalFilename: String
    ): String {
        val extension = originalFilename.substringAfterLast(".", "")
        val uuid = UUID.randomUUID().toString()
        return if (extension.isNotEmpty()) {
            "$uuid.$extension"
        } else {
            uuid
        }
    }

    /**
     * 파일 존재 여부 확인
     */
    fun fileExists(
        fileUrl: String
    ): Boolean {
        return try {
            val key = fileUrl.substringAfter("${imageProperties.bucket}/")
            amazonS3.doesObjectExist(imageProperties.bucket, key)
        } catch (e: Exception) {
            false
        }
    }
}

