# Roles and Vars

Managing a setup where **multiple groups of hosts** belong to different **services**, with **shared variables** and **overridden variables**, can be effectively handled in **Ansible** by leveraging its inventory, group variables, host variables, and roles structure. Here's a step-by-step guide to organize your Ansible setup:

---

## **1. Organizing Your Inventory**
Ansible's inventory allows you to group hosts logically. You can create groups for services and hosts, and even define nested groups (groups of groups).

### Example Inventory (YAML Format)
```yaml
all:
  children:
    web_servers:
      hosts:
        web1.example.com:
        web2.example.com:
      vars:
        service_name: "web"
        shared_var: "value_for_web"
    app_servers:
      hosts:
        app1.example.com:
        app2.example.com:
      vars:
        service_name: "app"
        shared_var: "value_for_app"
    db_servers:
      hosts:
        db1.example.com:
        db2.example.com:
      vars:
        service_name: "db"
        shared_var: "value_for_db"
    shared_services:
      children:
        web_servers:
        app_servers:
        db_servers:
      vars:
        global_shared_var: "common_value"
```

### Explanation:
1. **Groups for services**: `web_servers`, `app_servers`, and `db_servers` represent services, and their respective hosts are listed.
2. **Shared variables**:
    - `shared_var` is defined at the **service level** (e.g., web, app, or db).
    - `global_shared_var` applies to all services via the `shared_services` group.
3. **Overridden variables**: Variables defined at the **host level** (e.g., in `host_vars`) or group level override higher-level definitions.

---

## **2. Defining Variables in `group_vars` and `host_vars`**
Ansible allows you to define variables specific to:
- **Groups**: Use `group_vars/` for variables shared within a service or group of hosts.
- **Hosts**: Use `host_vars/` for variables specific to individual hosts.

### Directory Structure for Variables
```
inventory/
├── group_vars/
│   ├── all.yml                 # Variables shared across all hosts
│   ├── web_servers.yml         # Variables specific to `web_servers`
│   ├── app_servers.yml         # Variables specific to `app_servers`
│   ├── db_servers.yml          # Variables specific to `db_servers`
├── host_vars/
│   ├── web1.example.com.yml    # Variables specific to `web1`
│   ├── db1.example.com.yml     # Variables specific to `db1`
```

### Example `group_vars/all.yml`
```yaml
global_shared_var: "common_value"
timeout: 30
```

### Example `group_vars/web_servers.yml`
```yaml
service_name: "web"
shared_var: "value_for_web"
timeout: 20  # Overrides the global timeout for web servers
```

### Example `host_vars/web1.example.com.yml`
```yaml
node_name: "web1"
timeout: 15  # Overrides the group timeout for this host
```

---

## **3. Using Roles for Modularity**
Organizing your playbooks into **roles** ensures clean separation of logic for each service. Roles allow you to reuse tasks, handlers, templates, and variables.

### Directory Structure for Roles
```
roles/
├── web_server/
│   ├── tasks/
│   │   └── main.yml
│   ├── handlers/
│   │   └── main.yml
│   ├── templates/
│   │   └── nginx.conf.j2
│   ├── vars/
│   │   └── main.yml
│   ├── defaults/
│   │   └── main.yml
├── app_server/
│   ├── tasks/
│   │   └── main.yml
│   ├── handlers/
│   │   └── main.yml
│   ├── templates/
│   │   └── app_config.j2
│   ├── vars/
│   │   └── main.yml
│   ├── defaults/
│   │   └── main.yml
```

### Example Role: `web_server`
#### `roles/web_server/tasks/main.yml`
```yaml
- name: Install Nginx
  ansible.builtin.yum:
    name: nginx
    state: present

- name: Deploy Nginx Configuration
  ansible.builtin.template:
    src: nginx.conf.j2
    dest: /etc/nginx/nginx.conf

- name: Restart Nginx
  ansible.builtin.service:
    name: nginx
    state: restarted
  notify: Reload Nginx
```

#### `roles/web_server/handlers/main.yml`
```yaml
- name: Reload Nginx
  ansible.builtin.service:
    name: nginx
    state: reloaded
```

#### `roles/web_server/vars/main.yml`
```yaml
nginx_port: 80
```

#### `roles/web_server/templates/nginx.conf.j2`
```nginx
server {
    listen {{ nginx_port }};
    server_name {{ inventory_hostname }};
    root /var/www/{{ service_name }};
}
```

---

## **4. Playbook to Apply Roles**
Use the inventory and roles to apply configurations to the appropriate groups of hosts.

### Example Playbook
```yaml
- name: Configure all services
  hosts: all
  become: yes

  roles:
    - role: web_server
      when: "'web_servers' in group_names"
    - role: app_server
      when: "'app_servers' in group_names"
    - role: db_server
      when: "'db_servers' in group_names"
```

### Explanation:
- The `when` condition ensures that the correct role is applied to the appropriate group of hosts.
- `group_names` is an Ansible variable that lists all groups a host belongs to.

---

## **5. Overriding Variables**
Ansible follows a **variable precedence hierarchy**. If a variable is defined in multiple places, the one with the highest precedence is used.

### Precedence Order (Highest to Lowest)
1. **Extra vars** (e.g., `ansible-playbook playbook.yml -e "key=value"`)
2. Task-level `vars`.
3. Block-level `vars`.
4. Role `vars` (defined in `roles/role_name/vars/main.yml`).
5. Inventory `host_vars` and `group_vars`.
6. Play-level `vars`.
7. Role `defaults` (defined in `roles/role_name/defaults/main.yml`).

This allows you to override variables at different levels as needed.

---

## **6. Example Use Case**
### Scenario:
- `web_servers` share a common `nginx_port` but override it for specific hosts.
- `app_servers` have a shared database connection string but override it for specific applications.

### Playbook Example
```yaml
- name: Deploy services
  hosts: all
  become: yes

  roles:
    - role: web_server
    - role: app_server
```

### Variable Overrides
#### `group_vars/web_servers.yml`
```yaml
nginx_port: 80
```

#### `host_vars/web1.example.com.yml`
```yaml
nginx_port: 8080
```

#### `group_vars/app_servers.yml`
```yaml
db_connection: "mysql://user:password@db.example.com/app_db"
```

#### `host_vars/app1.example.com.yml`
```yaml
db_connection: "mysql://user:password@db.example.com/custom_db"
```

---

## **7. Using Dynamic Inventories**
If your infrastructure is dynamic (e.g., cloud-based), you can use **dynamic inventory scripts** or plugins (e.g., AWS, Azure, GCP) to generate inventory dynamically.

---

## **8. Summary**
- Use **inventory groups** to define services and their hosts.
- Define **shared variables** in `group_vars` and **overrides** in `host_vars`.
- Organize your logic into **roles** for modularity and reusability.
- Use **variable precedence** to handle overrides effectively.
- Leverage **dynamic inventory** for cloud environments.

This structure ensures scalability, maintainability, and flexibility for managing complex infrastructure using Ansible.