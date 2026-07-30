
import * as Li9za2lrby5tanM from './skiko.mjs';
import { instantiate } from './arcade-spike-wasm-js.uninstantiated.mjs';


const exports = (await instantiate({
    './skiko.mjs': Li9za2lrby5tanM
})).exports;

export const {
    main,
    memory,
    _initialize,
    startUnitTests
} = exports;

