import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { User, UserRole, JWTPayload } from '@/types';
import { userApi } from '@/services/api';

interface AuthContextType {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (token: string) => void;
  logout: () => void;
  updateUser: (userData: Partial<User>) => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

// Decode JWT token without external library
const decodeToken = (token: string): JWTPayload | null => {
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
  } catch {
    return null;
  }
};

// Check if token is expired
const isTokenExpired = (payload: JWTPayload): boolean => {
  return payload.exp * 1000 < Date.now();
};

// Normalize role from backend (ADMIN_G) to frontend (ADMIN_GENERAL)
const normalizeRole = (role: UserRole): UserRole => {
  if (role === 'ADMIN_G') {
    return 'ADMIN_GENERAL';
  }
  return role;
};

// Fetch user data from API
const fetchUserData = async (userId: number, role: UserRole): Promise<User> => {
  try {
    const userData = await userApi.getProfile(userId) as User;
    return {
      ...userData,
      role, // Ensure role from JWT is used
    };
  } catch (error) {
    console.error('Failed to fetch user profile data:', error);
    // Re-throw the error instead of silently failing
    throw new Error('Failed to load user profile. Please try logging in again.');
  }
};

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [token, setToken] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  const logout = useCallback(() => {
    localStorage.removeItem('auth_token');
    setToken(null);
    setUser(null);
  }, []);

  const login = useCallback(async (newToken: string) => {
    const payload = decodeToken(newToken);
    
    if (!payload) {
      throw new Error('Invalid token');
    }

    if (isTokenExpired(payload)) {
      throw new Error('Token expired');
    }

    localStorage.setItem('auth_token', newToken);
    setToken(newToken);
    
    // Normalize the role (ADMIN_G -> ADMIN_GENERAL)
    const normalizedRole = normalizeRole(payload.role);
    
    // Fetch user data from API
    const userData = await fetchUserData(payload.userId, normalizedRole);
    setUser(userData);
  }, []);

  const updateUser = useCallback((userData: Partial<User>) => {
    setUser((prev) => (prev ? { ...prev, ...userData } : null));
  }, []);

  // Check for existing token on mount
  useEffect(() => {
    const initAuth = async () => {
      const storedToken = localStorage.getItem('auth_token');
      
      if (storedToken) {
        const payload = decodeToken(storedToken);
        
        if (payload && !isTokenExpired(payload)) {
          setToken(storedToken);
          try {
            const normalizedRole = normalizeRole(payload.role);
            const userData = await fetchUserData(payload.userId, normalizedRole);
            setUser(userData);
          } catch (error) {
            console.error('Failed to initialize user data:', error);
            // Clear invalid token and user data
            localStorage.removeItem('auth_token');
            setToken(null);
            setUser(null);
          }
        } else {
          localStorage.removeItem('auth_token');
        }
      }
      
      setIsLoading(false);
    };

    initAuth();
  }, []);

  const value: AuthContextType = {
    user,
    token,
    isAuthenticated: !!token && !!user,
    isLoading,
    login,
    logout,
    updateUser,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
