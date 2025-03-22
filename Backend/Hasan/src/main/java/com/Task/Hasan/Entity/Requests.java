package com.Task.Hasan.Entity;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "requests")
public class Requests {
    @Id
    private String id;
    private String unStructuredData;
    private String clarifiedIntent; // NEW FIELD
    private String structuredData;
    private Status status = Status.Pending;
    private String errorMessage;
    @CreatedDate
    private Instant createdAt;
    @LastModifiedDate
    private Instant updatedAt;

    public enum Status {
        Pending,
        Processing,
        Completed,
        Failed
    }

    public Requests(String id, String unStructuredData, String clarifiedIntent, String structuredData, Status status, String errorMessage, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.unStructuredData = unStructuredData;
        this.clarifiedIntent = clarifiedIntent;
        this.structuredData = structuredData;
        this.status = status;
        this.errorMessage = errorMessage;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;

    }

    public Requests() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUnStructuredData() {
        return unStructuredData;
    }

    public void setUnStructuredData(String unStructuredData) {
        this.unStructuredData = unStructuredData;
    }

    public String getClarifiedIntent() {
        return clarifiedIntent;
    }

    public void setClarifiedIntent(String clarifiedIntent) {
        this.clarifiedIntent = clarifiedIntent;
    }

    public String getStructuredData() {
        return structuredData;
    }

    public void setStructuredData(String structuredData) {
        this.structuredData = structuredData;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "Requests{" +
                "id='" + id + '\'' +
                ", unStructuredData='" + unStructuredData + '\'' +
                ", clarifiedIntent='" + clarifiedIntent + '\'' +
                ", structuredData='" + structuredData + '\'' +
                ", status=" + status +
                ", errorMessage='" + errorMessage + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
