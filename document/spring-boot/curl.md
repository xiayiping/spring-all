# upload file

To upload a file with a `name` and `file path` using `curl`, use the `-F` flag to send a multipart/form-data request. Here’s how you can structure the `curl` command:

---

### **CURL Example**
Suppose your API endpoint is `http://localhost:8080/upload`, and it accepts:
1. A file (`file`) to be uploaded.
2. A `name` field specifying the file name.

Here’s the `curl` command:

```bash
curl -X POST http://localhost:8080/upload \
  -F "file=@/path/to/your/file/example.txt" \
  -F "name=example-file-name"
```

---

### **Explanation**
1. **`-X POST`**:
    - Specifies the HTTP method `POST`.

2. **`-F "field=value"`**:
    - Tells `curl` you're sending multipart/form-data fields.
    - For a file upload field:
        - Use `@` followed by the file path (e.g., `@/path/to/your/file/example.txt`).
        - The `file` field corresponds to the backend's expected field name.
    - For a regular text field like `name`:
        - Just specify the key-value pair (e.g., `name=example-file-name`).

3. **`http://localhost:8080/upload`**:
    - The API endpoint where the file and fields are being sent.

---

### **Real Example**
If the file is located at `/home/user/documents/myfile.txt` and you want to pass `TestFile` as the name:

```bash
curl -X POST http://localhost:8080/upload \
  -F "file=@/home/user/documents/myfile.txt" \
  -F "name=TestFile"
```

On Windows, adjust the file path with double backslashes (`\\`) or forward slashes (`/`):

```bash
curl -X POST http://localhost:8080/upload \
  -F "file=@C:\\path\\to\\file.txt" \
  -F "name=TestFile"
```

---

### **Custom Headers (Optional)**
If your server requires an authorization token or custom headers, include them with the `-H` flag:

```bash
curl -X POST http://localhost:8080/upload \
  -H "Authorization: Bearer my-token" \
  -F "file=@/path/to/your/file/example.txt" \
  -F "name=example-file-name"
```

---

### **Verbose Mode for Debugging**
Add `-v` to the command for detailed logs to debug the request:

```bash
curl -v -X POST http://localhost:8080/upload \
  -F "file=@/path/to/your/file/example.txt" \
  -F "name=example-file-name"
```

---

### **Server Response Example**
Once the file is uploaded successfully, the server might respond with a `200 OK` message like:

```json
{
  "message": "File uploaded successfully!",
  "fileName": "example-file-name",
  "fileSize": 12345
}
```

--- 

This setup will handle most file upload scenarios with `curl`.

