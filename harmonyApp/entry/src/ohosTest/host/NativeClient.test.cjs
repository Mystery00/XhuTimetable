const assert = require('node:assert/strict');
const { test } = require('node:test');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');

const sdk = process.env.DEVECO_SDK_HOME;
assert.ok(sdk, 'Set DEVECO_SDK_HOME to the DevEco Studio SDK directory');
const ts = require(path.join(sdk, 'default/openharmony/ets/build-tools/ets-loader/node_modules/typescript'));
const source = path.resolve(__dirname, '../../main/ets/platform/NativeClient.ets');

function loadClient(nativeApi = {}, services = {}) {
  const output = ts.transpileModule(fs.readFileSync(source, 'utf8'), {
    compilerOptions: { module: ts.ModuleKind.CommonJS, target: ts.ScriptTarget.ES2021 }
  }).outputText;
  const exports = {};
  const context = { exports, Map, Promise, Error, JSON, Date, setTimeout, clearTimeout,
    require(name) {
      if (name === 'libentry.so') return { default: nativeApi };
      if (name === './OhosServices') return { OhosServices: class { constructor() { return services; } } };
      throw new Error(`Unexpected platform dependency: ${name}`);
    }
  };
  vm.runInNewContext(output, context, { filename: source });
  return exports;
}

function loadRequests() { return loadClient().NativeRequests; }

test('a partially initialized core is disposed after startup fails', async () => {
  let nativeReleased = 0;
  let platformReleased = 0;
  const nativeApi = {
    initializeCore() { throw new Error('partial-startup-details'); },
    disposeCore() { nativeReleased++; }
  };
  const services = {
    setMessageHandler() {},
    getAppInfo() { return '{}'; },
    dispose() { platformReleased++; }
  };
  const NativeClient = loadClient(nativeApi, services).NativeClient;
  const client = new NativeClient({ filesDir: '/files', cacheDir: '/cache' });
  assert.throws(() => client.start(), /应用初始化失败/);
  assert.equal(nativeReleased, 1);
  assert.equal(platformReleased, 1);
  await assert.rejects(client.request('snapshot'), /已关闭/);
});

test('concurrent requests preserve payloads and settle by response id', async () => {
  const NativeRequests = loadRequests();
  const sent = [];
  const requests = new NativeRequests(json => sent.push(JSON.parse(json)));
  const first = requests.request('login', { username: 'student', password: 'private-input' });
  const second = requests.request('snapshot', {});
  assert.equal(sent[0].action, 'login');
  assert.deepEqual(sent[0].payload, { username: 'student', password: 'private-input' });
  assert.notEqual(sent[0].id, sent[1].id);
  requests.receive(JSON.stringify({ id: sent[1].id, data: [1, 2], error: '' }));
  requests.receive(JSON.stringify({ id: sent[0].id, data: { accepted: true }, error: '' }));
  assert.equal(await first, '{"accepted":true}');
  assert.equal(await second, '[1,2]');
  requests.dispose();
});

test('business errors reject the matching request without affecting another request', async () => {
  const NativeRequests = loadRequests();
  const sent = [];
  const requests = new NativeRequests(json => sent.push(JSON.parse(json)));
  const first = requests.request('login', {});
  const rejection = assert.rejects(first, /账号或密码错误/);
  const second = requests.request('snapshot', {});
  requests.receive(JSON.stringify({ id: sent[0].id, data: null, error: '账号或密码错误' }));
  requests.receive(JSON.stringify({ id: sent[1].id, data: false, error: '' }));
  await rejection;
  assert.equal(await second, 'false');
  requests.dispose();
});

test('dispose rejects outstanding and future requests and ignores late responses', async () => {
  const NativeRequests = loadRequests();
  const sent = [];
  const requests = new NativeRequests(json => sent.push(JSON.parse(json)));
  const pending = requests.request('snapshot', {});
  const rejected = assert.rejects(pending, /已关闭/);
  requests.dispose();
  requests.receive(JSON.stringify({ id: sent[0].id, data: 'late', error: '' }));
  await rejected;
  await assert.rejects(requests.request('snapshot', {}), /已关闭/);
  assert.equal(sent.length, 1);
});

test('synchronous native dispatch failures reject without exposing native input', async () => {
  const NativeRequests = loadRequests();
  const requests = new NativeRequests(() => { throw new Error('private-input'); });
  await assert.rejects(requests.request('login', {}), error => {
    assert.equal(error.message, '业务调用启动失败，请重试');
    return true;
  });
  requests.dispose();
});

