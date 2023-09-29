package com.akazimour.BackendForEto.controller;

import com.akazimour.BackendForEto.dto.UploadResponse;
import com.akazimour.BackendForEto.service.StorageService;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
@RequestMapping("/api/eto")
public class DownloaderAndUploaderController {

    @Autowired
    StorageService storageService;

    //Single 3D file upload
    @PostMapping("/file/upload")
public UploadResponse uploadFile(@RequestParam("file")MultipartFile file){
String fileName = storageService.storeFile(file);

    String uriString = ServletUriComponentsBuilder.fromCurrentContextPath()
            .path("/api/eto/download/")
            .path(fileName)
            .toUriString();

    String contentType = file.getContentType();
   UploadResponse uploadResponse = new UploadResponse(fileName,contentType,uriString);
  return uploadResponse;
}

//Multiple 3D file upload
    @PostMapping("/multiple/upload")
    public List<UploadResponse> uploadMultipleFiles(@RequestParam("files") MultipartFile[] files){
if (files.length > 5){
    throw new RuntimeException("Max number of files are 5!");
}
        List<UploadResponse> uploadedModels = new ArrayList<>();
        Arrays.asList(files).stream().forEach(file -> {
            String fileName = storageService.storeFile(file);

            String uriString = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/api/eto/download/")
                    .path(fileName)
                    .toUriString();

            String contentType = file.getContentType();
            UploadResponse uploadResponse = new UploadResponse(fileName,contentType,uriString);
           uploadedModels.add(uploadResponse);
        });
        return uploadedModels;
        };

    //Download multiple selected files as Zip
    @GetMapping("/download/zip")
    public void zipDownload(@RequestParam("fileName")String[] files, HttpServletResponse response) throws IOException {
        response.addHeader(HttpHeaders.CONTENT_DISPOSITION,"form-data;fileName="+"ZippedModels.zip");
         try(ZipOutputStream zos = new ZipOutputStream(response.getOutputStream())){
             Arrays.asList(files)
                     .stream()
                     .forEach(file -> {
                       Resource resource = storageService.downloadFile(file);

                         ZipEntry zipEntry = new ZipEntry(resource.getFilename());
                         try {
                             zipEntry.setSize(resource.contentLength());
                             zos.putNextEntry(zipEntry);

                             int copy = StreamUtils.copy(resource.getInputStream(), zos);

                             zos.closeEntry();
                         } catch (IOException e) {
                             System.out.println("Something went wrong during Zip-file creation!");
                         }
                     });
             zos.finish();
         }
    }
// Download a file
@GetMapping("/download/{fileName}")
    public ResponseEntity<Resource> downloadModel(@PathVariable String fileName, HttpServletRequest request){
Resource resource = storageService.downloadFile(fileName);

   // MediaType contentType = MediaType.MULTIPART_FORM_DATA;
    String mimeType;
    try {
       mimeType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
    } catch (IOException e) {
        mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
    }
    return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(mimeType))
            .header(HttpHeaders.CONTENT_DISPOSITION,"form-data;fileName="+resource.getFilename())
           // .header(HttpHeaders.CONTENT_DISPOSITION,"inline;fileName="+resource.getFilename())
            .body(resource);
}

}
