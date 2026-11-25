const assert = require('node:assert/strict');
const { test } = require('node:test');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');
const sdk = process.env.DEVECO_SDK_HOME;
assert.ok(sdk, 'Set DEVECO_SDK_HOME to the DevEco Studio SDK directory');
const ts = require(path.join(sdk, 'default/openharmony/ets/build-tools/ets-loader/node_modules/typescript'));
const source = path.resolve(__dirname, '../../main/ets/platform/OhosServices.ets');

class Socket {
  listeners = new Map();
  closeCount = 0;
  sent = [];
  sendResult = Promise.resolve(true);
  on(name, callback) { this.listeners.set(name, callback); }
  off(name) { this.listeners.delete(name); }
  connect(url, options) { this.url = url; this.options = options; return Promise.resolve(true); }
  send(text) { this.sent.push(text); return this.sendResult; }
  close() { this.closeCount++; return Promise.resolve(true); }
  emit(name, ...values) { this.listeners.get(name)?.(...values); }
}

function createServices(options = {}) {
  const sockets = [];
  const logs = [];
  const colors = [];
  const opened = [];
  const exports = {};
  const kit = {
    ConfigurationConstant: { ColorMode: { COLOR_MODE_NOT_SET: -1, COLOR_MODE_DARK: 0, COLOR_MODE_LIGHT: 1 } },
    url: { URL },
    webSocket: { createWebSocket() {
      const socket = options.createSocket ? options.createSocket() : new Socket();
      sockets.push(socket);
      return socket;
    } },
    hilog: { error(...values) { logs.push(values); }, warn(...values) { logs.push(values); } }
  };
  const output = ts.transpileModule(fs.readFileSync(source, 'utf8'), {
    compilerOptions: { module: ts.ModuleKind.CommonJS, target: ts.ScriptTarget.ES2021 }
  }).outputText;
  vm.runInNewContext(output, { exports, Map, Set, Promise, Error, JSON, Date, Array, setTimeout, clearTimeout,
    require(name) {
      if (name.startsWith('@kit.')) return kit;
      if (name === './LoginEncryption') return {};
      throw new Error(`Unexpected dependency: ${name}`);
    }
  }, { filename: source });
  const services = new exports.OhosServices({
    getApplicationContext() { return { setColorMode(value) { colors.push(value); } }; },
    openLink(value) { opened.push(value); return Promise.resolve(); }
  });
  return { services, sockets, colors, opened, logs };
}

const connection = JSON.stringify({ url: 'wss://example.com/feedback?session=test', headers: [{ name: 'X-Test', value: 'test' }] });

test('theme setting maps modes to supported system values', async () => {
  const { services, colors } = createServices();
  for (const mode of ['ON', 'OFF', 'AUTO', 'MATERIAL_YOU']) await services.setColorMode(mode);
  assert.deepEqual(colors, [0, 1, -1, -1]);
  await assert.rejects(services.setColorMode('wrong'));
  assert.equal(colors.length, 4);
  services.dispose();
});

test('external navigation rejects executable, credential-bearing and malformed URLs', async () => {
  const { services, opened } = createServices();
  for (const value of ['javascript:alert(1)', 'data:text/html,test', 'file:///data/a', 'https://',
    'https://user:password@example.com/', 'https://example.com/\nwrong']) {
    await assert.rejects(services.openUrl(value));
  }
  assert.equal(opened.length, 0);
  await services.openUrl('https://example.com/docs');
  assert.deepEqual(opened, ['https://example.com/docs']);
  services.dispose();
});

test('platform message delivery ends when the service is disposed', async () => {
  const { services } = createServices();
  const messages = [];
  services.setMessageHandler(value => messages.push(value));
  await services.nativeMessage('{"id":"1"}');
  services.dispose();
  services.setMessageHandler(value => messages.push(value));
  await services.nativeMessage('{"id":"2"}');
  assert.deepEqual(messages, ['{"id":"1"}']);
});

test('feedback waits for handshake, reuses connection and reports new messages', async () => {
  const { services, sockets } = createServices();
  const messages = [];
  services.setMessageHandler(value => messages.push(JSON.parse(value)));
  const connected = services.connectFeedback(connection);
  sockets[0].emit('open', undefined, {});
  assert.equal(await connected, '');
  await services.connectFeedback(connection);
  assert.equal(sockets.length, 1);
  assert.equal(await services.sendFeedback('反馈内容'), '');
  assert.deepEqual(sockets[0].sent, ['反馈内容']);
  sockets[0].emit('message', undefined, 'server-message');
  assert.deepEqual(messages, [
    { id: '', action: 'feedbackState' },
    { id: '', action: 'feedbackState' },
    { id: '', action: 'changed' }
  ]);
  services.dispose();
  assert.equal(sockets[0].closeCount, 1);
});

