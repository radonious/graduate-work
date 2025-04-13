export const authService = {
    accessToken: null,
    refreshToken: null,
    userRole: null,
    userId: null,
    username: null,

    setTokens(accessToken, refreshToken, userRole, userId, username) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.userRole = userRole;
        this.userId = userId;
        this.username = username;
        localStorage.setItem('accessToken', accessToken);
        localStorage.setItem('refreshToken', refreshToken);
        localStorage.setItem('userRole', userRole);
        localStorage.setItem('userId', userId);
        localStorage.setItem('username', username);
    },

    getTokens() {
        this.accessToken = localStorage.getItem('accessToken');
        this.refreshToken = localStorage.getItem('refreshToken');
        this.userRole = localStorage.getItem('role');
        this.userId = localStorage.getItem('userId');
        this.username = localStorage.getItem('username');
        return {
            accessToken: this.accessToken,
            refreshToken: this.refreshToken,
            userRole: this.userRole,
            userId: this.userId,
            username: this.username,
        };
    },

    clearTokens() {
        this.accessToken = null;
        this.refreshToken = null;
        this.userRole = null;
        this.userId = null;
        this.username = null;
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
        localStorage.removeItem('userRole');
        localStorage.removeItem('userId');
        localStorage.removeItem('username');
    },

    getUserRole() {
        if (!this.userRole) {
            this.userRole = localStorage.getItem('userRole');
        }
        return this.userRole;
    },

    getUserId() {
        if (!this.userId) {
            this.userId = localStorage.getItem('userId');
        }
        return this.userId;
    },

    getUsername() {
        if (!this.username) {
            this.username = localStorage.getItem('username');
        }
        return this.username;
    },

    async refreshAccessToken() {
        try {
            const response = await fetch(`${API_BASE_URL}/auth/refresh-token`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ refreshToken: this.refreshToken })
            });

            if (!response.ok) {
                throw new Error('Failed to refresh token');
            }

            const data = await response.json();
            this.setTokens(data.accessToken, data.refreshToken, data.userRole, data.userId, data.username);
            return data.accessToken;
        } catch (error) {
            console.error('Error refreshing token:', error);
            throw error;
        }
    },

    async fetchWithToken(url, options = {}) {
        const headers = options.headers || {};
        const { accessToken } = this.getTokens();
        if (accessToken) {
            headers['Authorization'] = `Bearer ${accessToken}`;
        }

        try {
            const response = await fetch(url, { ...options, headers });

            if (response.status === 401) {
                const newAccessToken = await this.refreshAccessToken();
                headers['Authorization'] = `Bearer ${newAccessToken}`;
                return fetch(url, { ...options, headers });
            }

            return response;
        } catch (error) {
            console.error('Error fetching with token:', error);
            throw error;
        }
    },

    hasRefreshToken() {
        return !!localStorage.getItem('refreshToken');
    }
};
