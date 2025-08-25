# app.py
from flask import Flask, render_template, jsonify
from datetime import datetime
import json
import os

app = Flask(__name__)

@app.route('/')
def hello_world():
    # Load version info if available
    version_info = {}
    if os.path.exists('version.json'):
        with open('version.json', 'r') as f:
            version_info = json.load(f)

    return render_template('index.html',
                           timestamp=datetime.now().strftime("%Y-%m-%d %H:%M:%S"),
                           version=version_info.get('version', 'dev'),
                           environment=os.getenv('DEPLOY_ENV', 'local'))

@app.route('/health')
def health_check():
    return jsonify({
        'status': 'healthy',
        'service': 'HelloWorld-CI-CD-Demo',
        'timestamp': datetime.now().isoformat(),
        'environment': os.getenv('DEPLOY_ENV', 'local')
    }), 200

@app.route('/api/info')
def api_info():
    return jsonify({
        'message': 'Hello World from Configuration as Code Demo by Tim Okito, August 2025!',
        'features': [
            'Version Control Integration',
            'Automated Testing',
            'Security Scanning',
            'Multi-environment Deployment',
            'Infrastructure as Code'
        ],
        'pipeline': {
            'source': 'GitHub',
            'ci_cd': 'TeamCity with Kotlin DSL',
            'deployment': 'Render.com'
        }
    })

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)