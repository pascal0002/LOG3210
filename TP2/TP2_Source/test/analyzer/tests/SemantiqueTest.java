package analyzer.tests;

import analyzer.visitors.SemantiqueVisitor;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.*;
import java.util.*;

import analyzer.ast.ParserVisitor;

@RunWith(Parameterized.class)
public class SemantiqueTest extends BaseTest {

    private static String m_test_suite_path = "./test-suite/SemantiqueTest/data";

    public SemantiqueTest(File file) {
        super(file);
    }

    @Test
    public void run() throws Exception {
        ParserVisitor algorithm = new SemantiqueVisitor(m_output);
        runAndAssert(algorithm);
    }

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> getFiles() {
        return getFiles(m_test_suite_path);
    }

}
