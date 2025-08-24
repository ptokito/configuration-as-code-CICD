#!/usr/bin/env python3
"""
Hello World Password Generator Application
A simple demonstration app for CI/CD Configuration as Code
"""
import sys
import platform
import os
import secrets
import string
from datetime import datetime

def generate_password(length=16):
    """Generate a secure random password"""
    # Define character sets
    lowercase = string.ascii_lowercase
    uppercase = string.ascii_uppercase
    digits = string.digits
    special_chars = "!@#$%^&*"

    # Ensure at least one character from each set
    password = [
        secrets.choice(lowercase),
        secrets.choice(uppercase),
        secrets.choice(digits),
        secrets.choice(special_chars)
    ]

    # Fill the rest with random characters from all sets
    all_chars = lowercase + uppercase + digits + special_chars
    for _ in range(length - 4):
        password.append(secrets.choice(all_chars))

    # Shuffle the password list and join to string
    secrets.SystemRandom().shuffle(password)
    return ''.join(password)

def get_tech_stack():
    """Get technology stack information"""
    return {
        'python_version': platform.python_version(),
        'os': platform.system() + " " + platform.release(),
        'architecture': platform.machine(),
        'libraries': ['secrets', 'string', 'platform', 'datetime'],
        'ci_cd': 'TeamCity Pipelines',
        'language': 'Python',
        'security': 'Cryptographically Secure Random Generation'
    }

def print_header():
    """Print the application header"""
    print("\n" + "="*80)
    print("🚀 Hello World!!!")
    print("="*80)
    print("🔐 PASSWORD GENERATOR")
    print("="*80)

def print_tech_stack_info():
    """Print technology stack information"""
    tech_stack = get_tech_stack()

    print("\n📱 This is a Password Generator built using Tech Stack:")
    print("-" * 60)
    print(f"  🐍 Language:        {tech_stack['language']} {tech_stack['python_version']}")
    print(f"  💻 Operating System: {tech_stack['os']}")
    print(f"  🏗️  Architecture:     {tech_stack['architecture']}")
    print(f"  🔧 CI/CD Platform:   {tech_stack['ci_cd']}")
    print(f"  🔒 Security Method:  {tech_stack['security']}")
    print(f"  📚 Core Libraries:   {', '.join(tech_stack['libraries'])}")
    print("-" * 60)
    print("\n🌍 This will keep the world safe!")

def display_password():
    """Generate and display a password in a nice format"""
    password = generate_password(16)

    print("\n" + "┌" + "─" * 78 + "┐")
    print("│" + " " * 25 + "🔐 GENERATED PASSWORD" + " " * 31 + "│")
    print("├" + "─" * 78 + "┤")
    print(f"│{' ' * 20}{password}{' ' * (58 - len(password))}│")
    print("└" + "─" * 78 + "┘")

    print("\n⚠️  Security Notice:")
    print("   • This password is cryptographically secure")
    print("   • Contains uppercase, lowercase, numbers, and special characters")
    print("   • Never share your password with anyone")
    print("   • Store it in a secure password manager")

def get_build_info():
    """Get build and environment information"""
    return {
        'build_number': os.getenv('BUILD_NUMBER', 'local'),
        'git_commit': os.getenv('GIT_COMMIT', 'unknown'),
        'ci_server': os.getenv('TEAMCITY_VERSION', 'local development'),
        'timestamp': datetime.now().strftime('%Y-%m-%d %H:%M:%S'),
        'author': 'Tim Okito'
    }

def print_build_info():
    """Print build information"""
    build_info = get_build_info()

    print(f"\n👨‍💻 Built by: {build_info['author']}")
    print(f"🏗️  Build: #{build_info['build_number']}")
    print(f"📝 Commit: {build_info['git_commit'][:8] if len(build_info['git_commit']) > 8 else build_info['git_commit']}")
    print(f"⏰ Build Time: {build_info['timestamp']}")
    print(f"🤖 CI Server: {build_info['ci_server']}")

def run_tests():
    """Simple test function for CI demonstration"""
    test_results = []

    # Test 1: Check Python version
    test_results.append(('Python Version Check', sys.version_info >= (3, 6)))

    # Test 2: Check if required modules are available
    try:
        import platform
        import datetime
        import secrets
        import string
        test_results.append(('Module Import Check', True))
    except ImportError:
        test_results.append(('Module Import Check', False))

    # Test 3: Password generation test
    try:
        password = generate_password(16)
        test_results.append(('Password Generation', len(password) == 16))
    except Exception:
        test_results.append(('Password Generation', False))

    # Test 4: Password complexity test
    try:
        password = generate_password(16)
        has_upper = any(c.isupper() for c in password)
        has_lower = any(c.islower() for c in password)
        has_digit = any(c.isdigit() for c in password)
        has_special = any(c in "!@#$%^&*" for c in password)
        test_results.append(('Password Complexity', has_upper and has_lower and has_digit and has_special))
    except Exception:
        test_results.append(('Password Complexity', False))

    return test_results

def main():
    """Main application entry point"""
    # Print header
    print_header()

    # Show tech stack information
    print_tech_stack_info()

    # Generate and display password
    display_password()

    # Show build information
    print_build_info()

    # Run tests if in test mode
    if '--test' in sys.argv:
        print("\n" + "="*60)
        print("🧪 RUNNING TESTS")
        print("="*60)
        test_results = run_tests()

        for test_name, passed in test_results:
            status = "✅ PASS" if passed else "❌ FAIL"
            print(f"  {test_name:<25}: {status}")

        all_passed = all(result for _, result in test_results)
        print("-" * 60)

        if all_passed:
            print("✨ All tests passed! Application is ready for production.")
            return 0
        else:
            print("⚠️  Some tests failed! Please check the issues above.")
            return 1

    print("\n" + "="*80)
    print("✨ Password Generator executed successfully!")
    print("🌍 Keeping the world safe, one password at a time!")
    print("="*80)
    return 0

if __name__ == "__main__":
    sys.exit(main())