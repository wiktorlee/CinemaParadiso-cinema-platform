package pl.cinemaparadiso.entity.audit.listener;

import org.hibernate.envers.RevisionListener;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import pl.cinemaparadiso.entity.audit.CustomRevisionEntity;
import pl.cinemaparadiso.entity.User;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Listener, który automatycznie ustawia informacje o użytkowniku
 * przy każdej zmianie w audytowanych encjach
 * 
 * Implementuje RevisionListener - interfejs Hibernate Envers
 */
public class AuditRevisionListener implements RevisionListener {

    @Override
    public void newRevision(Object revisionEntity) {
        CustomRevisionEntity revision = (CustomRevisionEntity) revisionEntity;
        
        // Timestamp jest automatycznie ustawiany przez Envers jako Long (milisekundy)
        // Nie musimy go ustawiać ręcznie
        
        // Pobierz informacje o zalogowanym użytkowniku
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.isAuthenticated() 
            && authentication.getPrincipal() instanceof User) {
            User user = (User) authentication.getPrincipal();
            revision.setUserId(user.getId());
            revision.setUserUsername(user.getUsername());
        } else {
            // Jeśli nie ma zalogowanego użytkownika (np. system, migracje)
            revision.setUserId(null);
            revision.setUserUsername("SYSTEM");
        }
        
        // Pobierz IP adres z requestu
        try {
            ServletRequestAttributes attributes = 
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String ipAddress = getClientIpAddress(request);
                revision.setIpAddress(ipAddress);
            }
        } catch (Exception e) {
            // Jeśli nie ma requestu (np. migracje, testy), ustaw null
            revision.setIpAddress(null);
        }
    }

    /**
     * Pobiera rzeczywisty IP adres klienta (uwzględnia proxy, load balancer)
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        
        // Jeśli IP zawiera wiele adresów (przez proxy), weź pierwszy
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        
        return ip;
    }
}

