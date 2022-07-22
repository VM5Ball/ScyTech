package ru.scytech.documentsearchsystembackend.services;

import lombok.EqualsAndHashCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.stream.Collectors;

@EqualsAndHashCode
class StringGrantedAuthority implements GrantedAuthority {
    private String authority;

    public static StringGrantedAuthority of(String authority) {
        return new StringGrantedAuthority(authority);
    }

    public StringGrantedAuthority(String authority) {
        this.authority = authority;
    }


    @Override
    public String getAuthority() {
        return authority;
    }
}

@Service
@Profile("secure")
public class SecurityRepository {
    private Map<String, UsernamePasswordAuthenticationToken> users;
    private Set<StringGrantedAuthority> roles;
    private PasswordEncoder passwordEncoder;

    public SecurityRepository(@Value("${application.security.config}") String secureConfig,
                              PasswordEncoder passwordEncoder) throws FileNotFoundException {
        this.passwordEncoder = passwordEncoder;
        users = new HashMap<>();
        roles = new HashSet<>();
        roles.add(StringGrantedAuthority.of("ADMIN"));
        FileInputStream fileInputStream = new FileInputStream(secureConfig);
        Scanner scanner = new Scanner(fileInputStream);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            var columns = line.split(" ");
            if (columns.length < 3) {
                throw new IllegalArgumentException("line \"" + line + "\" is invalid!");
            }

            String userName = columns[0];
            String password = columns[1];
            Set<StringGrantedAuthority> userRoles = new HashSet<>();
            for (int i = 2; i < columns.length; i++) {
                userRoles.add(StringGrantedAuthority.of(columns[i]));
            }
            roles.addAll(userRoles);
            users.put(userName, new UsernamePasswordAuthenticationToken(userName, passwordEncoder.encode(password), userRoles));
        }
    }

    public Optional<UsernamePasswordAuthenticationToken> getUserByName(String userName) {
        return Optional.ofNullable(users.get(userName));
    }

    public boolean containsAuthority(String name) {
        return roles.contains(StringGrantedAuthority.of(name));
    }

    public void addUser(String userName, String password, List<String> roles) {
        var converted = roles.stream().map(it -> StringGrantedAuthority.of(it)).collect(Collectors.toList());
        users.put(userName, new UsernamePasswordAuthenticationToken(userName, passwordEncoder.encode(password), converted));
    }


}
