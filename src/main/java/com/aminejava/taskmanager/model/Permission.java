package com.aminejava.taskmanager.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.parameters.P;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Data
@NoArgsConstructor
public class Permission {
    @Id
    @Column(name = "permession_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String permission;

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o == this) {
            return true;
        }
        if (getClass() != o.getClass()) {
            return false;
        }

        Permission permission1 = (Permission) o;

        return (getPermission() != null && Objects.equals(getPermission(), permission1.getPermission()));
    }

    @Override
    public int hashCode() {
        final int PRIME = 31;
        int result = 32;
        result = PRIME * getPermission().length() * result;
        return result;
    }

}
