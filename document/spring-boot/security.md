# customized login

To customize the login page in React, include the CSRF token and configure a custom login URL for Spring Security, follow these steps:

---

### 1. **Default Login URL in Spring Security**
The default login URL in Spring Security is:

```plaintext
POST /login
```

This is used by the default `UsernamePasswordAuthenticationFilter`. To create a custom login URL, you can override this default behavior in your Spring Security configuration.

---

### 2. **Customized Login Page in React**

Below is an example of a React component for a login page. This page includes an input field for the CSRF token, which Spring Security expects by default.

```jsx type=react
import React, { useState, useEffect } from "react";

function LoginPage() {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [csrfToken, setCsrfToken] = useState("");

  // Fetch the CSRF token from the server when the component loads
  useEffect(() => {
    fetch("/api/csrf")
      .then((response) => response.json())
      .then((data) => {
        setCsrfToken(data.csrfToken); // Assume the backend sends the CSRF token as JSON
      })
      .catch((error) => console.error("Error fetching CSRF token:", error));
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();

    const loginData = { username, password };

    try {
      const response = await fetch("/api/auth/custom-login", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "X-CSRF-TOKEN": csrfToken, // Include CSRF token in the request header
        },
        body: JSON.stringify(loginData),
      });

      if (response.ok) {
        const data = await response.json();
        alert(`Login successful! Token: ${data.token}`);
        // Save the token (e.g., in localStorage) and redirect if needed
      } else {
        alert("Login failed! Please check your credentials.");
      }
    } catch (error) {
      console.error("Error during login:", error);
    }
  };

  return (
    <div className="flex items-center justify-center min-h-screen bg-gray-100">
      <div className="w-full max-w-md p-6 bg-white shadow-md rounded-lg">
        <h2 className="text-xl font-bold text-center mb-6">Login</h2>
        <form onSubmit={handleSubmit}>
          <div className="mb-4">
            <label className="block text-sm font-medium mb-1" htmlFor="username">
              Username
            </label>
            <input
              type="text"
              id="username"
              className="w-full px-3 py-2 border rounded-md"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              required
            />
          </div>
          <div className="mb-4">
            <label className="block text-sm font-medium mb-1" htmlFor="password">
              Password
            </label>
            <input
              type="password"
              id="password"
              className="w-full px-3 py-2 border rounded-md"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />
          </div>
          {/* CSRF Token (Hidden Field for Debugging or as Fallback) */}
          <input type="hidden" name="_csrf" value={csrfToken} />
          <button
            type="submit"
            className="w-full py-2 px-4 bg-blue-500 text-white font-semibold rounded-md hover:bg-blue-600 transition"
          >
            Login
          </button>
        </form>
      </div>
    </div>
  );
}

export default LoginPage;
```

**What this does:**
- Fetches the CSRF token from `/api/csrf` when the page loads.
- Includes the CSRF token in the `X-CSRF-TOKEN` header when submitting the login request.
- Sends the login request to a custom URL (`/api/auth/custom-login`).

---

### 3. **Custom Login URL in Spring Security**

You can configure a custom login URL by overriding the default behavior in your `SecurityConfig` class:

```java
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    private final MyUserDetailsService userDetailsService;

    public SecurityConfig(MyUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf().disable() // For now, disable CSRF (can be customized later)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/custom-login", "/api/csrf").permitAll() // Allow CSRF and login endpoints
                .anyRequest().authenticated()
            )
            .formLogin()
                .loginProcessingUrl("/api/auth/custom-login") // Custom login URL
                .permitAll()
            .and()
            .logout()
                .logoutUrl("/api/auth/logout") // Custom logout URL
                .permitAll();

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class)
                .userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder())
                .and()
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

---

### 4. **CSRF Endpoint**

You need to expose an endpoint that provides the CSRF token for your React frontend. Add a controller:

```java
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class CsrfController {

    @GetMapping("/api/csrf")
    public CsrfToken csrf(HttpServletRequest request) {
        return (CsrfToken) request.getAttribute(CsrfToken.class.getName());
    }
}
```

---

### 5. **Test the Flow**
1. Start your Spring Boot application.
2. Open the React login page.
3. Submit the username and password to `/api/auth/custom-login`.
4. Verify that the login works and the JWT or session is returned.

---

### Notes:
- **Custom Login URL:** The `loginProcessingUrl()` in the Spring Security config changes the default `/login` URL to `/api/auth/custom-login`.
- **CSRF Token:** Ensure the CSRF token is included in the request header (`X-CSRF-TOKEN`) or as a hidden field in the form.
- **JWT Token (Optional):** You can replace session-based authentication with JWT if needed (see [previous response](#) for details).

