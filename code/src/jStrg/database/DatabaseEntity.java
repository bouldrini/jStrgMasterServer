package jStrg.database;


import javax.persistence.*;
import java.util.Date;

@MappedSuperclass
public abstract class DatabaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected int m_id;

    @Version
    protected Long version;

    @Temporal(TemporalType.TIMESTAMP)
    protected Date created;
    @Temporal(TemporalType.TIMESTAMP)
    protected Date modified;

    public DatabaseEntity() {
    }

    public Date get_created() {
        return created;
    }

    public void set_created(Date created) {
        this.created = created;
    }

    public int get_id() {
        return m_id;
    }

    public void set_id(int id) {
        this.m_id = id;
    }

    public Date get_modified() {
        return modified;
    }

    public void set_modified(Date modified) {
        this.modified = modified;
    }

    public Long get_version() {
        return version;
    }

    public void set_version(Long version) {
        this.version = version;
    }


    /*
        JPA Helper Methods
     */

    @PrePersist
    void onCreate() {
        this.set_created(new Date());
    }

    @PreUpdate
    void onUpdate() {
        this.set_modified(new Date());
    }

    @Override
    public String toString() {
        return "DatabaseEntity{" +
                "m_id=" + m_id +
                ", created=" + created +
                ", version=" + version +
                ", modified=" + modified +
                '}';
    }



}

