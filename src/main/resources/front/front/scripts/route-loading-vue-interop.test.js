const assert = require('assert')
const fs = require('fs')
const path = require('path')
const vm = require('vm')

const modulePath = path.join(__dirname, '../src/utils/route-loading.js')
const moduleSource = fs.readFileSync(modulePath, 'utf8')

function loadRouteLoadingWithVueMock(vueMock) {
  const module = { exports: {} }
  const sandbox = {
    module,
    exports: module.exports,
    require(id) {
      if (id === 'vue') {
        return vueMock
      }
      return require(id)
    },
    console,
    setTimeout,
    clearTimeout,
    Date,
  }

  vm.runInNewContext(moduleSource, sandbox, { filename: modulePath })
  return module.exports
}

function runTest(name, testFn) {
  try {
    testFn()
    console.log(`PASS ${name}`)
  } catch (error) {
    console.error(`FAIL ${name}`)
    throw error
  }
}

runTest('兼容默认导出包装的 Vue 模块', () => {
  const observableCalls = []
  const wrappedVue = {
    default: {
      observable(state) {
        observableCalls.push(state)
        return state
      },
    },
  }

  const routeLoading = loadRouteLoadingWithVueMock(wrappedVue)

  assert.strictEqual(observableCalls.length, 1)
  assert.strictEqual(routeLoading.routeLoadingState.visible, false)
  assert.strictEqual(routeLoading.routeLoadingState.phase, 'idle')
  assert.strictEqual(typeof routeLoading.beginRouteLoading, 'function')
  assert.strictEqual(typeof routeLoading.finishRouteLoading, 'function')
  assert.strictEqual(typeof routeLoading.failRouteLoading, 'function')
})
