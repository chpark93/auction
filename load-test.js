import http from 'k6/http';
import {check} from 'k6';

export const options = {
  scenarios: {
    high_load: {
      executor: 'constant-arrival-rate',
      rate: 1000, // 초당 1000개 요청
      timeUnit: '1s',
      duration: '10s', // 10초 동안 유지 (총 10,000건)
      preAllocatedVUs: 100, // 미리 할당할 가상 유저 수
      maxVUs: 1000, // 최대 가상 유저 수
    },
  },
};

export default function () {
  const auctionId = 1;
  const userId = Math.floor(Math.random() * 1000) + 1;
  const amount = Math.floor(Math.random() * 10000000) + 1000;

  const url = `http://localhost:8081/api/test/bid?auctionId=${auctionId}&userId=${userId}&amount=${amount}`;
  
  const res = http.post(url, null);

  check(res, {
    'is status 200': (r) => r.status === 200,
    'response check': (r) => r.body === "OK" || r.body === "FAIL"
  });
}
