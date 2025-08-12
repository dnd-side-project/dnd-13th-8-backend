package com.example.demo.domain.prop.controller;

import com.example.demo.global.r2.R2Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/prop")
@RequiredArgsConstructor
public class PropController {

    private final R2Service r2Service;

    @PostMapping(value = "/upload" ,consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> upload(@RequestPart("file") MultipartFile file) throws IOException {
        String key = r2Service.newKey(file.getOriginalFilename());
        r2Service.upload(file.getBytes(), file.getContentType(), key);
        return ResponseEntity.ok().body(key);
    }

    @GetMapping
    public ResponseEntity<Void> view(@RequestParam String key) {
        String url = r2Service.presignGetUrl(key);
        return ResponseEntity.status(302).header("Location", url).build();
    }
}
