package com.ringlesoft.visualenv;

import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlFile;
import com.intellij.testFramework.TestDataPath;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.intellij.util.PsiErrorElementUtil;
import com.ringlesoft.visualenv.services.ProjectService;

/**
 * Test class for the Visual Env plugin.
 */
@TestDataPath("$CONTENT_ROOT/src/test/testData")
public class PluginTest extends BasePlatformTestCase {

    public void testXMLFile() {
        PsiFile psiFile = myFixture.configureByText(XmlFileType.INSTANCE, "<foo>bar</foo>");
        XmlFile xmlFile = assertInstanceOf(psiFile, XmlFile.class);

        assertFalse(PsiErrorElementUtil.hasErrors(getProject(), xmlFile.getVirtualFile()));
        assertNotNull(xmlFile.getRootTag());

        if (xmlFile.getRootTag() != null) {
            assertEquals("foo", xmlFile.getRootTag().getName());
            assertEquals("bar", xmlFile.getRootTag().getValue().getText());
        }
    }

    public void testRename() {
        myFixture.testRename("foo.xml", "foo_after.xml", "a2");
    }

    public void testProjectService() {
        ProjectService projectService = getProject().getService(ProjectService.class);
        assertNotSame(projectService.getRandomNumber(), projectService.getRandomNumber());
    }

    @Override
    protected String getTestDataPath() {
        return "src/test/testData/rename";
    }
}
