package eu.cloudbutton.dobj.map;

import lombok.*;
import org.jetbrains.annotations.NotNull;

@Data
public class PowerLawCollisionKey extends AbstractCollisionKey {

    @Override
    public int hashCode() {
        return getHash();
    }

    @Override
    public String toString() {
        return "key#"+getHash();
    }

}
