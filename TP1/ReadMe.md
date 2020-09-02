# JavaCC Template for Intellij Idea and AnT

This template is a simple project that integrate correctly JavaCC to an IDE. You can now finally work in Intellij and simply press the Compile/Run buttons and enjoy. To make it works, an Ant step has been added before the compilation. Finally, the support for the visitors is present by default and a test-suite using JUnit is present and make it easy to test your parser.

## Getting Started

The easiest way to get started is to simply open this project in Intellij Idea and the project should be ready to run.

To open the project for the first time:
- Open Intellij Idea
- At the top-left corner of your screen, click on File -> Open
- In the opened file browser, search and select the folder of the project (which contains this ReadMe file)
- Open the project, you should now see a notification at the bottom right of your screen for a missing plugin, follow the procedures to install it
- You are now ready to work!

If you don't have Intellij installed, you will still need to install the correct version of JUnit and hamcrest and update the paths in the build.xml file. After this simply use the make file (**make compile/run/all/test**)

### Prerequisites

What things you need to install the software and how to install them

```
JavaCC 1.7 (Included in the libs folder)
Intellij Idea
JUnit 4.12 (Included in Intellij)
Hamcrest 1.3 (Included in Intellij)
```

## Project structure

- **.idea/** : Contains specifics files for the intellij project. Do not touch this.
- **gen-src/** : Contains java files that are generated. Every time you compile, this folder is rewritten. If you want to edit theses files, copy them to **/src/analyzer/ast/** and they will no longer be generated.
- **libs/** : Contains the javacc lib. Check this folder if you want to see documentations. You could move out this library of your project if you have multiples projects.
- **out/** : Contains the compiled program. **out/production/** contains the main program and **out/test/** contains the test suite.
- **src/** : Contains the source code for the java files.
- **test/** : Contains the source code for the tests. Read the description of BaseTest for more details.
- **test-suite/** : Contains the test cases for the tests.
- **.gitignore** : The files to ignore if you use git
- **build.xml** : The ant build file, if you use intellij, it will be helpful to generate the JavaCC files at the pre-compile step. And if you use the command line this file is helpful for everything.
- **LICENSE.txt** : The MIT License of this project
- **Makefile** : Simply redirect to the build.xml file.
- **ReadMe.md** : Hmmm, what is the use of this file? Nobody read this.
- **Template.iml** : The idea project file
- **Template.jjt** : **Important** ! This file is the JavaCC grammar file which generate everything in the gen-src/ folder!

## Running the tests

To test your program in Intellij, you have two options. You can run/debug the Main.main() on a specific file or you can execute the tests.

If you want to simply execute the parser on a file, edit the run configurations of Main.main to add a program argument corresponding to the path of the file you want to analyze.

If you want to run all tests, you can simply right click the **test** folder (which is supposed to be green) in intellij and press **Run all tests**.

If you don't want to use Intellij, you can still execute the tests with the command **make test**.

## Built With

* [JavaCC](https://javacc.org/doc) - The Parser
* [Ant](http://ant.apache.org/) - The pre-compile steps
* [JUnit](http://junit.org/junit4/) - The test library

## Contributing

If you want to contribute or report an issue, you are welcomed. Please use the [github repository](https://github.com/Nic007/JavaCC-Template).

## Authors

* **Nicolas Cloutier** - *Initial work* - [Nic007](https://github.com/Nic007)

See also the list of [contributors](https://github.com/your/project/contributors) who participated in this project.

## License

This project is licensed under the MIT License - see the [LICENSE.txt](LICENSE.txt) file for details

## Acknowledgments

* Thanks for **Polytechnique Montréal** for investing in this project for the course LOG3210.
* **Ettore Merlo** for the theory behind this project.