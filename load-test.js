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

  const url = `http://localhost:8080/api/v1/auctions/${auctionId}/bid`;
  
  const payload = JSON.stringify({
    userId: userId,
    amount: amount
  });

  const params = {
    headers: {
      'Content-Type': 'application/json',
    },
  };

  const res = http.post(url, payload, params);

  check(res, {
    'is status 200 or 400': (r) => r.status === 200 || r.status === 400,
    'response check': (r) => {
        try {
            const body = JSON.parse(r.body);
            return body.success === true || (body.success === false && body.error);
        } catch(e) {
            return false;
        }
    }
  });
}
