package ru.scytech.documentsearchsystembackend.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/security")
@Profile("secure")
public class CsrfTokenController {
    @GetMapping("/csrfToken")
    public CsrfTokenWithName get(HttpServletRequest request, SecurityProperties.User user) {
        var csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return new CsrfTokenWithName(csrfToken.getParameterName(),
                csrfToken.getToken(),
                csrfToken.getHeaderName(),
                authentication.getName(),
                authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).anyMatch(it -> it.equals("ADMIN")));

    }

    @GetMapping("/role")
    public List<String> getUserRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList());
    }
}

@Data
@AllArgsConstructor
class CsrfTokenWithName {
    private String parameterName;
    private String token;
    private String headerName;
    private String name;
    private boolean isAdmin;
}