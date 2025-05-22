import * as vscode from 'vscode';

export class InvariantInfoProvider implements vscode.WebviewViewProvider {
    private _view?: vscode.WebviewView;
    private _data: string = '';

    constructor() {
    }

    // Called when the view is first shown
    resolveWebviewView(view: vscode.WebviewView) {
        this._view = view;
        view.webview.options = {
            enableScripts: true
        };
        view.webview.html = this.getHtml(this._data);
    }

    // Call this to update the webview with new data
    setData(data: any) {
        this._data = JSON.stringify(data, null, 2);
        if (this._view) {
            this._view.webview.html = this.getHtml(this._data);
        }
    }

    private getHtml(data: string): string {
        return `
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <title>Invariant Information</title>
                <style></style>
            </head>
            <body>
                <pre>${data ? this.escapeHtml(data) : 'No data available.'}</pre>
            </body>
            </html>
        `;
    }

    private escapeHtml(unsafe: any): string {
        return String(unsafe)
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#039;");
    }
}
