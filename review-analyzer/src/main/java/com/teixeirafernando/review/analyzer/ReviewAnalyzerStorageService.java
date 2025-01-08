package com.teixeirafernando.review.analyzer;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.awspring.cloud.s3.ObjectMetadata;
import io.awspring.cloud.s3.S3Resource;
import io.awspring.cloud.s3.S3Template;

import java.io.IOException;
import java.io.InputStream;
import java.util.SequencedCollection;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

@Service
public class ReviewAnalyzerStorageService {

    private final S3Template s3Template;

    private final ObjectMetadata metadata = ObjectMetadata.builder()
            .contentType("application/json")
            .build();

    public ReviewAnalyzerStorageService(S3Template s3Template) {
        this.s3Template = s3Template;
    }

    public void upload(String bucketName, String key, InputStream stream) {
        this.s3Template.upload(bucketName, key, stream, metadata);
    }

    public void store(String bucketName, String key, Object content) {
        this.s3Template.store(bucketName, key, content);
    }

    public boolean reviewExists(String bucketName, String key) throws NoSuchKeyException {
        return this.s3Template.objectExists(bucketName, key);
    }

    public boolean bucketExists(String bucketName){
        return this.s3Template.bucketExists(bucketName);
    }
    
    public SequencedCollection<S3Resource> listObjects(String bucketName, String prefix){
        return this.s3Template.listObjects(bucketName, prefix);
    }

    public AnalyzedReview read(String bucketName, String key) throws IOException {
        try (InputStream is = this.download(bucketName, key)) {
            return new ObjectMapper().readValue(is, AnalyzedReview.class);
        }
    }

    public InputStream download(String bucketName, String key)
            throws IOException {
        return this.s3Template.download(bucketName, key).getInputStream();
    }

    public String downloadAsString(String bucketName, String key)
            throws IOException {
        try (InputStream is = this.download(bucketName, key)) {
            return new String(is.readAllBytes());
        }
    }
}
