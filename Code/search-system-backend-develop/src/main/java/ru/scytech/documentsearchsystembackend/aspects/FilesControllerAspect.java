package ru.scytech.documentsearchsystembackend.aspects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import ru.scytech.documentsearchsystembackend.security.DomainAccessManager;

import java.util.List;
import java.util.stream.Collectors;

@Aspect
@Profile("secure")
@Component
public class FilesControllerAspect {
    private DomainAccessManager domainAccessManager;

    public FilesControllerAspect(DomainAccessManager domainAccessManager) {
        this.domainAccessManager = domainAccessManager;
    }

    @Around("execution(* ru.scytech.documentsearchsystembackend.controllers.FilesController.*(..))")
    public Object downloadDocProxy(ProceedingJoinPoint joinPoint) throws Throwable {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        var userAuths = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet());
        Object[] args = joinPoint.getArgs();
        if (args.length == 0) {
            var response = (List<String>) joinPoint.proceed();
            return response.stream().filter(it -> {
                var domainAuthorities = domainAccessManager.getDomainAuthorities(it);
                if (domainAuthorities.isEmpty())
                    return true;
                for (var th : userAuths) {
                    if (domainAuthorities.contains(th)) {
                        return true;
                    }
                }
                return false;
            }).collect(Collectors.toList());
        }
        String domain = args[0].toString();
        var domainAuthorities = domainAccessManager.getDomainAuthorities(domain);
        if (domainAuthorities.isEmpty()) {
            return joinPoint.proceed();
        }
        for (var th : userAuths) {
            if (domainAuthorities.contains(th)) {
                return joinPoint.proceed();
            }
        }
        return ResponseEntity.status(403).build();
    }
}
