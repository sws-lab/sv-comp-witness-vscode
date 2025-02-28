package witnesses.data.yaml;

import com.fasterxml.jackson.annotation.JsonIgnore;
import witnesses.data.yaml.Content;
import witnesses.data.yaml.MetaData;

import java.util.List;

public record Witness(String entry_type, @JsonIgnore MetaData metadata, List<Content> content) {

    @Override
    public String toString() {
        return "Witness{" +
                "entry_type='" + entry_type + '\'' +
                ", content=" + content +
                '}';
    }
}
