package com.greentrade.greentrade.services;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.greentrade.greentrade.config.FileStorageConfig;
import com.greentrade.greentrade.config.FileValidationConfig;
import com.greentrade.greentrade.exception.file.FileStorageException;
import com.greentrade.greentrade.exception.file.InvalidFileException;

@Service
public class FileStorageService {

    private final Path fileStorageLocation;
    private final FileValidationConfig fileValidationConfig;

    @Autowired
    public FileStorageService(FileStorageConfig fileStorageConfig, FileValidationConfig fileValidationConfig) {
        this.fileValidationConfig = fileValidationConfig;
        this.fileStorageLocation = Paths.get(fileStorageConfig.getUploadDir())
                .toAbsolutePath().normalize();

        initializeStorageLocation();
    }
    
    private void initializeStorageLocation() {
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (IOException e) {
            throw new FileStorageException("Upload directory kon niet worden aangemaakt", e);
        } catch (SecurityException e) {
            throw new FileStorageException("Geen toegangsrechten voor het aanmaken van de upload directory", e);
        } catch (InvalidPathException e) {
            throw new FileStorageException("Ongeldig pad voor de upload directory", e);
        }
    }

    public String storeFile(MultipartFile file) {
        validateFile(file);
        
        String fileName = generateUniqueFileName(file);
        
        try {
            saveFileToStorage(file, fileName);
            return fileName;
        } catch (IOException e) {
            throw new FileStorageException("Kon bestand " + fileName + " niet opslaan", e);
        }
    }    
    
    private void validateFile(MultipartFile file) {
        // Null check for the file object itself
        if (file == null) {
            throw new InvalidFileException("Bestand mag niet null zijn");
        }
    
        // Check for null original filename
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new InvalidFileException("Bestandsnaam mag niet null zijn");
        }
    
        if (file.isEmpty()) {
            throw new InvalidFileException("Kan geen leeg bestand opslaan");
        }
    
        String fileName = StringUtils.cleanPath(originalFilename);
        
        // Check for malicious filenames
        if (fileName.contains("..")) {
            throw new InvalidFileException("Bestandsnaam bevat ongeldige karakters: " + fileName);
        }
    
        if (file.getSize() > fileValidationConfig.getMaxFileSize()) {
            throw InvalidFileException.tooLarge(fileValidationConfig.getMaxFileSize());
        }
        
        // Validate file type
        validateFileType(file, fileValidationConfig.getAllowedExtensions().toArray(String[]::new));
    }
    
    private String generateUniqueFileName(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        
        return UUID.randomUUID().toString() + fileExtension;
    }
    
    private void saveFileToStorage(MultipartFile file, String fileName) throws IOException {
        Path targetLocation = this.fileStorageLocation.resolve(fileName);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
    }
    
    public Resource loadFileAsResource(@NonNull String fileName) {
        try {
            String cleanFileName = StringUtils.cleanPath(Objects.requireNonNull(fileName, "Bestandsnaam mag niet null zijn"));
            Path filePath = this.fileStorageLocation.resolve(cleanFileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists()) {
                return resource;
            } else {
                throw new FileStorageException("Bestand niet gevonden: " + fileName);
            }
        } catch (MalformedURLException e) {
            throw new FileStorageException("Ongeldig bestandspad: " + fileName, e);
        }
    }

    public void deleteFile(@NonNull String fileName) {
        try {
            String cleanFileName = StringUtils.cleanPath(Objects.requireNonNull(fileName, "Bestandsnaam mag niet null zijn"));
            Path filePath = this.fileStorageLocation.resolve(cleanFileName).normalize();
            
            if (!Files.deleteIfExists(filePath)) {
                throw new FileStorageException("Bestand bestaat niet: " + fileName);
            }
        } catch (IOException e) {
            throw new FileStorageException("Kon bestand " + fileName + " niet verwijderen", e);
        }
    }
    
    public boolean validateFileType(@NonNull MultipartFile file, @NonNull String... allowedExtensions) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new InvalidFileException("Bestandsnaam mag niet null zijn");
        }
        
        String cleanFileName = StringUtils.cleanPath(originalFilename).toLowerCase();
        boolean matchesAllowedExtension = java.util.Arrays.stream(allowedExtensions)
                .filter(Objects::nonNull)
                .map(String::toLowerCase)
                .anyMatch(ext -> cleanFileName.endsWith("." + ext));
                
        if (!matchesAllowedExtension) {
            throw InvalidFileException.invalidType(String.join(", ", allowedExtensions));
        }
        
        return true;
    }
}