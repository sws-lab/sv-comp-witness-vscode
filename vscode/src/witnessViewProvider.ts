import * as vscode from "vscode";
import {LanguageClient} from "vscode-languageclient/node";

export class WitnessViewProvider implements vscode.WebviewViewProvider {

    constructor(private context: vscode.ExtensionContext, private lc: LanguageClient) {
    }

    resolveWebviewView(webviewView: vscode.WebviewView) {
        // Set up the webview content
        webviewView.webview.options = {
            enableScripts: true,
        };

        webviewView.webview.html = this.getWebviewContent();

        webviewView.webview.onDidReceiveMessage(
            (message) => {
                switch (message.command) {
                    case 'analyze':
                        message.fileUri = this.getActiveFileUri();
                        this.lc.sendNotification('custom/handleWebviewMessage', message);
                        break;
                }
            },
            null,
            this.context.subscriptions
        );
    }

    private getActiveFileUri() {
        const editor = vscode.window.activeTextEditor;
        if (editor && editor.document.languageId == 'c') {
            return editor.document.uri.toString();
        } else {
            vscode.window.showWarningMessage('Please open a C file (.c, .h, .i) to analyze.');
        }
    }

    private getWebviewContent() : string {
        return `
            <html>
                <head>
                    <style>
                        body {
                            color: var(--vscode-editor-foreground);
                            background-color: var(--vscode-editor-background);
                            font-family: var(--vscode-font-family);
                            font-size: var(--vscode-font-size);
                        }
                        
                        button {
                            background-color: var(--vscode-button-background);
                            color: var(--vscode-button-foreground);
                            font-family: inherit;
                            font-size: inherit;
                            line-height: var(--button-line-height);
                            border: 1px solid var(--button-border);
                            border-radius: 0.3rem;
                            padding: 5px 10px;
                            cursor: pointer;
                        }
                        
                        button:hover {
                            background-color: var(--vscode-button-hoverBackground);
                        }
                        
                        button:disabled {
                            background-color: var(--vscode-button-secondaryBackground);
                            color: var(--vscode-disabledForeground);
                            cursor: not-allowed;
                            opacity: 0.6;
                        }
                        
                        .selection-group {
                            margin-bottom: 20px; /* Adds spacing between sections */
                        }
                        
                        .group-title {
                            margin-bottom: 5px;
                            display: block;
                        }
                        
                        .selectable {
                            padding: 5px;
                            border: 1px solid var(--vscode-button-border);
                            background-color: var(--vscode-button-secondaryBackground);
                            cursor: pointer;
                            max-width: 100px;
                        }
                        
                        .selectable:hover {
                            background-color: var(--vscode-button-hoverBackground);
                        }
                        
                        .selectable[data-selected="true"] {
                            background-color: var(--vscode-button-background);
                            color: var(--vscode-button-foreground);
                        }
                    </style>
                </head>
                <body>            
                    <div class="selection-group">
                    <label class="group-title">Select Data Model:</label>
                        <div class="selectable" data-group="dataModel" data-value="LP64" data-selected="true">LP64</div>
                        <div class="selectable" data-group="dataModel" data-value="ILP32">ILP32</div>
                    </div>
                
                    <div class="selection-group">
                    <label class="group-title">Choose a Property:</label>
                        <div class="selectable" data-group="property" data-value="no-overflow" data-selected="true">No Overflow</div>
                        <div class="selectable" data-group="property" data-value="unreach-call">Unreach Call</div>
                        <div class="selectable" data-group="property" data-value="no-data-race">No Data Race</div>
                    </div>
            
                    <button id="analyzeButton" onclick="analyze()">Analyze</button>
            
                    <script>
                        const vscode = acquireVsCodeApi();
            
                        // Function to send the selected options to the backend
                        function analyze() {
                            // Get the selected data model
                            const dataModelElement = document.querySelector('[data-group="dataModel"][data-selected="true"]');
                            const dataModel = dataModelElement.getAttribute('data-value')
                        
                            // Get the selected property
                            const propertyElement = document.querySelector('[data-group="property"][data-selected="true"]');
                            const property = propertyElement.getAttribute('data-value');
            
                            // Send message to VS Code extension
                            vscode.postMessage({
                                command: 'analyze',
                                dataModel: dataModel,
                                property: property
                            });
                        }
                        
                        // Handle data model selection
                        document.querySelectorAll('[data-group="dataModel"]').forEach(model => {
                            model.addEventListener('click', () => {
                                document.querySelectorAll('[data-group="dataModel"]').forEach(mdl => mdl.setAttribute('data-selected', 'false'));
                                model.setAttribute('data-selected', 'true');
                            });
                        });
                
                        // Handle property selection
                        document.querySelectorAll('[data-group="property"]').forEach(property => {
                            property.addEventListener('click', () => {
                                document.querySelectorAll('[data-group="property"]').forEach(prp => prp.setAttribute('data-selected', 'false'));
                                property.setAttribute('data-selected', 'true');
                            });
                        });
                    </script>
                </body>
            </html>
        `;
    }
}