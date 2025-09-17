# Wiivi

VS Code extension for running [SV-COMP](https://sv-comp.sosy-lab.org/) verification tools and
visualizing their witnesses (correctness witnesses / invariants) for C programs.  
Runs one or more verifiers via **fm-weck** and renders their invariants inline in the editor.

## Use the extension

### Prerequisites
- VS Code
- fm-weck installed and working  
  (with its own prerequisites, e.g., a container runtime such as Podman)

### Installing

1. Install [fm-weck](https://gitlab.com/sosy-lab/software/fm-weck)
2. Get the extension package (`.vsix`) by either:
    * Downloading the extension from [latest GitHub Actions build](https://github.com/sws-lab/sv-comp-witness-vscode/actions) → choose a workflow run → Artifacts → `wiivi-plugin`
    * Building the extension (see [Building](#building))
3. Install the extension into VS Code:
```shell
cd VS Code && code --install-extension sv-comp-verifiers-0.0.1.vsix
```

### Quick start

After installing the extension:

1. Open the project in **VS Code** (e.g., calling `code .` in the project root directory).
2. Open the file `standard_strcpy_original-2.i` in VS Code.
3. Locate and open the **Witnesses** panel.
4. In the panel **select** a data model, property, and which tools to run.
5. Click **Analyze** to start analyzing the currently open file.  
   (*Note: the first time run will take time with fm-weck getting the required tools.*)
3. The invariants from the tools should be shown above lines 22, 26, and 31.

## Build / Develop the extension

### Prerequisites

Make sure the following are installed:
- `JDK 17`
- `mvn`
- `npm`
- `nodejs`
- `vsce` (using `npm install -g @vscode/vsce` for example)
- [fm-weck](https://gitlab.com/sosy-lab/software/fm-weck)


 ### Building
To build this extension, run the commands starting from project root directory:
1. `mvn install` (or can use `mvn package -Dmaven.test.skip` if tests are failing)
2. `cd vscode`
3. `npm install` (only needed the first time)
4. `vsce package`


