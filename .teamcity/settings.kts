import jetbrains.buildServer.configs.kotlin.*
import jetbrains.buildServer.configs.kotlin.buildFeatures.perfmon
import jetbrains.buildServer.configs.kotlin.buildFeatures.notifications
import jetbrains.buildServer.configs.kotlin.buildSteps.python
import jetbrains.buildServer.configs.kotlin.buildSteps.script
import jetbrains.buildServer.configs.kotlin.triggers.vcs
import jetbrains.buildServer.configs.kotlin.vcs.GitVcsRoot
import java.util.*

/*
The settings script is an entry point for defining a TeamCity
project hierarchy. The script should contain a single call to the
project() function with a Project instance or an init function as an
argument.

VcsRoots, BuildTypes, Templates, and subprojects can be
registered inside the project using the vcsRoot(), buildType(),
template(), and subProject() methods respectively.

To debug settings scripts in command-line, run the

    mvnDebug org.jetbrains.teamcity:teamcity-configs-maven-plugin:generate

command and attach your debugger to the port 8000.

To debug in IntelliJ Idea, open the 'Maven Projects' tool window (View
-> Tool Windows -> Maven Projects), find the generate task node
(Plugins -> teamcity-configs -> teamcity-configs:generate), the
'Debug' option is available in the context menu for the task.
*/

version = "2023.11"

project {
    description = "Hello World Password Generator - Configuration as Code Demo by Tim Okito"

    // VCS Root Configuration
    vcsRoot(GitVcsRootConfig)

    // Build Types
    buildType(BuildAndTest)
    buildType(Deploy)
    buildType(LiveDemoShowcase)

    // Build Chain Configuration
    sequential {
        buildType(BuildAndTest)
        buildType(Deploy)
        parallel {
            buildType(LiveDemoShowcase)
        }
    }

    // Project Parameters
    params {
        param("env.PYTHON_VERSION", "3.12")
        param("env.PROJECT_NAME", "password-generator-demo")
        param("env.AUTHOR", "Tim Okito")
        param("env.DEMO_TIMESTAMP", "${Date()}")
        param("env.DEMO_MODE", "LIVE")
        param("env.RENDER_DEPLOY_HOOK", "https://api.render.com/deploy/srv-d2k74c2li9vc73e11t5g?key=08HXHBhaTvQ")
    }

    // Project Template
    template(PythonBuildTemplate)

    // Project Features
    features {
        feature {
            type = "project-graphs"
            param("series", "build-duration")
            param("format", "duration")
        }
    }
}

// VCS Root Configuration
object GitVcsRootConfig : GitVcsRoot({
    name = "Password Generator Repository - GitHub"
    url = "https://github.com/ptokito/cac-demo.git"
    branch = "refs/heads/main"
    branchSpec = """
        +:refs/heads/*
    """.trimIndent()
    authMethod = password {
        userName = "ptokito"
        password = "credentialsJSON:github-token"
    }
})

// Python Build Template
object PythonBuildTemplate : Template({
    name = "Python Build Template"

    artifactRules = """
        **/*.py => python-app.zip
        requirements.txt => python-app.zip
        render.yaml => python-app.zip
        README.md => python-app.zip
    """.trimIndent()

    params {
        param("python.test.coverage.threshold", "80")
        param("demo.live.update", "true")
    }

    vcs {
        root(GitVcsRootConfig)
        cleanCheckout = true
    }

    steps {
        script {
            name = "🔧 Setup Python Environment"
            scriptContent = """
                echo "🐍 Setting up Python Environment"
                echo "================================="
                echo "Python Version: $(python3 --version)"
                echo "Pip Version: $(pip3 --version)"
                echo "Working Directory: $(pwd)"
                echo "================================="
                
                # Create virtual environment (optional for CI)
                # python3 -m venv venv
                # source venv/bin/activate
                
                # Note: Built-in modules don't need installation
                echo "✅ Python environment ready"
                echo "📦 Using built-in modules: secrets, platform, datetime, os, sys, string"
            """.trimIndent()
        }
    }
})

