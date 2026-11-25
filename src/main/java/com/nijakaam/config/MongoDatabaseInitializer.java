package com.nijakaam.config;

import com.nijakaam.entity.EmployerProfile;
import com.nijakaam.entity.Job;
import com.nijakaam.entity.JobApplication;
import com.nijakaam.entity.Notification;
import com.nijakaam.entity.User;
import com.nijakaam.entity.WorkerProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.IndexDefinition;
import org.springframework.data.mongodb.core.index.IndexOperations;
import org.springframework.data.mongodb.core.index.MongoPersistentEntityIndexResolver;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Initializes and verifies MongoDB collections and indexes on startup.
 * Ensures:
 * 1. The active database is 'hello'.
 * 2. All 6 required collections exist (creating missing ones non-destructively).
 * 3. Never drops, truncates, or overwrites existing collections or documents.
 * 4. Safe index synchronization without data loss.
 * 5. Clear audit logs without exposing credentials.
 */
@Component
@Order(1)
public class MongoDatabaseInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(MongoDatabaseInitializer.class);
    public static final String EXPECTED_DATABASE = "hello";

    private final MongoTemplate mongoTemplate;
    private final MongoMappingContext mongoMappingContext;

    public MongoDatabaseInitializer(MongoTemplate mongoTemplate, MongoMappingContext mongoMappingContext) {
        this.mongoTemplate = mongoTemplate;
        this.mongoMappingContext = mongoMappingContext;
    }

    @Override
    public void run(ApplicationArguments args) {
        log.info("==================================================================");
        log.info("       NIJAKAAM MONGODB INITIALIZATION & VERIFICATION            ");
        log.info("==================================================================");

        String activeDbName = mongoTemplate.getDb().getName();
        log.info("Active Connected MongoDB Database: '{}'", activeDbName);

        if (!EXPECTED_DATABASE.equalsIgnoreCase(activeDbName)) {
            log.error("CRITICAL: Connected database is '{}', but expected database is '{}'!",
                    activeDbName, EXPECTED_DATABASE);
            throw new IllegalStateException("Database mismatch! Connected to: " + activeDbName +
                    ", but expected: " + EXPECTED_DATABASE);
        } else {
            log.info("Database verification confirmed: Target database is '{}'.", EXPECTED_DATABASE);
        }

        // Required collections mapped to their respective document entity classes
        Map<String, Class<?>> requiredCollections = new LinkedHashMap<>();
        requiredCollections.put("users", User.class);
        requiredCollections.put("worker_profiles", WorkerProfile.class);
        requiredCollections.put("employer_profiles", EmployerProfile.class);
        requiredCollections.put("jobs", Job.class);
        requiredCollections.put("job_applications", JobApplication.class);
        requiredCollections.put("notifications", Notification.class);

        MongoPersistentEntityIndexResolver indexResolver = new MongoPersistentEntityIndexResolver(mongoMappingContext);

        for (Map.Entry<String, Class<?>> entry : requiredCollections.entrySet()) {
            String collectionName = entry.getKey();
            Class<?> entityClass = entry.getValue();

            boolean exists = mongoTemplate.collectionExists(collectionName);
            if (!exists) {
                mongoTemplate.createCollection(collectionName);
                log.info("Collection '{}': STATUS = CREATED (New collection initialized in database '{}').",
                        collectionName, activeDbName);
            } else {
                long count = mongoTemplate.getCollection(collectionName).countDocuments();
                log.info("Collection '{}': STATUS = ALREADY EXISTS (Total documents: {}). Left unchanged.",
                        collectionName, count);
            }

            // Ensure indexes are defined safely without dropping existing indexes or data
            try {
                IndexOperations indexOps = mongoTemplate.indexOps(entityClass);
                Iterable<? extends IndexDefinition> indexDefinitions = indexResolver.resolveIndexFor(entityClass);
                int indexCount = 0;
                for (IndexDefinition indexDef : indexDefinitions) {
                    indexOps.ensureIndex(indexDef);
                    indexCount++;
                }
                log.info("   -> Indexes ensured safely for collection '{}' (Count: {}).", collectionName, indexCount);
            } catch (Exception ex) {
                log.warn("   -> Warning ensuring indexes for collection '{}': {}", collectionName, ex.getMessage());
            }
        }

        log.info("==================================================================");
        log.info("All 6 MongoDB collections and indexes verified in database '{}'.", activeDbName);
        log.info("==================================================================");
    }
}
