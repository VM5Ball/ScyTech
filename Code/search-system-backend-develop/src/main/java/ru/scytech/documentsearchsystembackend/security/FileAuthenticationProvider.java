package ru.scytech.documentsearchsystembackend.security;

import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.scytech.documentsearchsystembackend.services.SecurityRepository;

@Service
@Profile("secure")
public class FileAuthenticationProvider implements AuthenticationProvider {
    private SecurityRepository securityRepository;
    private PasswordEncoder passwordEncoder;

    public FileAuthenticationProvider(SecurityRepository securityRepository, PasswordEncoder passwordEncoder) {
        this.securityRepository = securityRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String userName = authentication.getName();
        String password = authentication.getCredentials().toString();
        var userOptional = securityRepository.getUserByName(userName);
        if (userOptional.isEmpty()) {
            throw new UsernameNotFoundException("User not found!");
        }
        UsernamePasswordAuthenticationToken user = userOptional.get();
        if (passwordEncoder.matches(password, user.getCredentials().toString())) {
            return new UsernamePasswordAuthenticationToken(user.getName(), user.getCredentials(), user.getAuthorities());
        }
        throw new BadCredentialsException("Invalid credentials");
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}
