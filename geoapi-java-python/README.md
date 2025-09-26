# Readme

It is recommended to create a dedicated virtual environment:

```bash
python -m venv .venv
source .venv/bin/activate
```

Ensure JAVA_HOME is set and target a valid java jdk (tested on java 24.0.2).

For test execution:
Ensure the project is installed :

```bash
pip install -e ".[test,dev]"
```

then run test (by default search tests in */tests directory)

```bash
pytest
```

Note ; in order to run, the tests of the python bridge require python to start a JVM with classes of the module and its
dependencies referenced in the `classpath`.
Currently, the classpath is set manually in the  `src/test/python/tests/__init__.py` file.

On class not found error when trying to access a java class from python, check that the requested class AND its imported
dependencies are well added to the jvm's classpath.

[//]: # (export JVM_DLL=$JAVA_HOME/lib/server/libjvm.so)