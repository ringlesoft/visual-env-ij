# Visual Env

![Build](https://img.shields.io/github/workflow/status/ringlesoft/visual-env-ij/Build)
![Version](https://img.shields.io/jetbrains/plugin/v/com.ringlesoft.visualenv)
![Downloads](https://img.shields.io/jetbrains/plugin/d/com.ringlesoft.visualenv)
![Rating](https://img.shields.io/jetbrains/plugin/r/rating/com.ringlesoft.visualenv)

<!-- Plugin description -->
**Visual Env** is an IntelliJ plugin that helps developers visualize and manage environment variables within their projects. 
This plugin makes it easy to see which environment variables are in use, edit their values through a user-friendly interface, and run environment-related CLI commands directly from your IDE.
<!-- Plugin description end -->

## Features

### Environment Variable Management
- View and edit environment variables in a structured, user-friendly interface
- Group variables by category for better organization
- Support for various data types (string, boolean, number) with appropriate input controls
- Filter and search capabilities to quickly find specific variables

### Framework-Specific Support
- **Laravel Projects**: Automatic detection of Laravel projects
- Create `.env` files from `.env.example` templates with a single click
- Run artisan commands related to environment configuration
- Predefined variable definitions for common Laravel environment variables

### CLI Command Integration
- Execute environment-related CLI commands directly from the IDE
- Framework-specific command suggestions based on project type
- Command history and output display
- Parameter support with validation

## Installation

### From JetBrains Marketplace
- Open your IDE settings (File | Settings | Plugins)
- Search for "Visual Env"
- Click "Install" and restart your IDE

### Manual Installation
1. Download the latest release from the [releases page](https://github.com/ringlesoft/visual-env-ij/releases)
2. Open your IDE settings (File | Settings | Plugins)
3. Click the gear icon and select "Install Plugin from Disk..."
4. Select the downloaded file and restart your IDE

## Getting Started

1. Open your project in any JetBrains IDE
2. Look for the "Visual Env" tool window on the right side of your IDE
3. The plugin will automatically detect your project type and load any existing environment variables
4. If no `.env` file exists but an `.env.example` is found, you'll be prompted to create one

## Usage

### Viewing Environment Variables
- Environment variables are displayed in the "Env Editor" tab, grouped by category
- Toggle between different view modes using the toolbar buttons
- Use the search field to filter variables by name or value

### Editing Variables
- Click on any variable value to edit it
- Changes are saved automatically to your `.env` file
- Special controls are provided for boolean variables (toggles) and enumerated types (dropdowns)

### Running CLI Commands
- Navigate to the "CLI Actions" tab to view available commands for your project type
- Click on a command to execute it
- View command output directly in the tool window
- For commands requiring parameters, input fields will appear automatically

## Supported Frameworks

### Laravel
- Full support for Laravel projects with predefined environment variables
- Integrated artisan commands for environment management
- Automatic Laravel project detection

### Node.js/npm 
- Full support for Node.js/npm projects with types

### Django
- Full support for Django projects with types

## Requirements
- IntelliJ Platform IDEs build 224+
- Project must use environment variables in standard formats (`.env` files)

## Contributing
Contributions are welcome! Please feel free to submit a Pull Request.

## License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments
- Thanks to the JetBrains team for their excellent SDK
- Contributors and users who have provided valuable feedback

---

Plugin based on the [IntelliJ Platform Plugin Template](https://github.com/JetBrains/intellij-platform-plugin-template).
