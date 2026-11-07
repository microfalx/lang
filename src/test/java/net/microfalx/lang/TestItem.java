package net.microfalx.lang;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import static net.microfalx.lang.StringUtils.toIdentifier;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestItem implements Identifiable<String> {

    private String id;
    private String firstName;
    private String lastName;
    private int age;

    @Override
    public String getId() {
        return id != null ? id : toIdentifier(firstName + ":" + lastName);
    }
}
