package analyzer.tests;

import analyzer.ast.ParserVisitor;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;

import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.ArrayList;
import java.util.Collection;

import static org.junit.Assert.fail;

/**
 * Created: 17-08-02
 * Last Changed: 17-08-02
 * Author: Nicolas Cloutier
 *
 * Description: This is a base class for our tests.
 * It has different features:
 *  - The test will be run on a visitor // TODO:: Multiples visitors?
 *  - Execute the test for every file present in test-suite/@TestName/data
 *  - Compare the output with a file present in test-suite/@TestName/expected
 *  - Print the result in test-suite/@TestName/result for debug purpose
 */

public class BaseTest {

    protected FileInputStream m_input;
    protected PrintWriter m_output;
    protected String m_expected;

    private final File m_file;

    public BaseTest(File file) {
        m_file = file;
    }

    // Get all the files from the base path and insert it in a collection
    // to be sent as parameters for the tests constructors
    public static Collection<Object[]> getFiles(String basePath) {
        Collection<Object[]> paramsForAllTests = new ArrayList<>();
        for (File file : new File(basePath).listFiles()) {
            paramsForAllTests.add(new Object[] { file });
        }
        return paramsForAllTests;
    }

    // At the creation of the test, this function prepare all the files
    @Before
    public void prepare() throws Exception {
        // Prepare
        String name = m_file.getName();
        String path = m_file.getParentFile().getParent();

        Path expectedPath = Paths.get(path + "/expected/" + name);
        Path resultPath = Paths.get(path + "/result/" + name);

        Assume.assumeTrue("Expected " + expectedPath + " does not exist",
                Files.exists(expectedPath));

        m_input = new FileInputStream(m_file);
        m_output = new PrintWriter(resultPath.toString());

        m_expected = new String(Files.readAllBytes(expectedPath));
        m_expected = m_expected.replaceAll("\\r", "");
    }

    // This is magic function which run algorithm on the input file,
    // print the output in the output file and assert if it's matching
    // the expect file
    public void runAndAssert() throws Exception {
        // Run
        try{
            analyzer.Main.Run(m_input, m_output);
            m_output.flush();
        }

        // Assert
        catch (Exception ex) {
            // If we didn't expected this test to crash
            if(!ex.getMessage().contains(m_expected))
            {
                fail(ex.getMessage());
            }

            return;
        }

        String name = m_file.getName();
        String path = m_file.getParentFile().getParent();

        Path resultPath = Paths.get(path + "/result/" + name);

        String result = new String(Files.readAllBytes(resultPath));

        result = result.replaceAll("\\r", "");

        Assert.assertEquals(m_expected, result);
    }

}
