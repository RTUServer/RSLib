package com.github.ipecter.rtuserver.lib.util.data;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class MongoStorage implements Storage {

    private final Gson gson = new Gson();
    private final MongoClient client;
    private final MongoDatabase database;

    public MongoStorage(MongoInfo info) {
        // Replace the placeholder with your Atlas connection string
        String uri = "mongodb://" + info.getUsername() + ":" + info.getPassword() + "@" + info.getIp() + ":" + info.getPort();
        // Construct a ServerApi instance using the ServerApi.builder() method
        ServerApi serverApi = ServerApi.builder()
                .version(ServerApiVersion.V1)
                .build();
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(uri))
                .serverApi(serverApi)
                .build();
        // Create a new client and connect to the server
        this.client = MongoClients.create(settings);
        this.database = client.getDatabase(info.getDatabase());
    }

    @Override
    public boolean add(String collectionName, JsonObject data) {
        MongoCollection<Document> collection = database.getCollection(collectionName);
        Document document = Document.parse(gson.toJson(data));
        collection.insertOne(document);
        return true;
    }

    @Override
    public boolean set(String collectionName, Pair<String, Object> find, Pair<String, Object> data) {
        MongoCollection<Document> collection = database.getCollection(collectionName);
        Bson filter;
        if (find.getValue() instanceof JsonObject jsonElement) {
            filter = Filters.eq(find.getKey(), Document.parse(jsonElement.toString()));
        } else filter = Filters.eq(find.getKey(), (String) find.getValue());
        if (data == null) {
            DeleteResult result = collection.deleteOne(filter);
            return result.wasAcknowledged();
        } else {
            UpdateOptions options = new UpdateOptions().upsert(true);
            Bson update;
            if (data.getValue() instanceof JsonObject jsonObject) {
                update = Updates.set(data.getKey(), Document.parse(jsonObject.toString()));
            } else update = Updates.set(data.getKey(), data.getValue());
            UpdateResult result = collection.updateOne(filter, update, options);
            return result.wasAcknowledged();
        }
    }


    @NotNull
    @Override
    public List<JsonObject> get(String collectionName, Pair<String, Object> find) {
        Bson filter = Filters.eq(find.getKey(), find.getValue());
        FindIterable<Document> documents = database.getCollection(collectionName).find(filter);
        List<JsonObject> result = new ArrayList<>();
        for (Document document : documents) {
            if (document != null && !document.isEmpty()) {
                result.add(JsonParser.parseString(document.toJson()).getAsJsonObject());
            }
        }
        return result;
    }


    @Override
    public void close() {
        client.close();
    }
}
