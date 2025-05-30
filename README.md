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
2. Open the file `standard_strcpy_original-2.i` in VS Code.
3. The combined invariants should be shown above lines 22, 26, and 31.

## Building native CPAchecker

  NOTE: You need to have [GraalVM 22.04](https://www.oracle.com/java/graalvm/) installed to build the native CPAchecker.

To build the native CPAchecker, run the following commands:
```shell
make cpachecker
```

This command takes a while (~10min on a ThinkPad T14) and will produce a native binary in the `lib/cpachecker-native` directory.
