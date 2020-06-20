package mn.data.pg.domain;

import javax.persistence.*;
import javax.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@SequenceGenerator(name="HIBERNATE_SEQUENCE", initialValue=4)
@Entity(name="USER_DATA")
public class User {

    public static final String DEFAULT_ROLE = "VIEW";

    @Id
    @GeneratedValue
    @Column(name="ID")
    private Long id;
    @Column(name="USERNAME", nullable = false, unique = true)
    @Size(max = 50)
    private String username;
    @Column(name="PASSWORD")
    @Size(max = 50)
    private String password;

    @Builder.Default
    @Column(name="USER_ROLE")
    @Size(max = 10)
    private String role = DEFAULT_ROLE;

    @Column(name="TOKEN", unique = true)
    @Size(max = 100)
    private String token;

}

