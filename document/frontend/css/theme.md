# Switch Theme

Here's a complete example of theme switching using pure HTML, CSS, and JavaScript:

```html
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Theme Switcher</title>
    <style>
        /* CSS Variables for theming */
        :root {
            --bg-color: #ffffff;
            --text-color: #333333;
            --card-bg: #f5f5f5;
            --border-color: #dddddd;
            --button-bg: #007bff;
            --button-text: #ffffff;
        }

        [data-theme="dark"] {
            --bg-color: #1a1a1a;
            --text-color: #e0e0e0;
            --card-bg: #2d2d2d;
            --border-color: #444444;
            --button-bg: #0056b3;
            --button-text: #ffffff;
        }

        [data-theme="night"] {
            --bg-color: #0f0f23;
            --text-color: #cccccc;
            --card-bg: #1a1a3e;
            --border-color: #2a2a5e;
            --button-bg: #4a4a8e;
            --button-text: #ffffff;
        }

        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        body {
            background-color: var(--bg-color);
            color: var(--text-color);
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
            transition: background-color 0.3s ease, color 0.3s ease;
            min-height: 100vh;
            display: flex;
            justify-content: center;
            align-items: center;
            padding: 20px;
        }

        .container {
            max-width: 600px;
            width: 100%;
        }

        .card {
            background-color: var(--card-bg);
            border: 1px solid var(--border-color);
            border-radius: 12px;
            padding: 30px;
            transition: background-color 0.3s ease, border-color 0.3s ease;
            box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
        }

        h1 {
            margin-bottom: 20px;
            font-size: 2rem;
        }

        p {
            margin-bottom: 30px;
            line-height: 1.6;
        }

        .theme-switcher {
            display: flex;
            gap: 10px;
            flex-wrap: wrap;
            margin-bottom: 20px;
        }

        .theme-button {
            background-color: var(--button-bg);
            color: var(--button-text);
            border: none;
            padding: 12px 24px;
            border-radius: 6px;
            cursor: pointer;
            font-size: 16px;
            transition: opacity 0.2s ease, transform 0.1s ease;
        }

        .theme-button:hover {
            opacity: 0.9;
        }

        .theme-button:active {
            transform: scale(0.98);
        }

        .theme-button.active {
            box-shadow: 0 0 0 3px rgba(0, 123, 255, 0.3);
        }

        .toggle-container {
            display: flex;
            align-items: center;
            gap: 15px;
            margin-top: 20px;
        }

        /* Toggle Switch */
        .toggle-switch {
            position: relative;
            width: 60px;
            height: 30px;
            background-color: var(--border-color);
            border-radius: 30px;
            cursor: pointer;
            transition: background-color 0.3s ease;
        }

        .toggle-switch.active {
            background-color: var(--button-bg);
        }

        .toggle-slider {
            position: absolute;
            top: 3px;
            left: 3px;
            width: 24px;
            height: 24px;
            background-color: white;
            border-radius: 50%;
            transition: transform 0.3s ease;
        }

        .toggle-switch.active .toggle-slider {
            transform: translateX(30px);
        }

        .info {
            margin-top: 30px;
            padding: 15px;
            background-color: var(--bg-color);
            border-radius: 8px;
            border: 1px solid var(--border-color);
            font-size: 14px;
        }

        .auto-theme-label {
            font-weight: 600;
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="card">
            <h1>🎨 Theme Switcher</h1>
            <p>Switch between light, dark, and night themes. The theme preference is saved in localStorage and will persist across page reloads.</p>

            <div class="theme-switcher">
                <button class="theme-button" data-theme="light">☀️ Light</button>
                <button class="theme-button" data-theme="dark">🌙 Dark</button>
                <button class="theme-button" data-theme="night">🌃 Night</button>
            </div>

            <div class="toggle-container">
                <span class="auto-theme-label">Auto (based on time):</span>
                <div class="toggle-switch" id="autoToggle">
                    <div class="toggle-slider"></div>
                </div>
            </div>

            <div class="info">
                <strong>Current Theme:</strong> <span id="currentTheme">Light</span><br>
                <strong>Local Time:</strong> <span id="currentTime"></span><br>
                <strong>Auto Mode:</strong> <span id="autoMode">Off</span>
            </div>
        </div>
    </div>

    <script>
        // Theme management
        const ThemeManager = {
            // Get saved theme from localStorage
            getSavedTheme: function() {
                return localStorage.getItem('theme') || 'light';
            },

            // Save theme to localStorage
            saveTheme: function(theme) {
                localStorage.setItem('theme', theme);
            },

            // Get auto mode setting
            getAutoMode: function() {
                return localStorage.getItem('autoMode') === 'true';
            },

            // Save auto mode setting
            saveAutoMode: function(enabled) {
                localStorage.setItem('autoMode', enabled);
            },

            // Apply theme to document
            applyTheme: function(theme) {
                document.documentElement.setAttribute('data-theme', theme);
                this.updateUI(theme);
            },

            // Get theme based on current time (day/night)
            getAutoTheme: function() {
                const hour = new Date().getHours();
                // Night theme: 10 PM - 6 AM (22:00 - 6:00)
                if (hour >= 22 || hour < 6) {
                    return 'night';
                }
                // Dark theme: 6 PM - 10 PM (18:00 - 22:00)
                else if (hour >= 18) {
                    return 'dark';
                }
                // Light theme: 6 AM - 6 PM (6:00 - 18:00)
                else {
                    return 'light';
                }
            },

            // Update UI elements
            updateUI: function(theme) {
                // Update active button
                document.querySelectorAll('.theme-button').forEach(btn => {
                    btn.classList.remove('active');
                    if (btn.dataset.theme === theme) {
                        btn.classList.add('active');
                    }
                });

                // Update current theme display
                document.getElementById('currentTheme').textContent = 
                    theme.charAt(0).toUpperCase() + theme.slice(1);
            },

            // Update time display
            updateTime: function() {
                const now = new Date();
                const timeString = now.toLocaleTimeString('en-US', {
                    hour: '2-digit',
                    minute: '2-digit',
                    second: '2-digit'
                });
                document.getElementById('currentTime').textContent = timeString;
            },

            // Initialize theme
            init: function() {
                const autoMode = this.getAutoMode();
                
                if (autoMode) {
                    const autoTheme = this.getAutoTheme();
                    this.applyTheme(autoTheme);
                    document.getElementById('autoToggle').classList.add('active');
                    document.getElementById('autoMode').textContent = 'On';
                } else {
                    const savedTheme = this.getSavedTheme();
                    this.applyTheme(savedTheme);
                    document.getElementById('autoMode').textContent = 'Off';
                }

                this.updateTime();
                // Update time every second
                setInterval(() => this.updateTime(), 1000);
                
                // Check for auto theme updates every minute
                if (autoMode) {
                    setInterval(() => {
                        if (this.getAutoMode()) {
                            const autoTheme = this.getAutoTheme();
                            this.applyTheme(autoTheme);
                        }
                    }, 60000); // Check every minute
                }
            }
        };

        // Event listeners
        document.querySelectorAll('.theme-button').forEach(button => {
            button.addEventListener('click', function() {
                const theme = this.dataset.theme;
                ThemeManager.applyTheme(theme);
                ThemeManager.saveTheme(theme);
                
                // Turn off auto mode when manually selecting theme
                ThemeManager.saveAutoMode(false);
                document.getElementById('autoToggle').classList.remove('active');
                document.getElementById('autoMode').textContent = 'Off';
            });
        });

        // Auto toggle switch
        document.getElementById('autoToggle').addEventListener('click', function() {
            const isActive = this.classList.toggle('active');
            ThemeManager.saveAutoMode(isActive);
            
            if (isActive) {
                const autoTheme = ThemeManager.getAutoTheme();
                ThemeManager.applyTheme(autoTheme);
                ThemeManager.saveTheme(autoTheme);
                document.getElementById('autoMode').textContent = 'On';
            } else {
                document.getElementById('autoMode').textContent = 'Off';
            }
        });

        // Initialize on page load
        ThemeManager.init();

        // Optional: Listen for system theme changes
        if (window.matchMedia) {
            window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', e => {
                if (ThemeManager.getAutoMode()) {
                    const autoTheme = ThemeManager.getAutoTheme();
                    ThemeManager.applyTheme(autoTheme);
                }
            });
        }
    </script>
</body>
</html>
```

## Key Features:

1. **CSS Variables**: Define all theme colors in `:root` and theme-specific selectors
2. **LocalStorage**: Saves user preference and persists across page reloads
3. **Auto Mode**: Automatically switches theme based on time of day:
    - Light: 6 AM - 6 PM
    - Dark: 6 PM - 10 PM
    - Night: 10 PM - 6 AM
4. **Smooth Transitions**: CSS transitions for color changes
5. **Toggle Switch**: Visual toggle for auto mode
6. **Real-time Clock**: Shows current time
7. **Active State**: Highlights the currently selected theme

You can customize the time ranges in the `getAutoTheme()` function and add more themes by adding new `[data-theme="..."]` selectors in the CSS.