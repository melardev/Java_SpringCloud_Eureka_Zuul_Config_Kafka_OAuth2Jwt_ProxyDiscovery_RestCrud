package com.melardev.cloud.auth.config;

import com.melardev.cloud.auth.security.CustomTokenEnhancer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.builders.ClientDetailsServiceBuilder;
import org.springframework.security.oauth2.config.annotation.builders.InMemoryClientDetailsServiceBuilder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.NoSuchClientException;
import org.springframework.security.oauth2.provider.client.BaseClientDetails;
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenEnhancerChain;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableAuthorizationServer
public class OAuthServerConfiguration extends AuthorizationServerConfigurerAdapter {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private CustomTokenEnhancer authTokenEnhancer;

    @Autowired
    private UserDetailsService userDetailsService;

    @Value("${app.security.key_file_path}")
    String keyClasspathFilePath;

    @Value("${app.security.key_file_path_as_resource}")
    Resource keyFileAsResource;

    @Value("${app.security.key_file_password}")
    private String keyFilePassword;

    @Value("${app.security.key_pair_alias}")
    private String keyPairAlias;

    @Value("${app.security.keystore_password}")
    private String keyStorePassword;

    private JwtAccessTokenConverter accessTokenConverter;

    @Value("${app.security.oauth2.client1.id}")
    private String client1Id;

    @Value("${app.security.oauth2.client1.password}")
    private String client1Password;

    @Value("${app.security.oauth2.client1.scopes}")
    private List<String> client1Scopes;

    @Value("${app.security.oauth2.client2.id}")
    private String client2Id;

    @Value("${app.security.oauth2.client2.password}")
    private String client2Password;

    @Value("${app.security.oauth2.client2.scopes}")
    private List<String> client2Scopes;

    @Value("${app.security.oauth2.grant_types.password}")
    private String passwordGrantType;

    @Value("${app.security.oauth2.grant_types.authorization_code}")
    private String authorizationCodeGrantType;

    @Value("${app.security.oauth2.grant_types.refresh_token}")
    private String refreshTokenGrantType;

    @Value("${app.security.oauth2.grant_types.implicit}")
    private String implicitGrantType;

    @Value("${app.security.oauth2.access_token_validity_seconds}")
    private int accessTokenValiditySeconds;

