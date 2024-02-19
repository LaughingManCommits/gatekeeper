package com.gate.keeper.controller;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserInteractionTests {

    public static final BrowserType.LaunchOptions LAUNCH_OPTIONS = new BrowserType.LaunchOptions();//.setHeadless(false).setDevtools(true).setSlowMo(500);
    @LocalServerPort
    private int port;

    @Test
    @DisplayName("User tries to authenticates valid username and password")
    public void test1() {
        String home = "http://localhost:" + port + "/home";
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(LAUNCH_OPTIONS);
            BrowserContext context = browser.newContext();
            Page page = context.newPage();
            page.navigate(home);
            page.waitForLoadState(LoadState.DOMCONTENTLOADED);
            page.pause();
            page.locator("div .formcontainer").waitFor();
            page.locator("button", new Page.LocatorOptions().setHasText("/Level1/page.html")).click();
            page.locator("id=usr").waitFor();
            page.locator("id=psw").waitFor();
            page.locator("id=usr").fill("1#bob");
            page.locator("id=psw").fill("12341");
            page.locator("button", new Page.LocatorOptions().setHasText("Login")).click();
            page.locator("p", new Page.LocatorOptions().setHasText("authenticated")).waitFor();
            page.locator("button", new Page.LocatorOptions().setHasText("/Level1/page.html")).click();
            page.locator("p", new Page.LocatorOptions().setHasText("Sponge bob")).waitFor();
        }
    }

    @Disabled
    @Test
    @DisplayName("User tries to authenticate with wrong username or password")
    void test2()  {
        throw new RuntimeException("todo create tests for me");
    }

}
