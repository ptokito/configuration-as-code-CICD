#!/usr/bin/env python3
import unittest
import hello_world

class TestHelloWorld(unittest.TestCase):
    def test_get_build_info(self):
        build_info = hello_world.get_build_info()
        self.assertIn('python_version', build_info)

    def test_run_tests(self):
        test_results = hello_world.run_tests()
        for test_name, passed in test_results:
            self.assertTrue(passed)

if __name__ == '__main__':
    unittest.main()