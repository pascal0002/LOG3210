package analyzer.tests;

import analyzer.ast.ParserVisitor;
import analyzer.visitors.IntermediateCodeGenFallVisitor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.util.Collection;

@RunWith(Parameterized.class)
public class IntermediateCodeGenFallTest extends BaseTest {

    private static String m_test_suite_path = "./test-suite/IntermediateCodeGenFallTest/data";

    public IntermediateCodeGenFallTest(File file) {
        super(file);
    }

    @Test
    public void run() throws Exception {
        ParserVisitor algorithm = new IntermediateCodeGenFallVisitor(m_output);
        runAndAssert(algorithm);
    }

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> getFiles() {
        return getFiles(m_test_suite_path);
    }

}
