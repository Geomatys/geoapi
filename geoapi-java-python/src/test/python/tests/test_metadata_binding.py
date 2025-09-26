#     GeoAPI - Java interfaces for OGC/ISO standards
#        Copyright © 2009-2023 Open Geospatial Consortium, Inc.
#     http://www.geoapi.org
#
#     Licensed under the Apache License, Version 2.0 (the "License");
#     you may not use this file except in compliance with the License.
#     You may obtain a copy of the License at
#
#         http://www.apache.org/licenses/LICENSE-2.0
#
#     Unless required by applicable law or agreed to in writing, software
#     distributed under the License is distributed on an "AS IS" BASIS,
#     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#     See the License for the specific language governing permissions and
#     limitations under the License.

import jpy

import unittest
import logging

logger = logging.getLogger(__name__)

class TestMetadata(unittest.TestCase):


    def test_jvm(self):
        """
        Test that the jvm is well initialized and that classes can be loaded.
        :return:
        """
        java_system = jpy.get_type("java.lang.System")
        self.assertIsNotNone(java_system, "Java system not loaded")
        jRuntime = jpy.get_type("java.lang.Runtime")
        self.assertIsNotNone(jRuntime, "Java Runtime class not loaded")
        java_version = jRuntime.version()
        self.assertIsNotNone(java_version, "Java version of initialized jvm is None.")

        # Load java standard Math class
        jMath = jpy.get_type("java.lang.Math")
        self.assertIsNotNone(jMath, "Java Math class not loaded")
        self.assertEqual(jMath.cos(0), 1, "Java Math cos fails to compute cos(0)")

    def test_metadata(self):

        # Get java ClassLoader to check tested java classes are well configured in running JVM
        class_loader = jpy.get_type("java.lang.ClassLoader")
        self.assertIsNotNone(class_loader, "Java class loader not loaded.")
        # Get the system class loader (the one with your classpath)
        system_loader = class_loader.getSystemClassLoader()
        self.assertIsNotNone(system_loader, "Java class loader not loaded.")

        # Check access to project's java source ; If it fails ensure classpath is well set in ./init.py
        cls_url = system_loader.getResource("org/opengis/bridge/python/PythonHelper.class")
        self.assertIsNotNone(cls_url, "PythonHelper class not accessible in jvm classpath.")
        cls = system_loader.loadClass("org.opengis.bridge.python.PythonHelper")
        self.assertIsNotNone(cls, "PythonHelper class could not be loaded.")
        logger.info("PythonHelper loaded successfully : " +str(cls))

        # Check access to project's java test source ; If it fails ensure classpath is well set in ./init.py
        cls_url = system_loader.getResource("org/opengis/bridge/python/PythonBridgeHelper.class")
        self.assertIsNotNone(cls_url, "PythonBridgeTestHelper class not accessible in jvm classpath.")
        cls = system_loader.loadClass("org.opengis.bridge.python.PythonBridgeHelper")
        self.assertIsNotNone(cls, "PythonBridgeTestHelper class could not be loaded.")
        logger.info("PythonBridgeTestHelper loaded successfully : " +str(cls))

        # Check access to geoapi core module
        metadata_class = jpy.get_type('org.opengis.metadata.Metadata')
        logger.info("Metadata loaded successfully from geoapi dependency : " +str(metadata_class))

        self._test_helper = jpy.get_type('org.opengis.bridge.python.PythonBridgeHelper')
        self.assertIsNotNone(self._test_helper, "Failed to get python binding for PythonBridgeHelper class.")
        logger.info("Java PythonBridgeTestHelper helper class accessed from python.")


        metadata_ = self._test_helper.metadata()
        self.assertIsNotNone(metadata_, "Java PythonBridgeHelper#metadata run but returned None.")
        logger.info("Python binding instantiate for java metadata :"+str(metadata_))
        responsibilities_ = metadata_.getContacts()
        self.assertIsNotNone(responsibilities_, "Failed to obtain  from python metadata")
        logger.info("Contacts obtained from python metadata : "+str(responsibilities_))

