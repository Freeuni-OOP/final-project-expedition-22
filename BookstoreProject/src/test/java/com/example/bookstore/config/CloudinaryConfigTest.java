package com.example.bookstore.config;

import com.cloudinary.Cloudinary;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CloudinaryConfigTest {

    @Autowired
    private Cloudinary cloudinary;

    private final CloudinaryConfig config = new CloudinaryConfig();

    @Test
    void cloudinary_ShouldCreateBean() {
        Cloudinary cloudinary = config.cloudinary();

        assertNotNull(cloudinary);
    }

    @Test
    void cloudinaryBean_ShouldHaveCloudName() {
        assertNotNull(cloudinary.config.cloudName);
        assertFalse(cloudinary.config.cloudName.isEmpty());
    }

    @Test
    void cloudinaryBean_ShouldHaveApiKey() {
        assertNotNull(cloudinary.config.apiKey);
        assertFalse(cloudinary.config.apiKey.isEmpty());
    }

    @Test
    void cloudinaryBean_ShouldHaveApiSecret() {
        assertNotNull(cloudinary.config.apiSecret);
        assertFalse(cloudinary.config.apiSecret.isEmpty());
    }

    @Test
    void cloudinary_ShouldHaveValidConfiguration() {

        CloudinaryConfig config = new CloudinaryConfig();

        ReflectionTestUtils.setField(config, "cloudName", "txw9rip6");
        ReflectionTestUtils.setField(config, "apiKey", "842346397139355");
        ReflectionTestUtils.setField(config, "apiSecret", "whva8TcMLCknNERR3r1yeIWGvoQ");

        Cloudinary cloudinary = config.cloudinary();

        assertNotNull(cloudinary.config.cloudName);
        assertNotNull(cloudinary.config.apiKey);
        assertNotNull(cloudinary.config.apiSecret);

        assertEquals("txw9rip6", cloudinary.config.cloudName);
        assertEquals("842346397139355", cloudinary.config.apiKey);
        assertEquals("whva8TcMLCknNERR3r1yeIWGvoQ", cloudinary.config.apiSecret);
    }

}