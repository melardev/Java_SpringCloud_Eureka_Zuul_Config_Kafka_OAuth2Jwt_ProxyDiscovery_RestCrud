package com.melardev.cloud.rest.config;


import com.melardev.cloud.rest.security.OAuthAccessDeniedHandler;
import io.micrometer.core.instrument.util.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Configuration
@EnableResourceServer
public class ResourceServerConfig extends ResourceServerConfigurerAdapter {

    private TokenStore tokenStore;

    @Value("${app.security.key_file_path}")
    private String publicKeyPath;

    @Value("${app.security.key_file_path_as_resource}")
    private Resource publicKeyAsResource;
    private JwtAccessTokenConverter jwtAccessTokenConverter;

    @Autowired
    private AuthenticationEntryPoint oauthEntryPoint;

    @Autowired
    private OAuthAccessDeniedHandler oauthAccessDeniedHandler;
    private DefaultTokenServices tokenServices;


    @Override
    public void configure(final ResourceServerSecurityConfigurer resources) {
        resources
                .tokenServices(tokenServices(tokenStore()))
                .tokenStore(tokenStore());

        resources.authenticationEntryPoint(oauthEntryPoint);
    }

    @Bean
    public DefaultTokenServices tokenServices(final TokenStore tokenStore) {
        if (tokenServices == null) {
            tokenServices = new DefaultTokenServices();
            tokenServices.setSupportRefreshToken(true);
            tokenServices.setTokenStore(tokenStore);
        }
        return tokenServices;
    }

    @Bean
    public TokenStore tokenStore() {
        if (tokenStore == null) {
            tokenStore = new JwtTokenStore(jwtAccessTokenConverter());
        }
        return tokenStore;
    }

    @Bean
    public JwtAccessTokenConverter jwtAccessTokenConverter() {
        if (jwtAccessTokenConverter == null) {
            try {
                jwtAccessTokenConverter = new JwtAccessTokenConverter();
                ClassPathResource publicKey = new ClassPathResource(publicKeyPath);

                /*
                long fileLength = publicKey.contentLength();
                byte[] publicKeyBytes = new byte[(int) fileLength];
                int bytesRead = publicKey.getInputStream().read(publicKeyBytes);
                assert bytesRead == fileLength;
                */

                String publicKeyString = new String(FileCopyUtils.copyToByteArray(publicKey.getInputStream()), StandardCharsets.UTF_8);
                jwtAccessTokenConverter.setVerifierKey(publicKeyString);
                // String publicKeyString = new String(FileCopyUtils.copyToByteArray(publicKey.getInputStream()), StandardCharsets.UTF_8);
                // String publicKeyString = IOUtils.toString(publicKeyAsResource.getInputStream(), StandardCharsets.UTF_8);

                return jwtAccessTokenConverter;
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
        return jwtAccessTokenConverter;
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .anonymous().disable()
                .authorizeRequests()
                .antMatchers("/dummy/**").authenticated()
                .and().exceptionHandling()
                .authenticationEntryPoint(oauthEntryPoint)
                .accessDeniedHandler(oauthAccessDeniedHandler);
    }
}
