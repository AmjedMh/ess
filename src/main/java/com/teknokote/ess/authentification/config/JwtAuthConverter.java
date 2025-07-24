package com.teknokote.ess.authentification.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class JwtAuthConverter implements Converter<Jwt, JwtAuthenticationToken>
{

   private final JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();

   @Value("${jwt.auth.converter.principle-attribute}")
   private String principalAttribute;

   @Value("${jwt.auth.converter.resource-id}")
   private String resourceId;

   @Override
   public JwtAuthenticationToken convert(Jwt jwt) {
      Set<GrantedAuthority> authorities = Stream.of(
                      jwtGrantedAuthoritiesConverter.convert(jwt),
                      extractResourceRole(jwt)
              )
              .filter(Objects::nonNull) // Filter out null values
              .flatMap(Collection::stream)
              .collect(Collectors.toSet());

      return new JwtAuthenticationToken(jwt, authorities, getPrincipalClaimName(jwt));
   }


   private String getPrincipalClaimName(Jwt jwt)
   {
      String claimName = JwtClaimNames.SUB;
      if(principalAttribute != null){
         claimName = principalAttribute;
      }

      return jwt.getClaim(claimName);
   }

   private Collection<? extends GrantedAuthority> extractResourceRole(Jwt jwt) {
      Map<String, Object> realmAccess = jwt.getClaim("realm_access");
      if (realmAccess != null) {
         List<String> roles = (List<String>) realmAccess.get("roles");
         if (roles != null) {
            return roles.stream().map(role -> new SimpleGrantedAuthority("ROLE_" + role)).toList();
         }
      }
      return Collections.emptyList();
   }

}
