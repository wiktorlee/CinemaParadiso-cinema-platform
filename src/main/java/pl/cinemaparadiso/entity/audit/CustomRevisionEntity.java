package pl.cinemaparadiso.entity.audit;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.envers.RevisionEntity;
import org.hibernate.envers.RevisionNumber;
import org.hibernate.envers.RevisionTimestamp;
import pl.cinemaparadiso.entity.audit.listener.AuditRevisionListener;

@Entity
@Table(name = "revinfo")
@RevisionEntity(AuditRevisionListener.class)
@Data
public class CustomRevisionEntity {

    @Id
    @RevisionNumber
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long rev;

    @RevisionTimestamp
    @Column(nullable = false)
    private Long revtstmp;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "user_username")
    private String userUsername;

    @Column(name = "ip_address")
    private String ipAddress;
}

