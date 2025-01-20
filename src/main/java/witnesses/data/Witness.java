package witnesses.data;

import java.util.List;

public record Witness(String entry_type, List<Content> content) {

    @Override
    public String toString() {
        return "Witness{" +
                "entry_type='" + entry_type + '\'' +
                ", content=" + content +
                '}';
    }
}
