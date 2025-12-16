I recommend using **setuptools** - it's the most stable and widely-used packaging tool for Python, and it handles non-standard project structures well.

Here's how to package your project:

## 1. Install the tools

```bash
pip install setuptools wheel build
```

## 2. Create a `pyproject.toml` file in your project root

This is the modern way to configure Python packages:

```toml
[build-system]
requires = ["setuptools>=45", "wheel"]
build-backend = "setuptools.build_meta"

[project]
name = "your-package-name"
version = "0.1.0"
description = "Your package description"
authors = [{name = "Your Name", email = "your.email@example.com"}]
requires-python = ">=3.7"
dependencies = [
    # List your dependencies here, e.g.:
    # "requests>=2.25.0",
    # "numpy>=1.20.0",
]

[tool.setuptools]
# This tells setuptools to include all Python files it finds
py-modules = []  # Leave empty to auto-discover

[tool.setuptools.packages.find]
where = ["."]  # Root directory
include = ["*"]  # Include all packages
```

## 3. If your structure is really flat (no packages, just .py files)

Add this to your `pyproject.toml`:

```toml
[tool.setuptools]
py-modules = ["module1", "module2", "module3"]  # List each .py file without extension
```

## 4. Build the wheel

```bash
python -m build
```

This creates a `dist/` folder with your `.whl` file.

## 5. Install and test

```bash
pip install dist/your_package_name-0.1.0-py3-none-any.whl
```

**Can you share your current folder structure?** I can give you a more specific configuration based on how your files are organized.