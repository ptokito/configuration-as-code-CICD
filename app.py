import os
import json
from flask import Flask, render_template, jsonify
from datetime import datetime

app = Flask(__name__)

@app.route('/')
def hello_world():
    """Main page showing Configuration as Code demo"""
    # Try to load version info if it exists
    version_info = {}
    if os.path.exists('version.json'):
        try:
            with open('version.json', 'r') as f:
                version_info = json.load(f)
        except:
            pass

    # Get deployment information
    deploy_info = {
        'timestamp': datetime.now().strftime("%Y-%m-%d %H:%M:%S"),
        'version': os.getenv('RENDER_GIT_COMMIT', version_info.get('version', 'dev'))[:7],
        'environment': os.getenv('DEPLOY_ENV', 'production'),
        'deployed_by': os.getenv('DEPLOYED_BY', 'GitHub Actions'),
        'render_service': os.getenv('RENDER_SERVICE_NAME', 'configuration-as-code-cicd'),
        'region': os.getenv('RENDER_REGION', 'oregon')
    }

    return render_template('index.html', **deploy_info)

@app.route('/health')
def health_check():
    """Health check endpoint for monitoring"""
    return jsonify({
        'status': 'healthy',
        'service': 'Configuration-as-Code-Demo',
        'timestamp': datetime.now().isoformat(),
        'environment': os.getenv('DEPLOY_ENV', 'production'),
        'deployed_by': os.getenv('DEPLOYED_BY', 'GitHub Actions'),
        'version': os.getenv('RENDER_GIT_COMMIT', 'unknown')[:7],
        'message': 'Deployed via Configuration as Code'
    }), 200

@app.route('/api/info')
def api_info():
    """API endpoint showing pipeline information"""
    return jsonify({
        'message': 'Configuration as Code Demo',
        'pipeline': {
            'source': 'GitHub',
            'ci_cd': 'GitHub Actions',
            'deployment': 'Render.com via Webhook',
            'trigger': 'Push from IntelliJ → GitHub Actions → Render'
        },
        'features': [
            'Version Control Integration',
            'Automated Testing',
            'Security Scanning',
            'Multi-environment Deployment',
            'Infrastructure as Code'
        ],
        'deployment_info': {
            'method': 'Webhook from GitHub Actions',
            'auto_deploy': False,
            'environment': os.getenv('DEPLOY_ENV', 'production'),
            'timestamp': datetime.now().isoformat()
        },
        'configuration_as_code': {
            'definition': 'All pipeline configuration in .github/workflows/demo.yml',
            'benefits': [
                'Version controlled',
                'Code review process',
                'YAML configuration',
                'IDE support',
                'Reproducible'
            ]
        }
    })

@app.route('/api/deployment')
def deployment_info():
    """Show deployment details"""
    return jsonify({
        'deployment_method': 'GitHub Actions Webhook',
        'auto_deploy_github': False,
        'deployed_at': datetime.now().isoformat(),
        'deployed_by': 'GitHub Actions Pipeline',
        'trigger_chain': [
            '1. Code pushed from IntelliJ',
            '2. GitHub Actions detects change',
            '3. Pipeline executes',
            '4. GitHub Actions calls Render webhook',
            '5. Render deploys application'
        ],
        'render_commit': os.getenv('RENDER_GIT_COMMIT', 'unknown'),
        'render_service': os.getenv('RENDER_SERVICE_NAME', 'unknown')
    })

if __name__ == '__main__':
    # Get port from environment variable (Render provides this)
    port = int(os.environ.get('PORT', 5000))

    # Run the Flask app
    app.run(
        host='0.0.0.0',
        port=port,
        debug=False  # Never use debug=True in production
    )