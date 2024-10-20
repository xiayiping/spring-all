
```shell
docker build --help

# if docker desktop can't start, run
netsh winsock reset
# and restart PC
```

```shell

FROM node:alpine
COPY ./javascript /app
WORKDIR /app
CMD node app.js
```

```shell

docker build --tag myname:latest .
#last '.' tells where is the root path that can do a COPY#

```


```shell

docker build --tag myname:latest -f ./MyDockerfile
#-f will make you use your own docker file#

```


## useful commands

```shell

docker compose -f docker.yml up
docker compose -it -f docker.yml up
```


```shell

docker pull nginx
docker run nginx:latest ## run in blocking mode
docker container ls  ## ls all running container by default
docker run -d nginx:latest ## run in detached mode
docker ps  ## also show you all running container
docker stop $container_id  ## stop container
docker run -p 8080:80 -d nginx:latest ## host_port:internal_port
docker run -p 8080:80 -p 3000:80 -d nginx:latest ## map more than 1 port

docker start e96997b9727d ## start container by hash
docker start kind_colden  ## start container by name

docker compose -f ./docker/docker-compose-otel.yml  up
# in docker-compose file,  all path starts from the compose.yml file, not the script run path.
```

# DOCKER VS PODMAN

Docker and Podman are both containerization tools that allow you to create, manage, and run containers. While they share many similarities, they also have important differences in terms of architecture, use cases, and security. Below is a detailed comparison of **Docker** and **Podman**:

---

## 1. **Architecture and Daemon**

### Docker:
- **Daemon-based:** Docker follows a client-server architecture. It has a central **daemon** (`dockerd`) that runs in the background and manages various Docker objects like containers, images, networks, and volumes.
- **Single Process:** The Docker client communicates with the Docker daemon over a REST API, which means you need to have the Docker daemon running to execute any Docker commands.
- **Root Access:** The Docker daemon usually runs as a root process, which raises security concerns because of the elevated privileges required to manage containers.

### Podman:
- **Daemonless:** Podman is daemonless. Each Podman command directly interacts with the container runtime, which means there's no need for a central daemon. This allows users to manage containers without a background service running.
- **No Root Required:** Podman can run containers **rootless**, meaning you can run and manage containers without requiring root privileges. This improves security, especially in multi-user environments.
- **Systemd Integration:** Instead of a daemon, Podman can integrate with **systemd** to manage containers as system services.

**Summary:**
- **Docker**: Requires a daemon (`dockerd`), usually runs as root.
- **Podman**: Daemonless, can run rootless containers.

---

## 2. **Command Line Interface (CLI)**

### Docker:
- Docker has a well-established and widely adopted CLI. The commands are simple, and it is widely used across the industry.
- Example:
  ```bash
  docker run -d --name mycontainer nginx
  ```

### Podman:
- Podman’s CLI is **Docker-compatible**. Most Docker commands work with Podman without modification. This makes switching from Docker to Podman relatively easy.
- Example:
  ```bash
  podman run -d --name mycontainer nginx
  ```

**Summary:**
- The CLI commands for Docker and Podman are almost identical, with the exception of replacing `docker` with `podman`.

---

## 3. **Container Image Compatibility**

### Docker:
- Docker uses the **OCI (Open Container Initiative)** image specification. Images are stored in Docker Hub by default, but you can also use private registries.
- Docker’s image format is the de facto standard, and Docker Hub is the most popular container registry.

### Podman:
- Podman also uses the **OCI image standard**, meaning it can pull and run the same container images as Docker.
- Podman can pull images from Docker Hub or any OCI-compliant registry (such as Quay, Google Container Registry, etc.).

**Summary:**
- Both Docker and Podman use the OCI image format, so images are interchangeable between them.

---

## 4. **Security**

### Docker:
- **Root Daemon Security Risk:** Since Docker runs a root daemon, any vulnerabilities in Docker can potentially give attackers root access to the host system.
- **User Namespaces:** Docker introduced user namespaces to mitigate security risks by allowing containers to run under different user IDs than root.
- **Rootless Mode:** Docker has a rootless mode, but it is not as mature as Podman’s rootless capabilities.

### Podman:
- **Rootless Containers:** Podman was designed with rootless containers in mind. You can run containers as an unprivileged user, which enhances security by preventing containers from gaining elevated privileges on the host system.
- **No Daemon:** Since Podman doesn’t rely on a root daemon, the attack surface is smaller, and there's less risk of privilege escalation.
- **SELinux and AppArmor Integration:** Podman integrates well with Linux security modules like SELinux and AppArmor for additional security controls.

**Summary:**
- **Docker**: Traditionally daemon-based, which requires root access, though it has rootless support.
- **Podman**: Natively supports rootless containers, increasing security, and does not require a daemon.

---

## 5. **Pod Support**

### Docker:
- Docker does not natively support the concept of "pods" (a group of containers sharing the same network namespace, like Kubernetes Pods). However, Docker Compose can be used to orchestrate multiple containers.

