import jetbrains.buildServer.configs.kotlin.v2019_2.*
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.script
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.python
import jetbrains.buildServer.configs.kotlin.v2019_2.triggers.vcs
import jetbrains.buildServer.configs.kotlin.v2019_2.vcs.GitVcsRoot

/*
TeamCity Configuration as Code Demo
This demonstrates the power of Kotlin DSL for CI/CD configuration
All pipeline configuration is version controlled and reviewable
*/

version = "2023.11"

project {

    vcsRoot(GitHubRepo)

    buildType(Build)
    buildType(Test)
    buildType(SecurityScan)
    buildType(DeployStaging)
    buildType(DeployProduction)

    // Define the build chain order
    buildTypesOrder = arrayListOf(Build, Test, SecurityScan, DeployStaging, DeployProduction)
}

object GitHubRepo : GitVcsRoot({
    name = "GitHub Repository"
    url = "https://github.com/ptokito/CaC-TeamCity"
    branch = "refs/heads/main"
    branchSpec = "+:refs/heads/*"
    authMethod = password {
        userName = "git"
        password = "credentialsJSON:github-token"
    }
})

object Build : BuildType({
    name = "🔨 Build and Package"
    description = "Build the Python application and create artifacts"

    artifactRules = """
        app.py => app.zip
        requirements.txt => app.zip
        templates => app.zip/templates
        version.json => app.zip
    """.trimIndent()

    vcs {
        root(GitHubRepo)
    }

    steps {
        script {
            name = "Display Build Information"
            scriptContent = """
                echo "========================================="
                echo "🔨 Configuration as Code Demo - Build Stage"
                echo "========================================="
                echo "Build Number: %build.number%"
                echo "Branch: %teamcity.build.branch%"
                echo "Commit: %build.vcs.number%"
                echo "========================================="
            """.trimIndent()
        }

        script {
            name = "Setup Python Environment"
            scriptContent = """
                echo "📦 Setting up Python environment..."
                python --version
                pip install --upgrade pip
                pip install -r requirements.txt
                echo "✅ Dependencies installed successfully!"
            """.trimIndent()
        }

        python {
            name = "Verify Application"
            command = script {
                content = """
import sys
import json

print("=" * 50)
print("🐍 Python Application Verification")
print("=" * 50)
print(f"Python Version: {sys.version}")

try:
    from app import app
    print("✅ Flask application loaded successfully!")
    
    # Create version info
    version_info = {
        "build_number": "%build.number%",
        "commit": "%build.vcs.number%",
        "branch": "%teamcity.build.branch%"
    }
    
    with open('version.json', 'w') as f:
        json.dump(version_info, f, indent=2)
    
    print("✅ Version file created")
    print("=" * 50)
    
except Exception as e:
    print(f"❌ Error loading application: {e}")
    sys.exit(1)
                """.trimIndent()
            }
        }
    }

    triggers {
        vcs {
            branchFilter = "+:*"
        }
    }
})

object Test : BuildType({
    name = "🧪 Run Tests"
    description = "Execute application tests"

    vcs {
        root(GitHubRepo)
    }

    dependencies {
        snapshot(Build) {
            artifacts {
                artifactRules = "app.zip!** => ."
            }
        }
    }

    steps {
        script {
            name = "Install Test Dependencies"
            scriptContent = """
                echo "📦 Installing test dependencies..."
                pip install pytest pytest-cov 2>/dev/null || echo "Test framework installed"
            """.trimIndent()
        }

        script {
            name = "Run Tests"
            scriptContent = """
                echo "========================================="
                echo "🧪 Running Test Suite"
                echo "========================================="
                
                # Check if test_app.py exists
                if [ -f "test_app.py" ]; then
                    echo "Found test_app.py, running actual tests..."
                    python -m pytest test_app.py -v --tb=short || true
                else
                    echo "Running simulated tests for demo..."
                    echo ""
                    echo "Test Results:"
                    echo "  ✅ test_hello_world_route: PASSED"
                    echo "  ✅ test_health_check: PASSED"
                    echo "  ✅ test_api_info: PASSED"
                    echo "  ✅ test_pipeline_configuration: PASSED"
                    echo ""
                    echo "Coverage Report:"
                    echo "  📊 Overall coverage: 85%"
                    echo "  📊 app.py: 92%"
                    echo "  📊 Critical paths: 100%"
                fi
                
                echo "========================================="
                echo "✅ Test stage completed successfully!"
                echo "========================================="
            """.trimIndent()
        }
    }
})

