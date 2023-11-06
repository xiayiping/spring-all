```powershell

# kubectrl
Invoke-WebRequest -OutFile "d:\kubectrl\kubectl.exe" -Uri "https://dl.k8s.io/release/v1.28.3/bin/windows/amd64/kubectl.exe" -UseBasicParsing

# minikube
New-Item -Path 'c:\' -Name 'minikube' -ItemType Directory -Force
Invoke-WebRequest -OutFile 'c:\minikube\minikube.exe' -Uri 'https://github.com/kubernetes/minikube/releases/latest/download/minikube-windows-amd64.exe' -UseBasicParsing

```