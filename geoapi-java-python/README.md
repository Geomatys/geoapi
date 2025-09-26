# Test execution

It is recommended to create a dedicated virtual environment:

```bash
python -m venv .venv
source .venv/bin/activate
```

Ensure that the `JAVA_HOME` environment variable is set to the path to valid java JDK.
Then ensure that the project is installed:

```bash
pip install -e ".[test,dev]"
```

Then run test. By default, `pytest` searches for tests in the `*/tests` directory.

```bash
pytest
```

Note: in order to run, the tests of the Java-Python bridge require Python to start a JVM
with classes of the module and its dependencies referenced in the `classpath`.
Currently, the classpath is set manually in the  `src/test/python/tests/__init__.py` file.

If classes are not found when trying to access a java class from Python,
check that the requested class **and** its imported dependencies are present on the JVM's classpath.

[//]: # (export JVM_DLL=$JAVA_HOME/lib/server/libjvm.so)
