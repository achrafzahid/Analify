import { Toaster } from "@/components/ui/toaster";
import { Toaster as Sonner } from "@/components/ui/sonner";
import { TooltipProvider } from "@/components/ui/tooltip";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { AuthProvider } from "@/contexts/AuthContext";
import { DashboardLayout } from "@/components/layout/DashboardLayout";

// Pages
import Index from "./pages/Index";
import Login from "./pages/Login";
import NotFound from "./pages/NotFound";

// Dashboard Pages
import Profile from "./pages/dashboard/Profile";
import Orders from "./pages/dashboard/Orders";
import Employees from "./pages/dashboard/Employees";
import Products from "./pages/dashboard/Products";
import Statistics from "./pages/dashboard/Statistics";
import LowStock from "./pages/dashboard/LowStock";
import BiddingDashboard from "./pages/dashboard/BiddingDashboard";
import BiddingBrowse from "./pages/dashboard/BiddingBrowse";
import BiddingCategory from "./pages/dashboard/BiddingCategory";
import BiddingSection from "./pages/dashboard/BiddingSection";
import MyBids from "./pages/dashboard/MyBids";

const queryClient = new QueryClient();

const App = () => (
  <QueryClientProvider client={queryClient}>
    <AuthProvider>
      <TooltipProvider>
        <Toaster />
        <Sonner />
        <BrowserRouter>
          <Routes>
            {/* Public Routes */}
            <Route path="/" element={<Index />} />
            <Route path="/login" element={<Login />} />
            
            {/* Protected Dashboard Routes */}
            <Route path="/dashboard" element={<DashboardLayout />}>
              <Route index element={<Navigate to="/dashboard/profile" replace />} />
              <Route path="profile" element={<Profile />} />
              <Route path="orders" element={<Orders />} />
              <Route path="employees" element={<Employees />} />
              <Route path="products" element={<Products />} />
              <Route path="statistics" element={<Statistics />} />
              <Route path="low-stock" element={<LowStock />} />
              <Route path="bidding-overview" element={<BiddingDashboard />} />
              <Route path="bidding" element={<BiddingBrowse />} />
              <Route path="bidding/category/:categoryId" element={<BiddingCategory />} />
              <Route path="bidding/section/:sectionId" element={<BiddingSection />} />
              <Route path="my-bids" element={<MyBids />} />
            </Route>
            
            {/* Catch-all */}
            <Route path="*" element={<NotFound />} />
          </Routes>
        </BrowserRouter>
      </TooltipProvider>
    </AuthProvider>
  </QueryClientProvider>
);

export default App;
