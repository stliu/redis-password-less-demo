package com.example.redispasswordlessdemo;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.RedisURI;
import io.lettuce.core.SocketOptions;
import io.lettuce.core.protocol.ProtocolVersion;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.LettuceClientConfigurationBuilderCustomizer;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import reactor.core.publisher.Mono;

@SpringBootApplication
public class RedisPasswordLessDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(RedisPasswordLessDemoApplication.class, args);
    }



    public static interface RedisCredentialsProvider {
        RedisCredentials get();
    }

    @Bean
    RedisCredentialsProvider redisCredentialsProvider(TokenCredential tokenCredential) {
        return new AzureIdentityRedisCredentialsProvider("USERNAME", tokenCredential);

    }
    @Bean
    BeanPostProcessor lettuceConnectionFactoryPostProcessor(RedisCredentialsProvider redisCredentialsProvider) {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {

                if(bean instanceof LettuceConnectionFactory){
                    LettuceConnectionFactory lettuceConnectionFactory = (LettuceConnectionFactory) bean;
                    lettuceConnectionFactory.getStandaloneConfiguration().setPassword(redisCredentialsProvider.get().getPassword());
//                    lettuceConnectionFactory.setPassword(redisCredentialsProvider.get().getPassword());
                }
                return bean;
            }
        };
    }

    public static class AzureIdentityRedisCredentialsProvider implements RedisCredentialsProvider {
        // Note: The Scopes value will change as the Azure AD Authentication support hits public preview and
        // eventually GA's.
        private TokenRequestContext tokenRequestContext = new TokenRequestContext()
            .addScopes("https://*.cacheinfra.windows.net:10225/appid/.default");
        private final TokenCredential tokenCredential;
        private final String username;

        public AzureIdentityRedisCredentialsProvider(String username, TokenCredential tokenCredential) {
            this.username = username;
            this.tokenCredential = tokenCredential;
        }

        @Override
        public RedisCredentials get() {
            return new RedisCredentials(username, tokenCredential
                .getToken(tokenRequestContext).block().getToken());
        }
    }

    @Bean
    LettuceClientConfigurationBuilderCustomizer passwordlessCustomizer() {

        return builder -> {

            // Configure the client options.
            SocketOptions so = SocketOptions.builder()
                                               .keepAlive(true) // Keep the connection alive to work with Azure
                                               // Redis Cache
                                               .build();
            ClientOptions clientOptions = ClientOptions.builder()
                                               .socketOptions(so)
                                               .protocolVersion(ProtocolVersion.RESP2) // Use RESP2 Protocol to
                                               // ensure AUTH command is used for handshake.
                                               .build();

            builder.clientOptions(clientOptions);
        };
    }
}
