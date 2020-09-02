package analyzer.tests;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.util.Collection;

/**
 * Created: 17-08-02
 * Last Changed: 17-08-02
 * Author: Nicolas Cloutier
 *
 * Description: This is a dummy test, copy this file as a starting point
 * for a new test.
 */

@RunWith(Parameterized.class)
public class DummyTest extends BaseTest {

    // Edit this value for it to match the correct path
    private static String m_test_suite_path = "./test-suite/DummyTest/data";

    public DummyTest(File file) {
        super(file);
    }

    @Test
    public void run() throws Exception {
        // Here is the test which is executed for every file
        // in the folder m_test_suite_path

        // Use this method if you want to want to compare the output
        // of a visitor to the expected file
        // runAndAssert(InsertVisitorHere)

        Assert.assertTrue(true);
    }

    // Do not edit these lines!
    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> getFiles() {
        return getFiles(m_test_suite_path);
    }

}