package com.versionone.hudson;

/*
import com.gargoylesoftware.htmlunit.ElementNotFoundException;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import hudson.model.Descriptor;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.tasks.BatchFile;
import hudson.tasks.Publisher;
import hudson.util.DescribableList;
import org.apache.commons.io.FileUtils;
import org.jvnet.hudson.test.HudsonTestCase;
import org.xml.sax.SAXException;
import org.junit.Ignore;

import java.io.IOException;
*/


//public class AppTest extends HudsonTestCase {

    /*
	public void test1() throws Exception {
		FreeStyleProject project = createFreeStyleProject();
		project.getBuildersList().add(new BatchFile("echo hello"));
//		List<Action> actions = project.getActions();
		DescribableList<Publisher, Descriptor<Publisher>> publishers = project.getPublishersList();
		VersionOneNotifier versionOneNotifier = new VersionOneNotifier();
//		VersionOneNotifier.DESCRIPTOR.configure()
		publishers.add(versionOneNotifier);

		FreeStyleBuild build = project.scheduleBuild2(0).get();
		System.out.println(build.getDisplayName() + " completed");

		// TODO: change this to use HtmlUnit
		String s = FileUtils.readFileToString(build.getLogFile());
		assertTrue(s.contains("echo hello"));
	}
	*/

    /*
    @Ignore
	public void test2() throws IOException, SAXException {
		final HtmlPage page = new WebClient().goTo("configure");
		assertElementPresentByName(page, "com-versionone-hudson-VersionOneNotifier");
		int j = 3;
		int i = ((2009 - 2004) + j) > 1 ? 0 : 1;
	}
	*/

	/**
	 * Verifies that the specified page contains an element with the specified ID.
	 *
	 * @param page the page to check
	 * @param name the expected Name of an element in the page
	 */
    /*
	public static void assertElementPresentByName(final HtmlPage page, final String name) {
		try {
			page.getHtmlElementsByName(name);
		} catch (final ElementNotFoundException e) {
			throw new AssertionError("The page does not contain an element with name '" + name + "'.");
		}
	}
	*/
//}
