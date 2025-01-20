
```shell
## to freeze the requirements
python -m pip freeze > requirements.txt
## to apply the requirement.txt
python -m pip install -r requirements.txt
## or
pyp install -r requirements.txt
```


```shell
# This command will create a new directory named local in your project folder. This directory contains the Python interpreter and a copy of the standard library, along with the packages you install.
python -m venv local

# switch to your env
source local/Scripts/activate
or
local/Scripts/activate.ps1

```                