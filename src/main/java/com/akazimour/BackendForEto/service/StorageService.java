package com.akazimour.BackendForEto.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

@Service
public class StorageService {

   private Path fileStoragePath;
    private String fileStorageLocation;


    public StorageService(@Value("${file.storage.location}")String fileStorageLocation){
        this.fileStorageLocation = fileStorageLocation;
       fileStoragePath = Paths.get(fileStorageLocation).toAbsolutePath().normalize();

        try {
            Files.createDirectories(fileStoragePath);
        } catch (IOException e) {
            throw new RuntimeException("Directory can not created!");
        }
    }
    public String storeFile(MultipartFile file) {
        String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        Path path = Paths.get(fileStoragePath + "\\" + originalFilename);
        try {
            Files.copy(file.getInputStream(),path, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Issue saving the file!");
        }
        return originalFilename;
    }

    public Resource downloadFile(String fileName) {
        Path path = Paths.get(fileStorageLocation).toAbsolutePath().resolve(fileName);
        Resource resource;
        try {
            resource = new UrlResource(path.toUri());

        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        if (resource.exists()&&resource.isReadable()){
            return resource;
        }else
            throw new RuntimeException("Resource is not exists!");
    }
}
