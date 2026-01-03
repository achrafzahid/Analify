import React from 'react';
import { useNavigate } from 'react-router-dom';
import { Store, ArrowRight } from 'lucide-react';
import { Button } from '@/components/ui/button';

const Landing: React.FC = () => {
  const navigate = useNavigate();

  return (
    <div className="min-h-screen bg-background flex flex-col">
      {/* Background Pattern */}
      <div className="absolute inset-0 bg-[radial-gradient(ellipse_at_top,_var(--tw-gradient-stops))] from-primary/5 via-background to-background" />
      
      {/* Content */}
      <div className="relative flex-1 flex items-center justify-center px-4">
        <div className="text-center max-w-lg animate-fade-in">
          {/* Logo */}
          <div className="inline-flex items-center justify-center p-4 bg-primary/10 rounded-2xl mb-8">
            <Store className="h-12 w-12 text-primary" />
          </div>

          {/* Title */}
          <h1 className="text-4xl font-bold text-foreground mb-4">
            Analify
          </h1>
          <p className="text-xl text-muted-foreground mb-2">
            Business Analytics Platform
          </p>
          <p className="text-muted-foreground mb-10">
            Manage your stores, employees, products, and analytics in one place.
          </p>

          {/* Login Button */}
          <Button
            size="lg"
            onClick={() => navigate('/login')}
            className="px-8 py-6 text-lg gap-2"
          >
            Sign In to Dashboard
            <ArrowRight className="h-5 w-5" />
          </Button>
        </div>
      </div>

      {/* Footer */}
      <footer className="relative py-6 text-center text-sm text-muted-foreground">
        <p>© 2024 Analify. All rights reserved.</p>
      </footer>
    </div>
  );
};

export default Landing;
