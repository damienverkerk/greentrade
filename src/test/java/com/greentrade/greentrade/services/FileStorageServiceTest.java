package com.greentrade.greentrade.services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.greentrade.greentrade.config.FileStorageConfig;
import com.greentrade.greentrade.config.FileValidationConfig;
import com.greentrade.greentrade.exception.file.FileStorageException;
import com.greentrade.greentrade.exception.file.InvalidFileException;

class FileStorageServiceTest {
   
   @TempDir
   Path tempDir;
   
   private FileStorageService fileStorageService;
   private FileStorageConfig fileStorageConfig;
   private FileValidationConfig fileValidationConfig;
   
   @BeforeEach
   void setUp() {
       // Arrange
       fileStorageConfig = new FileStorageConfig();
       fileStorageConfig.setUploadDir(tempDir.toString());
       
       fileValidationConfig = new FileValidationConfig();
       fileValidationConfig.setMaxFileSize(10 * 1024 * 1024); // 10MB
       fileValidationConfig.setAllowedExtensions(java.util.Arrays.asList("pdf", "jpg", "jpeg", "png"));
       
       fileStorageService = new FileStorageService(fileStorageConfig, fileValidationConfig);
   }
   
   @Test
   void storeFile_WithValidFile_StoresSuccessfully() throws IOException {
       // Arrange
       String content = "test content";
       MockMultipartFile file = new MockMultipartFile(
           "test.pdf",
           "test.pdf",
           "application/pdf",
           content.getBytes()
       );
       
       // Act
       String storedFileName = fileStorageService.storeFile(file);
       
       // Assert
       assertNotNull(storedFileName);
       assertTrue(storedFileName.endsWith(".pdf"));
       Path storedFile = tempDir.resolve(storedFileName);
       assertTrue(Files.exists(storedFile));
       assertEquals(content, Files.readString(storedFile));
   }
   
   @Test
   void storeFile_WithEmptyFile_ThrowsInvalidFileException() {
       // Arrange
       MockMultipartFile emptyFile = new MockMultipartFile(
           "empty.pdf",
           "empty.pdf",
           "application/pdf",
           new byte[0]
       );
       
       // Act & Assert
       InvalidFileException exception = assertThrows(InvalidFileException.class, () -> 
           fileStorageService.storeFile(emptyFile)
       );
       assertEquals("Kan geen leeg bestand opslaan", exception.getMessage());
   }
   
   @Test
   void storeFile_WithTooLargeFile_ThrowsInvalidFileException() {
       // Arrange
       byte[] largeContent = new byte[11 * 1024 * 1024]; // 11MB
       MockMultipartFile largeFile = new MockMultipartFile(
           "large.pdf",
           "large.pdf",
           "application/pdf",
           largeContent
       );
       
       // Act & Assert
       InvalidFileException exception = assertThrows(InvalidFileException.class, () -> 
           fileStorageService.storeFile(largeFile)
       );
       assertTrue(exception.getMessage().contains("Bestand is te groot"));
   }
   
   @Test
   void loadFileAsResource_WithExistingFile_LoadsSuccessfully() throws IOException {
       // Arrange
       String content = "test content";
       String fileName = "test-resource.pdf";
       Path filePath = tempDir.resolve(fileName);
       Files.write(filePath, content.getBytes());
       
       // Act
       Resource resource = fileStorageService.loadFileAsResource(fileName);
       
       // Assert
       assertTrue(resource.exists());
       try (var inputStream = resource.getInputStream()) {
           String loadedContent = new String(inputStream.readAllBytes());
           assertEquals(content, loadedContent);
       }
   }
   
   @Test
   void loadFileAsResource_WithNonExistingFile_ThrowsFileStorageException() {
       // Arrange
       String nonExistingFile = "nonexisting.pdf";
       
       // Act & Assert
       FileStorageException exception = assertThrows(FileStorageException.class, () -> 
           fileStorageService.loadFileAsResource(nonExistingFile)
       );
       assertTrue(exception.getMessage().contains("Bestand niet gevonden"));
   }
   
   @Test
   void deleteFile_WithExistingFile_DeletesSuccessfully() throws IOException {
       // Arrange
       String fileName = "test.pdf";
       Path filePath = tempDir.resolve(fileName);
       Files.write(filePath, "test content".getBytes());
       
       // Act
       fileStorageService.deleteFile(fileName);
       
       // Assert
       assertFalse(Files.exists(filePath));
   }
   
   @Test
   void deleteFile_WithNonExistingFile_ThrowsFileStorageException() {
       // Arrange
       String nonExistingFile = "nonexisting.pdf";
       
       // Act & Assert
       FileStorageException exception = assertThrows(FileStorageException.class, () -> 
           fileStorageService.deleteFile(nonExistingFile)
       );
       assertTrue(exception.getMessage().contains("Bestand bestaat niet"));
   }
   
   @Test
   void validateFileType_WithValidExtension_ReturnsTrue() {
       // Arrange
       MockMultipartFile file = new MockMultipartFile(
           "test.pdf",
           "test.pdf",
           "application/pdf",
           "test content".getBytes()
       );
       
       // Act & Assert
       assertTrue(fileStorageService.validateFileType(file, "pdf"));
   }
   
   @Test
   void validateFileType_WithInvalidExtension_ThrowsInvalidFileException() {
       // Arrange
       MockMultipartFile file = new MockMultipartFile(
           "test.txt",
           "test.txt",
           "text/plain",
           "test content".getBytes()
       );
       
       // Act & Assert
       InvalidFileException exception = assertThrows(InvalidFileException.class, () -> 
           fileStorageService.validateFileType(file, "pdf", "jpg", "png")
       );
       assertTrue(exception.getMessage().contains("Ongeldig bestandstype"));
   }
   
   @Test
   void storeFile_WithMaliciousFileName_ThrowsInvalidFileException() {
       // Arrange
       MockMultipartFile file = new MockMultipartFile(
           "test../hack.pdf",
           "test../hack.pdf",
           "application/pdf",
           "test content".getBytes()
       );
       
       // Act & Assert
       InvalidFileException exception = assertThrows(InvalidFileException.class, () -> 
           fileStorageService.storeFile(file)
       );
       assertTrue(exception.getMessage().contains("ongeldige karakters"));
   }

   @Test
   void storeFile_WithNullFileName_ThrowsInvalidFileException() {
       // Arrange
       MultipartFile file = mock(MultipartFile.class);
       when(file.getOriginalFilename()).thenReturn(null);
       when(file.isEmpty()).thenReturn(false);
       
       // Act & Assert
       InvalidFileException exception = assertThrows(InvalidFileException.class, () -> 
           fileStorageService.storeFile(file)
       );
       assertEquals("Bestandsnaam mag niet null zijn", exception.getMessage());
   }
}