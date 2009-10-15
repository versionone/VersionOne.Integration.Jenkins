package com.versionone.hudson;

import org.junit.Test;
import org.jvnet.hudson.test.HudsonTestCase;
import org.xml.sax.SAXException;

import java.io.IOException;


public class AppTest extends HudsonTestCase {
	@Test
	public void test1() throws Exception {
//        FreeStyleProject project = createFreeStyleProject();
//        project.getBuildersList().add(new Shell("echo hello"));
//		List<Action> actions = project.getActions();
//		DescribableList<Publisher, Descriptor<Publisher>> publishers = project.getPublishersList();
//		VersionOneNotifier versionOneNotifier = new VersionOneNotifier();
//		VersionOneNotifier.DESCRIPTOR.configure()
//		publishers.add(versionOneNotifier);

//        FreeStyleBuild build = project.scheduleBuild2(0).get();
//        System.out.println(build.getDisplayName()+" completed");

		// TODO: change this to use HtmlUnit
//        String s = FileUtils.readFileToString(build.getLogFile());
//        assertTrue(s.contains("+ echo hello"));
	}

	@Test
	public void test2() throws IOException, SAXException {
//		HtmlPage page = new WebClient().goTo("configure");
//		WebAssert.assertElementPresent(page, "VersionOne Integration");
//		WebAssert.assertElementPresent(page, "hudson-scm-CVSSCM");
	}
}
