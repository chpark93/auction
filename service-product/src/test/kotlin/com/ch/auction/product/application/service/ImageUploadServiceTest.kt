package com.ch.auction.product.application.service

import com.ch.auction.product.config.ImageProperties
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockMultipartFile
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import software.amazon.awssdk.services.s3.model.HeadObjectRequest
import software.amazon.awssdk.services.s3.model.NoSuchKeyException
import software.amazon.awssdk.services.s3.model.PutObjectRequest

@DisplayName("Image Upload Service 테스트")
class ImageUploadServiceTest {

    private lateinit var imageUploadService: ImageUploadService
    private lateinit var s3Client: S3Client
    private lateinit var imageProperties: ImageProperties

    @BeforeEach
    fun setUp() {
        s3Client = mockk(relaxed = true)
        imageProperties = ImageProperties(
            endpoint = "http://localhost:9000",
            accessKey = "admin",
            secretKey = "password",
            bucket = "auction-bucket",
            region = "us-east-1"
        )
        
        imageUploadService = ImageUploadService(
            s3Client = s3Client,
            imageProperties = imageProperties
        )
    }

    @Test
    @DisplayName("단일 파일 업로드 - 성공")
    fun upload_single_file_success() {
        // Given
        val file = MockMultipartFile(
            "image",
            "test.jpg",
            "image/jpeg",
            "test content".toByteArray()
        )

        every { 
            s3Client.putObject(any<PutObjectRequest>(), any<RequestBody>()) 
        } returns mockk()

        // When
        val result = imageUploadService.uploadFile(file, "products")

        // Then
        assertNotNull(result)
        assertTrue(result.contains("auction-bucket"))
        assertTrue(result.contains("products/"))
        assertTrue(result.endsWith(".jpg"))
        
        verify { s3Client.putObject(any<PutObjectRequest>(), any<RequestBody>()) }
    }

    @Test
    @DisplayName("다중 파일 업로드 - 성공")
    fun upload_multiple_files_success() {
        // Given
        val files = listOf(
            MockMultipartFile("image1", "test1.jpg", "image/jpeg", "test content 1".toByteArray()),
            MockMultipartFile("image2", "test2.jpg", "image/jpeg", "test content 2".toByteArray()),
            MockMultipartFile("image3", "test3.jpg", "image/jpeg", "test content 3".toByteArray())
        )

        every { 
            s3Client.putObject(any<PutObjectRequest>(), any<RequestBody>()) 
        } returns mockk()

        // When
        val results = imageUploadService.uploadFiles(files, "products")

        // Then
        assertNotNull(results)
        assertEquals(3, results.size)
        results.forEach { url ->
            assertTrue(url.contains("auction-bucket"))
            assertTrue(url.contains("products/"))
        }
        
        verify(atLeast = 3) { s3Client.putObject(any<PutObjectRequest>(), any<RequestBody>()) }
    }

    @Test
    @DisplayName("빈 파일 목록 업로드 - 빈 리스트 반환")
    fun return_empty_list_when_upload_empty_files() {
        // Given
        val files = emptyList<MockMultipartFile>()

        // When
        val results = imageUploadService.uploadFiles(files, "products")

        // Then
        assertNotNull(results)
        assertTrue(results.isEmpty())
        
        verify(exactly = 0) { s3Client.putObject(any<PutObjectRequest>(), any<RequestBody>()) }
    }

    @Test
    @DisplayName("파일 삭제 - 성공")
    fun delete_file_success() {
        // Given
        val fileUrl = "http://localhost:9000/auction-bucket/products/test.jpg"

        every { 
            s3Client.deleteObject(any<DeleteObjectRequest>()) 
        } returns mockk()

        // When
        val result = imageUploadService.deleteFile(fileUrl)

        // Then
        assertTrue(result)
        verify { s3Client.deleteObject(any<DeleteObjectRequest>()) }
    }

    @Test
    @DisplayName("파일 삭제 - 실패")
    fun return_false_when_file_delete_fail() {
        // Given
        val fileUrl = "http://localhost:9000/auction-bucket/products/test.jpg"

        every { 
            s3Client.deleteObject(any<DeleteObjectRequest>()) 
        } throws RuntimeException("S3 Error")

        // When
        val result = imageUploadService.deleteFile(fileUrl)

        // Then
        assertFalse(result)
        verify { s3Client.deleteObject(any<DeleteObjectRequest>()) }
    }

    @Test
    @DisplayName("다중 파일 삭제 - 성공")
    fun delete_multiple_files_success() {
        // Given
        val fileUrls = listOf(
            "http://localhost:9000/auction-bucket/products/test1.jpg",
            "http://localhost:9000/auction-bucket/products/test2.jpg",
            "http://localhost:9000/auction-bucket/products/test3.jpg"
        )

        every { 
            s3Client.deleteObject(any<DeleteObjectRequest>()) 
        } returns mockk()

        // When
        val result = imageUploadService.deleteFiles(fileUrls)

        // Then
        assertEquals(3, result)
        verify(atLeast = 3) { s3Client.deleteObject(any<DeleteObjectRequest>()) }
    }

    @Test
    @DisplayName("파일 존재 여부 확인 - 존재함")
    fun return_true_when_file_exist() {
        // Given
        val fileUrl = "http://localhost:9000/auction-bucket/products/test.jpg"

        every { 
            s3Client.headObject(any<HeadObjectRequest>()) 
        } returns mockk()

        // When
        val result = imageUploadService.fileExists(fileUrl)

        // Then
        assertTrue(result)
        verify { s3Client.headObject(any<HeadObjectRequest>()) }
    }

    @Test
    @DisplayName("파일 존재 여부 확인 - 존재하지 않음")
    fun return_false_when_file_not_exist() {
        // Given
        val fileUrl = "http://localhost:9000/auction-bucket/products/test.jpg"

        every { 
            s3Client.headObject(any<HeadObjectRequest>()) 
        } throws NoSuchKeyException.builder().build()

        // When
        val result = imageUploadService.fileExists(fileUrl)

        // Then
        assertFalse(result)
        verify { s3Client.headObject(any<HeadObjectRequest>()) }
    }

    @Test
    @DisplayName("파일명 생성 - UUID 포함")
    fun generate_filename() {
        // Given
        val file1 = MockMultipartFile("image", "test.jpg", "image/jpeg", "content".toByteArray())
        val file2 = MockMultipartFile("image", "test.jpg", "image/jpeg", "content".toByteArray())

        every { 
            s3Client.putObject(any<PutObjectRequest>(), any<RequestBody>()) 
        } returns mockk()

        // When
        val url1 = imageUploadService.uploadFile(file1, "products")
        val url2 = imageUploadService.uploadFile(file2, "products")

        // Then
        assertNotEquals(url1, url2)
    }
}