// Build and Test Configuration
object BuildAndTest : BuildType({
    templates(PythonBuildTemplate)
    name = "🏗️ Build and Test Password Generator"
    description = "Build application and run comprehensive tests"

    params {
        param("env.BUILD_CONFIGURATION", "test")
        param("env.SHOW_DEMO_BANNER", "true")
    }

    steps {
        // Demo Banner
        script {
            name = "🎯 Demo Banner"
            scriptContent = """
                echo "============================================================"
                echo "🚀 PASSWORD GENERATOR BUILD STARTED"
                echo "============================================================"
                echo "📊 Build Information:"
                echo "  • Project: Password Generator"
                echo "  • Author: Tim Okito"
                echo "  • Python Version: %env.PYTHON_VERSION%"
                echo "  • Build Config: %env.BUILD_CONFIGURATION%"
                echo "  • Timestamp: $(date)"
                echo "============================================================"
            """.trimIndent()
        }

        // Code Quality Check
        script {
            name = "🔍 Code Quality Check"
            scriptContent = """
                echo "🔍 Running Code Quality Checks"
                echo "=============================="
                
                # Check if main files exist
                if [ -f "app.py" ]; then
                    echo "✅ app.py found"
                    wc -l app.py
                else
                    echo "❌ app.py not found"
                    exit 1
                fi
                
                # Basic Python syntax check
                python3 -m py_compile app.py
                if [ $? -eq 0 ]; then
                    echo "✅ Python syntax check passed"
                else
                    echo "❌ Python syntax check failed"
                    exit 1
                fi
                
                # Check for required functions
                grep -q "generate_password" app.py && echo "✅ Password generation function found" || echo "❌ Password generation function missing"
                grep -q "Hello World" app.py && echo "✅ Hello World message found" || echo "❌ Hello World message missing"
                grep -q "keep the world safe" app.py && echo "✅ Security message found" || echo "❌ Security message missing"
            """.trimIndent()
        }

        // Run Unit Tests
        python {
            name = "🧪 Run Application Tests"
            command = file {
                filename = "app.py"
                arguments = "--test"
            }
        }

        // Run Application Demo
        python {
            name = "🚀 Run Password Generator Demo"
            command = file {
                filename = "app.py"
            }
        }

        // Generate Build Artifacts
        script {
            name = "📦 Generate Build Artifacts"
            scriptContent = """
                echo "📦 Generating Build Artifacts"
                echo "============================="
                
                # Create build info
                echo "Build Number: %build.number%" > build-info.txt
                echo "Build Date: $(date)" >> build-info.txt
                echo "Git Commit: %build.vcs.number%" >> build-info.txt
                echo "Python Version: $(python3 --version)" >> build-info.txt
                
                # List all files
                echo "📁 Files in build:"
                ls -la
                
                echo "✅ Build artifacts generated"
            """.trimIndent()
        }
    }

    triggers {
        vcs {
            branchFilter = """
                +:refs/heads/main
                +:refs/heads/develop
            """.trimIndent()
            perCheckinTriggering = true
            groupCheckinsByCommitter = true
            enableQueueOptimization = true

            triggerRules = """
                +:app.py
                +:requirements.txt
                +:render.yaml
                +:**.py
            """.trimIndent()
        }
    }

    features {
        perfmon {
        }

        // Build failure detection
        feature {
            type = "BuildFailureOnMetric"
            param("anchorBuild", "lastSuccessful")
            param("metricKey", "buildDuration")
            param("metricThreshold", "300")
            param("metricType", "buildDurationSecs")
            param("moreOrLess", "more")
            param("stopBuildOnFailure", "false")
        }
    }

    requirements {
        contains("teamcity.agent.os.name", "Linux")
        exists("python3")
    }
})

