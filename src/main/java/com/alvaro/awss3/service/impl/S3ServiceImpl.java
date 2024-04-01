package com.alvaro.awss3.service.impl;

import com.alvaro.awss3.service.IS3Service;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Data
@Service
public class S3ServiceImpl implements IS3Service {

    private final S3Client s3Client;

    public ResponseEntity<List<String>> getAllFiles () throws IOException {
        try {
            ListObjectsRequest listObjectsRequest = ListObjectsRequest.builder()
                    .bucket("bucket-prueba--s3")
                    .build();

            List<S3Object> objects = s3Client.listObjects(listObjectsRequest).contents();
            List<String> fileNames = new ArrayList<>();

            for (S3Object object: objects) {
                fileNames.add(object.key());
            }
            return ResponseEntity.ok(fileNames);

        } catch (S3Exception e) {
            throw new IOException(e.getMessage());
        }
    }

    public ResponseEntity uploadFile (MultipartFile file) throws IOException {
        try {
            String fileName = file.getOriginalFilename();
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket("bucket-prueba--s3")
                    .key(fileName)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));

            return ResponseEntity.ok("Archivo subido correctamente");
        } catch (IOException e) {
            throw new IOException(e.getMessage());
        }
    }

    public ResponseEntity downloadFile (String fileName) throws IOException {

        String localPath = "/Users/" + System.getProperty("user.name") + "/Downloads/";

        if (!objectExists(fileName)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("El archivo '" + fileName + "' no se encontró.");
        }

        GetObjectRequest request = GetObjectRequest.builder()
                .bucket("bucket-prueba--s3")
                .key(fileName)
                .build();

        ResponseInputStream<GetObjectResponse> result = s3Client.getObject(request);

        // Se leen los bytes del archivo y se construye uno igual
        try (FileOutputStream fileOutputStream = new FileOutputStream(localPath + fileName)) {
            byte[] read_buf = new byte[1024];
            int read_len = 0;

            while ((read_len = result.read(read_buf)) > 0) {
                fileOutputStream.write(read_buf, 0, read_len);
            }

        } catch (IOException e) { // Excepción de lectura o escritura del archivo
            throw new IOException(e.getMessage());

        }
        return ResponseEntity.ok().body("Archivo descargado correctamente");
    }

    public byte[] downloadFileAsBytes(String fileName) throws IOException {
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket("bucket-prueba--s3")
                .key(fileName)
                .build();

        try (ResponseInputStream<GetObjectResponse> result = s3Client.getObject(request)) {
            // Lee los bytes del archivo y los almacena en un ByteArrayOutputStream
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = result.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            return outputStream.toByteArray(); // Retorna los bytes del archivo
        } catch (IOException e) {
            throw new IOException("Error al descargar el archivo: " + e.getMessage());
        }
    }

    public ResponseEntity renameFile (String oldFileName, String newFileName) throws IOException {
        if (!objectExists(oldFileName)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("El archivo '" + oldFileName + "' no se encontró.");
        }

        try {
            CopyObjectRequest copyObjectRequest = CopyObjectRequest.builder()
                    .destinationBucket("bucket-prueba--s3")
                    .copySource("bucket-prueba--s3/" + oldFileName)
                    .destinationKey(newFileName)
                    .build();

            s3Client.copyObject(copyObjectRequest);
            deleteFile(oldFileName);

        } catch (S3Exception e) {
            throw new IOException(e.getMessage());
        }
        return ResponseEntity.ok().body("Archivo renombrado con éxito a " + newFileName);
    }

    public ResponseEntity updateFile (String oldFileName, MultipartFile file) throws IOException {
        if (!objectExists(oldFileName)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("El archivo '" + oldFileName + "' no se encontró.");
        }

        try {
            String newFileName = file.getOriginalFilename();
            deleteFile(oldFileName);
            uploadFile(file);
        } catch (S3Exception e) {
            throw new IOException(e.getMessage());
        }
        return ResponseEntity.ok().body("Archivo actualizado correctamente");
    }

    public ResponseEntity deleteFile (String fileName) throws IOException {
        if (!objectExists(fileName)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("El archivo '" + fileName + "' no se encontró.");
        }

        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket("bucket-prueba--s3")
                    .key(fileName)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);

        } catch (S3Exception e) {
            throw new IOException(e.getMessage());
        }
        return ResponseEntity.ok().body("Archivo borrado correctamente");
    }

    private boolean objectExists (String objectKey) {
        try {
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket("bucket-prueba--s3")
                    .key(objectKey)
                    .build();

            s3Client.headObject(headObjectRequest);
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                return false;
            }
        }
        return true;
    }
}