test('account switching rejects old handshake and retires previous socket', async () => {
  const { services, sockets } = createServices();
  const first = services.connectFeedback(connection);
  const rejected = assert.rejects(first, /连接已关闭/);
  const second = services.connectFeedback(JSON.stringify({ url: 'wss://example.com/feedback?session=other', headers: [] }));
  await rejected;
  assert.equal(sockets[0].closeCount, 1);
  sockets[1].emit('open', undefined, {});
  assert.equal(await second, '');
  services.dispose();
});

test('closing feedback rejects pending sends and future sends', async () => {
  const { services, sockets } = createServices();
  const connected = services.connectFeedback(connection);
  sockets[0].emit('open', undefined, {});
  await connected;
  sockets[0].sendResult = new Promise(() => {});
  const sent = services.sendFeedback('pending');
  const rejected = assert.rejects(sent, /连接已关闭/);
  await services.closeFeedback('');
  await rejected;
  await assert.rejects(services.sendFeedback('after-close'));
  services.dispose();
});

test('WebSocket errors reject an incomplete handshake without exposing server details', async () => {
  const { services, sockets } = createServices();
  const connected = services.connectFeedback(connection);
  const rejected = assert.rejects(connected, error => {
    assert.equal(error.message, '反馈连接失败，请稍后重试');
    return true;
  });
  sockets[0].emit('error', { code: 100, message: 'private session details' });
  await rejected;
  services.dispose();
});

test('a disconnected feedback socket does not trigger a history reload loop', async () => {
  const { services, sockets } = createServices();
  const events = [];
  services.setMessageHandler(value => events.push(JSON.parse(value)));
  const connected = services.connectFeedback(connection);
  sockets[0].emit('open', undefined, {});
  await connected;
  sockets[0].emit('close', undefined, { code: 1006 });
  assert.deepEqual(events, Array(3).fill({ id: '', action: 'feedbackState' }));
  for (let index = 0; index < 3; index++) {
    assert.deepEqual(JSON.parse(await services.feedbackSnapshot('')), {
      state: 'FAILED', message: '反馈连接失败，请稍后重试', events: []
    });
  }
  assert.equal(sockets.length, 1);
  await assert.rejects(services.sendFeedback('after-close'));
  services.dispose();
});

async function snapshot(services) {
  assert.equal(typeof services.feedbackSnapshot, 'function', 'feedbackSnapshot must expose real transport state');
  return JSON.parse(await services.feedbackSnapshot(''));
}

test('feedback snapshot follows handshake events and drains only string frames', async t => {
  const { services, sockets, logs } = createServices();
  t.after(() => services.dispose());
  assert.deepEqual(await snapshot(services), { state: 'DISCONNECTED', message: '反馈连接已关闭', events: [] });
  const notifications = [];
  services.setMessageHandler(value => notifications.push(JSON.parse(value)));
  const connected = services.connectFeedback(connection);
  assert.deepEqual(await snapshot(services), { state: 'CONNECTING', message: '反馈连接中', events: [] });
  // connect() 返回 true 只表示发起成功，open 才表示握手完成。
  await Promise.resolve();
  assert.equal((await snapshot(services)).state, 'CONNECTING');
  sockets[0].emit('open', undefined, {});
  await connected;
  assert.deepEqual(await snapshot(services), { state: 'CONNECTED', message: '', events: [] });
  const frame = '{"contentType":"SYSTEM","content":"opaque-system-event"}';
  sockets[0].emit('message', undefined, frame);
  sockets[0].emit('message', undefined, new ArrayBuffer(2));
  sockets[0].emit('message', undefined, 'private-user-text');
  assert.deepEqual(await snapshot(services), { state: 'CONNECTED', message: '', events: [frame, 'private-user-text'] });
  assert.deepEqual((await snapshot(services)).events, []);
  assert.deepEqual(notifications.map(value => value.action), ['feedbackState', 'feedbackState', 'changed', 'changed']);
  assert.deepEqual(logs, []);
});

test('feedback buffers only the newest 128 frames and consumes each once', async t => {
  const { services, sockets } = createServices();
  t.after(() => services.dispose());
  const connected = services.connectFeedback(connection);
  sockets[0].emit('open', undefined, {});
  await connected;
  for (let index = 0; index < 130; index++) sockets[0].emit('message', undefined, `frame-${index}`);
  const result = await snapshot(services);
  assert.equal(result.events.length, 128);
  assert.equal(result.events[0], 'frame-2');
  assert.equal(result.events[127], 'frame-129');
  assert.deepEqual((await snapshot(services)).events, []);
});