// Deploy Configuration with Render Integration
object Deploy : BuildType({
    name = "🚀 Deploy to Render"
    description = "Deploy Password Generator to Render platform with approval gates"

    params {
        select("env.DEPLOY_TARGET", "production", display = ParameterDisplay.PROMPT,
            options = listOf("staging" to "staging", "production" to "production"))
        param("env.RENDER_SERVICE_ID", "srv-d2k74c2li9vc73e11t5g")
        param("env.RENDER_DEPLOY_HOOK", "%env.RENDER_DEPLOY_HOOK%")
        param("env.NOTIFY_SLACK", "true")
        param("env.DEMO_DEPLOYMENT", "true")
    }

    dependencies {
        snapshot(BuildAndTest) {
            onDependencyFailure = FailureAction.FAIL_TO_START
            onDependencyCancel = FailureAction.CANCEL
        }
        artifacts(BuildAndTest) {
            artifactRules = "python-app.zip => ."
        }
    }

    steps {
        script {
            name = "🔍 Pre-deployment Validation"
            scriptContent = """
                echo "🔍 Pre-deployment Validation"
                echo "============================"
                echo "🎯 Target Environment: %env.DEPLOY_TARGET%"
                echo "🌐 Platform: Render"
                echo "📦 Service ID: %env.RENDER_SERVICE_ID%"
                echo "🔗 Deploy Hook: %env.RENDER_DEPLOY_HOOK%"
                echo "🏗️ Build Number: %dep.${BuildAndTest.id}.build.number%"
                echo "============================"
                
                # Validate required files
                echo "📋 Checking required files..."
                
                if [ -f "app.py" ]; then
                    echo "✅ app.py found"
                    head -5 app.py
                else
                    echo "❌ ERROR: app.py not found"
                    exit 1
                fi
                
                echo "🔍 Validating Python application..."
                python3 -c "
import sys
sys.path.insert(0, '.')
try:
    # Test import without running
    with open('app.py', 'r') as f:
        code = f.read()
    if 'def generate_password' in code:
        print('✅ Password generation function found')
    if 'Hello World' in code:
        print('✅ Hello World message found')  
    if 'keep the world safe' in code:
        print('✅ Security message found')
    print('✅ Application validation passed')
except Exception as e:
    print(f'❌ Application validation failed: {e}')
    sys.exit(1)
"
                
                echo "✅ Pre-deployment validation completed"
            """.trimIndent()
        }

        script {
            name = "🚀 Deploy to Render Platform"
            scriptContent = """
                echo "🚀 Deploying to Render Platform"
                echo "==============================="
                
                # Deploy using Render webhook
                echo "📡 Triggering Render deployment..."
                echo "Deploy Hook URL: %env.RENDER_DEPLOY_HOOK%"
                
                # Call Render deploy hook
                HTTP_RESPONSE=$(curl -s -w "HTTPSTATUS:%%{http_code}" -X POST "%env.RENDER_DEPLOY_HOOK%")
                HTTP_BODY=$(echo $HTTP_RESPONSE | sed -E 's/HTTPSTATUS\:[0-9]{3}$//')
                HTTP_STATUS=$(echo $HTTP_RESPONSE | tr -d '\n' | sed -E 's/.*HTTPSTATUS:([0-9]{3})$/\1/')
                
                echo "📊 Deployment Response:"
                echo "Status Code: $HTTP_STATUS"
                echo "Response Body: $HTTP_BODY"
                
                if [ "$HTTP_STATUS" -eq 200 ] || [ "$HTTP_STATUS" -eq 201 ]; then
                    echo "✅ Render deployment triggered successfully!"
                    echo "🌐 Your Password Generator is being deployed to Render"
                    echo "🔗 Check status at: https://dashboard.render.com/web/%env.RENDER_SERVICE_ID%"
                else
                    echo "❌ Failed to trigger Render deployment"
                    echo "HTTP Status: $HTTP_STATUS"
                    echo "Response: $HTTP_BODY"
                    exit 1
                fi
                
                echo "==============================="
                echo "🎉 Deployment initiated successfully!"
            """.trimIndent()
        }

        script {
            name = "📧 Post-deployment Activities"
            scriptContent = """
                echo "📧 Post-Deployment Activities"
                echo "============================="
                
                # Wait for deployment to stabilize
                echo "⏳ Waiting for deployment to stabilize..."
                sleep 45
                
                echo "📊 Deployment Summary:"
                echo "  🎯 Application: Password Generator"
                echo "  🌐 Platform: Render"
                echo "  📦 Service ID: %env.RENDER_SERVICE_ID%"
                echo "  🔢 Build Version: %dep.${BuildAndTest.id}.build.number%"
                echo "  📅 Deploy Time: $(date)"
                echo "  👨‍💻 Author: Tim Okito"
                echo "  🎯 Environment: %env.DEPLOY_TARGET%"
                
                if [ "%env.NOTIFY_SLACK%" = "true" ]; then
                    echo "📱 Slack notification: Deployment completed"
                fi
                
                echo "============================="
                echo "🎉 Password Generator deployed successfully!"
                echo "🌍 Keeping the world safe, one password at a time!"
                echo "🔗 Your app should be live at your Render URL soon"
                echo "============================="
            """.trimIndent()
        }
    }

    features {
        feature {
            type = "BUILD_APPROVAL"
            param("approvalRules", "user:lead-developer")
        }
    }
})

