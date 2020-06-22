package mn.data.pg.dtos;

import java.time.Instant;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.micronaut.core.annotation.Introspected;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@JsonIgnoreProperties({"user"})
@Introspected
public class MessageDto {

    @NotNull
    private Long id;
    @NotBlank
    private String content;
    @NotNull
    private Instant creationDate;
    @NotNull
    private UserDto user;

    public String getUsername() {
        return user.getUsername();
    }
}
