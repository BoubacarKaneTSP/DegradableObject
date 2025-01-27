package eu.cloudbutton.dobj.utils;

import java.util.Objects;

public class SegmentableString extends BaseSegmentable {

    private String s;

    public SegmentableString(String s){
        this.s = s;
    }

    public String getS() {
        return s;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SegmentableString that = (SegmentableString) o;
        return Objects.equals(s, that.s);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(s);
    }
}
