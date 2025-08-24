#!/usr/bin/env python3
"""
Hello World Application
A simple demonstration app for CI/CD Configuration as Code
"""

import sys
import platform
import os
from datetime import datetime


def get_build_info():
    """Get build and environment information"""
    build_info = {
        'python_version': platform.python_version(),
        'os': platform.system(),
        'os_version': platform.release(),
        'architecture': platform.machine(),
        'build_number': os.getenv('BUILD_NUMBER', 'local'),
        'git_commit': os.getenv('GIT_COMMIT', 'unknown'),
        'ci_server': os.getenv('TEAMCITY_VERSION', 'local development'),
        'timestamp': datetime.now().strftime('%Y-%m-%d %H:%M:%S')
    }
    return build_info


def print_banner():
    """Print application banner"""
    print("=" * 60)
    print("🚀 HELLO WORLD!!")
    print("=" * 60)


def print_build_stack(build_info):
    """Print build stack information"""
    print("\n👨‍💻 Built by Tim Okito")
    print("\n📊 Build Stack Information:")
    print("-" * 40)
    print(f"  • Python Version:  {build_info['python_version']}")
    print(f"  • Operating System: {build_info['os']} {build_info['os_version']}")
    print(f"  • Architecture:     {build_info['architecture']}")
    print(f"  • Build Number:     {build_info['build_number']}")
    print(f"  • Git Commit:       {build_info['git_commit'][:8] if len(build_info['git_commit']) > 8 else build_info['git_commit']}")
    print(f"  • CI Server:        {build_info['ci_server']}")
    print(f"  • Build Time:       {build_info['timestamp']}")
    print("-" * 40)


def run_tests():
    """Simple test function for CI demonstration"""
    test_results = []

    # Test 1: Check Python version
    test_results.append(('Python Version Check', sys.version_info >= (3, 6)))

    # Test 2: Check if required modules are available
    try:
        import platform
        import datetime
        test_results.append(('Module Import Check', True))
    except ImportError:
        test_results.append(('Module Import Check', False))

    # Test 3: Simple assertion
    test_results.append(('Basic Math Check', 2 + 2 == 4))

    return test_results


def main():
    """Main application entry point"""
    print_banner()

    # Get build information
    build_info = get_build_info()
    print_build_stack(build_info)

    # Run tests if in test mode
    if '--test' in sys.argv:
        print("\n🧪 Running Tests...")
        print("-" * 40)
        test_results = run_tests()

        for test_name, passed in test_results:
            status = "✅ PASS" if passed else "❌ FAIL"
            print(f"  {test_name}: {status}")

        all_passed = all(result for _, result in test_results)
        print("-" * 40)
        if all_passed:
            print("✨ All tests passed!")
            return 0
        else:
            print("⚠️  Some tests failed!")
            return 1

    print("\n✨ Application executed successfully!")
    return 0


if __name__ == "__main__":
    sys.exit(main())