    @Value("${app.security.oauth2.refresh_token_validity_seconds}")
    private int refreshTokenValiditySeconds;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    private DefaultTokenServices defaultTokenServices;
    private JdbcClientDetailsService jdbcClientDetailsService;

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        endpoints
                .authenticationManager(this.authenticationManager)
                .tokenServices(tokenServices())
                .tokenStore(tokenStore())
                .accessTokenConverter(accessTokenConverter());
    }

    @Override
    public void configure(ClientDetailsServiceConfigurer configurer) throws Exception {
        // Configured with the tokenServices()
        // In Memory
        // configureInMemoryClientDetailsService(configurer);

        // JDBC
        // Approach 1
        // configurer.jdbc(dataSource).passwordEncoder(passwordEncoder);
        // Approach 2
        configurer.withClientDetails(jdbcDetailsService(dataSource));

        // Custom
        // configurer.withClientDetails(new AppClientsService());
    }

    private void configureInMemoryClientDetailsService(ClientDetailsServiceConfigurer configurer) throws Exception {

        InMemoryClientDetailsServiceBuilder inMemoryClientDetailsBuilder = configurer.inMemory();

        // Client 1
        ClientDetailsServiceBuilder.ClientBuilder client1DetailsBuilder = inMemoryClientDetailsBuilder.withClient(client1Id)
                .secret(passwordEncoder.encode(client1Password))
                .authorizedGrantTypes(passwordGrantType, authorizationCodeGrantType, refreshTokenGrantType, implicitGrantType)
                .authorities("ROLE_ADMIN")
                .accessTokenValiditySeconds(accessTokenValiditySeconds).
                        refreshTokenValiditySeconds(refreshTokenValiditySeconds);

        String[] stringArr = new String[client1Scopes.size()];
        client1DetailsBuilder.scopes(client1Scopes.toArray(stringArr));


        // Client 2
        ClientDetailsServiceBuilder.ClientBuilder client2DetailsBuilder = client1DetailsBuilder
                .and()
                .withClient(client2Id)
                .secret(passwordEncoder.encode(client2Password))
                .authorizedGrantTypes(passwordGrantType, authorizationCodeGrantType, refreshTokenGrantType, implicitGrantType)
                .authorities("ROLE_USER")
                .accessTokenValiditySeconds(accessTokenValiditySeconds).
                        refreshTokenValiditySeconds(refreshTokenValiditySeconds);


        for (String scope : client2Scopes) client2DetailsBuilder.scopes(scope);

    }

    @SuppressWarnings("Duplicates")
    @Bean
    JdbcClientDetailsService jdbcDetailsService(DataSource dataSource) {
        if(jdbcClientDetailsService == null) {
            String[] stringArr = new String[client1Scopes.size()];

            jdbcClientDetailsService = new JdbcClientDetailsService(dataSource);
            jdbcClientDetailsService.setPasswordEncoder(passwordEncoder);

            // User 1
            try {
                jdbcClientDetailsService.removeClientDetails(client1Id);
            } catch (NoSuchClientException ignored) {
            }

            BaseClientDetails user1 = new BaseClientDetails(client1Id, null, StringUtils.collectionToCommaDelimitedString(client1Scopes),
                    StringUtils.collectionToCommaDelimitedString(Arrays.asList(passwordGrantType, refreshTokenGrantType)), "ROLE_ADMIN");

            // WE SHOULD NOT ENCRYPT THE PASSWORD HERE, THIS IS DONE FOR US IN addClientDetails()
            user1.setClientSecret(client1Password);
            user1.setRefreshTokenValiditySeconds(refreshTokenValiditySeconds);
            user1.setAccessTokenValiditySeconds(accessTokenValiditySeconds);
            jdbcClientDetailsService.addClientDetails(user1);

            // User 2
            try {
                jdbcClientDetailsService.removeClientDetails(client2Id);
            } catch (NoSuchClientException ignored) {
            }
            BaseClientDetails user2 = new BaseClientDetails(client2Id, null, StringUtils.collectionToCommaDelimitedString(client2Scopes),
                    StringUtils.collectionToCommaDelimitedString(Arrays.asList(passwordGrantType, refreshTokenGrantType)), "ROLE_USER");

            // WE SHOULD NOT ENCRYPT THE PASSWORD HERE, THIS IS DONE FOR US IN addClientDetails()
            user2.setClientSecret(client2Password);
            user2.setRefreshTokenValiditySeconds(refreshTokenValiditySeconds);
            user2.setAccessTokenValiditySeconds(accessTokenValiditySeconds);
            jdbcClientDetailsService.addClientDetails(user2);
        }
        return jdbcClientDetailsService;
    }

    @Bean
    public TokenStore tokenStore() {
        return new JwtTokenStore(accessTokenConverter());
    }

    @Bean
    public JwtAccessTokenConverter accessTokenConverter() {
        if (accessTokenConverter == null) {
            accessTokenConverter = new JwtAccessTokenConverter();

            // new KeyStoreKeyFactory(keyFileAsResource, keyFilePassword.toCharArray());

            KeyStoreKeyFactory keyStoreKeyFactory = new KeyStoreKeyFactory(new ClassPathResource(keyClasspathFilePath),
                    keyFilePassword.toCharArray());
            // If you use the same password for keystore and key inside the keystore then it is safe to use the below
            // converter.setKeyPair(keyStoreKeyFactory.getKeyPair(keyPairAlias));
            // otherwise use this:
            accessTokenConverter.setKeyPair(keyStoreKeyFactory.getKeyPair(keyPairAlias, keyStorePassword.toCharArray()));
        }
        return accessTokenConverter;
    }

    @Bean
    @Primary
    public DefaultTokenServices tokenServices() {

        if (defaultTokenServices == null) {
            defaultTokenServices = new DefaultTokenServices();
            defaultTokenServices.setSupportRefreshToken(true);
            defaultTokenServices.setReuseRefreshToken(false);
            defaultTokenServices.setTokenStore(tokenStore());
            defaultTokenServices.setAccessTokenValiditySeconds(accessTokenValiditySeconds);
            defaultTokenServices.setRefreshTokenValiditySeconds(refreshTokenValiditySeconds);

            TokenEnhancerChain tokenEnhancerChain = new TokenEnhancerChain();
            tokenEnhancerChain.setTokenEnhancers(Arrays.asList(authTokenEnhancer, accessTokenConverter()));
            defaultTokenServices.setTokenEnhancer(tokenEnhancerChain);

            return defaultTokenServices;
        }
        return defaultTokenServices;

    }
}
