// nginx 프록시를 통해 접근 (빈 문자열 = 상대 경로)
const GATEWAY_URL = '';

// Axios 인스턴스 생성
const adminApi = axios.create({
    baseURL: GATEWAY_URL,
    timeout: 10000
});

// JWT 토큰 가져오기
function getAdminToken() {
    return localStorage.getItem('adminAccessToken');
}

// JWT 토큰 저장
function setAdminToken(token) {
    localStorage.setItem('adminAccessToken', token);
}

// JWT 토큰 삭제
function clearAdminToken() {
    localStorage.removeItem('adminAccessToken');
}

// JWT 페이로드 파싱
function parseJwt(token) {
    try {
        const base64Url = token.split('.')[1];
        const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
        const jsonPayload = decodeURIComponent(atob(base64).split('').map(c => {
            return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
        }).join(''));
        return JSON.parse(jsonPayload);
    } catch (e) {
        return null;
    }
}

// 현재 관리자 정보 가져오기
function getCurrentAdmin() {
    const token = getAdminToken();
    if (!token) return null;
    
    const payload = parseJwt(token);
    if (!payload) return null;
    
    return {
        userId: payload.userId,
        email: payload.sub,
        roles: payload.roles || []
    };
}

// ROLE_ADMIN 권한 체크
function checkAdminRole() {
    const admin = getCurrentAdmin();
    if (!admin) {
        alert('로그인이 필요합니다.');
        window.location.href = 'admin-login.html';
        return false;
    }
    
    if (!admin.roles.includes('ROLE_ADMIN')) {
        alert('관리자 권한이 없습니다.');
        window.location.href = 'admin-login.html';
        return false;
    }
    
    return true;
}

// Axios 요청 인터셉터 (JWT 토큰 자동 추가)
adminApi.interceptors.request.use(
    config => {
        const token = getAdminToken();
        if (token) {
            config.headers['Authorization'] = `Bearer ${token}`;
        }
        return config;
    },
    error => {
        return Promise.reject(error);
    }
);

// Axios 응답 인터셉터 (에러 처리)
adminApi.interceptors.response.use(
    response => response,
    error => {
        if (error.response?.status === 401) {
            alert('인증이 만료되었습니다. 다시 로그인해주세요.');
            clearAdminToken();
            window.location.href = 'admin-login.html';
        } else if (error.response?.status === 403) {
            alert('권한이 없습니다.');
        }
        return Promise.reject(error);
    }
);

// 관리자 API
const AdminAPI = {
    // 인증
    login: (email, password) => adminApi.post('/api/v1/auth/login', { email, password }),
    logout: () => {
        clearAdminToken();
        window.location.href = 'admin-login.html';
    },
    
    // 대시보드
    getDashboardStats: () => adminApi.get('/api/v1/admin/dashboard/stats'),
    
    // 회원 관리
    getUsers: (page = 0, size = 20, status = null) => {
        const params = { page, size };
        if (status) params.status = status;
        return adminApi.get('/api/v1/admin/users', { params });
    },
    getUserDetail: (userId) => adminApi.get(`/api/v1/admin/users/${userId}`),
    banUser: (userId, reason) => adminApi.post(`/api/v1/admin/users/${userId}/ban`, { reason }),
    unbanUser: (userId) => adminApi.post(`/api/v1/admin/users/${userId}/unban`),
    
    // 경매 관리
    getAuctions: (page = 0, size = 20, status = null) => {
        const params = { page, size };
        if (status) params.status = status;
        return adminApi.get('/api/v1/admin/auctions', { params });
    },
    getPendingAuctions: (page = 0, size = 20) => {
        return adminApi.get('/api/v1/admin/auctions/pending', { params: { page, size } });
    },
    getAuctionDetail: (auctionId) => adminApi.get(`/api/v1/admin/auctions/${auctionId}`),
    approveAuction: (auctionId) => adminApi.post(`/api/v1/admin/auctions/${auctionId}/approve`),
    rejectAuction: (auctionId, reason) => adminApi.post(`/api/v1/admin/auctions/${auctionId}/reject`, { reason }),
    deleteAuction: (auctionId) => adminApi.delete(`/api/v1/admin/auctions/${auctionId}`),
    
    // 정산 관리
    getSettlements: (page = 0, size = 20, status = null) => {
        const params = { page, size };
        if (status) params.status = status;
        return adminApi.get('/api/v1/admin/payments/settlements', { params });
    },
    getSettlementDetail: (settlementId) => adminApi.get(`/api/v1/admin/payments/settlements/${settlementId}`)
};

