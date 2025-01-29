# sv-comp-witness-vscode

Integration of [SV-Comp](https://sv-comp.sosy-lab.org/) verification tools for C into VS Code
for visualizing their combined verification witnesses.

## Developing

Make sure the following are installed: `JDK 17`, `mvn`, `npm`, `nodejs`, `@vscode/vsce`.

To build this extension, run the commands:
~~~
mvn install
cd vscode
npm install
npm install -g vsce
vsce package
~~~

## Installing

Install the extension into VS Code with `code --install-extension sv-comp-verifiers-0.0.1.vsix`.

## Testing

1. Open the project in VS Code after installing the extension.
2. Open the file `examples/safe-program-example.c` in VS Code.
3. The invariant should be shown above line 18.