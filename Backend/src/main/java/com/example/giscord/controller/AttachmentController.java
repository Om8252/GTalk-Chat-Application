package com.example.giscord.controller;

import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.example.giscord.entity.Attachment;
import com.example.giscord.repository.AttachmentRepository;

import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@RestController
@RequestMapping("/api/attachments")
public class AttachmentController {

    private final AttachmentRepository attachmentRepository;
    private final S3Client s3Client;

    @Value("${minio.bucket}")
    private String bucket;

    public AttachmentController(AttachmentRepository attachmentRepository, S3Client s3Client) {
        this.attachmentRepository = attachmentRepository;
        this.s3Client = s3Client;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadAttachment(@RequestParam("file") MultipartFile file) throws Exception {

        String key = UUID.randomUUID() + "-" + file.getOriginalFilename();

        s3Client.putObject(
            PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(file.getContentType())
                .contentLength(file.getSize())
                .build(),
            // software.amazon.awssdk.core.sync.RequestBody.fromBytes(file.getBytes())
            software.amazon.awssdk.core.sync.RequestBody.fromInputStream(file.getInputStream(), file.getSize())
        );

        Attachment a = new Attachment();
        a.setBucket(bucket);
        a.setContentType(file.getContentType());
        a.setObjectKey(key);
        a.setSize(file.getSize());


        // TODO: Do we need an AttachmentService ??
        attachmentRepository.save(a);
        
        return ResponseEntity
            .status(201)
            .body(Map.of("attachmentId", a.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> downloadAttachment(@PathVariable Long id) {
        // Errors as Values is my preffered way
        Attachment attachment = attachmentRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Attachment not found"));
        
        ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(
            GetObjectRequest.builder() 
                .bucket(attachment.getBucket())
                .key(attachment.getObjectKey())
                .build()
            );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(attachment.getContentType()));
        // headers.setContentLength(s3Object.asByteArray().length);
        headers.setContentLength(attachment.getSize());
        headers.setContentDisposition(
            ContentDisposition.inline()
                .filename(attachment.getObjectKey())
                .build()
        );

        return ResponseEntity
            .status(200)
            .headers(headers)
            .body(new InputStreamResource(s3Object));
            // .body(s3Object.asByteArray());
    }
    
}
