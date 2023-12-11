package immoscraper;

import com.microsoft.playwright.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// TODO: Add feature to display average time per page

public class ScrapeApplication {
  public static final String WILLHABEN_BASE_URL = "https://www.willhaben.at";

  private int successfullyParsed = 0;

  private final Logger logger = LoggerFactory.getLogger(ScrapeApplication.class.getName());

  private final FileWriter fileWriter;

  public ScrapeApplication() {
    try {
      fileWriter = new FileWriter("foo.csv", StandardCharsets.UTF_8);
      fileWriter.write("id;header;squareMeters;roomNumber;location\n");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void runApp() {
    var playwrightInstanceContainer = PlaywrightUtil.initBrowser();
    var currentPage = 1;
    while (true) {
      logger.info("Parsing page " + currentPage);
      var page = initNewPage(playwrightInstanceContainer);
      page.navigate(getCurrentUrl(currentPage));
      scrollDownPage(page);
      var allImmoDivs = getAllImmoDivs(page);
      handleImmoDivs(allImmoDivs);
      currentPage++;
      if (!nextPageExists(page)) break;
      page.close();
    }
    logger.info("Finished parsing. Successfully scraped " + successfullyParsed + " elements.");
    playwrightInstanceContainer.dispose();
  }

  private static Page initNewPage(PlaywrightInstanceContainer playwrightInstanceContainer) {
    return playwrightInstanceContainer.browserContext().newPage();
  }

  private boolean nextPageExists(Page page) {
    return page.getByTestId("pagination-top-next-button").getAttribute("disabled") == null;
  }

  private void handleImmoDivs(@NotNull List<ElementHandle> allImmoDivs) {
    for (var immoDiv : allImmoDivs) {
      var apartments = new ArrayList<ApartmentInfo>();
      if (isProject(immoDiv)) {
        logger.info("Found project, will skip for now");
      } else {
        var apartmentInfo = getApartmentInfo(immoDiv);
        apartments.add(apartmentInfo);
      }
      for (var a : apartments) {
        if (a != null) {
          try {
            writeToFile(a);
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
          successfullyParsed++;
        } else {
          logger.info("Could not parse obj with html " + immoDiv.innerHTML());
        }
      }
    }
  }

  private boolean isProject(ElementHandle immoDiv) {
    // TODO: Fix this, doesnt seem to work
    return immoDiv.querySelector("//a[text()='Zum Projekt']") != null;
  }

  private void writeToFile(ApartmentInfo apartmentInfo) throws IOException {
    var line = apartmentInfo.toCsvLine();
    fileWriter.append(line);
  }

  @Nullable
  private static ApartmentInfo getApartmentInfo(@NotNull ElementHandle immoDiv) {
    var id = getId(immoDiv);
    var header = getHeader(immoDiv);
    var squareMeters = getSquareMeters(immoDiv);
    var roomNumbers = getRommNumber(immoDiv);
    var location = getLocation(immoDiv);
    if (header == null || squareMeters == null || location == null) return null;
    return new ApartmentInfo(id, header, squareMeters, roomNumbers, location);
  }

  @Nullable
  private static String getId(ElementHandle immoDiv) {
    return immoDiv.getAttribute("id");
  }

  @Nullable
  private static String getLocation(ElementHandle immoDiv) {
    return Optional.ofNullable(immoDiv.querySelector("//span[starts-with(@aria-label,'Ort')]"))
        .map(ElementHandle::textContent)
        .orElse(null);
  }

  @Nullable
  private static Integer getRommNumber(ElementHandle immoDiv) {
    return Optional.ofNullable(immoDiv.querySelector("//span[text()='Zimmer']/../span[1]"))
        .map(ElementHandle::textContent)
        .map(Integer::parseInt)
        .orElse(null);
  }

  @Nullable
  private static Integer getSquareMeters(ElementHandle immoDiv) {
    Integer i =
        immoDiv.querySelectorAll("//div/span[2]").stream()
            .filter(f -> f.textContent().length() <= 3)
            .findFirst()
            .map(e -> e.querySelector("../span[1]"))
            .map(ElementHandle::textContent)
            .map(Integer::parseInt)
            .orElse(null);
    return i;
  }

  @Nullable
  private static String getHeader(ElementHandle immoDiv) {
    return Optional.ofNullable(immoDiv.querySelector("//h3"))
        .map(ElementHandle::innerText)
        .orElse(null);
  }

  @NotNull
  private static List<ElementHandle> getAllImmoDivs(Page page) {
    return page.querySelectorAll("//div[@id='skip-to-resultlist']/div/div");
  }

  private static void scrollDownPage(Page page) {
    for (var i = 0; i < 100; i++) {
      page.mouse().wheel(0, 500);
    }
  }

  String getCurrentUrl(int currentPage) {
    int NUM_ROWS = 90;
    String BASE_URL_FORMAT =
        "https://www.willhaben.at/iad/immobilien/mietwohnungen/mietwohnung-angebote?page=%d&rows=%d";
    return String.format(BASE_URL_FORMAT, currentPage, NUM_ROWS);
  }
}
