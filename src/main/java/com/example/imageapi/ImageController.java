package com.example.imageapi;

import jakarta.servlet.ServletContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

@RestController
@RequestMapping("api/images")
public class ImageController {
    @Autowired
    private ServletContext servletContext;

    @Autowired
    private DataSource dataSource;

    @PostMapping
    public ResponseEntity<String> uploadImage(@RequestParam("image") MultipartFile image) {
        try {

            // Generate a unique filename for the image
            String fileName = UUID.randomUUID().toString() + "_" + image.getOriginalFilename();

            // Save the image to a directory
            String uploadDir = "C:\\Users\\Zeenat\\AppData\\Local\\Temp\\tomcat-docbase.8080.7404553164153588725\\uploads";
            File file = new File(uploadDir, fileName);
            Path filePath = file.toPath();
            Files.copy(image.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement("INSERT INTO images (filename) VALUES (?)")) {
                statement.setString(1, fileName);
                statement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            String filePathResponse = servletContext.getContextPath() + "/uploads/" + fileName;
            return ResponseEntity.ok(filePathResponse);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{fileName}")
    public ResponseEntity<byte[]> getImage(@PathVariable String fileName) {
        try {
            // Retrieve the image file from the directory
            String uploadDir = "C:\\Users\\Zeenat\\AppData\\Local\\Temp\\tomcat-docbase.8080.7404553164153588725\\uploads";
            File file = new File(uploadDir, fileName);
            Path filePath = file.toPath();
            byte[] imageBytes = Files.readAllBytes(filePath);

            // Return the image bytes with appropriate headers
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(imageBytes);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }
    }
}
