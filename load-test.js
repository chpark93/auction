import http from 'k6/http';
import { check, sleep } from 'k6';

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
  const url = 'http://localhost:8080/api/test/bid';
  
  const params = {
    auctionId: '1',
    userId: Math.floor(Math.random() * 100000).toString(),
    amount: Math.floor(Math.random() * 1000000).toString()
  };

  const res = http.post(`${url}?auctionId=${params.auctionId}&userId=${params.userId}&amount=${params.amount}`);

  check(res, {
    'is status 200': (r) => r.status === 200,
    'is OK': (r) => r.body === 'OK' || r.body === 'FAIL',
  });
}