test('intentional close clears unread frames and ignores late callbacks', async t => {
  const { services, sockets } = createServices();
  t.after(() => services.dispose());
  const notifications = [];
  services.setMessageHandler(value => notifications.push(JSON.parse(value).action));
  const connected = services.connectFeedback(connection);
  sockets[0].emit('open', undefined, {});
  await connected;
  sockets[0].emit('message', undefined, 'unread');
  const lateMessage = sockets[0].listeners.get('message');
  const lateError = sockets[0].listeners.get('error');
  const lateOpen = sockets[0].listeners.get('open');
  await services.closeFeedback('');
  lateMessage(undefined, 'late');
  lateError({ message: 'private' });
  lateOpen(undefined, {});
  await services.closeFeedback('');
  assert.deepEqual(await snapshot(services), { state: 'DISCONNECTED', message: '反馈连接已关闭', events: [] });
  assert.deepEqual(notifications, ['feedbackState', 'feedbackState', 'changed', 'feedbackState']);
  assert.equal(sockets[0].listeners.size, 0);
  assert.equal(sockets[0].closeCount, 1);
});

test('normal remote close is disconnected and never a new message', async t => {
  const { services, sockets } = createServices();
  t.after(() => services.dispose());
  const connected = services.connectFeedback(connection);
  sockets[0].emit('open', undefined, {});
  await connected;
  const notifications = [];
  services.setMessageHandler(value => notifications.push(JSON.parse(value).action));
  sockets[0].emit('close', undefined, { code: 1000, reason: 'private-server-reason' });
  assert.deepEqual(await snapshot(services), { state: 'DISCONNECTED', message: '反馈连接已关闭', events: [] });
  assert.deepEqual(notifications, ['feedbackState']);
});

for (const event of ['open', 'message', 'error', 'close']) {
  test(`feedback ${event} errors expose a fixed FAILED status and release the handshake`, async t => {
    const { services, sockets, logs } = createServices();
    t.after(() => services.dispose());
    const connected = services.connectFeedback(connection);
    const rejection = assert.rejects(connected, /反馈连接失败，请稍后重试/);
    sockets[0].emit(event, { code: 100, message: 'wss://private.invalid/?token=private' }, 'private-frame');
    await rejection;
    assert.deepEqual(await snapshot(services), { state: 'FAILED', message: '反馈连接失败，请稍后重试', events: [] });
    assert.equal(sockets[0].listeners.size, 0);
    assert.equal(sockets[0].closeCount, 1);
    assert.deepEqual(logs, []);
  });
}

for (const failure of ['create', 'connect-throw', 'connect-reject', 'connect-false']) {
  test(`feedback ${failure} failure becomes FAILED without leaking SDK details`, async t => {
    const { services } = createServices({ createSocket() {
      if (failure === 'create') throw new Error('private-sdk-details');
      const socket = new Socket();
      socket.connect = () => {
        if (failure === 'connect-throw') throw new Error('private-sdk-details');
        return failure === 'connect-reject' ? Promise.reject(new Error('private-sdk-details')) : Promise.resolve(false);
      };
      return socket;
    } });
    t.after(() => services.dispose());
    await assert.rejects(services.connectFeedback(connection), /反馈连接失败，请稍后重试/);
    assert.deepEqual(await snapshot(services), { state: 'FAILED', message: '反馈连接失败，请稍后重试', events: [] });
  });
}

test('handshake timeout updates state and a late open cannot revive the socket', async t => {
  t.mock.timers.enable({ apis: ['setTimeout'] });
  const { services, sockets } = createServices();
  t.after(() => services.dispose());
  const connected = services.connectFeedback(connection);
  const rejection = assert.rejects(connected, /反馈连接失败，请稍后重试/);
  const lateOpen = sockets[0].listeners.get('open');
  t.mock.timers.tick(20000);
  await rejection;
  lateOpen(undefined, {});
  assert.equal((await snapshot(services)).state, 'FAILED');
  assert.equal(sockets[0].closeCount, 1);
  assert.equal(sockets[0].listeners.size, 0);
});

test('send timeout settles once, releases transport and reports FAILED', async t => {
  t.mock.timers.enable({ apis: ['setTimeout'] });
  const { services, sockets } = createServices();
  t.after(() => services.dispose());
  const connected = services.connectFeedback(connection);
  sockets[0].emit('open', undefined, {});
  await connected;
  let finish;
  sockets[0].sendResult = new Promise(resolve => { finish = resolve; });
  const sent = services.sendFeedback('private');
  const rejection = assert.rejects(sent, /反馈发送超时/);
  // 跨 vm 的 await 需要完成 thenable 接续，确认 send 已启动后再推进虚拟时钟。
  await new Promise(resolve => setImmediate(resolve));
  assert.deepEqual(sockets[0].sent, ['private']);
  t.mock.timers.tick(20000);
  await rejection;
  finish(true);
  assert.equal((await snapshot(services)).state, 'FAILED');
  assert.equal(sockets[0].closeCount, 1);
  await assert.rejects(services.sendFeedback('after-timeout'), /已关闭/);
});

