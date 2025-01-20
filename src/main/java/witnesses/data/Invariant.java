package witnesses.data;

public record Invariant(String type, Location location, String value, String format) {

    @Override
    public String toString() {
        return "Invariant{" +
                "type='" + type + '\'' +
                ", location=" + location +
                ", value='" + value + '\'' +
                ", format='" + format + '\'' +
                '}';
    }
}
