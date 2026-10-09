package com.fudn.gateway.filter;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.servlet.function.ServerRequest;

import java.util.function.Function;

public final class UserHeaderFilter {

    public static final String USER_ID = "X-User-Id";
    public static final String USER_EMAIL = "X-User-Email";
    public static final String USER_ROLE = "X-User-Role";

    private UserHeaderFilter() {
    }

    public static Function<ServerRequest, ServerRequest> forwardUserInfo() {
        return request -> {
            // Remove headers provided by the client to prevent spoofing.
            ServerRequest.Builder builder = ServerRequest.from(request)
                    .headers(headers -> {
                        headers.remove(USER_ID);
                        headers.remove(USER_EMAIL);
                        headers.remove(USER_ROLE);
                    });

            // Add trusted user context from the JWT verified by Spring Security.
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth instanceof JwtAuthenticationToken jwtAuth) {
                Jwt jwt = jwtAuth.getToken();
                builder.header(USER_ID, String.valueOf(jwt.getClaims().get("uid")))
                        .header(USER_EMAIL, jwt.getSubject())
                        .header(USER_ROLE, jwt.getClaimAsString("role"));
            }
            return builder.build();
        };
    }
}
