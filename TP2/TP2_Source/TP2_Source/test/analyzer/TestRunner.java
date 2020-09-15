package analyzer;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

/**
 * Created: 17-08-15
 * Last Changed: 17-08-15
 * Author: Nicolas Cloutier
 *
 * Description: This is the entry point for the tests if you want
 * to launch the tests from the command line.
 */

public class TestRunner {
    public static void main(String[] args) {
        Result r = JUnitCore.runClasses(TestSuite.class);
        if(r.wasSuccessful()) {
            System.out.println("All " + Integer.toString(r.getRunCount())
                    + " test have passed.");
        }
        else {
            for(Failure f : r.getFailures()) {
                System.out.println(f.toString());
            }
            System.exit(-1);
        }
    }
}
