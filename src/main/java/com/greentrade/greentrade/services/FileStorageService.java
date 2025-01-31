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
        // Nullcheck voor het file object zelf
        if (file == null) {
            throw new InvalidFileException("Bestand mag niet null zijn");
        }
    
        // Check voor null original filename
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new InvalidFileException("Bestandsnaam mag niet null zijn");
        }
    
        if (file.isEmpty()) {
            throw new InvalidFileException("Kan geen leeg bestand opslaan");
        }
    
        String fileName = StringUtils.cleanPath(originalFilename);
        
        // Check voor malicious bestandsnamen
        if (fileName.contains("..")) {
            throw new InvalidFileException("Bestandsnaam bevat ongeldige karakters: " + fileName);
        }
    
        if (file.getSize() > fileValidationConfig.getMaxFileSize()) {
            throw InvalidFileException.tooLarge(fileValidationConfig.getMaxFileSize());
        }
        
        try {
            String fileExtension = "";
            if (fileName.contains(".")) {
                fileExtension = fileName.substring(fileName.lastIndexOf("."));
            }
            
            String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
    
            Path targetLocation = this.fileStorageLocation.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
    
            return uniqueFileName;
        } catch (IOException e) {
            throw new FileStorageException("Kon bestand " + fileName + " niet opslaan", e);
        }
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
        if (originalFilename == null) {
            throw new InvalidFileException("Geen bestandsnaam opgegeven");
        }
        
        String cleanFileName = StringUtils.cleanPath(originalFilename).toLowerCase();
        boolean isValid = java.util.Arrays.stream(allowedExtensions)
                .filter(Objects::nonNull)
                .map(String::toLowerCase)
                .anyMatch(ext -> cleanFileName.endsWith("." + ext));

        if (!isValid) {
            throw InvalidFileException.invalidType(String.join(", ", allowedExtensions));
        }
        
        return true;
    }
}