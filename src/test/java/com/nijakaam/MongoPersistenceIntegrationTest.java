package com.nijakaam;

import com.nijakaam.config.MongoDatabaseInitializer;
import com.nijakaam.entity.User;
import com.nijakaam.enums.Role;
import com.nijakaam.repository.UserRepository;
import org.bson.Document;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class MongoPersistenceIntegrationTest {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Verify connected database is 'hello'")
    void testActiveDatabaseIsHello() {
        String dbName = mongoTemplate.getDb().getName();
        assertEquals("hello", dbName, "Connected database must strictly be 'hello'");
    }

    @Test
    @DisplayName("Verify all 6 required collections exist in MongoDB Atlas")
    void testRequiredCollectionsExist() {
        List<String> requiredCollections = List.of(
                "users",
                "worker_profiles",
                "employer_profiles",
                "jobs",
                "job_applications",
                "notifications"
        );

        for (String collectionName : requiredCollections) {
            boolean exists = mongoTemplate.collectionExists(collectionName);
            assertTrue(exists, "Collection '" + collectionName + "' must exist in database 'hello'");
        }
    }

    @Test
    @DisplayName("Verify document insert, persistence in Atlas, update, and verification")
    void testDocumentInsertUpdatePersistence() {
        String testEmail = "test_verification_" + System.currentTimeMillis() + "@nijakaam.com";
        User testUser = new User("Integration Test User", testEmail, "SecurePassword123!", "9999988888", Role.WORKER);

        // 1. Insert through repository
        User savedUser = userRepository.save(testUser);
        assertNotNull(savedUser.getId(), "Saved user must have a generated ID");

        try {
            // 2. Verify directly from MongoTemplate/MongoDB
            Document doc = mongoTemplate.getCollection("users")
                    .find(new Document("_id", new org.bson.types.ObjectId(savedUser.getId())))
                    .first();

            // If stored as String ID or ObjectId:
            if (doc == null) {
                doc = mongoTemplate.getCollection("users")
                        .find(new Document("_id", savedUser.getId()))
                        .first();
            }

            assertNotNull(doc, "Document must exist in MongoDB Atlas collection 'users'");
            assertEquals(testEmail, doc.getString("email"));
            assertEquals("Integration Test User", doc.getString("name"));

            // 3. Update the record
            savedUser.setName("Updated Test User");
            savedUser.setPhone("9876543210");
            User updatedUser = userRepository.save(savedUser);
            assertEquals("Updated Test User", updatedUser.getName());

            // 4. Verify updated document in MongoDB
            User fetchedFromDb = mongoTemplate.findById(savedUser.getId(), User.class);
            assertNotNull(fetchedFromDb, "Updated user must be retrieved from MongoDB");
            assertEquals("Updated Test User", fetchedFromDb.getName());
            assertEquals("9876543210", fetchedFromDb.getPhone());
        } finally {
            // Cleanup test record only
            userRepository.deleteById(savedUser.getId());
            Optional<User> deleted = userRepository.findById(savedUser.getId());
            assertTrue(deleted.isEmpty(), "Test record must be cleaned up");
        }
    }
}
