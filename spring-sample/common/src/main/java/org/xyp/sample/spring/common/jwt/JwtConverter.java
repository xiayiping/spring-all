package org.xyp.sample.spring.common.jwt;

import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.SignedJWT;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.xyp.function.wrapper.ResultOrError;

import java.util.List;
import java.util.Optional;

@Slf4j
@AllArgsConstructor
public class JwtConverter {

    private static final String DEFAULT_SECRET = "01234567890123456789012345678901";
    public static final String ROLE_PREFIX = "ROLE_";

    private final String jwtSecret;

    public JwtAuthentication convertJwtToken(String token) {
        val signedJWT = ResultOrError.on(() -> SignedJWT.parse(token))
            .getOrSpecError(IllegalArgumentException.class, IllegalArgumentException::new);

        val verified = ResultOrError.on(() -> new MACVerifier(Optional.ofNullable(jwtSecret).orElse(DEFAULT_SECRET)))
            .map(signedJWT::verify)// Create HMAC verifier
            .getOrSpecError(IllegalArgumentException.class, IllegalArgumentException::new);

        if (verified) {
            return ResultOrError.on(() -> {
                val claimsSet = signedJWT.getJWTClaimsSet();

                @SuppressWarnings("unchecked")
                val roles = ((List<String>) claimsSet.getClaim("roles"))
                    .stream()
                    .map(r -> ROLE_PREFIX + r)
                    .map(SimpleGrantedAuthority::new)
                    .toList();

                return new JwtAuthentication(
                    token,
                    Long.parseLong(claimsSet.getSubject()),
                    claimsSet.getStringClaim("name"),
                    roles
                );
            }).getOrSpecError(IllegalArgumentException.class, IllegalArgumentException::new);
        } else {
            throw new IllegalArgumentException();
        }
    }
}