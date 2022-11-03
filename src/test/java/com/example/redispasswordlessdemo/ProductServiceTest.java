package com.example.redispasswordlessdemo;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Testcontainers
public class ProductServiceTest {

    private static final String FAKE_TOKEN = "fake-token";
    @TestConfiguration
    public static class FakeTokenProvider {


        @Bean
        TokenCredential tokenCredential() {
            return new TokenCredential() {
                @Override
                public Mono<AccessToken> getToken(TokenRequestContext tokenRequestContext) {
                    return Mono.just(new AccessToken(FAKE_TOKEN, null));
                }
            };
        }


    }
    // will be shared between test methods
    @Container
   private static  GenericContainer<?> redis =
        new GenericContainer<>(DockerImageName.parse("redis:5.0.3-alpine"))
            .withCommand("--requirepass",FAKE_TOKEN)
            .withExposedPorts(6379);


    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.redis.host", redis::getHost);
        registry.add("spring.redis.port", redis::getFirstMappedPort);
//        registry.add("spring.redis.password", () -> "123456");
        registry.add("logging.level.io.lettuce", () -> "DEBUG");
    }
    @Autowired
    ProductService productService;
    @Test
    void givenProductCreated_whenGettingProductById_thenProductExistsAndHasSameProperties() {
        Product product = new Product("1", "Test Product", 10.0);
        productService.createProduct(product);
        Product productFromDb = productService.getProduct("1");
        assertEquals("1", productFromDb.getId());
        assertEquals("Test Product", productFromDb.getName());
        assertEquals(10.0, productFromDb.getPrice());
    }
}