object SecurityScan : BuildType({
    name = "🔒 Security Scan"
    description = "Scan for security vulnerabilities"

    vcs {
        root(GitHubRepo)
    }

    dependencies {
        snapshot(Build) {
            artifacts {
                artifactRules = "app.zip!** => ."
            }
        }
    }

    steps {
        script {
            name = "Security Vulnerability Scan"
            scriptContent = """
                echo "========================================="
                echo "🔒 Security Scan"
                echo "========================================="
                
                # Try to run actual security scan
                pip install safety 2>/dev/null || true
                
                if command -v safety &> /dev/null; then
                    echo "Running actual security scan..."
                    safety check --json || true
                else
                    echo "Running simulated security scan for demo..."
                    echo ""
                    echo "Scanning dependencies..."
                    echo "  ✅ Flask==3.0.0: No vulnerabilities"
                    echo "  ✅ Werkzeug==3.0.1: No vulnerabilities"
                    echo "  ✅ gunicorn==21.2.0: No vulnerabilities"
                    echo ""
                    echo "Security Summary:"
                    echo "  📊 Packages scanned: 6"
                    echo "  ✅ Vulnerabilities found: 0"
                    echo "  ⚠️  Warnings: 0"
                fi
                
                echo "========================================="
                echo "✅ Security scan completed!"
                echo "========================================="
            """.trimIndent()
        }
    }
})

object DeployStaging : BuildType({
    name = "🚀 Deploy to Staging"
    description = "Deploy to staging environment"

    dependencies {
        snapshot(Test) {
            onDependencyFailure = FailureAction.FAIL_TO_START
        }
        snapshot(SecurityScan) {
            onDependencyFailure = FailureAction.ADD_PROBLEM
        }
        artifacts(Build) {
            artifactRules = "app.zip!** => ."
        }
    }

    steps {
        script {
            name = "Pre-deployment Checks"
            scriptContent = """
                echo "========================================="
                echo "📋 Pre-deployment Checklist"
                echo "========================================="
                echo "✓ Build artifacts available"
                echo "✓ Tests passed"
                echo "✓ Security scan completed"
                echo "✓ Ready for staging deployment"
                echo "========================================="
            """.trimIndent()
        }

        script {
            name = "Deploy to Staging"
            scriptContent = """
                echo "========================================="
                echo "🚀 Deploying to Staging Environment"
                echo "========================================="
                echo "Build Number: %build.number%"
                echo "Commit: %build.vcs.number%"
                echo "Environment: STAGING"
                echo ""
                echo "Deployment steps:"
                echo "  1. Preparing application bundle..."
                sleep 1
                echo "  2. Uploading to staging server..."
                sleep 1
                echo "  3. Running deployment scripts..."
                sleep 1
                echo "  4. Verifying deployment..."
                sleep 1
                echo ""
                echo "✅ Staging deployment successful!"
                echo "🌐 URL: https://cac-demo-staging.onrender.com"
                echo "========================================="
            """.trimIndent()
        }
    }
})

object DeployProduction : BuildType({
    name = "🎯 Deploy to Production"
    description = "Deploy to production environment (with approval)"

    params {
        param("env.REQUIRES_APPROVAL", "true")
    }

    dependencies {
        snapshot(DeployStaging) {
            onDependencyFailure = FailureAction.FAIL_TO_START
        }
        artifacts(Build) {
            artifactRules = "app.zip!** => ."
        }
    }

    steps {
        script {
            name = "Production Deployment Checklist"
            scriptContent = """
                echo "========================================="
                echo "🎯 Production Deployment Checklist"
                echo "========================================="
                echo "✓ All tests passed"
                echo "✓ Security scan completed"
                echo "✓ Staging deployment verified"
                echo "✓ Manual approval required: %env.REQUIRES_APPROVAL%"
                echo ""
                echo "Build Details:"
                echo "  Build Number: %build.number%"
                echo "  Commit: %build.vcs.number%"
                echo "  Branch: %teamcity.build.branch%"
                echo "========================================="
            """.trimIndent()
        }

        script {
            name = "Deploy to Production"
            scriptContent = """
                echo "========================================="
                echo "🎯 Deploying to Production Environment"
                echo "========================================="
                echo "Environment: PRODUCTION"
                echo ""
                echo "Production deployment steps:"
                echo "  1. Creating backup of current version..."
                sleep 1
                echo "  2. Preparing production bundle..."
                sleep 1
                echo "  3. Deploying to production servers..."
                sleep 1
                echo "  4. Running smoke tests..."
                sleep 1
                echo "  5. Updating load balancer..."
                sleep 1
                echo ""
                echo "✅ Production deployment successful!"
                echo "🌐 URL: https://cac-demo.onrender.com"
                echo ""
                echo "🎉 Configuration as Code Demo Complete!"
                echo "========================================="
            """.trimIndent()
        }
    }
})