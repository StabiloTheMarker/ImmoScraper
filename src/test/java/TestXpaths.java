import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public class TestXpaths {

    @Test
    public void shouldGetTitle() {

    }

    @Test
    public void shouldGetSquareMeters() throws IOException {
        try(var inputStream = getClass().getResourceAsStream("willhaben-html.html")){
            assert inputStream != null;
            try(var reader = new BufferedReader(new InputStreamReader(inputStream))) {
                var content = reader.lines().collect(Collectors.joining("\n"));
                
            }
        }
    }
}
