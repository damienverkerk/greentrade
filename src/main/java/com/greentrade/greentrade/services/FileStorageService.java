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

@Service
public class FileStorageService {

    private final Path fileStorageLocation;

    @Autowired
    public FileStorageService(FileStorageConfig fileStorageConfig) {
        this.fileStorageLocation = Paths.get(fileStorageConfig.getUploadDir())
                .toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (IOException e) {
            throw new RuntimeException("Kon de upload directory niet aanmaken.", e);
        } catch (SecurityException e) {
            throw new RuntimeException("Geen toegangsrechten voor het aanmaken van de upload directory.", e);
        } catch (InvalidPathException e) {
            throw new RuntimeException("Ongeldig pad voor de upload directory.", e);
        }
    }

    public String storeFile(@NonNull MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("Kan geen leeg bestand opslaan");
        }

        String fileName = StringUtils.cleanPath(
            Objects.requireNonNull(file.getOriginalFilename(), "Bestandsnaam mag niet null zijn")
        );
        
        try {
            String fileExtension = "";
            if (fileName.contains(".")) {
                fileExtension = fileName.substring(fileName.lastIndexOf("."));
            }
            
            String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
            
            if (uniqueFileName.contains("..")) {
                throw new RuntimeException("Sorry! Bestandsnaam bevat een ongeldig pad " + fileName);
            }

            Path targetLocation = this.fileStorageLocation.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return uniqueFileName;
        } catch (IOException e) {
            throw new RuntimeException("Kon bestand " + fileName + " niet opslaan.", e);
        } catch (InvalidPathException e) {
            throw new RuntimeException("Ongeldig pad voor bestand " + fileName, e);
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
                throw new RuntimeException("Bestand niet gevonden: " + fileName);
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Ongeldig bestandspad: " + fileName, e);
        }
    }

    public void deleteFile(@NonNull String fileName) {
        try {
            String cleanFileName = StringUtils.cleanPath(Objects.requireNonNull(fileName, "Bestandsnaam mag niet null zijn"));
            Path filePath = this.fileStorageLocation.resolve(cleanFileName).normalize();
            
            if (!Files.deleteIfExists(filePath)) {
                throw new RuntimeException("Bestand bestaat niet: " + fileName);
            }
        } catch (IOException e) {
            throw new RuntimeException("Kon bestand " + fileName + " niet verwijderen.", e);
        }
    }
    
    public boolean validateFileType(@NonNull MultipartFile file, @NonNull String... allowedExtensions) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            return false;
        }
        
        String cleanFileName = StringUtils.cleanPath(originalFilename).toLowerCase();
        return java.util.Arrays.stream(allowedExtensions)
                .filter(Objects::nonNull)
                .map(String::toLowerCase)
                .anyMatch(ext -> cleanFileName.endsWith("." + ext));
    }
}