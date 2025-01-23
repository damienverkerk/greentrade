package com.greentrade.greentrade.exception.file;

public class InvalidFileException extends FileStorageException {
    public InvalidFileException(String message) {
        super(message);
    }

    public static InvalidFileException invalidType(String allowedTypes) {
        return new InvalidFileException("Ongeldig bestandstype. Toegestane types: " + allowedTypes);
    }

    public static InvalidFileException tooLarge(long maxSize) {
        return new InvalidFileException("Bestand is te groot. Maximum grootte is " + (maxSize / (1024 * 1024)) + "MB");
    }
}