### Podman:
- Podman supports **pods** natively, similar to Kubernetes' pod concept. Multiple containers can run in the same pod, sharing networking, IPC, and other namespaces.

  Example of creating a pod:
  ```bash
  podman pod create --name mypod
  podman run -d --pod mypod nginx
  ```

**Summary:**
- **Docker**: No native pod support (Docker Compose is typically used for multi-container setups).
- **Podman**: Natively supports Kubernetes-like pods.

---

## 6. **Kubernetes Integration**

### Docker:
- Docker was historically used as the default container runtime for Kubernetes. However, Kubernetes has moved away from Docker in favor of **CRI-O** and **containerd** as the default runtimes.
- **Docker Desktop** includes a local Kubernetes cluster for development purposes, making it convenient for developers.

### Podman:
- Podman is closely aligned with Kubernetes. It can generate Kubernetes YAML files directly from running containers or pods.
- Example of generating a Kubernetes YAML from a Podman pod:
  ```bash
  podman generate kube mypod > mypod.yaml
  ```
- Podman can also be used as a container runtime for Kubernetes through **CRI-O** (another Red Hat project).

**Summary:**
- **Docker**: Docker is no longer the default runtime for Kubernetes, but Docker Desktop provides a built-in Kubernetes environment.
- **Podman**: Podman can generate Kubernetes YAML and integrate with Kubernetes environments through CRI-O.

---

## 7. **Container Lifecycle and Management**

### Docker:
- Docker provides both low-level container management and orchestration features via **Docker Compose** and **Docker Swarm**.
- **Docker Compose** is widely used for managing multi-container applications in a declarative way.

### Podman:
- Podman can manage containers and pods, but it does not have an equivalent to Docker Compose. Instead, **Podman Compose** is a community-maintained project that mimics Docker Compose functionality.
- Podman supports **systemd** integration, allowing containers to be managed as system services.

**Summary:**
- **Docker**: Comes with Docker Compose for multi-container environments.
- **Podman**: Integrates with `systemd` for container lifecycle management but relies on the community for `podman-compose`.

---

## 8. **Operating System Support**

### Docker:
- Docker is supported on **Linux**, **Windows**, and **macOS**.
- Docker Desktop provides a user-friendly interface and comes with a built-in Kubernetes environment. However, Docker Desktop on macOS and Windows runs a Linux VM under the hood to manage containers.

### Podman:
- Podman is primarily designed for **Linux** systems. However, Podman can be used on **macOS** and **Windows** through virtualization (using tools like **Podman Machine** or **WSL 2** on Windows).
- Podman’s support for non-Linux operating systems is not as mature as Docker Desktop, but development is ongoing.

**Summary:**
- **Docker**: Has better support for non-Linux systems (Windows, macOS) via Docker Desktop.
- **Podman**: Primarily Linux-focused, but works on macOS/Windows through virtualization layers like **Podman Machine**.

---

## 9. **Community and Ecosystem**

### Docker:
- Docker has a large, well-established community and ecosystem.
- **Docker Hub** is the most popular public container registry, and Docker’s ecosystem includes tools like Docker Compose, Docker Swarm, and Docker Desktop.

### Podman:
- Podman is part of the **Red Hat** ecosystem and is widely used in enterprise settings, especially for those looking for daemonless, rootless container management.
- While Podman is gaining popularity, its community and ecosystem are smaller than Docker’s, though it benefits from strong integration with Kubernetes and the broader Red Hat ecosystem.

**Summary:**
- **Docker**: Large community and ecosystem, with Docker Hub as a central hub for container images.
- **Podman**: Growing community, strong in the Red Hat ecosystem, and Kubernetes-aligned.

---

## Conclusion

| **Feature**                | **Docker**                                   | **Podman**                                 |
|----------------------------|----------------------------------------------|--------------------------------------------|
| **Architecture**            | Daemon-based (root required)                 | Daemonless (rootless support)              |
| **CLI**                     | Docker CLI                                  | Docker-compatible CLI                     |
| **Image Compatibility**     | OCI-compliant, uses Docker Hub               | OCI-compliant, supports Docker Hub         |
| **Security**                | Root daemon, supports rootless mode          | Rootless by design, more secure            |
| **Pod Support**             | No native pod support                       | Native pod support (Kubernetes-like)       |
| **Kubernetes Integration**  | Integrated with Docker Desktop               | Strong Kubernetes integration              |
| **Container Lifecycle**     | Docker Compose, Docker Swarm                 | Systemd integration, Podman Compose        |
| **OS Support**              | Linux, macOS, Windows (via Docker Desktop)   | Primarily Linux, macOS/Windows via VM      |
| **Community**               | Large community, Docker Hub, Docker Desktop  | Smaller but growing, Red Hat ecosystem     |

Both Docker and Podman are powerful containerization tools, but the choice between them depends on your specific use cases:

- Choose **Docker** for ease of use, Docker Desktop, and a large ecosystem.
- Choose **Podman** if you need rootless containers, a daemonless architecture, or better integration with Kubernetes.



