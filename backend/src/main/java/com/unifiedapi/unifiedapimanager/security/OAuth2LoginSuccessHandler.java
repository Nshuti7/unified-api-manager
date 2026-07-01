package com.unifiedapi.unifiedapimanager.security;

import com.unifiedapi.unifiedapimanager.service.OAuth2Service;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final OAuth2Service oAuth2Service;
    private final OAuth2AuthorizedClientRepository authorizedClientRepository;
    private final String frontendUrl;

    public OAuth2LoginSuccessHandler(OAuth2Service oAuth2Service,
                                     OAuth2AuthorizedClientRepository authorizedClientRepository,
                                     @Value("${frontend.url}") String frontendUrl) {
        this.oAuth2Service = oAuth2Service;
        this.authorizedClientRepository = authorizedClientRepository;
        this.frontendUrl = frontendUrl;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        if (authentication instanceof OAuth2AuthenticationToken token) {
            OAuth2AuthorizedClient authorizedClient = authorizedClientRepository.loadAuthorizedClient(
                    token.getAuthorizedClientRegistrationId(), authentication, request);
            String jwt = oAuth2Service.processOAuth2Login(token, authorizedClient);
            String targetUrl = frontendUrl + "/oauth-callback?token=" + jwt + "&provider=" + token.getAuthorizedClientRegistrationId();
            getRedirectStrategy().sendRedirect(request, response, targetUrl);
        } else {
            super.onAuthenticationSuccess(request, response, authentication);
        }
    }
}
