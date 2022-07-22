package ru.scytech.documentsearchsystembackend.security;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import ru.scytech.documentsearchsystembackend.services.SecurityRepository;
import ru.scytech.documentsearchsystembackend.services.interfaces.DocumentAccessService;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;

@Service
@Profile("secure")
public class DomainAccessManager {
    private Map<String, Set<String>> domainsAuthorities;
    private String DOMAIN_AUTHORITIES_FILE = ".secureconfig";
    private SecurityRepository securityRepository;

    public DomainAccessManager(DocumentAccessService documentAccessService,
                               SecurityRepository securityRepository) throws IOException {
        domainsAuthorities = new HashMap<>();
        this.securityRepository = securityRepository;
        var repository = documentAccessService.loadDocsRepository(true);
        for (var domain : repository.keySet()) {
            Set<String> filenames = repository.get(domain);
            Set<String> authoritiesSet = new HashSet<>();
            if (filenames.contains(DOMAIN_AUTHORITIES_FILE)) {
                byte[] bytes = documentAccessService.getFile(DOMAIN_AUTHORITIES_FILE, domain);
                Scanner scanner = new Scanner(new ByteArrayInputStream(bytes));

                while (scanner.hasNextLine()) {
                    String[] authorities = scanner.nextLine().split(";");
                    for (var authority : authorities) {
                        if (!securityRepository.containsAuthority(authority)) {
                            throw new IllegalArgumentException("unknown role " + authority);
                        }
                        authoritiesSet.add(authority);
                    }
                }
            }
            domainsAuthorities.put(domain, authoritiesSet);
        }
    }

    public Set<String> getDomainAuthorities(String domain) {
        if (!domainsAuthorities.containsKey(domain)) {
            throw new IllegalArgumentException("not found domain " + domain);
        }
        return domainsAuthorities.get(domain);
    }

    public void addDomainAuthorities(String domain, List<String> authorities) {
        if (!domainsAuthorities.containsKey(domain)) {
            throw new IllegalArgumentException("not found domain " + domain);
        }
        domainsAuthorities.get(domain).addAll(authorities);
    }
}
