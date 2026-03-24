package com.ecommerce.auth.oauth;

import com.ecommerce.auth.entity.AuthMethod;
import com.ecommerce.auth.entity.AuthProvider;
import com.ecommerce.auth.entity.User;
import com.ecommerce.auth.event.UserCreatedEvent;
import com.ecommerce.auth.producer.AuthEventProducer;
import com.ecommerce.auth.repository.AuthMethodRepository;
import com.ecommerce.auth.repository.UserRepository;
import com.ecommerce.auth.security.JwtUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class OAuthSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final AuthMethodRepository authMethodRepository;
    private final AuthEventProducer producer;
    private final JwtUtil jwtUtil;

    public OAuthSuccessHandler(
        UserRepository userRepository,
        AuthMethodRepository authMethodRepository,
        AuthEventProducer producer,
        JwtUtil jwtUtil
    ) {
        this.userRepository = userRepository;
        this.authMethodRepository = authMethodRepository;
        this.producer = producer;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void onAuthenticationSuccess(
        HttpServletRequest request,
        HttpServletResponse response,
        Authentication authentication
    ) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        if (email == null || email.isBlank()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "OAuth2 email attribute is missing");
            return;
        }
        String resolvedName = name != null && !name.isBlank() ? name : email;

        final boolean[] isNewUser = {false};
        User user = userRepository.findByEmail(email).orElseGet(() -> {
            isNewUser[0] = true;
            return userRepository.save(
                User.builder()
                    .email(email)
                    .name(resolvedName)
                    .provider(AuthProvider.GOOGLE)
                    .role("USER")
                    .build()
            );
        });

        if (isNewUser[0]) {
            UserCreatedEvent event = UserCreatedEvent.builder()
                .email(email)
                .name(resolvedName)
                .timestamp(System.currentTimeMillis())
                .build();
            producer.sendUserCreatedEvent(event);
        }

        authMethodRepository.findByUserAndProvider(user, AuthProvider.GOOGLE).orElseGet(() ->
            authMethodRepository.save(
                AuthMethod.builder()
                    .user(user)
                    .provider(AuthProvider.GOOGLE)
                    .build()
            )
        );

        String token = jwtUtil.generateToken(email);
        response.setContentType("text/plain");
        response.getWriter().write(token);
    }
}