test('timeouts settle once and a late response cannot complete a new request', async t => {
  t.mock.timers.enable({ apis: ['setTimeout'] });
  const NativeRequests = loadRequests();
  const sent = [];
  const requests = new NativeRequests(json => sent.push(JSON.parse(json)));
  const first = requests.request('snapshot', {});
  const rejected = assert.rejects(first, /请求超时/);
  t.mock.timers.tick(30000);
  await rejected;
  const second = requests.request('snapshot', {});
  requests.receive(JSON.stringify({ id: sent[0].id, data: 'obsolete', error: '' }));
  requests.receive(JSON.stringify({ id: sent[1].id, data: 'current', error: '' }));
  assert.equal(await second, '"current"');
  requests.dispose();
});

test('image selection allows time for the system picker before timing out', async t => {
  t.mock.timers.enable({ apis: ['setTimeout'] });
  const NativeRequests = loadRequests();
  const sent = [];
  const requests = new NativeRequests(json => sent.push(JSON.parse(json)));
  for (const action of ['pickBackground', 'avatar']) {
    const pending = requests.request(action, {});
    t.mock.timers.tick(30000);
    requests.receive(JSON.stringify({ id: sent[sent.length - 1].id, data: '/files/image.png', error: '' }));
    assert.equal(await pending, '"/files/image.png"');
  }
  requests.dispose();
});

test('malformed messages and unknown ids do not consume pending responses', async () => {
  const NativeRequests = loadRequests();
  const sent = [];
  const requests = new NativeRequests(json => sent.push(JSON.parse(json)));
  const pending = requests.request('snapshot', {});
  for (const message of ['{', 'null', '[]', '{}', '{"id":4}', '{"id":"unknown","data":1}']) {
    requests.receive(message);
  }
  requests.receive(JSON.stringify({ id: sent[0].id, data: null, error: '' }));
  assert.equal(await pending, 'null');
  requests.dispose();
});

test('subscriptions can detach independently and stop on dispose', () => {
  const NativeRequests = loadRequests();
  const requests = new NativeRequests(() => {});
  const calls = [];
  const removeFirst = requests.subscribe(() => calls.push('first'));
  requests.subscribe(() => calls.push('second'));
  requests.receive('{"id":"","action":"changed"}');
  removeFirst();
  requests.receive('{"id":"","action":"changed"}');
  requests.dispose();
  requests.receive('{"id":"","action":"changed"}');
  assert.deepEqual(calls, ['first', 'second', 'second']);
});

test('feedback state notifies subscribers without reloading home or consuming responses', async t => {
  const NativeRequests = loadRequests();
  const sent = [];
  const requests = new NativeRequests(json => sent.push(JSON.parse(json)));
  t.after(() => requests.dispose());
  let homeReloads = 0;
  let firstCalls = 0;
  let secondCalls = 0;
  requests.onStateChanged = () => homeReloads++;
  const unsubscribe = requests.subscribe(() => firstCalls++);
  requests.subscribe(() => secondCalls++);
  const pending = requests.request('feedbackStatus', {});
  requests.receive('{"id":"","action":"feedbackState"}');
  assert.equal(firstCalls, 1);
  assert.equal(secondCalls, 1);
  assert.equal(homeReloads, 0);
  unsubscribe();
  requests.receive('{"id":"","action":"feedbackState"}');
  assert.equal(firstCalls, 1);
  assert.equal(secondCalls, 2);
  assert.equal(homeReloads, 0);
  requests.receive(JSON.stringify({ id: sent[0].id, data: { state: 'FAILED' }, error: '' }));
  assert.equal(await pending, '{"state":"FAILED"}');
  requests.receive('{"id":"","action":"changed"}');
  assert.equal(homeReloads, 1);
  assert.equal(secondCalls, 3);
  requests.dispose();
  requests.receive('{"id":"","action":"feedbackState"}');
  requests.receive('{"id":"","action":"changed"}');
  assert.equal(secondCalls, 3);
  assert.equal(homeReloads, 1);
});

test('state notifications trigger observers without settling unrelated requests', async () => {
  const NativeRequests = loadRequests();
  const sent = [];
  const requests = new NativeRequests(json => sent.push(JSON.parse(json)));
  let changes = 0;
  requests.onStateChanged = () => changes++;
  const pending = requests.request('snapshot', {});
  requests.receive('{"id":"","action":"changed"}');
  assert.equal(changes, 1);
  requests.receive(JSON.stringify({ id: sent[0].id, data: 'done', error: '' }));
  assert.equal(await pending, '"done"');
  requests.dispose();
  requests.receive('{"id":"","action":"changed"}');
  assert.equal(changes, 1);
});
