package mn.data.pg.domain;

import java.time.Instant;

import javax.persistence.*;
import javax.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Entity
public class Message {

    @Id
    @GeneratedValue
    @Column(name="ID")
    private Long id;

    @Column(name="CONTENT")
    @Size(max = 250)
    private String content;

    @Column(name="CREATION_DATE")
    private Instant creationDate;

    @Column(name="USER_REF")
    private String userRef;

}