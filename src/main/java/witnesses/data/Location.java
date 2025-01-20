package witnesses.data;

public record Location(String file_name, int line, int column, String function) {

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
