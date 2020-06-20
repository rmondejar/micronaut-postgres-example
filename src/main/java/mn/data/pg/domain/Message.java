package mn.data.pg.domain;

import java.time.LocalDateTime;

import javax.persistence.*;
import javax.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@SequenceGenerator(name="HIBERNATE_SEQUENCE", initialValue=4)
@Entity(name="MESSAGE")
public class Message {

    @Id
    @GeneratedValue
    @Column(name="ID")
    private Long id;

    @Column(name="CONTENT")
    @Size(max = 250)
    private String content;

    @Column(name="CREATION_DATE")
    @CreationTimestamp
    private LocalDateTime creationDate;

    @ManyToOne(targetEntity = User.class)
    @JoinColumn(name = "USER_REF")
    private User userRef;

}