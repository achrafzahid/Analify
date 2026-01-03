import React, { useState } from 'react';
import { useNavigate, Navigate } from 'react-router-dom';
import { Store, Eye, EyeOff, Loader2 } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { useAuth } from '@/contexts/AuthContext';
import { useToast } from '@/hooks/use-toast';
import { UserRole } from '@/types';
import { authApi } from '@/services/api';

// Demo tokens for testing different roles (fallback when backend is not available)
const DEMO_TOKENS: Record<UserRole, string> = {
  CAISSIER: 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOjEwMDEsInJvbGUiOiJDQUlTU0lFUiIsImV4cCI6MTkwMDAwMDAwMH0.demo',
  ADMIN_STORE: 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOjIwMDAsInJvbGUiOiJBRE1JTl9TVE9SRSIsImV4cCI6MTkwMDAwMDAwMH0.demo',
  INVESTOR: 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOjE2NDQsInJvbGUiOiJJTlZFU1RPUiIsImV4cCI6MTkwMDAwMDAwMH0.demo',
  ADMIN_GENERAL: 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOjEsInJvbGUiOiJBRE1JTl9HRU5FUkFMIiwiZXhwIjoxOTAwMDAwMDAwfQ.demo',
  ADMIN_G: 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOjEsInJvbGUiOiJBRE1JTl9HIiwiZXhwIjoxOTAwMDAwMDAwfQ.demo',
};

const roleRedirects: Record<UserRole, string> = {
  CAISSIER: '/dashboard/orders',
  ADMIN_STORE: '/dashboard/employees',
  INVESTOR: '/dashboard/products',
  ADMIN_GENERAL: '/dashboard/statistics',
  ADMIN_G: '/dashboard/statistics',
};

const Login: React.FC = () => {
  const navigate = useNavigate();
  const { login, isAuthenticated, user } = useAuth();
  const { toast } = useToast();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [selectedRole, setSelectedRole] = useState<UserRole | null>(null);
  const [useDemoMode, setUseDemoMode] = useState(false);

  // Redirect if already authenticated
  if (isAuthenticated && user) {
    return <Navigate to={roleRedirects[user.role]} replace />;
  }

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!email || !password) {
      toast({
        title: 'Validation Error',
        description: 'Please enter both email and password.',
        variant: 'destructive',
      });
      return;
    }

    setIsLoading(true);

    try {
      if (useDemoMode && selectedRole) {
        // Use demo token
        const demoToken = DEMO_TOKENS[selectedRole];
        login(demoToken);
        toast({
          title: 'Demo Mode',
          description: 'Logged in with demo account.',
        });
        navigate(roleRedirects[selectedRole]);
      } else {
        // Call the real API
        const response = await authApi.login(email, password);
        const token = response.token;
        
        login(token);
        
        toast({
          title: 'Welcome back!',
          description: 'You have successfully signed in.',
        });
        
        // Get the role from the decoded token and redirect
        const payload = JSON.parse(atob(token.split('.')[1]));
        const role = payload.role as UserRole;
        navigate(roleRedirects[role] || '/dashboard/profile');
      }
    } catch (error) {
      console.error('Login error:', error);
      toast({
        title: 'Authentication Failed',
        description: error instanceof Error ? error.message : 'Invalid email or password. Please try again.',
        variant: 'destructive',
      });
    } finally {
      setIsLoading(false);
    }
  };

  const handleDemoLogin = (role: UserRole) => {
    setSelectedRole(role);
    setUseDemoMode(true);
    setEmail(`demo_${role.toLowerCase()}@analify.com`);
    setPassword('demo123');
  };

  return (
    <div className="min-h-screen bg-background flex">
      {/* Left Side - Login Form */}
      <div className="flex-1 flex items-center justify-center px-4 py-12">
        <div className="w-full max-w-md animate-fade-in">
          {/* Logo */}
          <div className="text-center mb-8">
            <div className="inline-flex items-center justify-center p-3 bg-primary/10 rounded-xl mb-4">
              <Store className="h-8 w-8 text-primary" />
            </div>
            <h1 className="text-2xl font-bold text-foreground">Welcome back</h1>
            <p className="text-muted-foreground mt-1">Sign in to your account</p>
          </div>

          {/* Login Form */}
          <form onSubmit={handleLogin} className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="email">Email</Label>
              <Input
                id="email"
                type="email"
                placeholder="you@example.com"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                autoComplete="email"
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="password">Password</Label>
              <div className="relative">
                <Input
                  id="password"
                  type={showPassword ? 'text' : 'password'}
                  placeholder="••••••••"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  autoComplete="current-password"
                />
                <Button
                  type="button"
                  variant="ghost"
                  size="icon"
                  className="absolute right-1 top-1/2 -translate-y-1/2 h-8 w-8"
                  onClick={() => setShowPassword(!showPassword)}
                >
                  {showPassword ? (
                    <EyeOff className="h-4 w-4 text-muted-foreground" />
                  ) : (
                    <Eye className="h-4 w-4 text-muted-foreground" />
                  )}
                </Button>
              </div>
            </div>

            <Button type="submit" className="w-full" disabled={isLoading}>
              {isLoading ? (
                <>
                  <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                  Signing in...
                </>
              ) : (
                'Sign In'
              )}
            </Button>
          </form>

          {/* Demo Logins */}
          <div className="mt-8 pt-8 border-t border-border">
            <p className="text-sm text-center text-muted-foreground mb-4">
              Demo accounts (click to auto-fill)
            </p>
            <div className="grid grid-cols-2 gap-2">
              <Button
                variant="outline"
                size="sm"
                onClick={() => handleDemoLogin('CAISSIER')}
                className={selectedRole === 'CAISSIER' ? 'border-primary' : ''}
              >
                Cashier
              </Button>
              <Button
                variant="outline"
                size="sm"
                onClick={() => handleDemoLogin('ADMIN_STORE')}
                className={selectedRole === 'ADMIN_STORE' ? 'border-primary' : ''}
              >
                Store Admin
              </Button>
              <Button
                variant="outline"
                size="sm"
                onClick={() => handleDemoLogin('INVESTOR')}
                className={selectedRole === 'INVESTOR' ? 'border-primary' : ''}
              >
                Investor
              </Button>
              <Button
                variant="outline"
                size="sm"
                onClick={() => handleDemoLogin('ADMIN_GENERAL')}
                className={selectedRole === 'ADMIN_GENERAL' ? 'border-primary' : ''}
              >
                General Admin
              </Button>
            </div>
          </div>

          {/* Back Link */}
          <div className="mt-6 text-center">
            <Button variant="link" onClick={() => navigate('/')}>
              ← Back to home
            </Button>
          </div>
        </div>
      </div>

      {/* Right Side - Decorative */}
      <div className="hidden lg:flex flex-1 bg-sidebar items-center justify-center p-12">
        <div className="max-w-md text-center text-sidebar-foreground">
          <Store className="h-16 w-16 mx-auto mb-6 text-sidebar-primary" />
          <h2 className="text-3xl font-bold mb-4">Analify Dashboard</h2>
          <p className="text-sidebar-muted">
            A comprehensive platform to manage your retail network. Track orders, 
            monitor employees, analyze products, and gain insights across all your stores.
          </p>
        </div>
      </div>
    </div>
  );
};

export default Login;
