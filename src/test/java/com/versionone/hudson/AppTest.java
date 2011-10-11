package com.versionone.hudson;

import com.gargoylesoftware.htmlunit.ElementNotFoundException;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import hudson.model.Descriptor;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.tasks.Publisher;
import hudson.tasks.Shell;
import hudson.util.DescribableList;
import org.apache.commons.io.FileUtils;
import org.jvnet.hudson.test.HudsonTestCase;

public class AppTest extends HudsonTestCase {

/*  //TODO review and fix it
    public void test1() throws Exception {
        FreeStyleProject project = createFreeStyleProject();
        DescribableList<Publisher, Descriptor<Publisher>> publishers = project.getPublishersList();
        VersionOneNotifier versionOneNotifier = new VersionOneNotifier();
        versionOneNotifier.getDescriptor().setData("http://fake_domen/VersionOne/", "admin", "admin", "[A-Z]{1,2}-[0-9]+", "Number");
        publishers.add(versionOneNotifier);

        FreeStyleBuild build = project.scheduleBuild2(0).get();
        System.out.println(build.getDisplayName() + " completed");

        String s = FileUtils.readFileToString(build.getLogFile());
        assertTrue(s.contains("VersionOne:"));
    }
*/

/*
    public void test2() throws IOException, SAXException {
        final HtmlPage page = new WebClient().goTo("configure");
        assertElementPresentByName(page, "com-versionone-hudson-VersionOneNotifier");
    }
*/

    /**
     * Verifies that the specified page contains an element with the specified ID.
     *
     * @param page the page to check
     * @param name the expected Name of an element in the page
     */
    public static void assertElementPresentByName(final HtmlPage page, final String name) {
        try {
            page.getElementsByName(name);
        } catch (final ElementNotFoundException e) {
            throw new AssertionError("The page does not contain an element with name '" + name + "'.");
        }
    }
}