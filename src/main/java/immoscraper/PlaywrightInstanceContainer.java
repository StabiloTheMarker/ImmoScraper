package immoscraper;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Playwright;

public record PlaywrightInstanceContainer(
        Playwright playwright, Browser browser, BrowserContext browserContext) {
    public void dispose() {
        browserContext.close();
        browser.close();
        playwright.close();
    }
}
