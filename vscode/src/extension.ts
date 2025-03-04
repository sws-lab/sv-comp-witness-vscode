// The module 'vscode' contains the VS Code extensibility API
// Import the module and reference it with the alias vscode in your code below
import * as vscode from 'vscode';
import {LanguageClient, LanguageClientOptions, ServerOptions} from 'vscode-languageclient/node';
import {WitnessViewProvider} from './witnessViewProvider';

// This method is called when your extension is activated
export function activate(context: vscode.ExtensionContext) {
	let script = 'java';
	let args = ['-jar', context.asAbsolutePath('sv-comp-witness-vscode-0.0.1.jar')];

	// Use this for communicating on stdio
	let serverOptions: ServerOptions = {
		run: {command: script, args: args},
		debug: {command: script, args: args},
	};

	let clientOptions: LanguageClientOptions = {
		documentSelector: [{scheme: 'file', language: 'c'}],
		synchronize: {
			fileEvents: [vscode.workspace.createFileSystemWatcher('**/*.c')]
		}
	};

	// Create the language client and start the client.
	let lc = new LanguageClient('sv-comp-verifiers', 'sv-comp-verifiers', serverOptions, clientOptions);
	context.subscriptions.push(lc);

	// Wait until the client is ready before registering commands
	lc.start().then(() => {
		// Register a WebView view
		context.subscriptions.push(
			vscode.window.registerWebviewViewProvider('witnessPanel', new WitnessViewProvider(context, lc))
		);
	}).catch(error => {
		console.error("Error starting language client:", error);
	});


}
