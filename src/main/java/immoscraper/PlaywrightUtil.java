package immoscraper;

import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;

import static immoscraper.ScrapeApplication.WILLHABEN_BASE_URL;


public final class PlaywrightUtil {
    public static PlaywrightInstanceContainer initBrowser() {
        var playwrightInstance = Playwright.create();
        var browser =
                playwrightInstance.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
        var browserContext = browser.newContext();
        var page = browserContext.newPage();
        page.navigate(WILLHABEN_BASE_URL);
        page.click("//span[text() = 'Cookies akzeptieren']");
        page.close();
        return new PlaywrightInstanceContainer(playwrightInstance, browser, browserContext);
    }
}
