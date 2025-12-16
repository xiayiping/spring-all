# upload

Here's how to upload and download your tar from Nexus:

## 1. Upload to Nexus (using curl)

```bash
# Upload to Nexus raw repository
curl -u username:password \
  --upload-file myproject.tar.gz \
  "http://your-nexus-server:8081/repository/raw-hosted/myproject/myproject-1.0.0.tar.gz"
```

Or with API token (more secure):
```bash
curl -H "Authorization: Bearer YOUR_TOKEN" \
  --upload-file myproject.tar.gz \
  "http://your-nexus-server:8081/repository/raw-hosted/myproject/myproject-1.0.0.tar.gz"
```

## 2. Download on another machine

```bash
# Download from Nexus
curl -u username:password \
  -O "http://your-nexus-server:8081/repository/raw-hosted/myproject/myproject-1.0.0.tar.gz"

# Or with token
curl -H "Authorization: Bearer YOUR_TOKEN" \
  -O "http://your-nexus-server:8081/repository/raw-hosted/myproject/myproject-1.0.0.tar.gz"

# Extract
tar -xzf myproject-1.0.0.tar.gz
```

## 3. If you want to use it as a Python package repository

You can also upload as a Python package to a PyPI repository in Nexus:

```bash
# Install twine
pip install twine

# Upload (after creating a wheel)
twine upload --repository-url http://your-nexus-server:8081/repository/pypi-hosted/ \
  -u username -p password \
  dist/myproject-1.0.0-py3-none-any.whl

# Download/install on another machine
pip install myproject --index-url http://your-nexus-server:8081/repository/pypi-hosted/simple
```

**Which Nexus repository type are you using?** Raw repository (for tar files) or PyPI repository (for Python packages)?