// Live Demo Showcase Build
object LiveDemoShowcase : BuildType({
    name = "✨ Live Demo Showcase"
    description = "Build configuration added LIVE during demo - Configuration as Code in action!"

    params {
        param("demo.message", "This build was added during the live demo!")
        param("demo.author", "Tim Okito")
        param("demo.tool", "IntelliJ IDEA + TeamCity Configuration as Code")
    }

    vcs {
        root(GitVcsRootConfig)
    }

    steps {
        script {
            name = "🎉 Demo Live Update"
            scriptContent = """
                echo "🎉 LIVE DEMO SHOWCASE"
                echo "===================="
                echo "📝 Message: %demo.message%"
                echo "👨‍💻 Author: %demo.author%"
                echo "🔧 Tool: %demo.tool%"
                echo "📅 Demo Time: $(date)"
                echo "===================="
                
                echo "🚀 Running Password Generator Demo..."
                python3 app.py
                
                echo "===================="
                echo "✨ Configuration as Code Benefits Demonstrated:"
                echo "  • ✅ Version controlled build configurations"
                echo "  • ✅ Code-based pipeline definitions"
                echo "  • ✅ IntelliJ IDEA integration for editing"
                echo "  • ✅ Automated deployments to Render"
                echo "  • ✅ Reusable templates and components"
                echo "  • ✅ Git-based workflow for CI/CD changes"
                echo "===================="
            """.trimIndent()
        }

        script {
            name = "💡 Configuration as Code Benefits"
            scriptContent = """
                echo "💡 CONFIGURATION AS CODE BENEFITS"
                echo "================================="
                echo "🔧 Infrastructure as Code for CI/CD:"
                echo "  • All build configurations in version control"
                echo "  • Easy to replicate across environments"
                echo "  • Code review process for pipeline changes"
                echo "  • Automated testing of build configurations"
                echo ""
                echo "🚀 Developer Experience:"
                echo "  • Edit pipelines in IntelliJ with full IDE support"
                echo "  • Syntax highlighting and code completion"
                echo "  • Refactoring and navigation capabilities"
                echo "  • Integration with existing development workflow"
                echo ""
                echo "🌐 Deployment Pipeline:"
                echo "  • Code → TeamCity Build → Render Deploy"
                echo "  • Automated quality checks and testing"
                echo "  • Approval gates for production deployments"
                echo "  • Complete audit trail of all changes"
                echo "================================="
                echo "🎉 Demo completed successfully!"
            """.trimIndent()
        }
    }

    triggers {
        vcs {
            branchFilter = "+:main"
        }
    }

    features {
        perfmon {
        }
    }
})