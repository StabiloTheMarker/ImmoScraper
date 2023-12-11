package immoscraper;

import java.net.URL;

public class ParseImmoRunnable implements Runnable {
  private final URL urlToParse;

  public ParseImmoRunnable(URL urlToParse) {
    this.urlToParse = urlToParse;
  }

  @Override
  public void run() {
    var playwrightContainer = PlaywrightUtil.initBrowser();
    var page = playwrightContainer.browserContext().newPage();
    page.navigate(urlToParse.toString());
    var mietPreis = page.querySelector("//section[3]//span[text()='Gesamtmiete inkl. MWSt']/parent::div/span[1]").innerText();
    System.out.println(mietPreis);
    playwrightContainer.dispose();
  }
}
