package io.github.ripliquid.cloudops.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import io.github.ripliquid.cloudops.model.Incident;
import io.github.ripliquid.cloudops.model.IncidentStatus;
import io.github.ripliquid.cloudops.model.Severity;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;

@Repository
public class IncidentRepository {

    private final DynamoDbClient dynamoDbClient;
    private final String tableName;

    public IncidentRepository(
            DynamoDbClient dynamoDbClient,
            @Value("${aws.dynamodb.table-name}") String tableName
    ) {
        this.dynamoDbClient = dynamoDbClient;
        this.tableName = tableName;
    }

    public List<Incident> findAll() {

        List<Incident> incidents = new ArrayList<>();

        dynamoDbClient.scanPaginator(
                ScanRequest.builder()
                        .tableName(tableName)
                        .build()
        ).items().forEach(
                item -> incidents.add(fromItem(item))
        );

        return incidents;
    }

    public Incident findById(String id) {

        Map<String, AttributeValue> key = Map.of(
                "id",
                AttributeValue.builder()
                        .s(id)
                        .build()
        );

        GetItemResponse response =
                dynamoDbClient.getItem(
                        GetItemRequest.builder()
                                .tableName(tableName)
                                .key(key)
                                .build()
                );

        if (response.item().isEmpty()) {
            return null;
        }

        return fromItem(response.item());
    }

    public Incident save(Incident incident) {

        dynamoDbClient.putItem(
                PutItemRequest.builder()
                        .tableName(tableName)
                        .item(toItem(incident))
                        .build()
        );

        return incident;
    }

    public boolean deleteById(String id) {

        Incident existingIncident = findById(id);

        if (existingIncident == null) {
            return false;
        }

        Map<String, AttributeValue> key = Map.of(
                "id",
                AttributeValue.builder()
                        .s(id)
                        .build()
        );

        dynamoDbClient.deleteItem(
                DeleteItemRequest.builder()
                        .tableName(tableName)
                        .key(key)
                        .build()
        );

        return true;
    }

    private Map<String, AttributeValue> toItem(
            Incident incident
    ) {

        Map<String, AttributeValue> item =
                new HashMap<>();

        item.put(
                "id",
                AttributeValue.builder()
                        .s(incident.getId())
                        .build()
        );

        item.put(
                "title",
                AttributeValue.builder()
                        .s(incident.getTitle())
                        .build()
        );

        item.put(
                "description",
                AttributeValue.builder()
                        .s(incident.getDescription())
                        .build()
        );

        item.put(
                "severity",
                AttributeValue.builder()
                        .s(incident.getSeverity().name())
                        .build()
        );

        item.put(
                "status",
                AttributeValue.builder()
                        .s(incident.getStatus().name())
                        .build()
        );

        item.put(
                "owner",
                AttributeValue.builder()
                        .s(incident.getOwner())
                        .build()
        );

        return item;
    }

    private Incident fromItem(
            Map<String, AttributeValue> item
    ) {

        return new Incident(
                item.get("id").s(),
                item.get("title").s(),
                item.get("description").s(),
                Severity.valueOf(
                        item.get("severity").s()
                ),
                IncidentStatus.valueOf(
                        item.get("status").s()
                ),
                item.get("owner").s()
        );
    }
}