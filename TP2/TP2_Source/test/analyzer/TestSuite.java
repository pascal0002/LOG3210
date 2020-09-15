package analyzer;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import analyzer.tests.*;

/**
 * Created: 17-08-09
 * Last Changed: 17-08-09
 * Author: Nicolas Cloutier
 *
 * Description: This is the class holding all the different tests.
 * If you want to add a test, simply add it in this file
 */

@RunWith(Suite.class)

@Suite.SuiteClasses({
        SemantiqueTest.class,
})

public class TestSuite {
}
