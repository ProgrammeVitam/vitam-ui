// Starts the Angular CLI MCP server of the workspace (run from ui/ui-frontend).
// The Docker MCP gateway advertises the "roots" capability but returns no roots, so `list_projects`
// would find no workspace: hide this capability so that the server falls back to its working directory.
import { spawn } from 'node:child_process';
import { createInterface } from 'node:readline';

const server = spawn(process.execPath, ['node_modules/@angular/cli/bin/ng.js', 'mcp', '--experimental-tool', 'all'], {
  stdio: ['pipe', 'inherit', 'inherit'],
});

createInterface({ input: process.stdin })
  .on('line', (line) => {
    try {
      const message = JSON.parse(line);
      if (message.method === 'initialize' && message.params?.capabilities?.roots) {
        delete message.params.capabilities.roots;
        line = JSON.stringify(message);
      }
    } catch {
      // Not JSON: forward as is
    }
    server.stdin.write(line + '\n');
  })
  .on('close', () => server.stdin.end());

server.on('exit', (code) => process.exit(code ?? 0));
