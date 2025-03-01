package com.greentrade.greentrade.exception.file;

public class InvalidFileException extends FileStorageException {
    public InvalidFileException(String message) {
        super(message);
    }

    public static InvalidFileException invalidType(String allowedTypes) {
        return new InvalidFileException("Invalid file type. Allowed types: " + allowedTypes);
    }

    public static InvalidFileException tooLarge(long maxSize) {
        return new InvalidFileException("File is too large. Maximum size is " + (maxSize / (1024 * 1024)) + "MB");
    }
}