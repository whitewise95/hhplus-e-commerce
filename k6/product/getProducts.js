import http from 'k6/http';
import { check, sleep } from 'k6';

import {options, DEFAULT_URL} from "../common/option.js";

export { options };

export default function () {
  //상품목록조회 API 호출
  let res = http.get(`${DEFAULT_URL}/api/product`);
  check(res, { '상품목록조회': (r) => r.status === 200 });

  sleep(1); // 사이에 휴식 시간을 둠
}