for (const action of ['close', 'dispose']) {
  test(`a subscriber can ${action} during CONNECTING without starting a socket afterward`, async t => {
    t.mock.timers.enable({ apis: ['setTimeout'] });
    const { services, sockets } = createServices();
    t.after(() => services.dispose());
    let first = true;
    services.setMessageHandler(() => {
      if (!first) return;
      first = false;
      if (action === 'dispose') services.dispose();
      else void services.closeFeedback('');
    });
    const rejection = assert.rejects(services.connectFeedback(connection), /连接已关闭/);
    t.mock.timers.tick(20000);
    await rejection;
    assert.deepEqual(await snapshot(services), { state: 'DISCONNECTED', message: '反馈连接已关闭', events: [] });
    for (const socket of sockets) {
      assert.equal(socket.url, undefined);
      assert.equal(socket.closeCount, 1);
      assert.equal(socket.listeners.size, 0);
    }
  });
}

for (const failure of ['send-false', 'send-reject', 'send-throw']) {
  test(`${failure} releases the connected transport and marks it FAILED`, async t => {
    const { services, sockets } = createServices();
    t.after(() => services.dispose());
    const connected = services.connectFeedback(connection);
    sockets[0].emit('open', undefined, {});
    await connected;
    sockets[0].send = () => {
      if (failure === 'send-throw') throw new Error('private-details');
      return failure === 'send-reject' ? Promise.reject(new Error('private-details')) : Promise.resolve(false);
    };
    await assert.rejects(services.sendFeedback('private'), /反馈发送失败，请稍后重试/);
    assert.deepEqual(await snapshot(services), { state: 'FAILED', message: '反馈连接失败，请稍后重试', events: [] });
    assert.equal(sockets[0].closeCount, 1);
    assert.equal(sockets[0].listeners.size, 0);
  });
}

test('replacement connections discard old frames and reject stale callbacks', async t => {
  const { services, sockets } = createServices();
  t.after(() => services.dispose());
  const first = services.connectFeedback(connection);
  sockets[0].emit('open', undefined, {});
  await first;
  sockets[0].emit('message', undefined, 'old-account');
  const lateMessage = sockets[0].listeners.get('message');
  const lateError = sockets[0].listeners.get('error');
  const second = services.connectFeedback(JSON.stringify({ url: 'wss://example.com/feedback?session=other', headers: [] }));
  lateMessage(undefined, 'late-old-account');
  lateError({ message: 'private' });
  assert.equal((await snapshot(services)).state, 'CONNECTING');
  sockets[1].emit('open', undefined, {});
  await second;
  assert.deepEqual(await snapshot(services), { state: 'CONNECTED', message: '', events: [] });
});

for (const phase of ['connecting', 'sending', 'failed']) {
  test(`dispose clears feedback during ${phase} and releases resources once`, async t => {
    const { services, sockets } = createServices();
    t.after(() => services.dispose());
    const connected = services.connectFeedback(connection);
    let rejection;
    if (phase === 'connecting') {
      rejection = assert.rejects(connected, /已关闭/);
    } else {
      sockets[0].emit('open', undefined, {});
      await connected;
      sockets[0].emit('message', undefined, 'unread-private');
      if (phase === 'sending') {
        sockets[0].sendResult = new Promise(() => {});
        rejection = assert.rejects(services.sendFeedback('pending'), /已关闭/);
        await new Promise(resolve => setImmediate(resolve));
        assert.deepEqual(sockets[0].sent, ['pending']);
      } else {
        sockets[0].emit('error', { message: 'private' });
      }
    }
    const notifications = [];
    services.setMessageHandler(value => notifications.push(value));
    services.dispose();
    services.dispose();
    if (rejection) await rejection;
    assert.deepEqual(await snapshot(services), { state: 'DISCONNECTED', message: '反馈连接已关闭', events: [] });
    assert.equal(sockets[0].closeCount, 1);
    assert.equal(sockets[0].listeners.size, 0);
    assert.deepEqual(notifications, []);
    await assert.rejects(services.connectFeedback(connection), /已释放/);
  });
}

test('feedback rejects insecure endpoints and header injection before connecting', async () => {
  const { services, sockets } = createServices();
  for (const payload of ['{', 'null', '{}', JSON.stringify({ url: 'ws://example.com', headers: [] }),
    JSON.stringify({ url: 'wss://example.com', headers: [{ name: 'X-Test', value: 'x\r\ny' }] })]) {
    await assert.rejects(services.connectFeedback(payload));
  }
  assert.equal(sockets.length, 0);
  services.dispose();
});
