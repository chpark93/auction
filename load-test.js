import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  scenarios: {
    high_load: {
      executor: 'constant-arrival-rate',
      rate: 100, // 초당 100개 요청
      timeUnit: '1s',
      duration: '30s',
      preAllocatedVUs: 50,
      maxVUs: 200,
    },
  },
};

export default function () {
  const auctionId = 1;
  const userId = Math.floor(Math.random() * 1000) + 1; // 1 ~ 1000 (포인트 충전된 유저)
  
  // 금액은 랜덤하게 설정 (실제 경매처럼 계속 오르지는 않으므로 일부는 실패할 것임)
  // 1000원 ~ 10,000,000원 사이
  const amount = Math.floor(Math.random() * 10000000) + 1000;

  const url = `http://localhost:8080/api/v1/auctions/${auctionId}/bid`;
  
  const payload = JSON.stringify({
    userId: userId,
    amount: amount,
    maxLimit: 0
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
            // 성공(200)이거나, 가격이 낮음/포인트부족 등의 비즈니스 예외(400)는 정상 처리로 간주
            return body.success === true || (body.success === false && body.error);
        } catch(e) {
            return false;
        }
    }
  });
}
