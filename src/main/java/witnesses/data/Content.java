package witnesses.data;

public record Content(Invariant invariant) {

    @Override
    public String toString() {
        return "Content{" +
                "invariant=" + invariant +
                '}';
    }
}
