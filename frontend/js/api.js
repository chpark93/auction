// nginx 프록시를 통해 접근 (빈 문자열 = 상대 경로)
const API_BASE_URL = '';

// Axios Instance
const api = axios.create({
    baseURL: API_BASE_URL,
    timeout: 10000,
    headers: {
        'Content-Type': 'application/json'
    }
});

// Request Interceptor - JWT Token 자동 추가
api.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('accessToken');
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

// Response Interceptor - 에러 처리
api.interceptors.response.use(
    (response) => {
        return response;
    },
    (error) => {
        if (error.response) {
            const { status, data } = error.response;
            
            // 401 Unauthorized - 로그인 페이지로 이동
            if (status === 401) {
                alert('로그인이 필요합니다.');
                logout();
                return Promise.reject(error);
            }
            
            // 403 Forbidden
            if (status === 403) {
                alert('접근 권한이 없습니다.');
                return Promise.reject(error);
            }
            
            // 기타 에러 메시지 표시
            const errorMessage = data?.message || '요청 처리 중 오류가 발생했습니다.';
            console.error('API Error:', errorMessage);
        } else if (error.request) {
            console.error('Network Error:', error.message);
            alert('서버와 연결할 수 없습니다.');
        }
        
        return Promise.reject(error);
    }
);

// ==================== Token 관리 ====================

// JWT 토큰 저장
function saveTokens(accessToken, refreshToken) {
    localStorage.setItem('accessToken', accessToken);
    if (refreshToken) {
        localStorage.setItem('refreshToken', refreshToken);
    }
}

// JWT 토큰 가져오기
function getAccessToken() {
    return localStorage.getItem('accessToken');
}

function getRefreshToken() {
    return localStorage.getItem('refreshToken');
}

// JWT 토큰에서 사용자 정보 파싱
function parseJwt(token) {
    try {
        const base64Url = token.split('.')[1];
        const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
        const jsonPayload = decodeURIComponent(
            atob(base64)
                .split('')
                .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
                .join('')
        );
        return JSON.parse(jsonPayload);
    } catch (e) {
        console.error('Failed to parse JWT:', e);
        return null;
    }
}

// 현재 로그인한 사용자 정보 가져오기
function getCurrentUser() {
    const token = getAccessToken();
    if (!token) return null;
    
    const payload = parseJwt(token);
    if (!payload) return null;
    
    return {
        userId: payload.userId,
        email: payload.sub,
        roles: payload.roles || []
    };
}

// 로그인 여부 체크
function isLoggedIn() {
    return !!getAccessToken();
}

// 로그아웃
function logout() {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    window.location.href = 'login.html';
}

// ==================== API 호출 함수들 ====================

// Auth API
const AuthAPI = {
    signup: (data) => api.post('/api/v1/auth/signup', data),
    login: (data) => api.post('/api/v1/auth/login', data),
    reissue: (refreshToken) => api.post('/api/v1/auth/reissue', { refreshToken })
};

// User API
const UserAPI = {
    getMyInfo: () => {
        const user = getCurrentUser();
        if (!user) return Promise.reject(new Error('Not logged in'));
        return api.get(`/api/v1/users/${user.userId}`);
    },
    getMyPoint: () => {
        const user = getCurrentUser();
        if (!user) return Promise.reject(new Error('Not logged in'));
        return api.get(`/api/v1/users/${user.userId}/points`);
    },
    chargePoint: (amount) => {
        const user = getCurrentUser();
        if (!user) return Promise.reject(new Error('Not logged in'));
        return api.post(`/api/v1/users/${user.userId}/points/charge`, { amount });
    },
    getPointHistory: (page = 0, size = 20) => {
        const user = getCurrentUser();
        if (!user) return Promise.reject(new Error('Not logged in'));
        return api.get(`/api/v1/users/${user.userId}/points/history`, { params: { page, size } });
    }
};

// Auction API
const AuctionAPI = {
    getList: (params) => api.get('/api/v1/auctions', { params }),
    getDetail: (auctionId) => api.get(`/api/v1/auctions/${auctionId}`),
    getCurrentPrice: (auctionId) => api.get(`/api/v1/auctions/${auctionId}/price`),
    getBidHistory: (auctionId, limit = 20) => api.get(`/api/v1/auctions/${auctionId}/bids`, { params: { limit } }),
    placeBid: (auctionId, amount) => {
        const user = getCurrentUser();
        if (!user) return Promise.reject(new Error('Not logged in'));
        return api.post(`/api/v1/auctions/${auctionId}/bid`, { 
            userId: user.userId, 
            amount 
        });
    },
    cancelBid: (auctionId, reason = null) => {
        const user = getCurrentUser();
        if (!user) return Promise.reject(new Error('Not logged in'));
        return api.post(`/api/v1/auctions/${auctionId}/cancel-bid?userId=${user.userId}`, { reason });
    },
    getMyBidding: () => {
        const user = getCurrentUser();
        if (!user) return Promise.reject(new Error('Not logged in'));
        return api.get(`/api/v1/auctions/users/${user.userId}/bidding`);
    },
    create: (data) => api.post('/api/v1/seller/auctions', data),
    getMyAuctions: () => {
        const user = getCurrentUser();
        if (!user) return Promise.reject(new Error('Not logged in'));
        return api.get('/api/v1/seller/auctions', {
            headers: { 'X-User-Id': user.userId }
        });
    }
};

// Search API
const SearchAPI = {
    search: (params) => api.get('/api/v1/search/auctions', { params })
};

// Payment API
const PaymentAPI = {
    chargePoint: (data) => api.post('/api/v1/payments/charge', data)
};

// Chat API
const ChatAPI = {
    getRooms: () => api.get('/api/v1/chat/rooms'),
    createRoom: (auctionId) => api.post('/api/v1/chat/rooms', { auctionId }),
    getMessages: (roomId) => api.get(`/api/v1/chat/rooms/${roomId}/messages`)
};

// ==================== 유틸리티 함수 ====================

// 날짜 포맷팅
function formatDate(dateString) {
    const date = new Date(dateString);
    return date.toLocaleString('ko-KR');
}

// 가격 포맷팅 (콤마 추가)
function formatPrice(price) {
    if (price === null || price === undefined) {
        return '0원';
    }
    return price.toLocaleString('ko-KR') + '원';
}

// 남은 시간 계산
function getTimeRemaining(endTime) {
    const now = new Date();
    const end = new Date(endTime);
    const diff = end - now;
    
    if (diff <= 0) return '종료됨';
    
    const days = Math.floor(diff / (1000 * 60 * 60 * 24));
    const hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
    const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));
    
    if (days > 0) return `${days}일 ${hours}시간 남음`;
    if (hours > 0) return `${hours}시간 ${minutes}분 남음`;
    return `${minutes}분 남음`;
}

// 페이지 로드 시 로그인 체크
function requireLogin() {
    if (!isLoggedIn()) {
        alert('로그인이 필요합니다.');
        window.location.href = 'login.html';
        return false;
    }
    return true;
}

// URL 파라미터 가져오기
function getQueryParam(param) {
    const urlParams = new URLSearchParams(window.location.search);
    return urlParams.get(param);
}

