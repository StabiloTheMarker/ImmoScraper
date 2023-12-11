package immoscraper;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record ApartmentInfo(
    String id, String header, Integer squareMeters, Integer roomNumber, String location) {
  public String toCsvLine() {
    return Stream.of(id, header, squareMeters, roomNumber, location)
            .map(String::valueOf)
            .collect(Collectors.joining(";"))
        + "\n";
  }
}
