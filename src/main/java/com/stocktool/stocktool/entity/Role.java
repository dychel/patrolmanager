package com.stocktool.stocktool.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
@Entity
@NoArgsConstructor

public class Role implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String roleName;
    private Date dateCreate;
    private Date dateUpdate;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "role")
    @JsonIgnore
    private List<User> users;

    public Role(String roleName, Date dateCreate, Date dateUpdate) {
        this.roleName = roleName;
        this.dateCreate = dateCreate;
        this.dateUpdate = dateUpdate;
    }

    @Override
    public String toString() {
        return "Role{" +
                "id=" + id +
                ", roleName='" + roleName + '\'' +
                ", dateCreate=" + dateCreate +
                ", dateUpdate=" + dateUpdate +
                '}';

/*   Ne pas inclure la liste des users pour eviter l 'erreur StackOverflowError
     evitant ainsi les appels récursifs entre les méthodes toString() des deux classes.
 */
    }
}