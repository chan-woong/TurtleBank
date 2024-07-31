const dayjs = require('dayjs');
const utc = require('dayjs/plugin/utc');
const timezone = require('dayjs/plugin/timezone');

dayjs.extend(utc);
dayjs.extend(timezone);

const seoulTime = () => dayjs().tz('Asia/Seoul').format('YYYY-MM-DD HH:mm:ss');
const simpleTime = () => dayjs().tz('Asia/Seoul').format('YYYY-MM-DD');

Object.defineProperties(exports, {
    seoultime: {
        get() {
            return seoulTime();
        },
        enumerable: true
    },
    simpletime: {
        get() {
            return simpleTime();
        },
        enumerable: true
    }
});