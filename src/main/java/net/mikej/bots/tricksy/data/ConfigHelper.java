package net.mikej.bots.tricksy.data;

import com.mongodb.client.MongoCollection;

import org.bson.Document;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.types.ObjectId;

import static com.mongodb.client.model.Updates.*;

import com.mongodb.client.FindIterable;

import static com.mongodb.client.model.Filters.*;

public class ConfigHelper {
    public static <T> T getConfigItem(String key, Class<T> typedClass) {
        MongoCollection<T> configCollection = MongoContainer.getClient().getDatabase("discord-bot").getCollection("configs", typedClass);
        FindIterable<T> configItems = configCollection.find(eq("_id", key));
        return configItems.first();
    }

    public static boolean containsConfigItem(String key) {
        return MongoContainer.getClient().getDatabase("discord-bot").getCollection("configs").countDocuments(eq("_id", key)) > 0;
    }

    public static Document deleteConfigItem(String key) {
        return MongoContainer.getClient().getDatabase("discord-bot").getCollection("configs").findOneAndDelete(eq("_id", key));
    }

    public static <T> ConfigItem<T> setConfigItem(String key, T value, Class<ConfigItem<T>> typedClass) {
        MongoCollection<ConfigItem<T>> collection = MongoContainer.getClient().getDatabase("discord-bot").getCollection("configs", typedClass);
        if (containsConfigItem(key)) {
            return collection.findOneAndUpdate(eq("configKey", key), set("configValue", value));
        } else {
            ConfigItem<T> newItem = new ConfigItem<T>(key, value);
            collection.insertOne(newItem);
            return newItem;
        }
    }

    @BsonDiscriminator
    public static class ConfigItem<T> {
        @BsonId
        private ObjectId id;
        private String configKey;
        private T configValue;

        public ConfigItem() {}
        public ConfigItem(String configKey, T configValue) {
            this.configKey = configKey;
            this.configValue = configValue;
        }

        public ObjectId getId() { return id; }
        public String getConfigKey() { return configKey; }
        public T getConfigValue() { return configValue; }

        public void setId(ObjectId id) { this.id = id; }
        public void setConfigKey(String configKey) { this.configKey = configKey; }
        public void setConfigValue(T configValue) { this.configValue = configValue; }
    }
}