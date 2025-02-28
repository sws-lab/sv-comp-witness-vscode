package witnesses.data.yaml;

import com.fasterxml.jackson.annotation.JsonIgnore;

public record Location(String file_name, @JsonIgnore String file_hash, int line, int column, String function) {

    @Override
    public String toString() {
        return "Location{" +
                "file_name='" + file_name + '\'' +
                ", line=" + line +
                ", column=" + column +
                ", function='" + function + '\'' +
                '}';
    }
}
